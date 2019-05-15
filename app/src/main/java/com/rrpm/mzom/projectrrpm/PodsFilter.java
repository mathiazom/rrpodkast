package com.rrpm.mzom.projectrrpm;


import androidx.annotation.NonNull;

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

    int getDay() {
        return day;
    }

    PodsFilter setMonth(final int month) {

        this.month = month;

        return this;
    }

    int getMonth() {
        return month;
    }

    PodsFilter setYear(final int year) {

        this.year = year;

        return this;
    }

    int getYear() {
        return year;
    }

    PodsFilter setListenedToState(@NonNull final FilterTriState listenedToState) {

        this.listenedToState = listenedToState;

        return this;
    }

    FilterTriState getListenedToState() {
        return listenedToState;
    }

    private PodsFilter setDownloadedState(@NonNull final FilterTriState downloadedState) {

        this.downloadedState = downloadedState;

        return this;
    }

    public FilterTriState getDownloadedState() {
        return downloadedState;
    }

    private boolean fitsTriState(boolean state, FilterTriState triState){

        return triState == FilterTriState.ANY ||
               triState == FilterTriState.TRUE && state ||
               triState == FilterTriState.FALSE && !state;

    }


    ArrayList<RRPod> filter(@NonNull final ArrayList<RRPod> pods){

        final ArrayList<RRPod> filteredPods = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance();

        for (RRPod pod : pods){

            calendar.setTime(pod.getDate());

            boolean inRangeDay = calendar.get(Calendar.DAY_OF_MONTH) == day;
            boolean inRangeMonth = (calendar.get(Calendar.MONTH) + 1) == month;
            boolean inRangeYear = calendar.get(Calendar.YEAR) == year;

            boolean inTimeRange =
                    ((day == -1) || (inRangeDay)) &&
                    ((month == -1) || (inRangeMonth)) &&
                    ((year == -1) || (inRangeYear));

            if (!inTimeRange ||
                !fitsTriState(pod.isDownloaded(), downloadedState) ||
                !fitsTriState(pod.isListenedTo(), listenedToState)) {

                continue;

            }

            filteredPods.add(pod);

        }

        return filteredPods;

    }

    static PodsFilter noFilter(){

        return new PodsFilter()
                .setDay(-1)
                .setMonth(-1)
                .setYear(-1)
                .setListenedToState(FilterTriState.ANY)
                .setDownloadedState(FilterTriState.ANY);

    }


    String getPrintable(){

        return toString() + " - Params: \n"
                + "day: " + String.valueOf(day) + "\n"
                + "month: " + String.valueOf(month) + "\n"
                + "year: " + String.valueOf(year) + "\n";

    }



}
