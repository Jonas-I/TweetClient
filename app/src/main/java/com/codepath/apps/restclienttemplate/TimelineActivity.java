package com.codepath.apps.restclienttemplate;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private RecyclerView rvTweets;
    private TweetsAdapter adapter;
    private List<Tweet> tweets;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,

                android.R.color.holo_green_light,

                android.R.color.holo_orange_light,

                android.R.color.holo_red_light);
        //Find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Initialize list of tweets and adapter from the data source
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        //Recycler View Setup: Layout manager and setting the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        populateHomeTimeline();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TwitterClient", "Content Refreshing");
                populateHomeTimeline();
            }
        });
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                super.onSuccess(statusCode, headers, response);
//                Log.d("TwitterClient", response.toString());
                List<Tweet> tweetsToAdd = new ArrayList<>();
                //Iterate through list of tweets
                for (int i = 0; i < response.length(); i++) {
                    try {
                        //Convert each json object into a tweet object
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);
                        tweetsToAdd.add(tweet);
                        // Add the tweet into our data source
//                        tweets.add(tweet);
//                        //notify adapter
//                        adapter.notifyItemInserted(tweets.size()-1);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // Clear existing data
                adapter.clear();
                // Show data was received
                adapter.addTweets(tweetsToAdd);

                // Call set refreshing
                swipeContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("TwitterClient", responseString);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("TwitterClient", errorResponse.toString());
            }
        });
    }
}
