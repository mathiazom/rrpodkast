package com.rrpm.mzom.projectrrpm;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

public class PodsFragment extends android.support.v4.app.Fragment implements PodPlayer.PodPlayerListener {


    private static final String TAG = "RRP-PodsFragment";

    private View view;

    private ArrayList<RRPod> pods;

    private ArrayList<RRPod> filteredPods;

    private PodsRecyclerAdapter podsAdapter;

    private PodsFragmentListener listener;


    interface PodsFragmentListener{

        void playPod(RRPod pod);

        void loadSearchFragment();

    }


    public static PodsFragment newInstance(ArrayList<RRPod> pods) {

        final PodsFragment fragment = new PodsFragment();

        fragment.pods = pods;
        fragment.filteredPods = new ArrayList<>();
        fragment.filteredPods.addAll(pods);

        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            listener = (PodsFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement PodsFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_podlist_all,container,false);

        loadToolbarActions();

        loadPodsRecycler();

        return view;
    }

    private void loadToolbarActions(){

        final ImageView showFilterAction = view.findViewById(R.id.pod_filter_show);
        showFilterAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.loadSearchFragment();
            }
        });


    }

    private void loadPodsRecycler(){

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);

        podsRecycler.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        podsRecycler.setLayoutManager(layoutManager);

        podsAdapter = new PodsRecyclerAdapter(filteredPods, new PodsRecyclerAdapter.PodsRecyclerAdapterListener() {
            @Override
            public void onPodClicked(int position) {
                listener.playPod(filteredPods.get(position));
            }
        });

        podsRecycler.setAdapter(podsAdapter);

        podsRecycler.startLayoutAnimation();

    }


    void loadFilteredPods(@NonNull final PodsFilter filter){

        filteredPods.clear();

        filteredPods.addAll(filter.filter(pods));

        podsAdapter.notifyDataSetChanged();

    }




    /*public void ConfirmChangePod(final RRPod pod) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle("Er du sikker?");
        builder.setMessage("Er du sikker på at du vil stoppe spillende podkast for å starte denne?");
        builder.setPositiveButton(("Ja"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        podPlayer.playPod(pod);
                    }
                });
        builder.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }*/

    @Override
    public void onPodLoaded(@NonNull RRPod pod) {

    }

    @Override
    public void onCurrentPositionChanged(int position) {

    }

    @Override
    public void onPodStarted(@NonNull RRPod pod, int from) {

    }

    @Override
    public void onPlayerPaused() {

    }

    @Override
    public void onPlayerContinued() {

    }
}
