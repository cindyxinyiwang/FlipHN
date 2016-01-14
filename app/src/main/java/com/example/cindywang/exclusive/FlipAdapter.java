package com.example.cindywang.exclusive;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cindywang on 12/5/15.
 */
public class FlipAdapter extends BaseAdapter implements View.OnClickListener {

    public interface Callback {
        public void onPageRequested(int page);
    }

    static class Item {
        static long id = 0;

        long mId;
        int storyId;
        String storyUrl;
        Firebase storyUrlFireBase;

        public Item() {
            mId = id++;
        }

        void setStoryId(int sId) {
            storyId = sId;
        }
        int getStoryId(){
            return storyId;
        }
        void setStoryUrl(String sUrl) { storyUrl = sUrl; }
        String getStoryUrl() { return storyUrl; }

        void setupStoryUrl() {
            // setup storyUrl with storyUrlFirebase
            storyUrlFireBase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    storyUrl = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        long getId(){
            return mId;
        }
    }

    private LayoutInflater inflater;
    private  Callback callback;
    private List<Item> items = new ArrayList<Item>();
    private int NUM_ITEM = 10;
    Firebase hnFirebaseRef = new Firebase("https://hacker-news.firebaseio.com/v0/topstories");

    public FlipAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        for (int i = 0; i < NUM_ITEM; i++) {
            items.add(new Item());
        }
        getStoris();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void getStoris() {
        hnFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemCount = 0;
                for (DataSnapshot id: dataSnapshot.getChildren()) {
                    if (itemCount == NUM_ITEM) return;
                    items.get(itemCount).setStoryId(id.getValue(Integer.class));
                    Log.i("firebase", id.getValue(String.class));
                    String storyFirebaseUrl = "https://hacker-news.firebaseio.com/v0/item/"
                            +id.getValue(Integer.class) + "/url";
                    items.get(itemCount).storyUrlFireBase = new Firebase(storyFirebaseUrl);
                    items.get(itemCount).setupStoryUrl();
                    itemCount++;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println(firebaseError.getMessage());
            }
        });
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.page, parent, false);

            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.web = (WebView) convertView.findViewById(R.id.webview);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(items.get(position).getId() + ":" + position + ":"
            + items.get(position).getStoryId()+ items.get(position).getStoryUrl());
        // Configure related browser settings
        holder.web.getSettings().setLoadsImagesAutomatically(true);
        holder.web.getSettings().setJavaScriptEnabled(true);
        holder.web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        holder.web.setWebViewClient(new MyBrowser());
        // Load the initial URL
        holder.web.loadUrl(items.get(position).getStoryUrl());

        return convertView;
    }

    static class ViewHolder {
        TextView text;
        WebView web;
    }

    // Manages the behavior when URLs are loaded
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onClick(View v) {

    }
}
