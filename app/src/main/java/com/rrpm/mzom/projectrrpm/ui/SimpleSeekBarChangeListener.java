package com.rrpm.mzom.projectrrpm.ui;

import android.widget.SeekBar;


/**
 *
 * Wrapper class to simplify implementation of {@link SeekBar.OnSeekBarChangeListener}
 *
 */


public abstract class SimpleSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {


    protected abstract void onProgressChanged(int progress, boolean fromUser);


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
