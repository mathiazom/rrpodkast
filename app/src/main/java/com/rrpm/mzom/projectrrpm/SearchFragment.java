package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SearchFragment extends android.support.v4.app.Fragment {

    private View view;

    private SearchFragmentListener searchFragmentListener;

    private int currDay = 0;
    private int currMonth = 0;
    private int currYear = 2011;

    private boolean notListenedTo;

    Spinner spinnerYear;
    Spinner spinnerMonth;
    Spinner spinnerDay;

    private int numOfPods;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            searchFragmentListener = (SearchFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SearchFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        view = inflater.inflate(R.layout.search_fragment, container, false).findViewById(R.id.search_fragment);
        spinnerYear = (Spinner) view.findViewById(R.id.spinnerYear);
        spinnerMonth = (Spinner) view.findViewById(R.id.spinnerMonth);
        spinnerDay = (Spinner) view.findViewById(R.id.spinnerDay);
        initSearchSpinners();
        initSwitches();
        return view;
    }

    interface SearchFragmentListener {
        void OnBuildWithDate(int day, int month, int year, boolean onlyListenedTo);

        void OnHidePodFilter();
    }

    private void getSelectedTime(Spinner spinnerDay, Spinner spinnerMonth, Spinner spinnerYear) {
        int _currDay = spinnerDay.getSelectedItemPosition();
        int _currMonth = spinnerMonth.getSelectedItemPosition();
        int _currYear = spinnerYear.getSelectedItemPosition() + 2011;

        Switch notListenedToSwitch = (Switch) view.findViewById(R.id.notListenedToSwitch);

        // IF SPINNERS HAVE CHANGED
        if (!((_currDay == currDay) && (_currMonth == currMonth) && (_currYear == currYear) && (notListenedTo == notListenedToSwitch.isChecked()))) {
            currDay = _currDay;
            currMonth = _currMonth;
            currYear = _currYear;
            notListenedTo = notListenedToSwitch.isChecked();
            searchFragmentListener.OnBuildWithDate(currDay, currMonth, currYear,notListenedTo);
        } else viewResultStats(numOfPods);

    }

    private void initSearchSpinners() {
        if (view == null || getContext() == null) return;


        // YEAR DATA
        ArrayList<String> spinnerArrayYear = new ArrayList<>();
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        spinnerArrayYear.add("Alle år");
        for (int y = 2012; y < year + 1; y++) {
            spinnerArrayYear.add(String.valueOf(y));
        }

        // MONTH DATA
        ArrayList<String> spinnerArrayMonth = new ArrayList<>(Arrays.asList(new String[]{
                "Alle måneder", "Januar", "Februar", "Mars", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember"
        }));

        // DAY DATA
        ArrayList<String> spinnerArrayDay = new ArrayList<>();
        spinnerArrayDay.add("Alle dager");
        for (int d = 1; d < 32; d++) {
            spinnerArrayDay.add(String.valueOf(d));
        }

        List<Spinner> spinners = Arrays.asList(spinnerYear, spinnerMonth, spinnerDay);
        List<ArrayList<String>> spinnerArrays = Arrays.asList(spinnerArrayYear, spinnerArrayMonth, spinnerArrayDay);

        // ON ITEM SELECTED LISTENER
        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedTime(spinnerDay, spinnerMonth, spinnerYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        // GENERATE YEAR, MONTH AND DAY SPINNER
        for (int a = 0; a < 3; a++) {
            Spinner spinner = spinners.get(a);
            ArrayAdapter<String> temp_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerArrays.get(a));
            temp_adapter.setDropDownViewResource(R.layout.spinner_item_search);
            spinner.setAdapter(temp_adapter);
            spinner.setPopupBackgroundResource(R.color.app_white);
            spinner.setOnItemSelectedListener(onItemSelectedListener);
        }
    }

    private static final String SETTINGS_PREFS_NAME = "SettingsPreferences";
    private static final String FILTER_NOT_LISTENED_TO_KEY = "FILTER_NOT_LISTENED_TO_OPTION";

    private void initSwitches(){
        Switch notListenedToSwitch = (Switch) view.findViewById(R.id.notListenedToSwitch);

        ConstraintLayout notListenedToConstraint = (ConstraintLayout) view.findViewById(R.id.notListenedToConstraint);

        final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME,0);

        if(!settings_prefs.getBoolean(FILTER_NOT_LISTENED_TO_KEY,false)){
            notListenedToConstraint.setVisibility(View.GONE);
        }else{
            notListenedToConstraint.setVisibility(View.VISIBLE);
            notListenedToSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getSelectedTime(spinnerDay, spinnerMonth, spinnerYear);
                }
            });
        }
    }

    void viewResultStats(int validpods) {
        if (view == null) {
            return;
        }

        this.numOfPods = validpods;

        // VIEW NUMBER OF RESULTS
        TextView resultTextView = (TextView) view.findViewById(R.id.resultText);
        if(validpods == 0){
            resultTextView.setText(getResources().getString(R.string.no_pod));
        }else{
            resultTextView.setText(String.valueOf(validpods) + " podkast" + (validpods > 1 ? "er":""));
        }

        // OUTSIDE FRAGMENT CLICK
        view.findViewById(R.id.pod_filter_result_cont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFragmentListener.OnHidePodFilter();
            }
        });
    }

}
