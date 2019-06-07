package com.rrpm.mzom.projectrrpm.podfiltering;


import com.rrpm.mzom.projectrrpm.debugging.Printable;
import com.rrpm.mzom.projectrrpm.pod.RRPod;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class PodFilter implements Printable {


    private static final String TAG = "RRP-PodFilter";


    @NonNull private DateRange dateRange;

    @NonNull private FilterTriState completedState = FilterTriState.ANY;
    @NonNull private FilterTriState downloadedState = FilterTriState.ANY;
    @NonNull private FilterTriState startedState = FilterTriState.ANY;



    public PodFilter(@NonNull DateRange dateRange){

        this.dateRange = dateRange;

    }


    @NonNull
    public DateRange getDateRange() {
        return this.dateRange;
    }


    public void setCompletedState(@NonNull final FilterTriState completedState) {

        this.completedState = completedState;

    }

    @NonNull
    public FilterTriState getCompletedState() {
        return completedState;
    }


    public void setDownloadedState(@NonNull final FilterTriState downloadedState) {

        this.downloadedState = downloadedState;

    }

    @NonNull
    public FilterTriState getDownloadedState() {
        return downloadedState;
    }


    public void setStartedState(@NonNull final FilterTriState startedState){

        this.startedState = startedState;

    }

    @NonNull
    public FilterTriState getStartedState(){

        return this.startedState;

    }


    public void filter(@NonNull final ArrayList<RRPod> allPodsList, @NonNull final ArrayList<RRPod> filteredPodList){

        filteredPodList.clear();

        for (RRPod pod : allPodsList){

            if (
                    (dateRange.contains(pod.getDate())) &&
                    this.downloadedState.contains(pod.isDownloaded()) &&
                    this.completedState.contains(pod.isCompleted()) &&
                    this.startedState.contains(pod.isStarted())) {

                filteredPodList.add(pod);

            }

        }

    }


    @NonNull
    @Override
    public String toPrint() {
        return super.toString() +
                ": {" + "\n" +
                " DateRange: " + dateRange.toPrint() + "\n" +
                " CompletedState: " + completedState + "\n" +
                " DownloadedState: " + downloadedState + "\n" +
                " StartedState: " + startedState + "\n" +
                " }";
    }
}
