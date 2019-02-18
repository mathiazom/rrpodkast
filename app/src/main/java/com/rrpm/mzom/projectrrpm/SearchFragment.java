package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

    private Switch notListenedToSwitch;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_search, container, false).findViewById(R.id.search_fragment);

        spinnerYear = view.findViewById(R.id.spinnerYear);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerDay = view.findViewById(R.id.spinnerDay);

        notListenedToSwitch = view.findViewById(R.id.notListenedToSwitch);

        initTimeSearchSpinners();

        initSwitches();

        return view;
    }

    interface SearchFragmentListener {

        void onBuildWithDate(int day, int month, int year, boolean onlyListenedTo);

        void onHidePodFilter();

    }

    private void getSelectedTime(Spinner spinnerDay, Spinner spinnerMonth, Spinner spinnerYear) {

        int selectedDayPosition = spinnerDay.getSelectedItemPosition();
        int selectedDay = selectedDayPosition != 0 ? selectedDayPosition : -1;

        int selectedMonthPosition = spinnerMonth.getSelectedItemPosition();
        int selectedMonth = selectedMonthPosition != 0 ? selectedMonthPosition : -1;

        int selectedYearPosition = spinnerYear.getSelectedItemPosition();
        int selectedYear = selectedYearPosition != 0 ? selectedYearPosition + 2011 : -1;

        notListenedToSwitch = view.findViewById(R.id.notListenedToSwitch);

        // IF SPINNERS HAVE CHANGED
        if (!((selectedDay == this.currDay) && (selectedMonth == currMonth) && (selectedYear == currYear) && (notListenedTo == notListenedToSwitch.isChecked()))) {

            currDay = selectedDay;
            currMonth = selectedMonth;
            currYear = selectedYear;

            notListenedTo = notListenedToSwitch.isChecked();

            searchFragmentListener.onBuildWithDate(this.currDay, currMonth, currYear, notListenedTo);

        } else {

            viewResultStats(numOfPods);
        }

    }

    // Populate and initialize year, month and day spinners
    private void initTimeSearchSpinners() {

        if (view == null || getContext() == null){
            return;
        }

        final List<Spinner> spinners = Arrays.asList(spinnerYear, spinnerMonth, spinnerDay);

        final List<ArrayList<String>> spinnerOptionsArray = Arrays.asList(
                getYearSpinnerOptions(),
                getMonthSpinnerOptions(),
                getDaySpinnerOptions()
        );

        final AdapterView.OnItemSelectedListener optionSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedTime(spinnerDay, spinnerMonth, spinnerYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        for (int a = 0; a < 3; a++) {

            final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerOptionsArray.get(a));
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_search);

            final Spinner spinner = spinners.get(a);
            spinner.setAdapter(spinnerAdapter);
            spinner.setPopupBackgroundResource(R.color.colorWhite);
            spinner.setOnItemSelectedListener(optionSelectedListener);

        }
    }


    private ArrayList<String> getYearSpinnerOptions(){

        // Year spinner options
        final ArrayList<String> yearOptions = new ArrayList<>();

        // Year placeholder option
        yearOptions.add("Alle år");

        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int y = 2012; y < currentYear + 1; y++) {
            yearOptions.add(String.valueOf(y));
        }

        return yearOptions;

    }

    private ArrayList<String> getMonthSpinnerOptions(){

        // Month spinner options
        final ArrayList<String> monthOptions = new ArrayList<>();

        // Month placeholder option
        monthOptions.add("Alle måneder");

        monthOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.months)));

        return monthOptions;

    }

    private ArrayList<String> getDaySpinnerOptions(){

        // Day spinner options
        final ArrayList<String> dayOptions = new ArrayList<>();

        // Day placeholder option
        dayOptions.add("Alle dager");

        for (int d = 1; d < 32; d++) {
            dayOptions.add(String.valueOf(d));
        }

        return dayOptions;

    }


    /*private static final String SETTINGS_PREFS_NAME = "SettingsPreferences";
    private static final String FILTER_NOT_LISTENED_TO_KEY = "FILTER_NOT_LISTENED_TO_OPTION";*/

    private void initSwitches() {

        final Switch notListenedToSwitch = view.findViewById(R.id.notListenedToSwitch);

        final ConstraintLayout notListenedToConstraint = view.findViewById(R.id.notListenedToField);

        notListenedToConstraint.setVisibility(View.VISIBLE);
        notListenedToSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSelectedTime(spinnerDay, spinnerMonth, spinnerYear);
            }
        });


        // TODO: Revise the usefulness of user-controlled switch visibility
        /*final SharedPreferences settings_prefs = getContext().getSharedPreferences(SETTINGS_PREFS_NAME, 0);

        if (!settings_prefs.getBoolean(FILTER_NOT_LISTENED_TO_KEY, false)) {
            notListenedToConstraint.setVisibility(View.GONE);
        } else {
            notListenedToConstraint.setVisibility(View.VISIBLE);
            notListenedToSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getSelectedTime(spinnerDay, spinnerMonth, spinnerYear);
                }
            });
        }*/
    }

    void viewResultStats(int validpods) {
        if (view == null) {
            return;
        }

        this.numOfPods = validpods;

        // VIEW NUMBER OF RESULTS
        final TextView resultTextView = view.findViewById(R.id.resultText);
        if (validpods == 0) {
            resultTextView.setText(getResources().getString(R.string.no_pod));
        } else {
            resultTextView.setText(String.valueOf(validpods) + " podkast" + (validpods > 1 ? "er" : ""));
        }

        // OUTSIDE FRAGMENT CLICK
        view.findViewById(R.id.pod_filter_result_cont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFragmentListener.onHidePodFilter();
            }
        });
    }

}
