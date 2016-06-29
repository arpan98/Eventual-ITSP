package com.nihal.arpan.eventual;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MyEvents extends AppCompatActivity {

    final String TAG = "MyEvents";
    String username = "unknown";
    ProgressDialog dialog;
    long stime, now;
    Boolean handlerneeded = false, started = false;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CREATE_URL = "http://www.eventual.co.in";
    private static final String SEARCH_URL = "http://www.eventual.co.in";


    private Handler handler = new Handler();
    private Runnable timeout = new Runnable() {
        public void run() {
            if (handlerneeded) {

                if (!started) {
                    stime = System.currentTimeMillis();
                    started = true;
                }
                now = System.currentTimeMillis();
                if (now > stime + 10000) {
                    dialog.dismiss();
                    handlerneeded = false;
                    started = false;
                    Toast.makeText(MyEvents.this, "Cannot connect to server. Please check your connection", Toast.LENGTH_LONG).show();
                }
                handler.post(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        getUsername();
        String jsonData = "{"+ "\"username\": \"" + username + "\"}";

        handlerneeded = true;
        handler.post(timeout);
        dialog = ProgressDialog.show(MyEvents.this, "Retrieving", "Please wait...", true);

        RequestBody body = RequestBody.create(JSON, jsonData);
        Log.d(TAG, "My Events JSON = " + jsonData);

        Request request = new com.squareup.okhttp.Request.Builder()
                .url(SEARCH_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                else {
                    final String jsonData = response.body().string();
                    Log.d(TAG, "Response from " + CREATE_URL + ": " + jsonData);
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<Event>>() {
                        }.getType();
                        final ArrayList<Event> eventList = gson.fromJson(jsonArray.toString(), type);
                        Log.d(TAG, "Retrieved " + eventList.size() + " events");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                handlerneeded = false;
                                if (eventList.size() > 0) {
                                    List<String> titlelist = new ArrayList<String>();
                                    List<String> locationlist = new ArrayList<String>();
                                    List<String> objectIdlist = new ArrayList<String>();
                                    for (int i = 0; i < eventList.size(); i++) {
                                        titlelist.add(eventList.get(i).title);
                                        locationlist.add(eventList.get(i).location);
                                        objectIdlist.add(String.valueOf(eventList.get(i).id));
                                    }
                                    final MyArrayAdapter adapter = new MyArrayAdapter(MyEvents.this, titlelist, locationlist, objectIdlist);
                                    ListView listView = (ListView)findViewById(R.id.myeventslistview);
                                    listView.setAdapter(adapter);

                                    // Click on listitem opens the event in SearchResult page
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            ListObject obj = adapter.getItem(position);
                                            Log.d(TAG, "CLICKED = " + obj.objectId);
                                            Intent i = new Intent(MyEvents.this, SearchResult.class);
                                            i.putExtra("objectId", obj.objectId);                   //Pass the objectId to SearchResult page.
                                            startActivity(i);
                                        }
                                    });
                                } else {
                                    Toast.makeText(MyEvents.this, "You haven't made any events", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void getUsername() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        try {
            Account[] accounts = AccountManager.get(MyEvents.this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    String possibleEmail = account.name;
                    username = possibleEmail;
                    Log.d(TAG, "Username: " + username);
                    return;
                }
            }
        }
        catch(Exception err) {
            Log.d(TAG , "Error in getting username");
        }
        username = "unknown";
    }
}
