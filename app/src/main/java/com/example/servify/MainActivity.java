package com.example.servify;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.servify.ARModule.DetectorActivity;
import com.example.servify.WifiScanModule.WifiInfoFragment;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderView;

public class MainActivity extends AppCompatActivity {

    Button camera, wifiInfo, uidButton;
    private SliderView sliderView;

    RelativeLayout fragmentContainer;

    WifiInfoFragment wifiInfoFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
        wifiInfo = findViewById(R.id.wifi_info);
        fragmentContainer = findViewById(R.id.fragment_container);
        uidButton = findViewById(R.id.uid);

        camera.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));


        wifiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                wifiInfoFragment = new WifiInfoFragment();
                fragmentTransaction.add(R.id.fragment_container,wifiInfoFragment).commit();
            }
        });
        sliderView = findViewById(R.id.sv_onboarding);
        SliderAdapterOnboard adapter = new SliderAdapterOnboard(this);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM);

        uidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateUID();
            }
        });



    }

    void generateUID()
    {
        String UID = "";

        int boardLength = Build.BOARD.length();
        int newBoardLength = 8%boardLength;
        if(newBoardLength != 0)
        {
            boardLength = newBoardLength*boardLength;
        }

        UID = UID + Build.BOARD.charAt(Integer.valueOf(newBoardLength));


        int brandLength = Build.BRAND.length();
        int newBrandLength = boardLength%brandLength;
        if(newBrandLength!=0)
            brandLength = newBrandLength*brandLength;

        UID = UID + Build.BRAND.charAt(Integer.valueOf(newBrandLength));


        int deviceLength = Build.DEVICE.length();
        int newDeviceLength = brandLength%deviceLength;
        if(newDeviceLength!=0)
            deviceLength = newDeviceLength*deviceLength;

        UID = UID + Build.DEVICE.charAt(Integer.valueOf(newDeviceLength));



        int manLength = Build.MANUFACTURER.length();
        int newManLength = deviceLength%manLength;
        if(newManLength!=0)
            manLength = newManLength*manLength;

        UID = UID + Build.MANUFACTURER.charAt(Integer.valueOf(newManLength));


        int hardLength = Build.HARDWARE.length();
        int newHardLength = manLength%hardLength;
        if(newHardLength!=0)
            hardLength = newHardLength*hardLength;

        UID = UID + Build.HARDWARE.charAt(Integer.valueOf(newHardLength));


        int modLength = Build.MODEL.length();
        int newModLength = hardLength%modLength;
        if(newModLength!=0)
            modLength = newModLength*modLength;

        UID = UID + Build.MODEL.charAt(Integer.valueOf(newModLength));


        int proLength = Build.PRODUCT.length();
        int newProLength = modLength%proLength;
        if(newProLength!=0)
            proLength = newProLength*proLength;

        UID = UID + Build.PRODUCT.charAt(Integer.valueOf(newProLength));


        Toast.makeText(getApplicationContext(),UID,Toast.LENGTH_LONG).show();

    }

}
