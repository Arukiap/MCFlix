package com.example.mcflix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button watchVideo,watchStream,stream;

    public static final String appServerUrl = "http://35.205.234.126:8080"; //App Server for the client to connect to
    public static final String streamingServerUrl = "rtmp://35.205.234.126/live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        watchVideo = (Button)findViewById(R.id.firstCardButton);
        watchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,VideoCategoriesActivity.class);
                startActivity(i);
            }
        });
        watchStream = (Button)findViewById(R.id.secondCardButton);
        watchStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ContentActivity.class);
                i.putExtra("ContentType","stream"); //Send the type of content to video browser
                startActivity(i);
            }
        });
        stream = (Button)findViewById(R.id.thirdCardButton);
        stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,StreamActivity.class);
                startActivity(i);
            }
        });
    }
}
