/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.servify.ARModule;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.servify.ARModule.customview.OverlayView;
import com.example.servify.ARModule.customview.OverlayView.DrawCallback;
import com.example.servify.ARModule.env.BorderedText;
import com.example.servify.ARModule.env.ImageUtils;
import com.example.servify.ARModule.env.Logger;
import com.example.servify.ARModule.tflite.Classifier;
import com.example.servify.ARModule.tflite.TFLiteObjectDetectionAPIModel;
import com.example.servify.ARModule.tracking.MultiBoxTracker;
import com.example.servify.ObjectModel;
import com.example.servify.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    private ImageView deviceIV;
    private TextView deviceTypeTV;
    private EditText deviceNameET;
    private Button addBtn, cancelBtn;
    private ConstraintLayout container;

    private List<ObjectModel> objects = new ArrayList<>();


    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private ObjectModel device;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);

        deviceIV = findViewById(R.id.iv_device_image);
        deviceNameET = findViewById(R.id.et_device_name);
        deviceTypeTV = findViewById(R.id.tv_device_name);
        addBtn = findViewById(R.id.btn_add);
        cancelBtn = findViewById(R.id.btn_cancel);
        container = findViewById(R.id.cl_detection_container);

        cancelBtn.setOnClickListener(view -> hideDialog());

        addBtn.setOnClickListener(view -> uploadDevice(device));

        initFirebase();
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
//                        SystemClock.sleep(1000);
                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            final String _name = result.getTitle();

                            final ImageView laptop = findViewById(R.id.iv_obj_laptop);
                            final ImageView mouse = findViewById(R.id.iv_obj_mouse);
                            final ImageView keyboard = findViewById(R.id.iv_obj_keyboard);
                            final ImageView phone = findViewById(R.id.iv_obj_phone);

                            if (location != null && result.getConfidence() >= minimumConfidence && (_name.equals("tv")
                                    || _name.equals("laptop") || _name.equals("mouse") || _name.equals("keyboard")
                                    || _name.equals("cell phone") || _name.equals("microwave") || _name.equals("oven")
                                    || _name.equals("toaster") || _name.equals("refrigerator"))) {


                                //=============================
                                ObjectModel obj;
                                switch (_name) {
                                    case "laptop":
                                        obj = new ObjectModel("laptop");
                                        if (!objects.contains(obj)) {
                                            objects.add(obj);
                                            runOnUiThread(() -> {
                                                laptop.setVisibility(View.VISIBLE);
                                                laptop.setOnClickListener(view -> showDialog(obj));
                                            });
                                        }
                                        break;
                                    case "mouse":
                                        obj = new ObjectModel("mouse");
                                        if (!objects.contains(obj)) {
                                            objects.add(obj);
                                            runOnUiThread(() -> {
                                                mouse.setVisibility(View.VISIBLE);
                                                mouse.setOnClickListener(view -> showDialog(obj));
                                            });
                                        }
                                        break;
                                    case "keyboard":
                                        obj = new ObjectModel("keyboard");
                                        if (!objects.contains(obj)) {
                                            objects.add(obj);
                                            runOnUiThread(() -> keyboard.setVisibility(View.VISIBLE));
                                            keyboard.setOnClickListener(view -> showDialog(obj));
                                        }
                                        break;
                                    case "cell phone":
                                        obj = new ObjectModel("cell phone");
                                        if (!objects.contains(obj)) {
                                            objects.add(obj);
                                            runOnUiThread(() -> phone.setVisibility(View.VISIBLE));
                                            phone.setOnClickListener(view -> showDialog(obj));
                                        }
                                        break;
                                }

                                for (int i = 0; i < objects.size(); i++) {
                                    Log.d("Objects", "objects: " + objects.get(i).getType());
                                }
                                //=============================

                            }
                        }

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;

                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        showFrameInfo(previewWidth + "x" + previewHeight);
                                        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                                        showInference(lastProcessingTimeMs + "ms");
                                    }
                                });
                    }
                });
    }

    private void showDialog(ObjectModel objectModel) {
//        container.setVisibility(View.VISIBLE);
        container.animate().alpha(1).setDuration(300);
        deviceTypeTV.setText(objectModel.getType());

        switch (objectModel.getType()) {
            case "cell phone":
                device = new ObjectModel("phone");
                deviceIV.setImageResource(R.drawable.ic_cell_phone);
                break;

            case "laptop":
                device = new ObjectModel("laptop");
                deviceIV.setImageResource(R.drawable.ic_laptop2);
                break;
        }
    }

    private void hideDialog() {
//        container.setVisibility(View.GONE);
        container.animate().alpha(0).setDuration(300);
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("devices");
    }

    private void uploadDevice(ObjectModel object) {
        String devName = deviceNameET.getText().toString();

        object.setName(devName);

        mReference.push().setValue(object).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DetectorActivity.this, "Device Successfully Added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some Error Occured!", Toast.LENGTH_SHORT).show();
            }
            hideDialog();
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API
    }
}
