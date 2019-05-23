package com.aminhx.earthquakeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class InformationActivity extends AppCompatActivity {

    TextView txt_distance;
    TextView txt_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        txt_distance = (TextView) findViewById(R.id.txt_distance);
        txt_speed = (TextView) findViewById(R.id.txt_speed);
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            String distance = "";
            String speed = "";

            if(extras.containsKey("distance")) {
                distance = extras.getString("distance");
            }
            if(extras.containsKey("speed")) {
                speed = extras.getString("speed");
            }

            txt_distance.setText(distance);
            txt_speed.setText(speed);
        }
    }



}
