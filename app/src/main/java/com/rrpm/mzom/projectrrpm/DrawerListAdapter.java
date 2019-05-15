package com.rrpm.mzom.projectrrpm;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class DrawerListAdapter extends ArrayAdapter<String> {

    @NonNull private final Context context;

    @NonNull private final String[] stringOpts;
    @NonNull private final int[] imgOpts;

    DrawerListAdapter(@NonNull Context context, @NonNull String[] stringOpts, @NonNull int[] imgOpts) {

        super(context, -1, stringOpts);

        this.context = context;

        this.stringOpts = stringOpts;
        this.imgOpts = imgOpts;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {

            final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if(mInflater == null){
                throw new NullPointerException("Inflater was null");
            }

            convertView = mInflater.inflate(R.layout.navigation_drawer_item, parent, false);

        }

        final TextView itemTextView = convertView.findViewById(R.id.drawer_item_text);
        itemTextView.setText(stringOpts[position]);

        final ImageView itemImageView = convertView.findViewById(R.id.drawer_item_img);
        itemImageView.setImageResource(imgOpts[position]);

        return convertView;
    }
}
