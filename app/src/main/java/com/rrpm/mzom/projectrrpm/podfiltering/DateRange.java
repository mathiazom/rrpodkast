package com.rrpm.mzom.projectrrpm.podfiltering;

import com.rrpm.mzom.projectrrpm.debugging.Printable;

import java.util.Date;

import androidx.annotation.NonNull;

public class DateRange implements Printable {


    @NonNull private Date fromDate;
    @NonNull private Date toDate;


    public DateRange(@NonNull Date fromDate, @NonNull Date toDate){

        this.fromDate = fromDate;

        this.toDate = toDate;

    }

    public boolean contains(@NonNull Date date){

        final long millis = date.getTime();

        return fromDate.getTime() <= millis && millis <= toDate.getTime();

    }


    @NonNull
    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(@NonNull Date fromDate) {
        this.fromDate = fromDate;
    }

    @NonNull
    public Date getToDate() {
        return toDate;
    }

    public void setToDate(@NonNull Date toDate) {
        this.toDate = toDate;
    }


    @NonNull
    @Override
    public String toPrint() {
        return super.toString() +
                ": {" + "\n" +
                " From: " + fromDate.getTime() + "\n" +
                " To: " + toDate.getTime() + "\n" +
                " }";
    }
}
