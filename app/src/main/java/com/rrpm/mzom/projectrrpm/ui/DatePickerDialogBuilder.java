package com.rrpm.mzom.projectrrpm.ui;

import android.app.DatePickerDialog;
import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DatePickerDialogBuilder {



    public static DatePickerDialog build(@NonNull Context context, @Nullable DatePickerDialog.OnDateSetListener listener, Date date){

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(context,listener,year,month,dayOfMonth);

    }

}
