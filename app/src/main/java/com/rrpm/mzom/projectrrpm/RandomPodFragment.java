package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


// FINDS AND PLAYS A PSEUDORANDOM POD FROM ARRAYLIST OF ANY PODS

public class RandomPodFragment extends android.support.v4.app.Fragment {

    RandomPodFragmentListener randomPodFragmentListener;

    public static RandomPodFragment newInstance() {
        return new RandomPodFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            randomPodFragmentListener = (RandomPodFragmentListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RandomPodFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        randomPodFragmentListener.toolbarTextChange("Tilfeldig podkast");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        View view= inflater.inflate(R.layout.fragment_randompod,container,false);
        final ImageView rpbtn = (ImageView) view.findViewById(R.id.random_pod_play_button);
        rpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomPodFragmentListener.onPlayRandomPod();
            }
        });
        return view;
    }

    interface RandomPodFragmentListener{
        void onPlayRandomPod();
        void toolbarTextChange(String title);
    }
}
