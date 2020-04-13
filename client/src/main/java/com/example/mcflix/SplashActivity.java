package com.example.mcflix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*
 * Simple implementation that shows the splash image while app is loading. Follows the implementation explained in curricular classes.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =  new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
