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

import com.google.zxing.BarcodeFormat;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SearchResult extends AppCompatActivity {
    EditText title,description,location;
    Switch allday;
    TextView startdatedisplay,starttimedisplay,enddatedisplay,endtimedisplay,starttimetv,endtimetv;
    String TAG="swag",oId,QRStart="EVENTualQR",seq="~!#";
    Bitmap b;
    QRCodeEncoder qrCodeEncoder;
    ProgressDialog dialog;
    Boolean handlerneeded=false,started=false;
    long stime,now;

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

        title = (EditText)findViewById(R.id.title);
        description = (EditText)findViewById(R.id.description);
        location = (EditText)findViewById(R.id.location);
        allday = (Switch)findViewById(R.id.allday);
        startdatedisplay = (TextView)findViewById(R.id.startdatedisplay);
        starttimedisplay = (TextView)findViewById(R.id.starttimedisplay);
        enddatedisplay = (TextView)findViewById(R.id.enddatedisplay);
        endtimedisplay = (TextView)findViewById(R.id.endtimedisplay);
        starttimetv = (TextView)findViewById(R.id.starttimetv);
        endtimetv = (TextView)findViewById(R.id.endtimetv);

        if(isNetworkConnected()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("EventData");
            query.getInBackground(oId, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    handlerneeded=false;
                    started=false;
                    dialog.dismiss();
                    if (e == null) {
                        try {
                            title.setText(object.getString("title"));
                            description.setText(object.getString("description"));
                            location.setText(object.getString("location"));

                            String startyear = object.getString("startyear");
                            String startmonth = object.getString("startmonth");
                            String startdate = object.getString("startdate");
                            String endyear = object.getString("endyear");
                            String endmonth = object.getString("endmonth");
                            String enddate = object.getString("enddate");

                            String day = (String) ((Integer.parseInt(startdate) < 10) ? "0" + Integer.parseInt(startdate)
                                    : Integer.toString(Integer.parseInt(startdate)));
                            String month = (String) ((Integer.parseInt(startmonth) < 10) ? "0" + Integer.parseInt(startmonth)
                                    : Integer.toString(Integer.parseInt(startmonth)));
                            String prettyStartDate = day + "/" + month + "/" + String.valueOf(Integer.parseInt(startyear));

                            day = (String) ((Integer.parseInt(enddate) < 10) ? "0" + Integer.parseInt(enddate)
                                    : Integer.toString(Integer.parseInt(enddate)));
                            month = (String) ((Integer.parseInt(endmonth) < 10) ? "0" + Integer.parseInt(endmonth)
                                    : Integer.toString(Integer.parseInt(endmonth)));
                            String prettyEndDate = day + "/" + month + "/" + String.valueOf(Integer.parseInt(endyear));

                            startdatedisplay.setText(prettyStartDate);
                            enddatedisplay.setText(prettyEndDate);
                        }
                        catch(Exception err) {
                            err.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error in fetching data", Toast.LENGTH_LONG).show();
                        }

                        if (object.getBoolean("allday")) {
                            allday.setChecked(true);
                            starttimetv.setVisibility(View.INVISIBLE);
                            starttimedisplay.setVisibility(View.INVISIBLE);
                            endtimedisplay.setVisibility(View.INVISIBLE);
                            endtimetv.setVisibility(View.INVISIBLE);
                            allday.setChecked(true);
                        } else {
                            allday.setChecked(false);
                            try {
                                String starthour = object.getString("starthour");
                                String startminute = object.getString("startminute");
                                String endhour = object.getString("endhour");
                                String endminute = object.getString("endminute");

                                String hours = (String) ((Integer.parseInt(starthour) < 10) ? "0" + Integer.parseInt(starthour)
                                        : Integer.toString(Integer.parseInt(starthour)));
                                String minutes = (String) ((Integer.parseInt(startminute) < 10) ? "0" + Integer.parseInt(startminute)
                                        : Integer.toString(Integer.parseInt(startminute)));
                                String prettyStartTime = hours + ":" + minutes;

                                hours = (String) ((Integer.parseInt(endhour) < 10) ? "0" + Integer.parseInt(endhour)
                                        : Integer.toString(Integer.parseInt(endhour)));
                                minutes = (String) ((Integer.parseInt(endminute) < 10) ? "0" + Integer.parseInt(endminute)
                                        : Integer.toString(Integer.parseInt(endminute)));
                                String prettyEndTime = hours + ":" + minutes;

                                starttimedisplay.setText(prettyStartTime);
                                endtimedisplay.setText(prettyEndTime);
                            } catch (Exception err) {
                                err.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error in fetching data", Toast.LENGTH_LONG).show();
                            }
                        }
                        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        Display display = manager.getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int width = point.x;
                        int height = point.y;
                        int smallerDimension = width < height ? width : height;
                        smallerDimension = smallerDimension * 3 / 4;
                        String start, end;
                        if (allday.isChecked()) {
                            start = startdatedisplay.getText().toString();
                            end = enddatedisplay.getText().toString();
                        } else {
                            start = startdatedisplay.getText().toString() + " " + starttimedisplay.getText().toString();
                            end = enddatedisplay.getText().toString() + " " + endtimedisplay.getText().toString();
                        }

                        String QRText = QRStart + seq + title.getText().toString() + seq + description.getText().toString() + seq + Boolean.toString(allday.isChecked()) + seq + start + seq + end + seq + location.getText().toString() + seq;
                        Log.d(TAG,QRText);
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
                        }
                        catch (Exception err) {
                            Toast.makeText(getApplicationContext(), "Error in QR Code creation", Toast.LENGTH_LONG).show();
                            err.printStackTrace();
                        }

                    }
                    else {
                        Log.d(TAG, "Error in getting");
                    }
                }
            });
            handlerneeded=true;
            handler.post(timeout);
            dialog = ProgressDialog.show(SearchResult.this, "Loading", "Please wait...", true);
        }
        else {
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
        Intent i = new Intent(SearchResult.this, MainActivity.class);
        i.putExtra("identifier","SearchResult");
        i.putExtra("objectId", oId);
        finish();
        startActivity(i);
    }

    public void ShareQR(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + title.getText().toString()+".jpg");
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

    Thread thread = new Thread(){
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
        String shareBody = "EVENTual: \nEvent Title - "+title.getText().toString()+"\nEvent Link - http://www.EVENTual.com/"+oId;
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
