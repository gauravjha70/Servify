package com.example.servify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.servify.ARModule.DetectorActivity;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderView;

public class MainActivity extends AppCompatActivity {

    Button camera, wifiInfo;
    private SliderView sliderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
        wifiInfo = findViewById(R.id.wifi_info);
        sliderView = findViewById(R.id.sv_onboarding);
        SliderAdapterOnboard adapter = new SliderAdapterOnboard(this);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM);

        camera.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));

        wifiInfo.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, WifiInfoActivity.class)));

    }
}
