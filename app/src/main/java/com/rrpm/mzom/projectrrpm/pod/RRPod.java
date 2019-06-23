package com.rrpm.mzom.projectrrpm.pod;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;

import java.util.Date;

import androidx.annotation.NonNull;


/**
 *
 *  Class to represent a "Radioresepsjonen" podcast episode
 *
 */

public class RRPod implements Parcelable {


    private static final String TAG = "RRP-RRPod";


    @NonNull private final PodId id;

    @NonNull private final PodType podType;

    @NonNull private final String title;

    @NonNull private final String description;

    @NonNull private final Date date;

    @NonNull private final String url;

    private int duration;

    private int progress;

    private boolean isDownloaded;


    @NonNull
    public static RRPodBuilder createBuilder(){

        return new RRPodBuilder();

    }



    RRPod(@NonNull PodId id, @NonNull PodType podType, @NonNull String title, @NonNull Date date, @NonNull String url, @NonNull String description, int duration, boolean isDownloaded, int progress) {

        this.id = id;

        this.podType = podType;

        this.title = title;

        this.url = url;

        this.date = date;

        this.description = description;

        this.duration = duration;

        this.isDownloaded = isDownloaded;

        this.progress = progress;

    }

    @NonNull
    public PodId getId(){
        return this.id;
    }

    @NonNull
    public PodType getPodType() {
        return this.podType;
    }

    @NonNull
    public Date getDate() {
        return this.date;
    }

    @NonNull
    public String getTitle() {
        return this.title;
    }

    @NonNull
    public String getDescription(){
        return this.description;
    }

    @NonNull
    public String getUrl() {
        return this.url;
    }

    public int getDuration(){
        return this.duration;
    }

    public void setDuration(int duration){

        this.duration = duration;

    }

    public int getProgress(){
        return this.progress;
    }

    public void setProgress(int progress){

        this.progress = progress;

    }

    public void setDownloadedState(boolean downloaded) {
        this.isDownloaded = downloaded;
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public boolean isCompleted(){

        return this.duration - this.progress <= PodStorageConstants.COMPLETED_LIMIT;

    }

    public boolean isStarted(){

        return this.progress >= PodUIConstants.SHOW_PROGRESS_LIMIT;

    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {

        Assertions._assert(parcel != null , "Destination parcel was null");

        parcel.writeParcelable(this.id,flags);

        parcel.writeString(this.podType.name());

        parcel.writeString(this.title);

        parcel.writeString(this.url);

        parcel.writeLong(this.date.getTime());

        parcel.writeString(this.description);

        parcel.writeInt(this.duration);

        parcel.writeByte((byte)(this.isDownloaded ? 1 : 0));

        parcel.writeInt(this.progress);

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public RRPod createFromParcel(Parcel source) {

            Assertions._assert(source != null , "Source parcel was null");

            final RRPodBuilder builder = RRPodBuilder.fromParcel(source);

            return builder.build();

        }

        public RRPod[] newArray(int size) {
            return new RRPod[size];
        }

    };


}