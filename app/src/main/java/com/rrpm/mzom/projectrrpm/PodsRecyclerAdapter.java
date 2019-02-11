package com.rrpm.mzom.projectrrpm;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class PodsRecyclerAdapter extends RecyclerView.Adapter<PodsRecyclerAdapter.PodViewHolder>{

    private static final String TAG = "RRP-PodsRecyclerAdapter";

    @NonNull
    private ArrayList<RRPod> pods;

    private PodsRecyclerAdapterListener listener;

    interface PodsRecyclerAdapterListener{

        void onPodClicked(int position);

    }


    PodsRecyclerAdapter(@NonNull ArrayList<RRPod> pods, PodsRecyclerAdapterListener listener){

        this.pods = pods;
        this.listener = listener;

        Log.i(TAG,"PodsAdapter Count: " + pods.size());

    }


    @Override
    public PodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final ConstraintLayout podLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_podkast,parent,false);

        return new PodViewHolder(podLayout);

    }

    @Override
    public void onBindViewHolder(final PodViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPodClicked(holder.getAdapterPosition());
            }
        });

        final RRPod pod = pods.get(position);

        final String podTitle = pod.getTitle();
        holder.podItemTitle.setText(podTitle);

        final String podDuration = pod.getDescription();
        holder.podItemDuration.setText(podDuration);

    }

    @Override
    public int getItemCount() {

        return pods.size();
    }


    static class PodViewHolder extends RecyclerView.ViewHolder{

        final ConstraintLayout podItemLayout;
        final TextView podItemTitle;
        final TextView podItemDuration;


        PodViewHolder(ConstraintLayout podItemLayout){

            super(podItemLayout);

            this.podItemLayout = podItemLayout;
            this.podItemTitle = podItemLayout.findViewById(R.id.podItemTitle);
            this.podItemDuration = podItemLayout.findViewById(R.id.podItemDuration);

        }


    }

}
