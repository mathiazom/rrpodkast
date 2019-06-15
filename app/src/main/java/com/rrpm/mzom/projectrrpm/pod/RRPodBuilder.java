package com.rrpm.mzom.projectrrpm.pod;

import android.os.Parcel;

import com.rrpm.mzom.projectrrpm.debugging.AssertUtils;

import java.util.Date;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RRPodBuilder {


    private PodId id = null;

    private PodType podType = null;

    private String title = null;

    private String description = "";

    private Date date = null;

    private String url = null;

    private int duration = -1;

    private boolean isDownloaded;

    private int progress = 0;


    @NonNull
    public RRPod build(){


        // TODO: Use alternate to assertions

        AssertUtils._assert(id != null,"Pod id was null");

        AssertUtils._assert(podType != null , "Pod type was null");

        AssertUtils._assert(title != null , "Pod title was null");

        AssertUtils._assert(date != null , "Pod date was null");

        AssertUtils._assert(url != null , "Pod url was null");

        AssertUtils._assert(description != null , "Pod description was null");

        AssertUtils._assert(duration >= 0 , "Pod duration was negative");


        return new RRPod(
                id,
                podType,
                title,
                date,
                url,
                description,
                duration,
                isDownloaded,
                progress
        );

    }

    @NonNull
    static RRPodBuilder fromParcel(@NonNull Parcel parcel){

        parcel.readInt();

        final RRPodBuilder builder = new RRPodBuilder();

        final PodId id = parcel.readParcelable(PodId.class.getClassLoader());
        builder.setId(id);

        final PodType podType = PodType.valueOf(parcel.readString());
        builder.setPodType(podType);

        final String title = parcel.readString();
        builder.setTitle(title);

        final String url = parcel.readString();
        builder.setUrl(url);

        final Date date = new Date(parcel.readLong());
        builder.setDate(date);

        final String description = parcel.readString();
        builder.setDescription(description);

        final int duration = parcel.readInt();
        builder.setDuration(duration);

        final boolean isDownloaded = parcel.readByte() != 0;
        builder.setDownloaded(isDownloaded);

        final int progress = parcel.readInt();
        builder.setProgress(progress);

        return builder;


    }

    public void setId(@NonNull PodId id) {

        AssertUtils._assert(id != null , "Pod id was null");

        this.id = id;

    }

    public void setPodType(@NonNull PodType podType) {

        AssertUtils._assert(podType != null , "Pod type was null");

        this.podType = podType;

    }

    public void setTitle(@NonNull String title) {

        AssertUtils._assert(title != null , "Pod title was null");

        this.title = title;

    }

    public void setDescription(@NonNull String description) {

        AssertUtils._assert(description != null , "Pod description was null");

        this.description = description;

    }

    public void setDate(@NonNull Date date) {

        AssertUtils._assert(date != null , "Pod date was null");

        this.date = date;

    }

    public void setUrl(@NonNull String url) {

        AssertUtils._assert(url != null , "Pod url was null");

        this.url = url;

    }

    public void setDuration(int duration) {

        AssertUtils._assert(duration >= 0 , "Pod duration was negative");

        this.duration = duration;

    }

    public void setDownloaded(boolean downloaded) {

        isDownloaded = downloaded;

    }

    public void setProgress(int progress) {

        AssertUtils._assert(duration >= 0 , "Pod progress was negative");

        this.progress = progress;

    }


    @Nullable
    public Date getDate() {

        return date;
    }

}

