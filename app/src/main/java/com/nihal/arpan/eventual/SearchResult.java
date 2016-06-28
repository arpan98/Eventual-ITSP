package com.nihal.arpan.eventual;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class SearchResult extends AppCompatActivity {
    EditText title, description, location;
    Switch alldayswitch;
    TextView startdatedisplay, starttimedisplay, enddatedisplay, endtimedisplay, starttimetv, endtimetv;
    String TAG = "SearchResult", oId, QRStart = "EVENTualQR", seq = "~!#", isPrivate;
    Bitmap b;
    QRCodeEncoder qrCodeEncoder;
    ProgressDialog dialog;
    Boolean handlerneeded = false, started = false;
    long stime, now;

    Gson gson = new Gson();
    Type type = new TypeToken<ArrayList<Event>>() {
    }.getType();
    ArrayList<Event> arrayList;
    Event e;
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
                    Toast.makeText(SearchResult.this, "Cannot connect to server. Please check your connection", Toast.LENGTH_LONG).show();
                    thread.start();
                }
                handler.post(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Intent i = getIntent();
        oId = i.getStringExtra("objectId");
        Log.d(TAG, "EventID: " + oId);

        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        location = (EditText) findViewById(R.id.location);
        alldayswitch = (Switch) findViewById(R.id.allday);
        startdatedisplay = (TextView) findViewById(R.id.startdatedisplay);
        starttimedisplay = (TextView) findViewById(R.id.starttimedisplay);
        enddatedisplay = (TextView) findViewById(R.id.enddatedisplay);
        endtimedisplay = (TextView) findViewById(R.id.endtimedisplay);
        starttimetv = (TextView) findViewById(R.id.starttimetv);
        endtimetv = (TextView) findViewById(R.id.endtimetv);

        if (isNetworkConnected()) {

            String jsonData = "{" + "\"id\": \"" + oId + "\""
                    + "}";

            RequestBody body = RequestBody.create(JSON, jsonData);
            Log.d(TAG, "findById JSON = " + jsonData);

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

                        handlerneeded = false;
                        started = false;
                        dialog.dismiss();

                        Log.d(TAG, "Response from " + SEARCH_URL + ": " + jsonData);
                        arrayList = gson.fromJson(jsonData, type);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Iterator itr = arrayList.iterator();
                                while (itr.hasNext()) {
                                    e = (Event) itr.next();
                                    Log.d("Event:", "id: " + e.id + " username: " + e.username);
                                    title.setText(e.title);
                                    description.setText(e.description);
                                    location.setText(e.location);
                                    String allday = e.allday;
                                    startdatedisplay.setText(e.startdate);
                                    enddatedisplay.setText(e.enddate);

                                    if (allday.equals("true")) {
                                        alldayswitch.setChecked(true);
                                        starttimetv.setVisibility(View.INVISIBLE);
                                        starttimedisplay.setVisibility(View.INVISIBLE);
                                        endtimedisplay.setVisibility(View.INVISIBLE);
                                        endtimetv.setVisibility(View.INVISIBLE);
                                    } else {
                                        alldayswitch.setChecked(false);
                                        starttimedisplay.setText(e.starttime);
                                        endtimedisplay.setText(e.endtime);
                                    }
                                    isPrivate = e.isPrivate;
                                    break;
                                }

                                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                                Display display = manager.getDefaultDisplay();
                                Point point = new Point();
                                display.getSize(point);
                                int width = point.x;
                                int height = point.y;
                                int smallerDimension = width < height ? width : height;
                                smallerDimension = smallerDimension * 3 / 4;

                                String QRText = QRStart + seq + title.getText().toString() + seq + description.getText().toString() + seq + Boolean.toString(alldayswitch.isChecked()).toLowerCase() + seq + startdatedisplay.getText().toString() + seq + starttimedisplay.getText().toString() + seq + enddatedisplay.getText().toString() + seq + endtimedisplay.getText().toString() + seq + location.getText().toString() + seq + isPrivate;
                                Log.d(TAG, QRText);
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
                                    Toast.makeText(getApplicationContext(), "Error in QR Code creation", Toast.LENGTH_LONG).show();
                                    err.printStackTrace();
                                }

                            }
                        });
                    }
                }
            });

            handlerneeded = true;
            handler.post(timeout);
            dialog = ProgressDialog.show(SearchResult.this, "Loading", "Please wait...", true);
        } else {
            Toast.makeText(getApplicationContext(), "Internet Connection Required!", Toast.LENGTH_LONG).show();
            thread.start();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void add(View v) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            Calendar beginTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            long startMillis = 0;
            long endMillis = 0;

            if (e.allday.equals("true")) {
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                beginTime.set(Integer.parseInt(e.startdate.split("/")[2]), Integer.parseInt(e.startdate.split("/")[1]) - 1, Integer.parseInt(e.startdate.split("/")[0]));
                endTime.set(Integer.parseInt(e.enddate.split("/")[2]), Integer.parseInt(e.enddate.split("/")[1]) - 1, Integer.parseInt(e.enddate.split("/")[0]));
            } else {
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
                beginTime.set(Integer.parseInt(e.startdate.split("/")[2]), Integer.parseInt(e.startdate.split("/")[1]) - 1, Integer.parseInt(e.startdate.split("/")[0]), Integer.parseInt(e.starttime.split(":")[0]), Integer.parseInt(e.starttime.split(":")[1]));              //year,month,day,hour of day,minute
                endTime.set(Integer.parseInt(e.enddate.split("/")[2]), Integer.parseInt(e.enddate.split("/")[1]) - 1, Integer.parseInt(e.enddate.split("/")[0]), Integer.parseInt(e.endtime.split(":")[0]), Integer.parseInt(e.endtime.split(":")[1]));
            }

            startMillis = beginTime.getTimeInMillis();
            endMillis = endTime.getTimeInMillis();
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
            intent.putExtra(CalendarContract.Events.TITLE, e.title);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, e.description);
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, e.location);

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in opening Calendar", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void ShareQR(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + title.getText().toString() + ".jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + title.getText().toString() + ".jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                Thread.sleep(Toast.LENGTH_LONG); // As I am using LENGTH_LONG in Toast
                SearchResult.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void ShareLink(View v) {
        String shareBody = "EVENTual: \nEvent Title - " + title.getText().toString() + "\nEvent Link - http://www.wncc-iitb.org:5697/event/" + oId;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Event");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timeout);
    }
}
