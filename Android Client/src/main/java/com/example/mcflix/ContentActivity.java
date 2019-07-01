package com.example.mcflix;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

interface ContentRequestCallBack {
    void onSuccess(ProgressBar progressBar);
}

/*
 * This activity is where the user selects which content he wants to watch.
 */
public class ContentActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private ContentAdapter contentAdapter;
    private List<Content> contentList;

    private String getCategoryUrl(String category){
        return MainActivity.appServerUrl+"/movies/search?category='"+category+"'";
    }

    private String getStreamsUrl(){
        return MainActivity.appServerUrl+"/stream/streams";
    }

    //Returns a string description that takes into account the length and year of a certain video/movie
    private String getDescriptionContent(int length_seconds, int year){
        int minutes = (length_seconds % 3600) / 60;
        int seconds = length_seconds % 60;
        String lengthString = Integer.toString(minutes)+"m "+Integer.toString(seconds)+"s";
        String yearString = Integer.toString(year);
        return lengthString+", "+yearString;
    }

    /*
     * Requests the video content to the application server using a GET method
     */
    private void getVideoContent(String category, final ProgressBar progressBar, final ContentRequestCallBack callback){
        String requestUrl = getCategoryUrl(category);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("movies");
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject movie = jsonArray.getJSONObject(i);

                        //Get video/movie metadata
                        int id = movie.getInt("movie_id");
                        String name = movie.getString("name");
                        int year = movie.getInt("year");
                        int length_seconds = movie.getInt("length_seconds");

                        //Get all the categories the movie/video has and store them in a list
                        JSONArray categories = movie.getJSONArray("categories");
                        List<String> categoriesList = new ArrayList<String>();
                        for (int j=0;j<categories.length();j++){
                            categoriesList.add(categories.getString(j));
                        }

                        //Get all the available urls of a certain movie/video but only select
                        //  the one encapsulated with MP4 type for now
                        JSONObject urlObject = movie.getJSONObject("urls");
                        JSONArray urls = urlObject.getJSONArray("entry");
                        List<String> urlList = new ArrayList<>();
                        for (int j=0;j<urls.length();j++){
                            JSONObject urlEntry = urls.getJSONObject(j);
                            String codec = urlEntry.getString("key");
                            if(codec.equals("MP4")){
                                String url = urlEntry.getString("value");
                                urlList.add(url);
                            }
                        }

                        Content toAdd = new Content(id,name,Content.VIDEO_TYPE,getDescriptionContent(length_seconds,year),categoriesList,urlList);
                        contentList.add(toAdd);
                    }
                    //Call back to hide progress bar
                    callback.onSuccess(progressBar);
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

    private void getStreamContent(final ProgressBar progressBar,final ContentRequestCallBack callback){
        String requestUrl = getStreamsUrl();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("streams");
                    for (int i=0;i<jsonArray.length();i++){
                        String streamID = jsonArray.getString(i);
                        Content toAdd = new Content(streamID,Content.STREAM_TYPE);
                        contentList.add(toAdd);
                    }
                    callback.onSuccess(progressBar);
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
        setContentView(R.layout.content_view);

        String category = getIntent().getStringExtra("category"); //Get category selected by user for video/movies
        String contentType = getIntent().getStringExtra("ContentType"); //What type of content to list? Video/movie or stream?

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        contentList = new ArrayList<Content>();
        contentAdapter = new ContentAdapter(this,contentList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contentAdapter);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.contentProgressBar);

        if(contentType.equals("video")){
            //Get all the video content with the selected category and in the end hide the progress bar
            getVideoContent(category, progressBar,new ContentRequestCallBack() {
                @Override
                public void onSuccess(ProgressBar progressBar) {
                    contentAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else if(contentType.equals("stream")){
            getStreamContent(progressBar,new ContentRequestCallBack() {
                @Override
                public void onSuccess(ProgressBar progressBar) {
                    contentAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }


    }
}
