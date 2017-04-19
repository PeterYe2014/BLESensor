package com.example.peterye.blesensor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.peterye.blesensor.ui.DeviceView;

public class TestActivity extends AppCompatActivity {

    private RelativeLayout relativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceView deviceView = new DeviceView(this);
        setContentView(R.layout.activity_test);
        relativeLayout = (RelativeLayout) findViewById(R.id.content_test);
        relativeLayout.addView(deviceView);
        deviceView.addSensorRow();

    }


}
