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
import com.rrpm.mzom.projectrrpm.podfiltering.PodListFilterViewModel;

import java.util.Date;

public class FilterFragment extends Fragment {


    //private static final String TAG = "RRP-FilterFragment";

    private View view;

    private MainFragmentsHandler mainFragmentsHandler;

    private CheckBox notCompletedCheckBox;
    private CheckBox downloadedCheckBox;
    private CheckBox startedCheckBox;

    private PodListFilterViewModel podListFilterViewModel;

    private DateRange widestDateRange;


    @NonNull
    public static FilterFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsLoader, @NonNull DateRange widestDateRange) {

        final FilterFragment fragment = new FilterFragment();

        fragment.mainFragmentsHandler = mainFragmentsLoader;
        fragment.widestDateRange = widestDateRange;

        return fragment;
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_filter, container, false).findViewById(R.id.filterBase);

        initCheckBoxes();

        initFocusLossListener();

        podListFilterViewModel = ViewModelProviders.of(requireActivity()).get(PodListFilterViewModel.class);
        podListFilterViewModel.getObservablePodFilter().observe(this, podFilter -> {
            initDatePickers();
            displayFilter(podFilter);
        });

        displayFilter(podListFilterViewModel.getPodFilter());

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        podListFilterViewModel.setPodFilter(podListFilterViewModel.getPodFilter());

    }

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

        if(dateRange != null){

            final Date fromDate = dateRange.getFromDate();

            final TextView dateRangeFrom = view.findViewById(R.id.filterDateRangeFromText);
            dateRangeFrom.setText(DateUtils.getDateAsString(fromDate));


            final Date toDate = dateRange.getToDate();

            final TextView dateRangeTo = view.findViewById(R.id.filterDateRangeToText);
            dateRangeTo.setText(DateUtils.getDateAsString(toDate));

        }


        notCompletedCheckBox.setChecked(filter.getCompletedState() == FilterTriState.FALSE);

        downloadedCheckBox.setChecked(filter.getDownloadedState() == FilterTriState.TRUE);

        startedCheckBox.setChecked(filter.getStartedState() == FilterTriState.TRUE);

    }


    private void initDatePickers(){

        final PodFilter podFilter = podListFilterViewModel.getPodFilter();

        if(podFilter.getDateRange() == null){

            podFilter.setDateRange(widestDateRange);

        }

        final DateRange dateRange = podFilter.getDateRange();

        final LinearLayout dateRangeFromField = view.findViewById(R.id.filterDateRangeFromField);
        dateRangeFromField.setOnClickListener(v -> DatePickerDialogBuilder.build(requireContext(), (datePicker, pickedYear, pickedMonth, pickedDay) -> {

            final PodFilter tempFilter = podListFilterViewModel.getPodFilter();

            if(tempFilter.getDateRange() == null){
                tempFilter.setDateRange(widestDateRange);
            }

            tempFilter.getDateRange().setFromDate(DateUtils.getDateFromDatePicker(datePicker,true));
            podListFilterViewModel.setPodFilter(tempFilter);

        }, dateRange.getFromDate()).show());


        final LinearLayout dateRangeToField = view.findViewById(R.id.filterDateRangeToField);
        dateRangeToField.setOnClickListener(v -> DatePickerDialogBuilder.build(requireContext(), (datePicker, pickedYear, pickedMonth, pickedDay) -> {

            final PodFilter tempFilter = podListFilterViewModel.getPodFilter();

            if(tempFilter.getDateRange() == null){
                tempFilter.setDateRange(widestDateRange);
            }

            tempFilter.getDateRange().setToDate(DateUtils.getDateFromDatePicker(datePicker,false));
            podListFilterViewModel.setPodFilter(tempFilter);

        }, dateRange.getToDate()).show());

    }


    private void initCheckBoxes(){

        notCompletedCheckBox = view.findViewById(R.id.notCompletedCheckBox);
        notCompletedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            final PodFilter tempPodFilter = podListFilterViewModel.getPodFilter();
            tempPodFilter.setCompletedState(notCompletedCheckBox.isChecked() ? FilterTriState.FALSE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(tempPodFilter);

        });

        downloadedCheckBox = view.findViewById(R.id.downloadedCheckBox);
        downloadedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            final PodFilter tempPodFilter = podListFilterViewModel.getPodFilter();
            tempPodFilter.setDownloadedState(downloadedCheckBox.isChecked() ? FilterTriState.TRUE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(tempPodFilter);

        });

        startedCheckBox = view.findViewById(R.id.startedCheckBox);
        startedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            final PodFilter tempPodFilter = podListFilterViewModel.getPodFilter();
            tempPodFilter.setStartedState(startedCheckBox.isChecked() ? FilterTriState.TRUE : FilterTriState.ANY);
            podListFilterViewModel.setPodFilter(tempPodFilter);

        });

    }

}
