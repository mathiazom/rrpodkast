package com.rrpm.mzom.projectrrpm;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FilterFragment extends Fragment {


    private static final String TAG = "RRP-FilterFragment";

    private View view;

    private FilterFragmentListener filterFragmentListener;

    private Spinner spinnerYear;
    private Spinner spinnerMonth;
    private Spinner spinnerDay;

    private Switch notListenedToSwitch;

    private PodsFilter podsFilter = PodsFilter.noFilter();


    static FilterFragment newInstance(PodsFilter podsFilter, FilterFragmentListener filterFragmentListener) {

        final FilterFragment fragment = new FilterFragment();

        fragment.podsFilter = podsFilter;

        fragment.filterFragmentListener = filterFragmentListener;

        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_search, container, false).findViewById(R.id.search_fragment);

        spinnerYear = view.findViewById(R.id.spinnerYear);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerDay = view.findViewById(R.id.spinnerDay);
        notListenedToSwitch = view.findViewById(R.id.notListenedToSwitch);

        initFilterSpinners();

        initSwitches();

        loadFilter(podsFilter);

        return view;
    }

    interface FilterFragmentListener {

        ArrayList<RRPod> loadFilteredPods(PodsFilter filter);

        void hideFilterFragment();

    }

    private void loadFilter(@NonNull PodsFilter filter){

        int selectedDay = filter.getDay();
        int daySelection = selectedDay == -1 ? 0 : selectedDay;
        spinnerDay.setSelection(daySelection);

        int selectedMonth = filter.getMonth();
        int monthSelection = selectedMonth == -1 ? 0 : selectedMonth;
        spinnerMonth.setSelection(monthSelection);

        int selectedYear = filter.getYear();
        int yearSelection = selectedYear == -1 ? 0 : selectedYear;
        spinnerYear.setSelection(yearSelection);

        notListenedToSwitch.setChecked(filter.getListenedToState() == PodsFilter.FilterTriState.FALSE);

        applyFilter(filter);

    }

    private PodsFilter getFilterFromInputs(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner, Switch notListenedToSwitch){

        int daySelection = daySpinner.getSelectedItemPosition();
        int selectedDay = daySelection != 0 ? daySelection : -1;

        int monthSelection = monthSpinner.getSelectedItemPosition();
        int selectedMonth = monthSelection != 0 ? monthSelection : -1;

        int yearSelection = yearSpinner.getSelectedItemPosition();
        int selectedYear = yearSelection != 0 ? yearSelection + 2011 : -1;

        return new PodsFilter()
                .setDay(selectedDay)
                .setMonth(selectedMonth)
                .setYear(selectedYear)
                .setListenedToState(notListenedToSwitch.isChecked() ? PodsFilter.FilterTriState.FALSE : PodsFilter.FilterTriState.ANY);

    }

    private void applyFilter(@NonNull PodsFilter filter){

        Log.i(TAG,filter.getPrintable());

        ArrayList<RRPod> filteredPods = filterFragmentListener.loadFilteredPods(filter);

        viewResultStats(filteredPods.size());

    }

    // Populate and initialize year, month and day spinners
    private void initFilterSpinners() {

        final List<Spinner> spinners = Arrays.asList(spinnerYear, spinnerMonth, spinnerDay);

        final List<ArrayList<String>> spinnerOptionsArray = Arrays.asList(
                getYearSpinnerOptions(),
                getMonthSpinnerOptions(),
                getDaySpinnerOptions()
        );

        final AdapterView.OnItemSelectedListener optionSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                applyFilter(getFilterFromInputs(spinnerDay,spinnerMonth,spinnerYear,notListenedToSwitch));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        for (int s = 0; s < 3; s++) {

            final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerOptionsArray.get(s));
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_search);

            final Spinner spinner = spinners.get(s);
            spinner.setAdapter(spinnerAdapter);
            spinner.setPopupBackgroundResource(R.color.colorWhite);
            spinner.setOnItemSelectedListener(optionSelectedListener);

        }
    }


    private ArrayList<String> getYearSpinnerOptions(){

        // Year spinner options
        final ArrayList<String> yearOptions = new ArrayList<>();

        // Year placeholder option
        yearOptions.add(getString(R.string.search_spinner_placeholder_year));

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
        monthOptions.add(getString(R.string.search_spinner_placeholder_month));

        monthOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.months)));

        return monthOptions;

    }

    private ArrayList<String> getDaySpinnerOptions(){

        // Day spinner options
        final ArrayList<String> dayOptions = new ArrayList<>();

        // Day placeholder option
        dayOptions.add(getString(R.string.search_spinner_placeholder_day));

        for (int d = 1; d < 32; d++) {
            dayOptions.add(String.valueOf(d));
        }

        return dayOptions;

    }

    private void initSwitches() {

        final Switch notListenedToSwitch = view.findViewById(R.id.notListenedToSwitch);

        final ConstraintLayout notListenedToConstraint = view.findViewById(R.id.notListenedToField);

        notListenedToConstraint.setVisibility(View.VISIBLE);
        notListenedToSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilter(getFilterFromInputs(spinnerDay,spinnerMonth,spinnerYear,notListenedToSwitch)));
    }

    private void viewResultStats(int validPods) {

        // Display total pods after filtering
        final TextView resultTextView = view.findViewById(R.id.resultText);
        resultTextView.setText(String.valueOf(validPods));

        // Outside fragment click
        view.findViewById(R.id.pod_filter_result_cont).setOnClickListener(v -> filterFragmentListener.hideFilterFragment());
    }

}
