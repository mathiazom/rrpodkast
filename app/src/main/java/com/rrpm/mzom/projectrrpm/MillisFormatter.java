package com.rrpm.mzom.projectrrpm;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

class MillisFormatter {


    enum MillisFormat{
        HH_MM_SS // "HH:MM:SS"
    }


    /**
     * Convert milliseconds to time with a give MillisFormat
     *
     * @param ms: Total number of milliseconds to be represented
     * @param format: MillisFormat to be used in conversion from int to String
     * @return Converted millisecond as a String
     */

    static String toFormat(int ms, final MillisFormat format){

        switch (format){
            case HH_MM_SS:
                return String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                        TimeUnit.MILLISECONDS.toMinutes(ms) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1));
        }

        // Format not recognized
        throw new InvalidFormatException(format);

    }


    /**
     * Convert format "HH:MM:SS" to milliseconds
     *
     * @param formatted: String of the "HH:MM:SS" format
     * @param format: MillisFormat to be used in conversion from formatted String to millis int
     * @return Total milliseconds
     */

    static int fromFormat(@NonNull final String formatted, MillisFormat format){

        switch (format){
            case HH_MM_SS:
                return (Integer.valueOf(formatted.substring(0,2))*60*60*1000) + // Hours to ms
                        (Integer.valueOf(formatted.substring(3,5))*60*1000) + // Minutes to ms
                        (Integer.valueOf(formatted.substring(6,8))*1000); // Seconds to ms
        }

        throw new InvalidFormatException(format);

    }


    private static class InvalidFormatException extends RuntimeException{

        InvalidFormatException(final MillisFormat format){
            super("Format " + format + " is not valid");
        }

    }


}
