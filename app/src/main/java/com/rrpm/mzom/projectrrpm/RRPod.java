package com.rrpm.mzom.projectrrpm;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Calendar;
import java.util.Date;

class RRPod implements Parcelable {

    private final String title;
    private final String desc;
    private final Date dateObj;
    private final String url;

    private boolean isSelected;

    private boolean monthEnd;
    private boolean downloaded;
    private boolean listenedTo;

    // DEFAULT CONSTRUCTOR
    RRPod(String title,Date dateObj, String url, String desc) {
        this.title = title;
        this.url = url;
        this.dateObj = dateObj;
        this.desc = desc;
    }

    // PARCEL CONSTRUCTOR
    private RRPod(Parcel in) {
        in.readInt();
        this.title = in.readString();
        this.url = in.readString();
        this.dateObj = new Date(in.readLong());
        this.desc = in.readString();
    }

    // GETTERS & SETTERS
    Date getDateObj() {
        return this.dateObj;
    }

    String getTitle() {
        return this.title;
    }

    String getUrl() {
        return this.url;
    }

    int getMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateObj);
        return cal.get(Calendar.MONTH);
    }

    int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateObj);
        return cal.get(Calendar.YEAR);
    }

    void toggleSelectionState(){
        this.isSelected = !this.isSelected;
    }

    void unSelect(){
        this.isSelected = false;
    }

    boolean getSelectionState(){
        return isSelected;
    }

    boolean getMonthEnd() {
        return this.monthEnd;
    }

    void setIsMonthEnd(boolean isMonthEnd) {
        this.monthEnd = isMonthEnd;
    }

    void setDownloadedState(boolean downloaded) {
        this.downloaded = downloaded;
    }

    boolean getDownloadState() {
        return this.downloaded;
    }

    void setListenedToState(boolean listenedTo) {
        this.listenedTo = listenedTo;
    }

    boolean getListenedToState() {
        return this.listenedTo;
    }

    // DESCRIBING CLASS FOR PARCEL
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeLong(this.dateObj.getTime());
        dest.writeString(this.desc);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public RRPod createFromParcel(Parcel in) {
            return new RRPod(in);
        }

        public RRPod[] newArray(int size) {
            return new RRPod[size];
        }
    };
}
