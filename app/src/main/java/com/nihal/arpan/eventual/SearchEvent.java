package com.nihal.arpan.eventual;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class SearchEvent extends AppCompatActivity {

    String TAG = "SearchEvent", username;
    Switch alldayswitch, privateswitch;
    TextView titletv, locationtv;
    TextView startdatetv, startdatedisplay, starttimetv, starttimedisplay;
    TextView enddatetv, enddatedisplay, endtimetv, endtimedisplay;
    EditText titlefield, descriptionfield, locationfield;
    String startyear, startmonth, startdate, starthour, startminute;
    String endyear, endmonth, enddate, endhour, endminute;
    DatePickerDialog.OnDateSetListener date1, date2;
    MyArrayAdapter adapter;
    ListView listView;
    Button search;
    ProgressDialog dialog;
    Boolean handlerneeded = false, started = false, allday;
    long stime, now;
    ArrayList<Event> eventList;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CREATE_URL = "http://wncc-iitb.org:5697/create";
    private static final String SEARCH_URL = "http://wncc-iitb.org:5697/search";

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
                    Toast.makeText(SearchEvent.this, "Cannot connect to server. Please check your connection", Toast.LENGTH_LONG).show();
                }
                handler.post(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final TextView resultstv = (TextView) findViewById(R.id.resultstv);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)resultstv.getLayoutParams();
        params.setMargins(0, metrics.heightPixels, 0, 0);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        alldayswitch = (Switch) findViewById(R.id.alldayswitch);
        privateswitch = (Switch) findViewById(R.id.privateswitch);

        titletv = (TextView) findViewById(R.id.titletv);
        locationtv = (TextView) findViewById(R.id.locationtv);

        startdatetv = (TextView) findViewById(R.id.startdatetv);
        starttimetv = (TextView) findViewById(R.id.starttimetv);
        startdatedisplay = (TextView) findViewById(R.id.startdatedisplay);
        starttimedisplay = (TextView) findViewById(R.id.starttimedisplay);

        enddatetv = (TextView) findViewById(R.id.enddatetv);
        endtimetv = (TextView) findViewById(R.id.endtimetv);
        enddatedisplay = (TextView) findViewById(R.id.enddatedisplay);
        endtimedisplay = (TextView) findViewById(R.id.endtimedisplay);

        titlefield = (EditText) findViewById(R.id.title);
        descriptionfield = (EditText) findViewById(R.id.description);
        locationfield = (EditText) findViewById(R.id.location);
        search = (Button) findViewById(R.id.searchbutton);
        eventList = new ArrayList<>();

        try {
            final Calendar startdate = Calendar.getInstance();
            final Calendar enddate = Calendar.getInstance();

            date1 = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    monthOfYear += 1;
                    startdate.set(Calendar.YEAR, year);
                    startdate.set(Calendar.MONTH, monthOfYear);
                    startdate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String day = (String) ((dayOfMonth < 10) ? "0" + dayOfMonth
                            : Integer.toString(dayOfMonth));
                    String month = (String) ((monthOfYear < 10) ? "0" + monthOfYear
                            : Integer.toString(monthOfYear));
                    String prettyDate = day + "/" + month + "/" + String.valueOf(year);
                    startdatedisplay.setText(prettyDate);
                }

            };

            startdatetv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(SearchEvent.this, date1, startdate
                            .get(Calendar.YEAR), startdate.get(Calendar.MONTH),
                            startdate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            startdatedisplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(SearchEvent.this, date1, startdate
                            .get(Calendar.YEAR), startdate.get(Calendar.MONTH),
                            startdate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            starttimetv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(SearchEvent.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            //Add leading zeros if required
                            String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                    : Integer.toString(selectedHour));
                            String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                    : Integer.toString(selectedMinute));
                            String prettyTime = hours + ":" + minutes;
                            starttimedisplay.setText(prettyTime);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }
            });
            starttimedisplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(SearchEvent.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            //Add leading zeros if required
                            String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                    : Integer.toString(selectedHour));
                            String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                    : Integer.toString(selectedMinute));
                            String prettyTime = hours + ":" + minutes;
                            starttimedisplay.setText(prettyTime);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }
            });

            date2 = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    monthOfYear += 1;
                    enddate.set(Calendar.YEAR, year);
                    enddate.set(Calendar.MONTH, monthOfYear);
                    enddate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String day = (String) ((dayOfMonth < 10) ? "0" + dayOfMonth
                            : Integer.toString(dayOfMonth));
                    String month = (String) ((monthOfYear < 10) ? "0" + monthOfYear
                            : Integer.toString(monthOfYear));
                    String prettyDate = day + "/" + month + "/" + String.valueOf(year);
                    enddatedisplay.setText(prettyDate);
                }

            };

            enddatetv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(SearchEvent.this, date2, enddate
                            .get(Calendar.YEAR), enddate.get(Calendar.MONTH),
                            enddate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            enddatedisplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(SearchEvent.this, date2, enddate
                            .get(Calendar.YEAR), enddate.get(Calendar.MONTH),
                            enddate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            endtimetv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(SearchEvent.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            //Add leading zeros if required
                            String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                    : Integer.toString(selectedHour));
                            String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                    : Integer.toString(selectedMinute));
                            String prettyTime = hours + ":" + minutes;
                            endtimedisplay.setText(prettyTime);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }
            });
            endtimedisplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(SearchEvent.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            //Add leading zeros if required
                            String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                    : Integer.toString(selectedHour));
                            String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                    : Integer.toString(selectedMinute));
                            String prettyTime = hours + ":" + minutes;
                            endtimedisplay.setText(prettyTime);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in creating date and time entries", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        alldayswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    starttimetv.setEnabled(false);
                    starttimetv.setVisibility(View.INVISIBLE);
                    starttimedisplay.setEnabled(false);
                    starttimedisplay.setVisibility(View.INVISIBLE);
                } else {
                    starttimetv.setEnabled(true);
                    starttimetv.setVisibility(View.VISIBLE);
                    starttimedisplay.setEnabled(true);
                    starttimedisplay.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent i = new Intent();
            setResult(Activity.RESULT_CANCELED, i);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void getUsername() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        try {
            Account[] accounts = AccountManager.get(SearchEvent.this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    String possibleEmail = account.name;
                    username = possibleEmail;
                    Log.d(TAG, "Username: " + username);
                    return;
                }
            }
        } catch (Exception err) {
            Log.d(TAG, "Error in getting username");
        }
        username = "unknown";
    }

    public void Search(View v) {

        if (!isNetworkConnected()) {
            Toast.makeText(getApplicationContext(), "Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        getUsername();

        /* after all checks */
//        handlerneeded = false;
//        started = false;
//        search.setEnabled(true);
//        dialog.dismiss();
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception err) {
            Log.d(TAG, "Keyboard already down");
            err.printStackTrace();
        }

        String title = titlefield.getText().toString();
        String description = descriptionfield.getText().toString();
        String location = locationfield.getText().toString();
        allday = alldayswitch.isChecked();

//        When date and time Textviews not clicked at all
        String totalstartdate, totalenddate;
        if (startdate == null) {
            totalstartdate = "";
        } else {
            totalstartdate = startdate + "/" + startmonth + "/" + startyear;
        }
        if (enddate == null) {
            totalenddate = "";
        } else {
            totalenddate = enddate + "/" + endmonth + "/" + endyear;
        }

        String starttime, endtime = "00:00";
        if (allday) {
            starttime = "00:00";
            endtime = "00:00";
        } else {
            if (starthour == null) {
                starttime = "";
            } else {
                starttime = starthour + ":" + startminute;
            }
            if (endhour == null) {
                endtime = "";
            } else {
                endtime = endhour + ":" + endminute;
            }
        }

        String jsonData = "{" + "\"username\": \"" + username + "\",";
        // Add param to json if not left blank ie ""
        if (!title.equals("")) {
            jsonData += "\"title\": \"" + title + "\",";
        }
        if (!description.equals("")) {
            jsonData += "\"description\": \"" + description + "\",";
        }
        if (!location.equals("")) {
            jsonData += "\"location\": \"" + location + "\",";
        }
        if (!totalstartdate.equals("")) {
            jsonData += "\"startdate\": \"" + totalstartdate + "\",";
        }
        if (!totalenddate.equals("")) {
            jsonData += "\"enddate\": \"" + totalenddate + "\",";
        }
        if (!starttime.equals("")) {
            jsonData += "\"starttime\": \"" + starttime + "\",";
        }
        if (!endtime.equals("")) {
            jsonData += "\"endtime\": \"" + endtime + "\",";
        }
        jsonData += "\"allday\": \"" + allday.toString().toLowerCase() + "\","
                + "\"private\": \"" + String.valueOf(privateswitch.isChecked()).toLowerCase() + "\""
                + "}";
        search.setEnabled(false);
        handlerneeded = true;
        handler.post(timeout);
        dialog = ProgressDialog.show(SearchEvent.this, "Retrieving", "Please wait...", true);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.execute(jsonData);
    }

    class SearchRequest extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            RequestBody body = RequestBody.create(JSON, params[0]);
            Log.d(TAG, "search JSON = " + params[0]);

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(SEARCH_URL)
                    .post(body)
                    .build();

            Response response;
            try {
                response = client.newCall(request).execute();
                if(response.isSuccessful()) {
                    final String jsonResponseData = response.body().string();
                    Log.d(TAG, "Response from " + SEARCH_URL + ": " + params[0]);
                    try {
                        JSONArray jsonArray = new JSONArray(jsonResponseData);
                        Log.d(TAG, jsonArray.toString());
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<Event>>() {
                        }.getType();
                        eventList = gson.fromJson(jsonArray.toString(), type);
                        Log.d(TAG, "Retrieved " + eventList.size() + " scores");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView resultstv = (TextView)findViewById(R.id.resultstv);
                    resultstv.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);
                    search.setEnabled(true);
                    dialog.dismiss();
                    handlerneeded = false;
                    if (eventList.size() > 0) {
                        ScrollView sv = (ScrollView)findViewById(R.id.scrollView);
                        sv.scrollTo(0, listView.getTop());
                        List<String> titlelist = new ArrayList<String>();
                        List<String> locationlist = new ArrayList<String>();
                        List<String> objectIdlist = new ArrayList<String>();
                        for (int i = 0; i < eventList.size(); i++) {
                            titlelist.add(eventList.get(i).title);
                            locationlist.add(eventList.get(i).location);
                            objectIdlist.add(String.valueOf(eventList.get(i).id));
                        }
                        adapter = new MyArrayAdapter(SearchEvent.this, titlelist, locationlist, objectIdlist);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ListObject obj = adapter.getItem(position);
                                Log.d(TAG, "CLICKED = " + obj.objectId);
                                Intent i = new Intent(SearchEvent.this, SearchResult.class);
                                i.putExtra("objectId", obj.objectId);
                                startActivity(i);
                            }
                        });
                    } else {
                        resultstv.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        Toast.makeText(SearchEvent.this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timeout);
    }
}
