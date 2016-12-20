package com.example.tonipagliaro.botchain.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tonipagliaro.botchain.R;

import java.util.List;

/**
 * Created by giuse on 17/12/2016.
 */
public class BotListAdapter extends ArrayAdapter<String> {


    private int resource;
    private Context context;
    private List<String> values;

    public BotListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);

        this.context = context;
        this.resource = resource;
        this.values = objects;
    }

    public void refreshAdapter(List<String> objects) {
        values.clear();
        values.addAll(objects);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        String[] strings = values.get(position).split("-");
        String address = strings[0];
        String ping = strings[1];

        TextView textView = (TextView) rowView.findViewById(R.id.list_bot_textview);
        textView.setText(address);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.list_bot_imageView);
        if (ping.equalsIgnoreCase("no"))
            imgView.setImageResource(R.drawable.red);
        else
            imgView.setImageResource(R.drawable.green);

        return rowView;
    }
}
