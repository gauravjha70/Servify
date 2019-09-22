package com.example.servify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.servify.ARModule.DetectorActivity;

public class MainActivity extends AppCompatActivity {

    Button camera, wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
        wifiInfo = findViewById(R.id.wifi_info);

        camera.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));


        wifiInfo.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, WifiInfoActivity.class)));

    }
}
