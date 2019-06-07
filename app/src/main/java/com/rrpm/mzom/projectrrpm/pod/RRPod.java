package com.rrpm.mzom.projectrrpm.pod;

import android.os.Parcel;
import android.os.Parcelable;

import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;
import com.rrpm.mzom.projectrrpm.rss.RRReader;

import java.util.Date;

import androidx.annotation.NonNull;


/**
 *
 *  Class to represent a "Radioresepsjonen" podcast episode
 *
 */

public class RRPod implements Parcelable {


    private final PodId id;

    private final RRReader.PodType podType;

    private final String title;

    private final String description;

    private final Date date;

    private final String url;

    private final int duration;

    private int progress;

    private boolean isDownloaded;


    public static class Builder{

        private PodId id;

        private RRReader.PodType podType;

        private String title;

        private String description;

        private Date date;

        private String url;

        private int duration;

        private int progress;

        private boolean isDownloaded;

        @NonNull
        public RRPod build(){

            return new RRPod(id,podType,title,date,url,description,duration,false,0);

        }

        public RRPod.Builder setId(PodId id) {
            this.id = id;
            return this;
        }

        public RRPod.Builder setPodType(RRReader.PodType podType) {
            this.podType = podType;
            return this;
        }

        public RRPod.Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public RRPod.Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public RRPod.Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public RRPod.Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public RRPod.Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public RRPod.Builder setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public RRPod.Builder setIsDownloaded(boolean isDownloaded) {
            this.isDownloaded = isDownloaded;
            return this;
        }

        public Date getDate() {
            return date;
        }
    }




    private RRPod(PodId id, RRReader.PodType podType, String title, Date date, String url, String description, int duration, boolean isDownloaded, int progress) {
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


    public PodId getId(){
        return this.id;
    }

    public RRReader.PodType getPodType() {
        return this.podType;
    }

    public Date getDate() {
        return this.date;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }

    public String getUrl() {
        return this.url;
    }

    public int getDuration(){
        return this.duration;
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


    // Parcel constructor
    private RRPod(Parcel in) {
        in.readInt();
        this.id = in.readParcelable(PodId.class.getClassLoader());
        this.podType = RRReader.PodType.valueOf(in.readString());
        this.title = in.readString();
        this.url = in.readString();
        this.date = new Date(in.readLong());
        this.description = in.readString();
        this.duration = in.readInt();
        this.isDownloaded = in.readByte() != 0;
        this.progress = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
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

        public RRPod createFromParcel(Parcel parcel) {
            return new RRPod(parcel);
        }

        public RRPod[] newArray(int size) {
            return new RRPod[size];
        }
    };

}