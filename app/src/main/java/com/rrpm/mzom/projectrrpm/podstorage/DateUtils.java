package com.rrpm.mzom.projectrrpm.podstorage;

import android.widget.DatePicker;

import com.rrpm.mzom.projectrrpm.debugging.AssertUtils;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;
import com.rrpm.mzom.projectrrpm.podfiltering.DateRange;
import com.rrpm.mzom.projectrrpm.annotations.NonEmpty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class DateUtils {




    @NonNull
    public static Date getDateFromDatePicker(@NonNull final DatePicker datePicker, final boolean startOfDay){

        final int pickedDay = datePicker.getDayOfMonth();
        final int pickedMonth = datePicker.getMonth();
        final int pickedYear = datePicker.getYear();

        final Calendar calendar = Calendar.getInstance();

        if (startOfDay){
            calendar.set(pickedYear,pickedMonth,pickedDay,calendar.getMinimum(Calendar.HOUR_OF_DAY),calendar.getMinimum(Calendar.MINUTE));
        }else{
            calendar.set(pickedYear,pickedMonth,pickedDay,calendar.getMaximum(Calendar.HOUR_OF_DAY),calendar.getMaximum(Calendar.MINUTE));
        }

        return calendar.getTime();
    }


    /**
     *
     * Retrieves a string representation of a given Date.
     * The format is determined by the constant {@link PodUIConstants#POD_DATE_FORMAT}.
     *
     * @param date: The date object to be represented as a formatted string.
     *
     * @return String representation of the given date.
     */

    public static String getDateAsString(@NonNull final Date date) {

        final Locale locale = new Locale("no","NO");

        final SimpleDateFormat formatter = new SimpleDateFormat(PodUIConstants.POD_DATE_FORMAT,locale);

        final String rawDate = formatter.format(date);

        return rawDate.substring(0,1).toUpperCase() + rawDate.substring(1);

    }


    /**
     *
     * Determines the DateRange for a given list. This list does not need to be ordered.
     *
     * The item date with the smallest long value will be assigned {@link DateRange#fromDate}
     * The item date with the largest long value will be assigned {@link DateRange#toDate}
     *
     * @param list: Items with associated Date objects to create the DateRange from.
     * @param itemDateRetriever: Interface to retrieve each item date.
     * @param <E>: Generic list item type
     *
     * @return The list's DateRange, or null if list is empty.
     */

    @NonNull
    static <E> DateRange getDateRangeFromList(@NonNull @NonEmpty final ArrayList<E> list, @NonNull ItemDateRetriever<E> itemDateRetriever){

        AssertUtils._assert(!list.isEmpty(),"List was empty");

        Date fromDate = itemDateRetriever.getItemDate(list.get(0));
        Date toDate = fromDate;

        for (E item : list){

            final Date itemDate = itemDateRetriever.getItemDate(item);

            if(fromDate == null || itemDate.getTime() < fromDate.getTime()){
                fromDate = itemDate;
            }

            if(toDate == null || itemDate.getTime() > toDate.getTime()){
                toDate = itemDate;
            }

        }

        return new DateRange(fromDate,toDate);

    }

    public interface ItemDateRetriever<E> {
        Date getItemDate(@NonNull E item);
    }




}
