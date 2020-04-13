package com.example.mcflix;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import org.json.JSONException;
import org.json.JSONObject;


interface StreamRequestCallBack {
    void onSuccess(String url);
}

/*
 * Implements the ability to watch a given rtmp stream using the ExoPlayer library
 * This implementation follows https://github.com/teocci/Android-RTMP-Player
 */
public class WatchStreamActivity extends AppCompatActivity {

    private void getStreamURL(Content streamContent, final StreamRequestCallBack callback){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, MainActivity.appServerUrl+"/stream/?id="+streamContent.getTitle(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");
                    if(status!=1){
                        Toast.makeText(WatchStreamActivity.this,  "We are sorry. This stream could not be loaded.", Toast.LENGTH_SHORT).show();
                    } else {
                        callback.onSuccess(response.getString("streamID"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        //Add request to queue, i.e run the request.
        queue.add(request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_stream);

        //Create ExoPlayer
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);

        PlayerView playerView = (PlayerView) findViewById(R.id.simple_player);

        playerView.setPlayer(player);

        //Create RTMP Data Source
        final RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();


        Content content = (Content)getIntent().getSerializableExtra("content"); //Get the content that is going to be played
        getStreamURL(content, new StreamRequestCallBack() {
            @Override
            public void onSuccess(String url) {
                MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(Uri.parse(url));
                player.prepare(videoSource);
                player.setPlayWhenReady(true);

            }
        });
    }
}
