package com.rrpm.mzom.projectrrpm.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.ui.DatePickerDialogBuilder;
import com.rrpm.mzom.projectrrpm.podfiltering.DateRange;
import com.rrpm.mzom.projectrrpm.podstorage.DateUtils;
import com.rrpm.mzom.projectrrpm.podfiltering.FilterTriState;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilter;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilterViewModel;

import java.util.Date;

public class PodFilterFragment extends Fragment {


    //private static final String TAG = "RRP-PodFilterFragment";

    private View view;

    private PodFilter podFilter;

    private MainFragmentsHandler mainFragmentsHandler;

    private CheckBox notCompletedCheckBox;
    private CheckBox downloadedCheckBox;
    private CheckBox startedCheckBox;

    private PodFilterViewModel podListFilterViewModel;


    @NonNull
    static PodFilterFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsLoader) {

        final PodFilterFragment fragment = new PodFilterFragment();

        fragment.mainFragmentsHandler = mainFragmentsLoader;

        return fragment;
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_filter, container, false).findViewById(R.id.filterBase);

        initCheckBoxes();

        initFocusLossListener();

        podListFilterViewModel = ViewModelProviders.of(requireActivity()).get(PodFilterViewModel.class);
        podListFilterViewModel.getObservablePodFilter().observe(this, podFilter -> {

            if(podFilter == null){

                return;

            }

            this.podFilter = podFilter;
            displayDatePickers();
            displayFilter(podFilter);
        });

        //displayFilter(podListFilterViewModel.getPodFilter());

        return view;
    }


    /*@Override
    public void onResume() {
        super.onResume();

        podListFilterViewModel.setPodFilter(podListFilterViewModel.getPodFilter());

    }*/



    private void initFocusLossListener(){

        final ConstraintLayout filterContainer = view.findViewById(R.id.filterContainer);

        filterContainer.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){

                mainFragmentsHandler.hideFilterFragment();

            }
        });

        final ConstraintLayout filterBaseContainer = view.findViewById(R.id.filterBase);

        filterBaseContainer.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){

                mainFragmentsHandler.hideFilterFragment();

            }
        });

        filterContainer.requestFocus();

    }


    private void displayFilter(@NonNull PodFilter filter){

        final DateRange dateRange = filter.getDateRange();

        final Date fromDate = dateRange.getFromDate();
        final TextView dateRangeFrom = view.findViewById(R.id.filterDateRangeFromText);
        dateRangeFrom.setText(DateUtils.getDateAsString(fromDate));

        final Date toDate = dateRange.getToDate();
        final TextView dateRangeTo = view.findViewById(R.id.filterDateRangeToText);
        dateRangeTo.setText(DateUtils.getDateAsString(toDate));


        notCompletedCheckBox.setChecked(filter.getCompletedState() == FilterTriState.FALSE);

        downloadedCheckBox.setChecked(filter.getDownloadedState() == FilterTriState.TRUE);

        startedCheckBox.setChecked(filter.getStartedState() == FilterTriState.TRUE);

    }


    private void displayDatePickers(){

        final DateRange dateRange = podFilter.getDateRange();

        final LinearLayout dateRangeFromField = view.findViewById(R.id.filterDateRangeFromField);
        dateRangeFromField.setOnClickListener(v -> DatePickerDialogBuilder.build(requireContext(), (datePicker, pickedYear, pickedMonth, pickedDay) -> {

            podFilter.getDateRange().setFromDate(DateUtils.getDateFromDatePicker(datePicker,true));

            podListFilterViewModel.setPodFilter(podFilter);

        }, dateRange.getFromDate()).show());


        final LinearLayout dateRangeToField = view.findViewById(R.id.filterDateRangeToField);
        dateRangeToField.setOnClickListener(v -> DatePickerDialogBuilder.build(requireContext(), (datePicker, pickedYear, pickedMonth, pickedDay) -> {

            podFilter.getDateRange().setToDate(DateUtils.getDateFromDatePicker(datePicker,false));

            podListFilterViewModel.setPodFilter(podFilter);

        }, dateRange.getToDate()).show());

    }


    private void initCheckBoxes(){

        notCompletedCheckBox = view.findViewById(R.id.notCompletedCheckBox);
        notCompletedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            podFilter.setCompletedState(notCompletedCheckBox.isChecked() ? FilterTriState.FALSE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(podFilter);

        });

        downloadedCheckBox = view.findViewById(R.id.downloadedCheckBox);
        downloadedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            podFilter.setDownloadedState(downloadedCheckBox.isChecked() ? FilterTriState.TRUE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(podFilter);

        });

        startedCheckBox = view.findViewById(R.id.startedCheckBox);
        startedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            podFilter.setStartedState(startedCheckBox.isChecked() ? FilterTriState.TRUE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(podFilter);

        });

    }

}
