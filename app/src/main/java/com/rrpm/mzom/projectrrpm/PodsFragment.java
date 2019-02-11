package com.rrpm.mzom.projectrrpm;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

    private PodPlayer podPlayer;


    interface PodsFragmentListener{

        void playPod(RRPod pod);

    }


    public static PodsFragment newInstance(ArrayList<RRPod> pods, PodPlayer podPlayer) {

        final PodsFragment fragment = new PodsFragment();

        fragment.pods = pods;
        fragment.podPlayer = podPlayer;

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

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);

        podsRecycler.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        podsRecycler.setLayoutManager(layoutManager);

        podsAdapter = new PodsRecyclerAdapter(pods, new PodsRecyclerAdapter.PodsRecyclerAdapterListener() {
            @Override
            public void onPodClicked(int position) {
                podsFragmentListener.playPod(pods.get(position));
            }
        });

        podsRecycler.setAdapter(podsAdapter);

        podsRecycler.startLayoutAnimation();

    }

    public void ConfirmChangePod(final RRPod pod) {
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

    }
}
