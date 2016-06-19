package com.nihal.arpan.eventual;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arpan on 18/1/16.
 */
public class MyArrayAdapter extends BaseAdapter {
    String TAG = "swag";
    private final Context context;
    List<ListObject> objects;

    public MyArrayAdapter(Context context, List<String> titles, List<String> locations ,List<String> objectIds) {
        this.context = context;
        objects = new ArrayList<ListObject>();
        for(int i=0 ; i<titles.size() ; i++) {
            objects.add(new ListObject(titles.get(i),locations.get(i),objectIds.get(i)));
        }
    }

    @Override
    public int getCount() {
        return objects.size();    // total number of elements in the list
    }

    @Override
    public ListObject getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;                   // index number
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listview = inflater.inflate(R.layout.listitem, parent, false);
        TextView titletv = (TextView) listview.findViewById(R.id.titletv);
        TextView locationtv = (TextView) listview.findViewById(R.id.locationtv);
        titletv.setText(objects.get(position).title);
        locationtv.setText(objects.get(position).location);


        return listview;
    }
}
