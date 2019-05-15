package com.rrpm.mzom.projectrrpm;


import android.util.Log;

import androidx.annotation.NonNull;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class TaskIterator {


    //private Timer timer;

    private TimerTask timerTask;

    private long period;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> iteration;


    TaskIterator(@NonNull TimerTask timerTask, long period){

        this.timerTask = timerTask;

        this.period = period;

    }


    /**
     *
     * Starts iteration of progress storing tasks according to {@link PodStorageConstants#SAVE_PROGRESS_FREQ_MS}
     *
     */

    void start(){

        /*timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerTask.run();
            }
        },0,period);*/

        iteration = executorService.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerTask.run();
            }
        },0,period, TimeUnit.MILLISECONDS);

    }


    /**
     *
     * Terminates iteration of {@link TaskIterator#timerTask}
     *
     */

    void stop(){

        /*timer.cancel();
        timer.purge();

        timer = null;*/

        if(iteration == null || iteration.isCancelled()){
            // Iteration already terminated
            return;
        }

        iteration.cancel(false);
        iteration = null;

    }


}
