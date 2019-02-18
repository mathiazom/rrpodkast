package com.rrpm.mzom.projectrrpm;


import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

class PodsFilter {

    private static final String TAG = "RRP-PodsFilter";

    private int day = -1;
    private int month = -1;
    private int year = -1;

    private FilterTriState listenedToState = FilterTriState.ANY;

    private FilterTriState downloadedState = FilterTriState.ANY;


    enum FilterTriState {
        TRUE,
        FALSE,
        ANY
    }


    PodsFilter(){

    }


    PodsFilter setDay(final int day){

        this.day = day;

        return this;
    }

    PodsFilter setMonth(final int month) {

        this.month = month;

        return this;
    }

    PodsFilter setYear(final int year) {

        this.year = year;

        return this;
    }

    PodsFilter setListenedToState(@NonNull final FilterTriState listenedToState) {

        this.listenedToState = listenedToState;

        return this;
    }

    PodsFilter setDownloadedState(@NonNull final FilterTriState downloadedState) {

        this.downloadedState = downloadedState;

        return this;
    }


    ArrayList<RRPod> filter(@NonNull final ArrayList<RRPod> pods){

        final ArrayList<RRPod> filteredPods = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance();

        for (RRPod pod : pods){

            calendar.setTime(pod.getDateObj());

            boolean inRangeDay = calendar.get(Calendar.DAY_OF_MONTH) == day;
            boolean inRangeMonth = (calendar.get(Calendar.MONTH) + 1) == month;
            boolean inRangeYear = calendar.get(Calendar.YEAR) == year;

            boolean inTimeRange =
                    ((day == -1) || (inRangeDay))
                    && ((month == -1) || (inRangeMonth))
                    && ((year == -1) || (inRangeYear));

            if(!inTimeRange){
                continue;
            }

            boolean correctDownloadedState = downloadedState == FilterTriState.ANY || pod.isDownloaded();

            if(!correctDownloadedState){
                continue;
            }

            boolean correctListenedToState = listenedToState == FilterTriState.ANY || pod.isListenedTo();

            if(!correctListenedToState){
                continue;
            }

            filteredPods.add(pod);

        }

        return filteredPods;

    }



}
