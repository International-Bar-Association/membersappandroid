package com.ibamembers.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.BaseFragment;
import com.ibamembers.app.DataItem;
import com.ibamembers.app.DatabaseHelper;
import com.ibamembers.app.SettingDao;
import com.ibamembers.search.database.AreaOfPractice;
import com.ibamembers.search.database.AreaOfPracticeDao;
import com.ibamembers.search.database.Committee;
import com.ibamembers.search.database.CommitteeDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DataPickerFragment extends BaseFragment {

    public enum DataType {
        COMMITTEES,
        AREA_OF_PRACTICES,
        COUNTRIES,
        CONFERENCE;

        public static DataType forInt(int id) {
            switch (id) {
                case 0:
                    return COMMITTEES;
                case 1:
                    return AREA_OF_PRACTICES;
                case 2:
                    return COUNTRIES;
                case 3:
                    return CONFERENCE;
            }
            return null;
        }

        public static String getToolbarTitle(DataType dataType, Context context) {
            switch (dataType) {
                case COMMITTEES:
                    return context.getResources().getString(R.string.toolbar_title_committees);
                case AREA_OF_PRACTICES:
                    return context.getResources().getString(R.string.toolbar_title_areas_of_practice);
                case COUNTRIES:
                    return context.getResources().getString(R.string.toolbar_title_countries);
                case CONFERENCE :
                    return context.getResources().getString(R.string.toolbar_title_conference);
            }
            return null;
        }
    }

    private static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";

    public static Bundle getDataPickerFragmentArguments(DataType dataType) {
        Bundle args = new Bundle();
        args.putInt(KEY_DATA_TYPE, dataType.ordinal());
        return args;
    }

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.data_picker_no_results)
    protected TextView noResults;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.data_picker_recycler)
    protected RecyclerView dataPickerRecycler;

    private DataType dataType;
    private DataListAdapter dataListAdapter;
    private DataPickerFragmentListener dataPickerFragmentListener;
    private LinearLayoutManager linearLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_picker_fragment, container, false);
        ButterKnife.bind(this, view);

        Activity activity = getActivity();
        if (activity != null) {
            dataType = DataType.forInt(getArguments().getInt(KEY_DATA_TYPE));
            activity.setTitle(DataType.getToolbarTitle(dataType, activity));
            try {
                setUpRecycler(dataType);

                int selectedPosition = dataListAdapter.getOldSelectedPosition();
                if (selectedPosition != -1) {
                    linearLayoutManager.scrollToPositionWithOffset(selectedPosition, activity.getResources().getInteger(R.integer.data_picker_offset_amount));
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    private void setUpRecycler(DataType dataType) throws SQLException, IOException {
        Activity activity = getActivity();
        if (activity != null) {
            dataListAdapter = new DataListAdapter(getDataItems(dataType));
            linearLayoutManager = new LinearLayoutManager(activity);
            dataPickerRecycler.setLayoutManager(linearLayoutManager);
            dataPickerRecycler.setAdapter(dataListAdapter);
            //dataPickerRecycler.addItemDecoration(new StickyRecyclerHeadersDecoration(dataListAdapter));
        }
    }

    private List<DataItem> getDataItems(DataType dataType) throws SQLException, IOException {
        Activity activity = getActivity();
        App app = getApp();
        if (app != null && activity != null) {
            DatabaseHelper databaseHelper = app.getDatabaseHelper();
            SettingDao settingDao = databaseHelper.getSettingDao();

            List<DataItem> dataItems = new ArrayList<>();

            if (dataType == DataType.COMMITTEES) {
                int selectedCommitteeId = (int) settingDao.getSearchCommitteeId();
                CommitteeDao committeeDao = databaseHelper.getCommitteeDao();
                List<Committee> committees = committeeDao.queryForAll();

                for (Committee committee : committees) {
                    DataItem dataItem = new DataItem(committee.getId(), committee.getName());
                    dataItem.setSelected(selectedCommitteeId == committee.getId());
                    dataItems.add(dataItem);
                }
            } else if (dataType == DataType.AREA_OF_PRACTICES) {
                int selectedAreaOfPracticeId = (int) settingDao.getSearchAreaOfPracticeId();
                AreaOfPracticeDao areaOfPracticeDao = databaseHelper.getAreaOfPracticeDao();
                List<AreaOfPractice> areaOfPractices = areaOfPracticeDao.queryForAll();

                for (AreaOfPractice areaOfPractice : areaOfPractices) {
                    DataItem dataItem = new DataItem(areaOfPractice.getId(), areaOfPractice.getName());
                    dataItem.setSelected(selectedAreaOfPracticeId == areaOfPractice.getId());
                    dataItems.add(dataItem);
                }
            } else if (dataType == DataType.COUNTRIES){
                String selectedCountry = settingDao.getSearchCountry();
                List<String> countries = app.getDataManager().getCountriesList(activity);

                for (int i = 0; i < countries.size(); i++) {
                    String countryName = countries.get(i);
                    DataItem dataItem = new DataItem(i, countryName);

                    if (selectedCountry != null) {
                        dataItem.setSelected(selectedCountry.equals(countryName));
                    }

                    dataItems.add(dataItem);
                }
            } else {
                boolean isAttendingConference = settingDao.getConferenceIsShow();
                if (isAttendingConference) {
                    boolean isConferenceSelected = settingDao.getSearchConference();
                    DataItem dataItem = new DataItem(0, getString(R.string.data_picker_default_conference));
                    dataItem.setSelected(isConferenceSelected);
                    dataItems.add(dataItem);
                }
            }

            Collections.sort(dataItems, new Comparator<DataItem>() {
                @Override
                public int compare(DataItem lhs, DataItem rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });

            return dataItems;
        }
        return null;
    }

    private void dataItemClicked(int dataItemId, boolean isSelected, @Nullable String countryName) throws SQLException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

            if (isSelected) {
                dataItemId = -1;
                countryName = null;
            }

            if (dataType == DataType.COMMITTEES) {
                settingDao.setIdSearchCommitteeId(dataItemId);
            } else if (dataType == DataType.AREA_OF_PRACTICES) {
                settingDao.setSearchAreaOfPracticeId(dataItemId);
            } else if (dataType == DataType.COUNTRIES) {
                settingDao.setSearchCountry(countryName);
            } else {
                settingDao.setSearchConference(!isSelected);
            }

            dataPickerFragmentListener.newDataItemSelected();
        }
    }

    public void setSearchTerm(String searchTerm) {
        if (dataListAdapter != null) {
            dataListAdapter.setSearchTerm(searchTerm);
        }
    }


    protected class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> implements View.OnClickListener {
        private List<DataItem> dataItems;
        private List<DataItem> searchDataItems;
        private int oldSelectedPosition;
        private boolean isFiltered;

        public DataListAdapter(List<DataItem> dataItems) {
            this.dataItems = dataItems;
            oldSelectedPosition = getSelectedItemPosition();
            isFiltered = false;
            updateNoResultsVisibility();
        }

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new DataViewHolder(inflater, parent, this);
        }

        @Override
        public void onBindViewHolder(DataViewHolder holder, int position) {
            holder.fillView(getDataItem(position));
        }

        private DataItem getDataItem(int position) {
            return isFiltered ? searchDataItems.get(position) : dataItems.get(position);
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
            holder.fillView(getDataItem(position));
        }

        @Override
        public int getItemCount() {
            return isFiltered ? searchDataItems.size() : dataItems.size();
        }

        private void setSearchTerm(String searchTerm) {
            List<DataItem> filteredList = new ArrayList<>();

            if (!TextUtils.isEmpty(searchTerm)) {
                isFiltered = true;

                for (DataItem dataItem : dataItems) {
                    String s1 = dataItem.getName().toLowerCase();
                    String s2 = searchTerm.toLowerCase();
                    if (s1.contains(s2)) {
                        filteredList.add(dataItem.getCopy());
                    }
                }

                this.searchDataItems = filteredList;

                updateNoResultsVisibility();
                notifyDataSetChanged();
            } else {
                isFiltered = false;
                updateNoResultsVisibility();
                notifyDataSetChanged();
            }
        }

        private void updateNoResultsVisibility() {
            if (isFiltered) {
                if (this.searchDataItems.size() > 0) {
                    noResults.setVisibility(View.INVISIBLE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                if (this.dataItems.size() > 0) {
                    noResults.setVisibility(View.INVISIBLE);
                } else {
                    if (dataType == DataType.CONFERENCE) {
                        noResults.setText(getString(R.string.data_picker_no_conferences));
                    }
                    noResults.setVisibility(View.VISIBLE);
                }
            }
        }

        private int getSelectedItemPosition() {
            List<DataItem> dataItems = isFiltered ? this.searchDataItems : this.dataItems;

            for (int i = 0; i < dataItems.size(); i++) {
                if (dataItems.get(i).isSelected()) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void onClick(View v) {
            try {
                int position = (int) v.getTag();
                DataItem dataItem = getDataItem(position);
                String countryName = null;
                if (dataType == DataType.COUNTRIES) {
                    countryName = dataItem.getName();
                }

                dataItemClicked((int) dataItem.getId(), dataItem.isSelected(), countryName);
                setIsSelected(dataItem, !dataItem.isSelected());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void setIsSelected(DataItem dataItem, boolean isSelected) {
            int normalDataPos = -1;
            int filteredDataPos = -1;
            int normalSelectedDataPos = -1;
            int filterSelectedDataPos = -1;

            for (int i = 0; i < dataItems.size(); i++) {
                DataItem data = dataItems.get(i);
                if (data.isSelected()) {
                    data.setSelected(false);
                    normalDataPos = i;
                }

                if (dataItem.getName().equals(data.getName()) && isSelected) {
                    data.setSelected(true);
                    normalSelectedDataPos = i;
                }
            }

            if (isFiltered) {
                for (int i = 0; i < searchDataItems.size(); i++) {
                    DataItem data = searchDataItems.get(i);
                    if (data.isSelected()) {
                        data.setSelected(false);
                        filteredDataPos = i;
                    }

                    if (dataItem.getName().equals(data.getName()) && isSelected) {
                        data.setSelected(true);
                        filterSelectedDataPos = i;
                    }
                }

                if (filteredDataPos != -1) {
                    notifyItemChanged(filteredDataPos);
                }

                if (isSelected && filterSelectedDataPos != -1) {
                    notifyItemChanged(filterSelectedDataPos);
                }
            } else {
                if (normalDataPos != -1) {
                    notifyItemChanged(normalDataPos);
                }

                if (isSelected && normalSelectedDataPos != -1) {
                    notifyItemChanged(normalSelectedDataPos);
                }
            }
        }

        private int getOldSelectedPosition() {
            return oldSelectedPosition;
        }

        protected class HeaderViewHolder extends RecyclerView.ViewHolder {

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.headerName)
            TextView headerName;

            public HeaderViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.data_picker_header_view, parent, false));
                ButterKnife.bind(this, itemView);
            }

            public void fillView(char headerName) {
                String name = Character.toString(headerName);
                this.headerName.setText(name);
            }
        }

        protected class DataViewHolder extends RecyclerView.ViewHolder {

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.data_name)
            TextView dataName;

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.data_selected_indicator)
            RadioButton selectionIndicator;

            public DataViewHolder(LayoutInflater inflater, ViewGroup parent, View.OnClickListener clickListener) {
                super(inflater.inflate(R.layout.data_picker_view, parent, false));
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(clickListener);
            }

            public void  fillView(DataItem dataItem) {
                App app = getApp();
                Activity activity = getActivity();
                if (app != null && activity != null) {
                    itemView.setTag(getAdapterPosition());
                    dataName.setText(dataItem.getName());

                    if (dataItem.isSelected()) {
                        selectionIndicator.setChecked(true);
                    } else {
                        selectionIndicator.setChecked(false);
                    }
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dataPickerFragmentListener = (DataPickerFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DataPickerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataPickerFragmentListener = null;
    }

    public interface DataPickerFragmentListener {
        void newDataItemSelected();
    }
}
