package com.example.mcflix;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.takusemba.rtmppublisher.RtmpPublisher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


interface StreamCallback{
    void onSuccess();
    void onFail();
}

/*
 * Defines the streaming activity. Uses the RtmpPublisher API to communicate with the rtmp server.
 */
public class StreamActivity extends AppCompatActivity {

    final private String rtmpUrl = MainActivity.streamingServerUrl;


    private Button toggleStreamButton;
    private EditText titleSelector;
    private RtmpPublisher publisher;

    /*
     * Creates or deletes a given stream by executing a request to the application server.
     * If isPublish == true creates, else deletes
     */
    private void editStream(boolean isPublish,String streamTitle,final StreamCallback callback){
        if(streamTitle == null || streamTitle.equals("")){
            callback.onFail();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("streamID",streamTitle);
        } catch(JSONException e){
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();

        String url = isPublish? MainActivity.appServerUrl+"/stream/": MainActivity.appServerUrl+"/stream/delete";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFail();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(stringRequest);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        final Context streamActivityContext = this;

        GLSurfaceView glView = (GLSurfaceView) findViewById(R.id.surface_view);
        publisher = new RtmpPublisher();
        publisher.initialize(this,glView);

        checkRecordPermissions();

        toggleStreamButton = (Button) findViewById(R.id.toggle_publish);
        titleSelector = (EditText) findViewById(R.id.editText);

        toggleStreamButton.setText(R.string.start_publishing);

        toggleStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(publisher.isPublishing()){
                    //User is publishing, stop streaming and ask for its deletion server side.
                    editStream(false,titleSelector.getText().toString(), new StreamCallback() {
                        @Override
                        public void onSuccess() {
                            publisher.stopPublishing();
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(streamActivityContext,"Failed to delete stream.",Toast.LENGTH_SHORT);
                        }
                    });

                    toggleStreamButton.setText(R.string.start_publishing);
                } else {
                    //User is not publishing, start streaming and ask for its creation server side.
                    editStream(true,titleSelector.getText().toString(), new StreamCallback() {
                        @Override
                        public void onSuccess() {
                            publisher.startPublishing(rtmpUrl+"/"+titleSelector.getText().toString());
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(streamActivityContext,"Failed to publish stream. Try another title",Toast.LENGTH_LONG);
                        }
                    });
                    toggleStreamButton.setText(R.string.stop_publishing);
                }
            }
        });


    }


    /*
     * Check if app has permissions to use Microphone and Camera. If it has no permissions, ask for
     * them to the user.
     */
    private void checkRecordPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    2);
        }
    }
}
