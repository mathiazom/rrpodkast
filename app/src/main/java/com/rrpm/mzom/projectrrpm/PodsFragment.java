package com.rrpm.mzom.projectrrpm;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PodsFragment extends android.support.v4.app.Fragment  {

    private View view;

    private ArrayList<RRPod> pods;

    private PodsRecyclerAdapter podsAdapter;

    private PodsFragmentListener podsFragmentListener;


    interface PodsFragmentListener{

        void onPlayPod(RRPod pod);

    }


    public static PodsFragment newInstance(ArrayList<RRPod> pods) {

        final PodsFragment fragment = new PodsFragment();

        fragment.pods = pods;

        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            podsFragmentListener = (PodsFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement PodsFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_podlist_all,container,false);

        loadPodsRecycler();

        return view;
    }

    private void loadPodsRecycler(){

        final RecyclerView podsRecycler = (RecyclerView) view.findViewById(R.id.podsRecycler);

        podsRecycler.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        podsRecycler.setLayoutManager(layoutManager);

        podsAdapter = new PodsRecyclerAdapter(pods, new PodsRecyclerAdapter.PodsRecyclerAdapterListener() {
            @Override
            public void onPodClicked(int position) {
                podsFragmentListener.onPlayPod(pods.get(position));
            }
        });

        podsRecycler.setAdapter(podsAdapter);

        podsRecycler.startLayoutAnimation();

    }
}
