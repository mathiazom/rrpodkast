package com.rrpm.mzom.projectrrpm;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class DrawerListAdapter extends ArrayAdapter<String> {

    private final String[] stringOpts;
    private final int[] imgOpts;
    private final Context context;

    DrawerListAdapter(Context context, String[] stringOpts, int[] imgOpts) {
        super(context, -1, stringOpts);
        this.stringOpts = stringOpts;
        this.imgOpts = imgOpts;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_item, parent, false);
        }

        TextView itemTv = (TextView) convertView.findViewById(R.id.drawer_item_text);
        ImageView itemIv = (ImageView) convertView.findViewById(R.id.drawer_item_img);

        itemIv.setImageResource(imgOpts[position]);
        itemTv.setText(stringOpts[position]);

        return convertView;
    }
}
