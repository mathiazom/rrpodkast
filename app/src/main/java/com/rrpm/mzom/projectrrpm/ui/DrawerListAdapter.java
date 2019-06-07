package com.rrpm.mzom.projectrrpm.ui;

        import android.app.Activity;
        import android.content.Context;
        import androidx.annotation.NonNull;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.rrpm.mzom.projectrrpm.R;

class DrawerListAdapter extends ArrayAdapter<String> {

    @NonNull private final Context context;

    @NonNull private final String[] itemTitles;
    @NonNull private final Integer[] itemImgResIds;

    public DrawerListAdapter(@NonNull Context context, @NonNull String[] itemTitles, @NonNull Integer[] itemImgResIds) {

        super(context, -1, itemTitles);

        this.context = context;

        this.itemTitles = itemTitles;
        this.itemImgResIds = itemImgResIds;

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
        itemTextView.setText(itemTitles[position]);

        final ImageView itemImageView = convertView.findViewById(R.id.drawer_item_img);
        itemImageView.setImageResource(itemImgResIds[position]);

        return convertView;
    }
}
