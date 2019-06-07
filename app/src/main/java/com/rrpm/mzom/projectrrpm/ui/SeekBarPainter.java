package com.rrpm.mzom.projectrrpm.ui;

import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import android.widget.SeekBar;

public class SeekBarPainter {

    public static void paint(@NonNull final SeekBar seekBar, final int color) {

        // Colorize progressBar
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // Colorize thumb
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

    }

}
