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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class SearchEvent extends AppCompatActivity {

    String TAG = "SearchEvent", username;
    Switch alldayswitch;
    CheckBox titlecheck, startdatetimecheck, locationcheck;
    TextView titletv, startdatetv, startdatedisplay, starttimetv, starttimedisplay, locationtv;
    EditText titlefield, locationfield;
    String title, startyear, startmonth, startdate, starthour, startminute, location;
    DatePickerDialog.OnDateSetListener date1;
    MyArrayAdapter adapter;
    ListView listView;
    Button search;
    ProgressDialog dialog;
    Boolean handlerneeded = false, started = false, allday;
    long stime, now;
    ArrayList<Event> eventList;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CREATE_URL = "http://wncc-iitb.org:8000/create";
    private static final String SEARCH_URL = "http://wncc-iitb.org:8000/search";

    private Handler handler = new Handler();
    private Runnable timeout = new Runnable(){
        public void run() {
            if(handlerneeded) {

                if(!started) {
                    stime = System.currentTimeMillis();
                    started=true;
                }
                now = System.currentTimeMillis();
                if(now>stime+10000) {
                    dialog.dismiss();
                    handlerneeded=false;
                    started=false;
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

        titlecheck = (CheckBox)findViewById(R.id.titlecheck);
        startdatetimecheck = (CheckBox)findViewById(R.id.datetimecheck);
        locationcheck = (CheckBox)findViewById(R.id.locationcheck);

        titlecheck.setTextSize(6*getResources().getDisplayMetrics().density);
        startdatetimecheck.setTextSize(6*getResources().getDisplayMetrics().density);
        locationcheck.setTextSize(6*getResources().getDisplayMetrics().density);

        alldayswitch = (Switch)findViewById(R.id.alldayswitch);

        titletv = (TextView)findViewById(R.id.titletv);
        startdatetv = (TextView)findViewById(R.id.startdatetv);
        starttimetv = (TextView)findViewById(R.id.starttimetv);
        startdatedisplay = (TextView)findViewById(R.id.startdatedisplay);
        starttimedisplay = (TextView)findViewById(R.id.starttimedisplay);
        locationtv = (TextView)findViewById(R.id.locationtv);

        titlefield = (EditText)findViewById(R.id.title);
        locationfield = (EditText)findViewById(R.id.location);
        search = (Button)findViewById(R.id.searchbutton);
        listView = (ListView) findViewById(R.id.listView);

        titlecheck.setChecked(true);
        titletv.setEnabled(true);
        titlefield.setEnabled(true);

        alldayswitch.setEnabled(false);
        startdatetv.setEnabled(false);
        starttimetv.setEnabled(false);
        locationtv.setEnabled(false);
        locationfield.setEnabled(false);

        try {
            final Calendar startdate = Calendar.getInstance();

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
        }
        catch(Exception e) {
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
                }
                else {
                    starttimetv.setEnabled(true);
                    starttimetv.setVisibility(View.VISIBLE);
                    starttimedisplay.setEnabled(true);
                    starttimedisplay.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void onCheckBoxClicked(View v) {
        switch (v.getId()) {
            case R.id.titlecheck:
                if(titlecheck.isChecked()) {
                    titletv.setEnabled(true);
                    titlefield.setEnabled(true);
                    titlefield.requestFocus();
                }
                else {
                    titlefield.setText("");
                    titletv.setEnabled(false);
                    titlefield.setEnabled(false);
                }
                break;
            case R.id.datetimecheck:
                if(startdatetimecheck.isChecked()) {
                    startdatetv.setEnabled(true);
                    starttimetv.setEnabled(true);
                    startdatedisplay.setEnabled(true);
                    starttimedisplay.setEnabled(true);
                    alldayswitch.setEnabled(true);
                    alldayswitch.setChecked(false);
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);
                    String day = (String) ((mDay < 10) ? "0" + mDay
                            : Integer.toString(mDay));
                    String month = (String) ((mMonth+1 < 10) ? "0" + (mMonth+1)
                            : Integer.toString(mMonth+1));
                    String prettyDate = day + "/" + month + "/" + String.valueOf(mYear);
                    String hours = (String) ((mHour < 10) ? "0" + mHour
                            : Integer.toString(mHour));
                    String minutes = (String) ((mMinute < 10) ? "0" + mMinute
                            : Integer.toString(mMinute));
                    String prettyTime = hours + ":" + minutes;
                    startdatedisplay.setText(prettyDate);
                    starttimedisplay.setText(prettyTime);
                }
                else {
                    startdatetv.setEnabled(false);
                    startdatedisplay.setEnabled(false);
                    starttimetv.setEnabled(false);
                    starttimedisplay.setEnabled(false);
                    alldayswitch.setEnabled(false);
                    startdatedisplay.setText("");
                    starttimedisplay.setText("");
                }
                break;
            case R.id.locationcheck:
                if(locationcheck.isChecked()) {
                    locationtv.setEnabled(true);
                    locationfield.setEnabled(true);
                    locationfield.requestFocus();
                }
                else {
                    locationfield.setText("");
                    locationtv.setEnabled(false);
                    locationfield.setEnabled(false);
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent i =new Intent();
            setResult(Activity.RESULT_CANCELED,i);
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
        }
        catch(Exception err) {
            Log.d(TAG , "Error in getting username");
        }
        username = "unknown";
    }

    public void Search(View v) {

        if (!isNetworkConnected()) {
            Toast.makeText(getApplicationContext(), "Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!(titlecheck.isChecked() || startdatetimecheck.isChecked() || locationcheck.isChecked())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Invalid Search")
                    .setMessage("At least one of the search parameters must be defined!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Yes button clicked, do something
                        }
                    })
                    .show();
            return;
        }

        if (startdatetimecheck.isChecked()) {
            allday = alldayswitch.isChecked();

            if (!alldayswitch.isChecked()) {
                if (startdatedisplay.getText().toString().equals("") || starttimedisplay.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setTitle("Invalid Entry")
                            .setMessage("Both Date and Time fields must be filled!")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Yes button clicked, do something
                                }
                            })
                            .show();
                    return;
                } else {
                    startdate = (startdatedisplay.getText().toString()).substring(0, 2);
                    startmonth = (startdatedisplay.getText().toString()).substring(3, 5);
                    startyear = (startdatedisplay.getText().toString()).substring(6, 10);
                    starthour = (starttimedisplay.getText().toString()).substring(0, 2);
                    startminute = (starttimedisplay.getText().toString()).substring(3, 5);
                }
            } else {
                if (startdatedisplay.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setTitle("Invalid Entry")
                            .setMessage("Date field cannot be empty!")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Yes button clicked, do something
                                }
                            })
                            .show();
                    return;
                } else {
                    startdate = (startdatedisplay.getText().toString()).substring(0, 2);
                    startmonth = (startdatedisplay.getText().toString()).substring(3, 5);
                    startyear = (startdatedisplay.getText().toString()).substring(6, 10);
                }
            }
        }

        if (locationcheck.isChecked()) {
            location = locationfield.getText().toString();
        }

        getUsername();

        /* after all checks */
        handlerneeded = false;
        started = false;
        search.setEnabled(true);
        dialog.dismiss();
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception err) {
            Log.d(TAG, "Keyboard already down");
            err.printStackTrace();
        }

        String starttime, endtime= "00:00";
        if (allday) {
            starttime = "00:00";
            endtime = "00:00";
        } else {
            starttime = starthour + ":" + startminute;
//            endtime = endhour + ":" + endminute;
        }

        String jsonData = "{"+ "\"allday\": \"" + allday.toString() + "\"";
        if (titlecheck.isChecked())
            jsonData += "\"title\": \"" + title + "\",";
        if (locationcheck.isChecked())
                jsonData += "\"location\": \"" + location + "\",";
        if (startdatetimecheck.isChecked())
            if (allday)
                jsonData += "\"startdate\": \"" + startdate + "/" + startmonth + "/" + startyear+ "\","
                + "\"starttime\": \"" + starttime + "\",";
            else
                jsonData += "\"startdate\": \"" + startdate + "/" + startmonth + "/" + startyear+ "\",";

        jsonData += "}";

        RequestBody body = RequestBody.create(JSON, jsonData);
        Log.d(TAG, "search JSON = " + jsonData);

        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
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
                    Log.d(TAG, "Response from " + SEARCH_URL + ": " + jsonData);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Event>>() {
                    }.getType();
                    eventList = gson.fromJson(jsonData, type);
                }
            }
        });

        Log.d(TAG, "Retrieved " + eventList.size() + " scores");
        if (eventList.size() > 0) {
            List<String> titlelist = new ArrayList<String>();
            List<String> locationlist = new ArrayList<String>();
            List<String> objectIdlist = new ArrayList<String>();
            for (int i = 0; i < eventList.size(); i++) {
                titlelist.add(eventList.get(i).title);
                locationlist.add(eventList.get(i).location);
                objectIdlist.add(eventList.get(i).id);
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

            search.setEnabled(false);
            handlerneeded = true;
            handler.post(timeout);
            dialog = ProgressDialog.show(SearchEvent.this, "Retrieving", "Please wait...", true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timeout);
    }
}
