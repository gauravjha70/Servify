package com.example.servify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.servify.ARModule.DetectorActivity;
import com.example.servify.WifiScanModule.WifiInfoFragment;

public class MainActivity extends AppCompatActivity {

    Button camera, wifiInfo;

    RelativeLayout fragmentContainer;

    WifiInfoFragment wifiInfoFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
        wifiInfo = findViewById(R.id.wifi_info);
        fragmentContainer = findViewById(R.id.fragment_container);

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

    }
}
