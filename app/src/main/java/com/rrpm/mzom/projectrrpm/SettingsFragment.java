package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends android.support.v4.app.Fragment {

    View view;

    SettingsFragmentListener settingsFragmentListener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            settingsFragmentListener = (SettingsFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SettingsFragmentListener");
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        view = inflater.inflate(R.layout.settings_fragment,container,false);
        prepareRandomSelectOption();
        prepareFilterNotListenedToOption();

        displayDeviceSpaceUsage();
        return view;
    }

        // SETTINGS

    private static final String SETTINGS_PREFS_NAME = "SettingsPreferences";

        // RANDOM SELECT SETTING
    private static final String RANDOM_SELECT_KEY = "RANDOM_SELECT_OPTION";

    private void prepareRandomSelectOption(){
        final String[] items = getResources().getStringArray(R.array.random_select_from_array);

        final ConstraintLayout random_select_container = (ConstraintLayout) view.findViewById(R.id.random_select_container);

        final boolean[] checkedItems = new boolean[items.length];

        final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0);

        for(int i = 0;i<checkedItems.length;i++){
            checkedItems[i] = settings_prefs.getBoolean(RANDOM_SELECT_KEY + " " + i,false);
        }

        random_select_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context ctx = getContext();
                new AlertDialog.Builder(ctx)
                        .setTitle(getResources().getString(R.string.random_select_setting_title))
                        .setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                settings_prefs.edit().putBoolean(RANDOM_SELECT_KEY + " " + which,isChecked).apply();
                                updateCheckedItems();
                            }
                        })
                        .create()
                        .show();
            }
        });

        updateCheckedItems();
    }

    private void updateCheckedItems(){

        final TextView current = (TextView) view.findViewById(R.id.random_select_current);

        final String[] items = getResources().getStringArray(R.array.random_select_from_array);

        final boolean[] checkedItems = new boolean[items.length];

        final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0);

        for(int i = 0;i<checkedItems.length;i++){
            checkedItems[i] = settings_prefs.getBoolean(RANDOM_SELECT_KEY + " " + i,false);
        }

        int trues = 0;
        current.setText("");
        for(int i = 0;i<checkedItems.length;i++){
            if(checkedItems[i]){
                trues++;
                if(current.getText().length() == 0){
                    current.setText(items[i]);
                }else{
                    current.setText(current.getText() + ", " + items[i]);
                }
            }
        }
        if(trues == 0){
            checkedItems[0] = true;
            settings_prefs.edit().putBoolean(RANDOM_SELECT_KEY + " " + 0,true).apply();
            current.setText("");
            for(int i = 0;i<checkedItems.length;i++){
                if(checkedItems[i]){
                    trues++;
                    if(current.getText().length() == 0){
                        current.setText(items[i]);
                    }else{
                        current.setText(current.getText() + ", " + items[i]);
                    }
                }
            }
        }else if(trues == checkedItems.length){
            checkedItems[0] = true;
            for(int i = 1;i<checkedItems.length;i++){
                checkedItems[i] = false;
                settings_prefs.edit().putBoolean(RANDOM_SELECT_KEY + " " + i,false).apply();
            }
        }
    }

        // "NOT LISTENED TO"-FILTER SETTING
    private static final String FILTER_NOT_LISTENED_TO_KEY = "FILTER_NOT_LISTENED_TO_OPTION";

    private void prepareFilterNotListenedToOption(){

        final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0);

        Switch swi = (Switch) view.findViewById(R.id.filter_select_filters_switch);

        swi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings_prefs.edit().putBoolean(FILTER_NOT_LISTENED_TO_KEY,isChecked).apply();
            }
        });

        swi.setChecked(settings_prefs.getBoolean(FILTER_NOT_LISTENED_TO_KEY,false));

    }


    private void displayDeviceSpaceUsage(){
        final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0);

        float spaceUsage = settings_prefs.getFloat("INFO_SPACEUSAGE",0);
        int podnum = settings_prefs.getInt("INFO_PODNUM",0);

        TextView spaceUsageTV = (TextView) view.findViewById(R.id.space_usage_text);
        spaceUsageTV.setText(spaceUsage + " MB (" + podnum + " podkaster)");

        //PRINT ALL SHAREDPREFERENCES
        /*Map<String, ?> allPrefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0).getAll();
        Set<String> set = allPrefs.keySet();
        for(String s : set) System.out.println( s + "<" + allPrefs.get(s).getClass().getSimpleName() +"> =  " + allPrefs.get(s).toString());*/
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        settingsFragmentListener.toolbarTextChange("Innstillinger");
        displayDeviceSpaceUsage();
        super.onActivityCreated(savedInstanceState);
    }

    interface SettingsFragmentListener{
        void toolbarTextChange(String title);
    }
}
