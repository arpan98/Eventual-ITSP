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
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Pattern;

public class EventCreate extends AppCompatActivity {
    String TAG = "EventCreate", existingOId="", QRStart="EVENTualQR", seq="~!#", username="unknown";
    TextView startdatedisplay, starttimedisplay, enddatedisplay, endtimedisplay, startdatetv, starttimetv, enddatetv, endtimetv;
    EditText titlefield, descriptionfield, locationfield;
    Switch alldayfield;
    DatePickerDialog.OnDateSetListener date1, date2;
    Button savebutton;
    Boolean allday, duplicate, handlerneeded=false, started=false;
    String title, description, location, startyear, startmonth, startdate, starthour, startminute, endyear, endmonth, enddate, endhour, endminute;
    ProgressDialog dialog;
    QRCodeEncoder qrCodeEncoder;
    Bitmap b;
    Long stime,now;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CREATE_URL = "http://wncc-iitb.org:8000/create";
    private static final String SEARCH_URL = "http://wncc-iitb.org:8000/search";
    String create;

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
                    Toast.makeText(EventCreate.this, "Cannot connect to server. Please check your connection", Toast.LENGTH_LONG).show();
                }
                handler.post(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        /* Initalising all views and extracting values */
        startdatetv = (TextView)findViewById(R.id.startdatetv);
        starttimetv = (TextView)findViewById(R.id.starttimetv);
        enddatetv = (TextView)findViewById(R.id.enddatetv);
        endtimetv = (TextView)findViewById(R.id.endtimetv);
        startdatedisplay = (TextView)findViewById(R.id.startdatedisplay);
        starttimedisplay = (TextView)findViewById(R.id.starttimedisplay);
        enddatedisplay = (TextView)findViewById(R.id.enddatedisplay);
        endtimedisplay = (TextView)findViewById(R.id.endtimedisplay);

        titlefield=(EditText)findViewById(R.id.title);
        descriptionfield=(EditText)findViewById(R.id.description);
        alldayfield=(Switch)findViewById(R.id.allday);
        locationfield=(EditText)findViewById(R.id.location);

        savebutton = (Button)findViewById(R.id.savebutton);

        final Calendar startdate = Calendar.getInstance();
        final Calendar enddate = Calendar.getInstance();

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

        c.add(Calendar.HOUR_OF_DAY, 1);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        day = (String) ((mDay < 10) ? "0" + mDay
                : Integer.toString(mDay));
        month = (String) ((mMonth+1 < 10) ? "0" + (mMonth+1)
                : Integer.toString(mMonth+1));
        prettyDate = day + "/" + month + "/" + String.valueOf(mYear);
        hours = (String) ((mHour < 10) ? "0" + mHour
                : Integer.toString(mHour));
        minutes = (String) ((mMinute < 10) ? "0" + mMinute
                : Integer.toString(mMinute));
        prettyTime = hours + ":" + minutes;
        enddatedisplay.setText(prettyDate);
        endtimedisplay.setText(prettyTime);


        date1 = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                monthOfYear+=1;
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
                new DatePickerDialog(EventCreate.this, date1, startdate
                        .get(Calendar.YEAR), startdate.get(Calendar.MONTH),
                        startdate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        startdatedisplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(EventCreate.this, date1, startdate
                        .get(Calendar.YEAR), startdate.get(Calendar.MONTH),
                        startdate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        date2 = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                monthOfYear+=1;
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
                new DatePickerDialog(EventCreate.this, date2, enddate
                        .get(Calendar.YEAR), enddate.get(Calendar.MONTH),
                        enddate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        enddatedisplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(EventCreate.this, date2, enddate
                        .get(Calendar.YEAR), enddate.get(Calendar.MONTH),
                        enddate.get(Calendar.DAY_OF_MONTH)).show();
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
                mTimePicker = new TimePickerDialog(EventCreate.this, new TimePickerDialog.OnTimeSetListener() {
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
                mTimePicker = new TimePickerDialog(EventCreate.this, new TimePickerDialog.OnTimeSetListener() {
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

        endtimetv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(EventCreate.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Add leading zeros if required
                        String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                : Integer.toString(selectedHour));
                        String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                : Integer.toString(selectedMinute));
                        String prettyTime = hours + ":" + minutes;
                        endtimedisplay.setText( prettyTime);
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
                mTimePicker = new TimePickerDialog(EventCreate.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Add leading zeros if required
                        String hours = (String) ((selectedHour < 10) ? "0" + selectedHour
                                : Integer.toString(selectedHour));
                        String minutes = (String) ((selectedMinute < 10) ? "0" + selectedMinute
                                : Integer.toString(selectedMinute));
                        String prettyTime = hours + ":" + minutes;
                        endtimedisplay.setText( prettyTime);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        alldayfield.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    starttimetv.setEnabled(false);
                    starttimetv.setVisibility(View.INVISIBLE);
                    endtimetv.setEnabled(false);
                    endtimetv.setVisibility(View.INVISIBLE);
                    starttimedisplay.setEnabled(false);
                    starttimedisplay.setVisibility(View.INVISIBLE);
                    endtimedisplay.setEnabled(false);
                    endtimedisplay.setVisibility(View.INVISIBLE);
                }
                else {
                    starttimetv.setEnabled(true);
                    starttimetv.setVisibility(View.VISIBLE);
                    endtimetv.setEnabled(true);
                    endtimetv.setVisibility(View.VISIBLE);
                    starttimedisplay.setEnabled(true);
                    starttimedisplay.setVisibility(View.VISIBLE);
                    endtimedisplay.setEnabled(true);
                    endtimedisplay.setVisibility(View.VISIBLE);
                }
            }
        });

    }
    // End of value extraction and onCreate


    /* Check for network connection */
    public boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isDateEmpty() {
        return (startdatedisplay.getText().toString().equals("") || enddatedisplay.getText().toString().equals(""));
    }

    public boolean isTimeEmpty() {
        return (starttimedisplay.getText().toString().equals("") || endtimedisplay.getText().toString().equals(""));
    }

    public void checkIfEventAlreadyExists() {
        String jsonData = "{"+ "\"username\": \"" + username + "\","
                + "\"title\": \"" + title + "\","
                + "\"description\": \"" + description + "\","
                + "\"location\": \"" + location + "\","
                + "\"startdate\": \"" + startdate + "/" + startmonth + "/" + startyear+ "\","
                + "\"enddate\": \"" + enddate + "/" + endmonth + "/" + endyear + "\","
                + "\"starttime\": \"" + starthour + ":" + startminute + "\","
                + "\"endtime\": \"" + endhour + ":" + endminute + "\","
                + "\"allday\": \"" + allday.toString() + "\""
                + "}";

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
                    if (jsonData.equals("[]"))
                    {
                        Log.d(TAG, "New Event");
                        duplicate = false;
                        newEvent();
                    }
                    else
                    {
                        Log.d(TAG, "Existing Event");
                        duplicate = true;
                        existingEvent();
                    }
                }
            }
        });
    }

    public void existingEvent() {

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
              handlerneeded = false;
              started=false;
              dialog.dismiss();
              savebutton.setEnabled(true);
              //existingOId = object.getObjectId(); Object ID ka kuch karna hai
              WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
              Display display = manager.getDefaultDisplay();
              Point point = new Point();
              display.getSize(point);
              int width = point.x;
              int height = point.y;
              int smallerDimension = width < height ? width : height;
              smallerDimension = smallerDimension * 3 / 4;
              String start, end;
              if (allday) {
                  start = startdatedisplay.getText().toString();
                  end = enddatedisplay.getText().toString();
              } else {
                  start = startdatedisplay.getText().toString() + " " + starttimedisplay.getText().toString();
                  end = enddatedisplay.getText().toString() + " " + endtimedisplay.getText().toString();
              }
              AlertDialog.Builder builder = new AlertDialog.Builder(EventCreate.this);
              builder
                      .setOnKeyListener(new DialogInterface.OnKeyListener() {
                          @Override
                          public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                              if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                                  dialog.cancel();
                                  savebutton.setEnabled(true);
                                  return true;
                              }
                              return false;
                          }
                      })
                      .setTitle("Event already exists!!")
                      .setMessage("http://www.EVENTual.com/" + existingOId)
                      .setIcon(android.R.drawable.ic_menu_my_calendar)
                      .setPositiveButton("Share Link", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              String shareBody = "EVENTual: \nEvent Title - "+title+"\nEvent Link - http://www.EVENTual.com/"+existingOId;
                              Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                              sharingIntent.setType("text/plain");
                              sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Event");
                              sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                              startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

                          }
                      })
                      .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              Intent i = new Intent();
                              i.putExtra("title", title);
                              i.putExtra("description", description);
                              i.putExtra("allday", allday);
                              i.putExtra("startyear", startyear);
                              i.putExtra("startmonth", startmonth);
                              i.putExtra("startdate", startdate);
                              i.putExtra("endyear", endyear);
                              i.putExtra("endmonth", endmonth);
                              i.putExtra("enddate", enddate);
                              i.putExtra("location", location);
                              if (allday) {
                                  i.putExtra("starthour", "");
                                  i.putExtra("startminute", "");
                                  i.putExtra("endhour", "");
                                  i.putExtra("endminute", "");
                              } else {
                                  i.putExtra("starthour", starthour);
                                  i.putExtra("startminute", startminute);
                                  i.putExtra("endhour", endhour);
                                  i.putExtra("endminute", endminute);
                              }
                              setResult(Activity.RESULT_OK, i);
                              finish();
                          }
                      })
                      .setNeutralButton("Share QR Code", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              try {
                                  Intent share = new Intent(Intent.ACTION_SEND);
                                  share.setType("image/jpeg");
                                  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                  b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                  File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                                  try {
                                      f.createNewFile();
                                      FileOutputStream fo = new FileOutputStream(f);
                                      fo.write(bytes.toByteArray());
                                      fo.close();
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }
                                  share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                                  startActivity(Intent.createChooser(share, "Share Image"));

                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          }
                      })
                      .show();
          }
        });

    }

    public void newEvent(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String jsonData = "{"+ "\"username\": \"" + username + "\","
                        + "\"title\": \"" + title + "\","
                        + "\"description\": \"" + description + "\","
                        + "\"location\": \"" + location + "\","
                        + "\"startdate\": \"" + startdate + "/" + startmonth + "/" + startyear+ "\","
                        + "\"enddate\": \"" + enddate + "/" + endmonth + "/" + endyear + "\","
                        + "\"starttime\": \"" + starthour + ":" + startminute + "\","
                        + "\"endtime\": \"" + endhour + ":" + endminute + "\","
                        + "\"allday\": \"" + allday.toString() + "\""
                        + "}";
                RequestBody body = RequestBody.create(JSON, jsonData);
                Log.d(TAG, "JSON = " + jsonData);

                Request request = new com.squareup.okhttp.Request.Builder()
                        .url(CREATE_URL)
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
                            create="success";
                        }
                    }
                });

                handlerneeded = false;
                started = false;
                dialog.dismiss();
                savebutton.setEnabled(true);
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3 / 4;
                String start, end;
                if (allday) {
                    start = startdatedisplay.getText().toString();
                    end = enddatedisplay.getText().toString();
                } else {
                    start = startdatedisplay.getText().toString() + " " + starttimedisplay.getText().toString();
                    end = enddatedisplay.getText().toString() + " " + endtimedisplay.getText().toString();
                }

                String QRText = QRStart + seq + title + seq + description + seq + Boolean.toString(allday) + seq + start + seq + end + seq + location + seq;
                try {
                    qrCodeEncoder = new QRCodeEncoder(QRText, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), smallerDimension);
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    ImageView myImage = (ImageView) findViewById(R.id.imageView1);
                    myImage.setImageBitmap(bitmap);

                    myImage.setDrawingCacheEnabled(true);
                    // this is the important code :)
                    // Without it the view will have a dimension of 0,0 and the bitmap will be null
                    myImage.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    myImage.layout(0, 0, myImage.getMeasuredWidth(), myImage.getMeasuredHeight());
                    myImage.buildDrawingCache(true);
                    b = Bitmap.createBitmap(myImage.getDrawingCache());
                    myImage.setDrawingCacheEnabled(false); // clear drawing cache
                } catch (Exception err) {
                    err.printStackTrace();
                }
                final String objectId = jsonData;
                AlertDialog.Builder builder = new AlertDialog.Builder(EventCreate.this);
                builder
                        .setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                                    dialog.cancel();
                                    savebutton.setEnabled(true);
                                    return true;
                                }
                                return false;
                            }
                        })
                        .setTitle("Event Saved!")
                        .setMessage("http://www.EVENTual.com/" + objectId)
                        .setIcon(android.R.drawable.ic_menu_my_calendar)
                        .setPositiveButton("Share Link", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String shareBody = "EVENTual: \nEvent Title - " + title + "\nEvent Link - http://www.EVENTual.com/" + objectId;
                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Event");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

                            }
                        })
                        .setNegativeButton("Share QR Code", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent share = new Intent(Intent.ACTION_SEND);
                                    share.setType("image/jpeg");
                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    File f = new File(Environment.getExternalStorageDirectory() + File.separator + title + ".jpg");
                                    try {
                                        f.createNewFile();
                                        FileOutputStream fo = new FileOutputStream(f);
                                        fo.write(bytes.toByteArray());
                                        fo.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + title + ".jpg"));
                                    startActivity(Intent.createChooser(share, "Share Image"));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent();
                                i.putExtra("title", title);
                                i.putExtra("description", description);
                                i.putExtra("allday", allday);
                                i.putExtra("startyear", startyear);
                                i.putExtra("startmonth", startmonth);
                                i.putExtra("startdate", startdate);
                                i.putExtra("endyear", endyear);
                                i.putExtra("endmonth", endmonth);
                                i.putExtra("enddate", enddate);
                                i.putExtra("location", location);
                                if (allday) {
                                    i.putExtra("starthour", "");
                                    i.putExtra("startminute", "");
                                    i.putExtra("endhour", "");
                                    i.putExtra("endminute", "");
                                } else {
                                    i.putExtra("starthour", starthour);
                                    i.putExtra("startminute", startminute);
                                    i.putExtra("endhour", endhour);
                                    i.putExtra("endminute", endminute);
                                }
                                setResult(Activity.RESULT_OK, i);
                                finish();
                            }
                        })
                        .show();
            }
        });



    }

    public boolean isStartAheadOfEnd() {
        Long starttimestamp = 0L, endtimestamp = 1L;
        if (allday) {
            startdate = (startdatedisplay.getText().toString()).substring(0, 2);
            startmonth = (startdatedisplay.getText().toString()).substring(3, 5);
            startyear = (startdatedisplay.getText().toString()).substring(6, 10);
            enddate = (enddatedisplay.getText().toString()).substring(0, 2);
            endmonth = (enddatedisplay.getText().toString()).substring(3, 5);
            endyear = (enddatedisplay.getText().toString()).substring(6, 10);
            starttimestamp = Long.parseLong(startyear + startmonth + startdate);
            endtimestamp = Long.parseLong(endyear + endmonth + enddate);
            starthour = "";
            startminute = "";
            endhour = "";
            endminute = "";
        } else {
            startdate = (startdatedisplay.getText().toString()).substring(0, 2);
            startmonth = (startdatedisplay.getText().toString()).substring(3, 5);
            startyear = (startdatedisplay.getText().toString()).substring(6, 10);
            enddate = (enddatedisplay.getText().toString()).substring(0, 2);
            endmonth = (enddatedisplay.getText().toString()).substring(3, 5);
            endyear = (enddatedisplay.getText().toString()).substring(6, 10);
            starthour = (starttimedisplay.getText().toString()).substring(0, 2);
            startminute = (starttimedisplay.getText().toString()).substring(3, 5);
            endhour = (endtimedisplay.getText().toString()).substring(0, 2);
            endminute = (endtimedisplay.getText().toString()).substring(3, 5);
            starttimestamp = Long.parseLong(startyear + startmonth + startdate + starthour + startminute);
            endtimestamp = Long.parseLong(endyear + endmonth + enddate + endhour + endminute);
        }
        return (starttimestamp>endtimestamp);
    }

    public void getUsername() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        try {
            Account[] accounts = AccountManager.get(EventCreate.this).getAccounts();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(allday!=null) {
                Intent i = new Intent();
                i.putExtra("title", title);
                i.putExtra("description", description);
                i.putExtra("allday", allday);
                i.putExtra("startyear", startyear);
                i.putExtra("startmonth", startmonth);
                i.putExtra("startdate", startdate);
                i.putExtra("endyear", endyear);
                i.putExtra("endmonth", endmonth);
                i.putExtra("enddate", enddate);
                i.putExtra("location", location);
                if (allday) {
                    i.putExtra("starthour", "");
                    i.putExtra("startminute", "");
                    i.putExtra("endhour", "");
                    i.putExtra("endminute", "");
                } else {
                    i.putExtra("starthour", starthour);
                    i.putExtra("startminute", startminute);
                    i.putExtra("endhour", endhour);
                    i.putExtra("endminute", endminute);
                }
                setResult(Activity.RESULT_OK, i);
                finish();
            }
            else {
                Intent i = new Intent();
                setResult(Activity.RESULT_CANCELED,i);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void save(View v) {

        /* onClick of save button */

        // Change Title to "(No Title)" when title is empty
        title = titlefield.getText().toString();
        if (title.equals("")) {
            title = "(No Title)";
        }
        titlefield.setText(title);

        // Obtaining values
        description = descriptionfield.getText().toString();
        location = locationfield.getText().toString();
        allday = alldayfield.isChecked();
        getUsername();

        Log.d(TAG, "Save clicked");

        if (!isNetworkConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("No Internet Connection")
                    .setMessage("You must have an internet connection to create new events.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent();
                            setResult(Activity.RESULT_CANCELED, i);
                        }
                    })
                    .show();
            return;
        }

        if (isDateEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Invalid Entry")
                    .setMessage("Both start date and end date are mandatory fields!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }

        if (isTimeEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Invalid Entry")
                    .setMessage("Both starting time and ending time are mandatory fields!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }

        if (isStartAheadOfEnd()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Invalid Entry")
                    .setMessage("Ending time should be after starting time!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }

        checkIfEventAlreadyExists();

        handlerneeded=true;
        started=false;
        handler.post(timeout);
        savebutton.setEnabled(false);
        dialog = ProgressDialog.show(EventCreate.this, "Creating", "Please wait...", true);


    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timeout);
    }
}
