package com.nihal.arpan.eventual;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    int MAKE_REQUEST_CODE=1;
    boolean allday = false, handlerneeded = false, started = false;
    String TAG = "MainActivity";
    String title,description, location, startyear, startmonth, startdate, starthour, startminute, endyear, endmonth, enddate, endhour, endminute;
    String HOST = "www.eventual.co.in", QRStart = "EVENTualQR", QRSeperator = "~!#";
    ProgressDialog dialog;
    long stime, now;
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CREATE_URL = "http://www.eventual.co.in/create";
    private static final String SEARCH_URL = "http://www.eventual.co.in/search";

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
                    Toast.makeText(MainActivity.this, "Cannot connect to server. Please check your connection", Toast.LENGTH_LONG).show();
                }
                handler.post(this);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            getSupportActionBar().hide();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        String link="",website="";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        try {
            Uri data = intent.getData();
            if (data != null) {
                website = data.getHost();
                link = data.getPath();
            }

            if (data != null && website.equals(HOST)) {
                final String objectId = link.substring(7, link.length());
                Log.d(TAG, "ObjectId = " + objectId);
                Intent i = new Intent(MainActivity.this, SearchResult.class);
                i.putExtra("objectId", objectId);
                startActivity(i);
                finish();
            }

        }
        catch(Exception e) {
            Log.d(TAG, "Deep link");
        }


        Button make = (Button)findViewById(R.id.make_button);
        Log.d(TAG,String.valueOf(getResources().getDisplayMetrics().density));
        make.setTextSize(10*getResources().getDisplayMetrics().density);
    }

    public void linkToWebsite(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://arpan98.github.io/EVENTual/"));
        startActivity(browserIntent);
    }

    public void onQRButtonClicked(View v) {
        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error in initiating QR Scan", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void onMyEventsClicked(View view) {
        Intent intent = new Intent(MainActivity.this, MyEvents.class);
        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {

        try {
           if (requestCode != MAKE_REQUEST_CODE) {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (scanResult.getContents() != null) {
                    String scanres = intent.getStringExtra("SCAN_RESULT");

                    if (scanres.length() > 22) {
                        if (scanres.substring(0, 23).equals(HOST)) {
                            try {
                                final String code = scanres.substring(24, 34);
                                Log.d(TAG, code);
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder
                                        .setTitle("Add event")
                                        .setMessage("Do you want to add this event to calendar?")
                                        .setIcon(android.R.drawable.ic_menu_my_calendar)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Yes button clicked, do something
                                                Button addButton = (Button) findViewById(R.id.add);
                                                setOnClick(addButton, code);
                                                addButton.performClick();
                                            }
                                        })
                                        .setNegativeButton("No", null)                    //Do nothing on no
                                        .show();
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                                Toast.makeText(getApplicationContext(), "Error in parsing QR Code", Toast.LENGTH_LONG).show();
                            }
                        } else if (scanres.substring(0, 10).equals(QRStart)) {
                            try {
                                //looks like EVENTualQR~!#title~!#description~!#allday~!#startdate~!#starttime~!#enddate~!#endtime~!#location~!#private
                                // eg. EVENTualQR~!#Football match~!#England vs Iceland~!#false~!#28/06/2016/~!#00:30~!#29/06/16~!#02:30~!#Europe~!#false
                                // or EVENTualQR~!#Football match~!#England vs Iceland~!#false~!#28/06/2016/~!#29/06/16~!#Europe~!#false

                                Log.d("QR",scanres);
                                int index1 = scanres.indexOf(QRSeperator);
                                int index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                title = scanres.substring(index1 + 3, index2);
                                Log.d("QR",title);
                                index1 = index2;
                                index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                description = scanres.substring(index1 + 3, index2);
                                Log.d("QR",description);
                                index1 = index2;
                                index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                String ad = scanres.substring(index1 + 3, index2);
                                Log.d("QR",ad);
                                if (ad.equals("false")) {
                                    allday = false;
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    startdate = scanres.substring(index1 + 3, index1 + 5);
                                    startmonth = scanres.substring(index1 + 6, index1 + 8);
                                    startyear = scanres.substring(index1 + 9, index1 + 13);
                                    starthour = scanres.substring(index1 + 14, index1 + 16);
                                    startminute = scanres.substring(index1 + 17, index1 + 19);
                                    Log.d("QR",scanres.substring(index1+3, index2));
                                    Log.d("QR",startdate+startmonth+startyear+starthour+startminute);
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    enddate = scanres.substring(index1 + 3, index1 + 5);
                                    endmonth = scanres.substring(index1 + 6, index1 + 8);
                                    endyear = scanres.substring(index1 + 9, index1 + 13);
                                    endhour = scanres.substring(index1 + 14, index1 + 16);
                                    endminute = scanres.substring(index1 + 17, index1 + 19);
                                    Log.d("QR",scanres.substring(index1+3, index2));
                                    Log.d("QR",enddate+endmonth+endyear+endhour+endminute);
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    location = scanres.substring(index1 + 3, index2);
                                    Log.d("QR", location);
                                    //TODO:Set timezone in QR reader

                                    try {
                                        startdate = String.valueOf(Integer.parseInt(startdate));
                                        startmonth = String.valueOf(Integer.parseInt(startmonth));
                                        startyear = String.valueOf(Integer.parseInt(startyear));
                                        starthour = String.valueOf(Integer.parseInt(starthour));
                                        startminute = String.valueOf(Integer.parseInt(startminute));

                                        enddate = String.valueOf(Integer.parseInt(enddate));
                                        endmonth = String.valueOf(Integer.parseInt(endmonth));
                                        endyear = String.valueOf(Integer.parseInt(endyear));
                                        endhour = String.valueOf(Integer.parseInt(endhour));
                                        endminute = String.valueOf(Integer.parseInt(endminute));
                                    } catch (Exception e) {
                                        Log.d(TAG, "FROM QR - " + e.toString());
                                    }
                                } else {
                                    allday = true;
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    startdate = scanres.substring(index1 + 3, index1 + 5);
                                    startmonth = scanres.substring(index1 + 6, index1 + 8);
                                    startyear = scanres.substring(index1 + 9, index1 + 13);
                                    Log.d("QR",scanres.substring(index1+3, index2));
                                    Log.d("QR",startdate+startmonth+startyear);
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    enddate = scanres.substring(index1 + 3, index1 + 5);
                                    endmonth = scanres.substring(index1 + 6, index1 + 8);
                                    endyear = scanres.substring(index1 + 9, index1 + 13);
                                    Log.d("QR",scanres.substring(index1+3, index2));
                                    Log.d("QR",enddate+endmonth+endyear);
                                    index1 = index2;
                                    index2 = scanres.indexOf(QRSeperator, index1 + 1);
                                    location = scanres.substring(index1 + 3, index2);
                                    Log.d("QR", location);
                                    //TODO:Set timezone in QR reader

                                    try {
                                        startdate = String.valueOf(Integer.parseInt(startdate));
                                        startmonth = String.valueOf(Integer.parseInt(startmonth));
                                        startyear = String.valueOf(Integer.parseInt(startyear));

                                        enddate = String.valueOf(Integer.parseInt(enddate));
                                        endmonth = String.valueOf(Integer.parseInt(endmonth));
                                        endyear = String.valueOf(Integer.parseInt(endyear));
                                    } catch (Exception e) {
                                        Log.d(TAG, "FROM QR - " + e.toString());
                                    }
                                }
                                Log.d(TAG, "Title = " + title);
                                Log.d(TAG, "Description = " + description);
                                Log.d(TAG, "Location = " + location);
                                Log.d(TAG, "startyear = " + startyear);
                                Log.d(TAG, "startmonth = " + startmonth);
                                Log.d(TAG, "startdate = " + startdate);
                                Log.d(TAG, "starthour = " + starthour);
                                Log.d(TAG, "startminute = " + startminute);
                                Log.d(TAG, "endyear = " + endyear);
                                Log.d(TAG, "endmonth = " + endmonth);
                                Log.d(TAG, "enddate = " + enddate);
                                Log.d(TAG, "endhour = " + endhour);
                                Log.d(TAG, "endminute = " + endminute);
                                Log.d(TAG, "allday = " + allday);
                                makeCalenderEvent();
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                                Toast.makeText(getApplicationContext(), "Error in parsing QR Code", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder
                                    .setTitle("QR Code Text")
                                    .setMessage(scanres)
                                    .setIcon(android.R.drawable.ic_menu_my_calendar)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder
                                .setTitle("QR Code Text")
                                .setMessage(scanres)
                                .setIcon(android.R.drawable.ic_menu_my_calendar)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                }
            }
        }
        catch(Exception err) {
            Toast.makeText(getApplicationContext(), "Error in getting result from Activity. Please write a detailed report on the playstore page.", Toast.LENGTH_LONG).show();
            err.printStackTrace();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void makeCalenderEvent() {
        if(title!=null) {
            handlerneeded = false;

            Log.d(TAG, "Title = " + title);
            Log.d(TAG, "Description = " + description);
            Log.d(TAG, "Location = " + location);
            Log.d(TAG, "startyear = " + startyear);
            Log.d(TAG, "startmonth = " + startmonth);   //-1 done later
            Log.d(TAG, "startdate = " + startdate);
            Log.d(TAG, "starthour = " + starthour);
            Log.d(TAG, "startminute = " + startminute);
            Log.d(TAG, "endyear = " + endyear);
            Log.d(TAG, "endmonth = " + endmonth);   //-1 done later
            Log.d(TAG, "enddate = " + enddate);
            Log.d(TAG, "endhour = " + endhour);
            Log.d(TAG, "endminute = " + endminute);
            Log.d(TAG, "allday = " + allday);

            long startMillis = 0;
            long endMillis = 0;
            Calendar beginTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            try {
                if (!allday) {
                    beginTime.set(Integer.parseInt(startyear), Integer.parseInt(startmonth) - 1, Integer.parseInt(startdate), Integer.parseInt(starthour), Integer.parseInt(startminute));               //year,month,day,hour of day,minute
                    endTime.set(Integer.parseInt(endyear), Integer.parseInt(endmonth) - 1, Integer.parseInt(enddate), Integer.parseInt(endhour), Integer.parseInt(endminute));
                } else {
                    beginTime.set(Integer.parseInt(startyear), Integer.parseInt(startmonth) - 1, Integer.parseInt(startdate));
                    endTime.set(Integer.parseInt(endyear), Integer.parseInt(endmonth) - 1, Integer.parseInt(enddate));
                }
                startMillis = beginTime.getTimeInMillis();
                endMillis = endTime.getTimeInMillis();
                insertEntry(title, description, location, startMillis, endMillis, allday);
            }
            catch(Exception e) {
                Log.d(TAG,"Probably in parseInt() - " +e.toString());
                Toast.makeText(getApplicationContext(), "Error in creating event", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setOnClick(final Button btn, final String objectId){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkConnected()) {

                }
                else{
                    Toast.makeText(getApplicationContext(), "Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
                }


            }
        });
    }
    public void insertEntry(String pTitle, String pDescription, String pLocation, long pStartTimestamp, long pEndTimestamp, boolean allDay) {
        /*
        ContentValues values = new ContentValues();
        ContentResolver mContentResolver = this.getContentResolver();
        values.put(CalendarContract.Events.CALENDAR_ID, DEFAULT_CALENDAR_ID);
        values.put(CalendarContract.Events.TITLE, pTitle);
        values.put(CalendarContract.Events.DESCRIPTION, pDescription);
        values.put(CalendarContract.Events.EVENT_LOCATION, pLocation);
        values.put(CalendarContract.Events.DTSTART, pStartTimestamp);
        values.put(CalendarContract.Events.DTEND, pEndTimestamp);
        values.put(CalendarContract.Events.HAS_ALARM, 1); // 0 for false, 1 for true
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "India Standard Time"); //get the Timezone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            Uri uri = mContentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
        }
        */
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, pStartTimestamp);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, pEndTimestamp);
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay);

            intent.putExtra(CalendarContract.Events.TITLE, pTitle);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, pDescription);
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, pLocation);
            //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

            startActivity(intent);
        }
        catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Error in opening Calendar", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void onMakeNewEventClicked(View v) {
        Intent i = new Intent(this,EventCreate.class);
        startActivityForResult(i, MAKE_REQUEST_CODE);
    }

    public void onSearchEventsClicked(View v) {
        Intent i = new Intent(this,SearchEvent.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timeout);
    }
}
