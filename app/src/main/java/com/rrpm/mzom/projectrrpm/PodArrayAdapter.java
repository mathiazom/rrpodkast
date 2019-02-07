package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

class PodArrayAdapter extends ArrayAdapter<RRPod> {

    private final Context context;
    private final RRPod[] pods;
    private final String[] måneder = getContext().getResources().getStringArray(R.array.months);


    // DEFAULT CONSTRUCTOR

    PodArrayAdapter(Context context, RRPod[] pods) {
        super(context, -1, pods);
        this.context = context;
        this.pods = pods;
    }

    // ADAPTER MAIN METHOD

    @NonNull @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        RRPod pod = pods[position];

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.podkast_template, parent, false);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.podText);
        tv.setText(pod.getTitle());

        LinearLayout mdc = (LinearLayout) convertView.findViewById(R.id.month_divider_cont);
        if (pod.getMonthEnd()) {
            TextView mdtv = (TextView) mdc.findViewById(R.id.month_divider_text);
            mdtv.setText(måneder[pod.getMonth()] + " " + pod.getYear());
            mdc.setVisibility(View.VISIBLE);
        } else {
            mdc.setVisibility(View.GONE);
        }

        ConstraintLayout content = (ConstraintLayout) convertView.findViewById(R.id.podcastViewContent);

        if(pod.getSelectionState()){
            if(pod.getDownloadState()){
                content.setBackgroundResource(R.drawable.selected_downloaded);
            }else{
                content.setBackgroundResource(R.drawable.selected);
            }



        }else if(pod.getDownloadState()){
            if(pod.getListenedToState()){
                content.setBackgroundResource(R.drawable.downloaded_listened);
            }else{
                content.setBackgroundResource(R.drawable.downloaded_streamable);
            }

        }else if (pod.getListenedToState()) {
            content.setBackgroundResource(R.color.listened);
        } else {
            content.setBackgroundResource(R.color.streamable);
        }

        long timeSince = Calendar.getInstance().getTimeInMillis()-pod.getDateObj().getTime();

        // MARK AS NEW IF NOT OLDER THAN 2 DAYS AND NOT LISTENED TO NOR DOWNLOADED
        if(timeSince <= 172800000 && !pod.getListenedToState() && !pod.getDownloadState()){
            content.findViewById(R.id.new_pod_mark).setVisibility(View.VISIBLE);
        }else{
            content.findViewById(R.id.new_pod_mark).setVisibility(View.GONE);
        }

        return convertView;
    }
}
