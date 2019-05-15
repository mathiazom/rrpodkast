package com.rrpm.mzom.projectrrpm;

import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import android.widget.SeekBar;

class SeekBarPainter {

    static void paint(@NonNull final SeekBar seekBar, final int color) {

        // Colorize progressBar
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // Colorize thumb
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

    }

}
