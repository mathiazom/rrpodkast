package com.rrpm.mzom.projectrrpm;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PodId implements Parcelable {


    private final String id;


    PodId(@NonNull final String id){
        this.id = id;
    }

    private PodId(Parcel in){
        this.id = in.readString();
    }


    boolean equals(@Nullable PodId podId) {

        if(podId == null){
            return false;
        }

        return this.id.equals(podId.id);

    }


    long uniqueLong(){

        CharSequence subStringSequence = id.subSequence(21,id.length());

        StringBuilder longStringBuilder = new StringBuilder();

        for(int i = 0; i < subStringSequence.length();i++){

            char c = subStringSequence.charAt(i);

            if(Character.isDigit(c)){
                longStringBuilder.append(c);
            }

        }

        String longString = longStringBuilder.toString();

        if(longString.length() > String.valueOf(Long.MAX_VALUE).length()){
            longString = longString.substring(0,String.valueOf(Long.MAX_VALUE).length()-1);
        }

        return Long.parseLong(longString);

    }


    @NonNull
    @Override
    public String toString() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
    }

    public static final Creator<PodId> CREATOR = new Creator<PodId>() {
        @Override
        public PodId createFromParcel(Parcel in) {
            return new PodId(in);
        }

        @Override
        public PodId[] newArray(int size) {
            return new PodId[size];
        }
    };
}
