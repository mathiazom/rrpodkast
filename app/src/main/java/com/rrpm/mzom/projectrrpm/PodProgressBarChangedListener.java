package com.rrpm.mzom.projectrrpm;

import android.widget.SeekBar;


/**
 *
 * Wrapper class to simplify implementation of {@link SeekBar.OnSeekBarChangeListener}
 *
 */


abstract class PodProgressBarChangedListener implements SeekBar.OnSeekBarChangeListener {


    abstract void onProgressChanged(int progress, boolean fromUser);


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        onProgressChanged(progress,fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // No implementation
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // No implementation
    }

}
