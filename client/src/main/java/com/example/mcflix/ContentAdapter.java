package com.example.mcflix;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/*
 * Defines an adapter in order to create a dynamic content selector
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.MyViewHolder> {

    private List<Content> contentList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title, categories,description;
        public CardView parentLayout;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.content_item_title);
            categories = (TextView) view.findViewById(R.id.content_item_category);
            description = (TextView) view.findViewById(R.id.content_item_description);
            parentLayout = (CardView) view.findViewById(R.id.parent_layout);
        }
    }

    public ContentAdapter(Context context,List<Content> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_item, parent, false);

        return new MyViewHolder(itemView);
    }

    private String categoriesToString(List<String> categories){
        String categoriesString = "";
        for (int i=0;i<categories.size();i++){
            if(i==categories.size()-1){
                categoriesString+=categories.get(i);
            } else{
                categoriesString+=categories.get(i)+", ";
            }
        }
        return categoriesString;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Content element = contentList.get(position);
        holder.title.setText(element.getTitle());

        holder.categories.setText(categoriesToString(element.getCategories()));
        holder.description.setText(element.getDescription());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(element.getType() == Content.VIDEO_TYPE){
                    intent = new Intent(context, VideoActivity.class);
                } else {
                    intent = new Intent(context, WatchStreamActivity.class);
                }
                intent.putExtra("content",element); //Send content to video or stream activity
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
