package com.example.mcflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

interface CategoryRequestCallBack {
    void onSuccess(List<String> categoryList, Context context, ListView categoriesList,ProgressBar progressBar);
}

/*
 * Implements a video/movie category selection activity that later calls another activity that plays the selected content.
 */
public class VideoCategoriesActivity extends AppCompatActivity {

    private String getCategoriesUrl(){
        return MainActivity.appServerUrl+"/movies/categories";
    }

    /*
     * Does a GET method request to the application server to retrieve all the available categories for video/movie content and adds those to the category list
     */
    private void getCategoryList(final ListView categoriesList, final ProgressBar progressBar, final Context context, final CategoryRequestCallBack callBack){
        String requestUrl = getCategoriesUrl();
        RequestQueue queue = Volley.newRequestQueue(this);
        final List<String> categoryList = new ArrayList<String>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("categories");
                    for (int i=0;i<jsonArray.length();i++){
                        String category = jsonArray.getString(i);
                        categoryList.add(category);
                    }
                    //Callback to hide progress bar
                    callBack.onSuccess(categoryList,context,categoriesList,progressBar);
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
        queue.add(request);
    }

    ListView categoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_categories);
        categoriesList = (ListView)findViewById(R.id.categories_list);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.categoryProgressBar);

        getCategoryList(categoriesList,progressBar,this,new CategoryRequestCallBack() {
            @Override
            public void onSuccess(final List<String> categoryList, Context context, ListView categoriesList, ProgressBar progressBar) {

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,categoryList);

                categoriesList.setAdapter(arrayAdapter); //Show the current category list to the user

                progressBar.setVisibility(View.GONE); //Hide progerss bar

                categoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(VideoCategoriesActivity.this,ContentActivity.class);
                        i.putExtra("category",categoryList.get(position)); //Send name of category to video browser
                        i.putExtra("ContentType","video"); //Send the type of content to video browser
                        startActivity(i); //Call the content browser activity
                    }
                });
            }
        });




    }
}
