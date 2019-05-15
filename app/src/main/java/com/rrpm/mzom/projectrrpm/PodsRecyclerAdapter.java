package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import static com.rrpm.mzom.projectrrpm.PodUIConstants.SHOW_PROGRESS_LIMIT;

public class PodsRecyclerAdapter extends RecyclerView.Adapter<PodsRecyclerAdapter.PodViewHolder>{

    private static final String TAG = "RRP-PodsRecyclerAdapter";

    @NonNull
    private ArrayList<RRPod> pods;

    private PlayerPodViewModel playerPodViewModel;

    @NonNull
    private final PodsRecyclerAdapterListener podsRecyclerAdapterListener;

    interface PodsRecyclerAdapterListener{

        void onPodClicked(@NonNull final RRPod pod);

    }




    PodsRecyclerAdapter(
            @NonNull final ArrayList<RRPod> pods,
            @NonNull PlayerPodViewModel playerPodViewModel,
            @NonNull final PodsRecyclerAdapterListener listener
    ){

        this.pods = pods;
        this.playerPodViewModel = playerPodViewModel;
        this.podsRecyclerAdapterListener = listener;
    }


    @NonNull
    ArrayList<RRPod> getPods(){
        return this.pods;
    }

    void setPods(@NonNull final ArrayList<RRPod> pods){
        this.pods = pods;
    }


    @NonNull
    @Override
    public PodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return PodViewHolder.newInstance(parent,podsRecyclerAdapterListener);

    }

    @Override
    public void onBindViewHolder(@NonNull final PodViewHolder holder, final int position) {

        final RRPod pod = pods.get(position);
        final boolean isPlaying = playerPodViewModel.isPlaying() && playerPodViewModel.getPlayerPod().getValue() == pod;
        holder.setPod(pod,isPlaying);

    }

    @Override
    public int getItemCount() {
        return pods.size();
    }

    @Override
    public long getItemId(int position) {

        return pods.get(position).getId().uniqueLong();

    }

    static class PodViewHolder extends RecyclerView.ViewHolder{

        final ConstraintLayout layout;
        final ConstraintLayout content;
        final TextView title;
        final TextView description;
        final ImageView downloadedMark;
        final TextView newMark;
        final ConstraintLayout progressContainer;
        final LinearLayout progressBar;
        final Guideline progressGuideline;
        final LinearLayout playingMarker;

        final PodsRecyclerAdapterListener listener;

        @NonNull
        static PodViewHolder newInstance(@NonNull final ViewGroup parent,@NonNull final PodsRecyclerAdapterListener listener) {

            final ConstraintLayout podLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.module_pod,parent,false);

            return new PodViewHolder(podLayout, listener);
        }


        private PodViewHolder(ConstraintLayout layout, PodsRecyclerAdapterListener listener){

            super(layout);

            this.listener = listener;

            this.layout = layout;
            this.content = layout.findViewById(R.id.podViewContent);
            this.title = layout.findViewById(R.id.podItemTitle);
            this.description = layout.findViewById(R.id.podItemDescription);
            this.downloadedMark = layout.findViewById(R.id.podItemDownloadedMark);
            this.newMark = layout.findViewById(R.id.podItemNewMark);
            this.progressContainer = layout.findViewById(R.id.podItemProgressContainer);
            this.progressBar = layout.findViewById(R.id.podItemProgress);
            this.progressGuideline = layout.findViewById(R.id.podItemProgressGuideline);
            this.playingMarker = layout.findViewById(R.id.podItemPlayingMarker);

        }

        private void setPod(@NonNull final RRPod pod, boolean isPlaying){

            itemView.setOnClickListener(v -> listener.onPodClicked(pod));

            title.setText(pod.getTitle());

            final String podDescription = pod.getDescription();
            description.setText(podDescription);

            downloadedMark.setVisibility(pod.isDownloaded() ? View.VISIBLE : View.GONE);

            long podTime = pod.getDate().getTime();
            long currentTime = Calendar.getInstance().getTime().getTime();
            long recency = currentTime - podTime;
            long msPerDay = 1000*60*60*24;

            newMark.setVisibility(recency < 2*msPerDay ? View.VISIBLE : View.GONE);

            final Context context = itemView.getContext();

            playingMarker.setVisibility(isPlaying ? View.VISIBLE : View.GONE);

            int progress = pod.getProgress();
            float percent = ((float) progress)/pod.getDuration();

            // Hide progressBar if progressBar is below limit
            progressContainer.setVisibility(progress < SHOW_PROGRESS_LIMIT ? View.GONE : View.VISIBLE);

            final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) progressGuideline.getLayoutParams();
            params.guidePercent = percent;
            progressGuideline.setLayoutParams(params);

            progressBar.getBackground().setColorFilter(context.getResources().getColor(
                    pod.isListenedTo() ? R.color.colorGrey : R.color.colorAccent
            ), PorterDuff.Mode.SRC_ATOP);

        }



    }

}
