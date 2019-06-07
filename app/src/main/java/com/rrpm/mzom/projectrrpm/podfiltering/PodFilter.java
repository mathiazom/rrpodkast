package com.rrpm.mzom.projectrrpm.podfiltering;


import com.rrpm.mzom.projectrrpm.debugging.Printable;
import com.rrpm.mzom.projectrrpm.pod.RRPod;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PodFilter implements Printable {


    private static final String TAG = "RRP-PodFilter";


    @Nullable private DateRange dateRange;

    @NonNull private FilterTriState completedState = FilterTriState.ANY;
    @NonNull private FilterTriState downloadedState = FilterTriState.ANY;
    @NonNull private FilterTriState startedState = FilterTriState.ANY;



    private PodFilter(){

    }


    @Nullable
    public DateRange getDateRange() {
        return this.dateRange;
    }

    @NonNull
    public PodFilter setDateRange(@Nullable DateRange dateRange) {
        this.dateRange = dateRange;
        return this;
    }


    @NonNull
    public PodFilter setCompletedState(@NonNull final FilterTriState completedState) {

        this.completedState = completedState;

        return this;
    }

    @NonNull
    public FilterTriState getCompletedState() {
        return completedState;
    }

    @NonNull
    public PodFilter setDownloadedState(@NonNull final FilterTriState downloadedState) {

        this.downloadedState = downloadedState;

        return this;
    }

    @NonNull
    public FilterTriState getDownloadedState() {
        return downloadedState;
    }


    @NonNull
    public PodFilter setStartedState(@NonNull final FilterTriState startedState){

        this.startedState = startedState;

        return this;

    }

    @NonNull
    public FilterTriState getStartedState(){

        return this.startedState;

    }


    public void filter(@NonNull final ArrayList<RRPod> allPodsList, @NonNull final ArrayList<RRPod> filteredPodList){

        filteredPodList.clear();

        for (RRPod pod : allPodsList){

            if (
                    (dateRange == null || dateRange.contains(pod.getDate())) &&
                    this.downloadedState.contains(pod.isDownloaded()) &&
                    this.completedState.contains(pod.isCompleted()) &&
                    this.startedState.contains(pod.isStarted())) {

                filteredPodList.add(pod);

            }

        }

    }

    @NonNull
    public static PodFilter noFilter(){

        return new PodFilter()
                .setDateRange(null)
                .setCompletedState(FilterTriState.ANY)
                .setDownloadedState(FilterTriState.ANY)
                .setStartedState(FilterTriState.ANY);

    }


    @NonNull
    @Override
    public String toPrint() {
        return super.toString() +
                ": {" + "\n" +
                " DateRange: " + (dateRange == null ? "null" : dateRange.toPrint()) + "\n" +
                " CompletedState: " + completedState + "\n" +
                " DownloadedState: " + downloadedState + "\n" +
                " StartedState: " + startedState + "\n" +
                " }";
    }
}
