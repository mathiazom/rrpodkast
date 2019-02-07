package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends android.support.v4.app.Fragment {

    AboutFragmentListener aboutFragmentListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.about_splash,container,false);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            aboutFragmentListener = (AboutFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AboutFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aboutFragmentListener.toolbarTextChange("Om appen");
    }

    interface AboutFragmentListener{
        void toolbarTextChange(String title);
    }
}
