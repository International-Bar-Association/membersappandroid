package com.ibamembers.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.BaseFragment;
import com.ibamembers.app.SettingDao;
import com.ibamembers.search.database.AreaOfPractice;
import com.ibamembers.search.database.AreaOfPracticeDao;
import com.ibamembers.search.database.Committee;
import com.ibamembers.search.database.CommitteeDao;

import java.sql.SQLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFilterFragment extends BaseFragment {

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_favourites_layout)
	protected LinearLayout favouritesLayout;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.header_text)
	protected TextView favouritesTab;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_first_name)
	protected EditText firstName;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_surname)
	protected EditText surname;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_firm_name)
	protected EditText firmName;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_conference_layout)
	protected TextInputLayout conferenceLayout;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_conference)
	protected EditText conference;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_city)
	protected EditText city;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_country)
	protected EditText country;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_committee)
	protected EditText committee;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_area_of_practice)
	protected EditText areaOfPractice;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_filter_fake_focus_view)
	protected LinearLayout fakeFocusView;

	private SearchFilterFragmentListener searchFilterFragmentListener;
	private boolean shouldRefreshCommittee;
	private boolean shouldRefreshAreaOfPractices;
	private boolean shouldRefreshCountries;
	private boolean shouldRefreshConference;
	private boolean filterChanged;
	private boolean isSearchFabVisible;

	private boolean isConference;

	public void setIsConference(boolean conference) {
		isConference = conference;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.search_filter_fragment, container, false);
		ButterKnife.bind(this, view);
		filterChanged = false;
		isSearchFabVisible = false;
		favouritesTab.setText(getString(R.string.toolbar_title_favourites));
		try {
			fillView();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		setupListeners();

		if (isConference) favouritesLayout.setVisibility(View.GONE);

		return view;
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.search_filter_clear_all)
	public void onClearAllClicked() {
		resetAllSearchFilters();
		setFilterChanged(true);
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.search_filter_favourites_layout)
	public void favouritesClicked() {
		searchFilterFragmentListener.favouritesClicked();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setupListeners() {
		firstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				App app = getApp();
				if (app != null) {
					try {
						String newInput = firstName.getText().toString();
						SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
						settingDao.setSearchFirstName(newInput);
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		surname.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				App app = getApp();
				if (app != null) {
					try {
						SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
						settingDao.setSearchLastName(surname.getText().toString());
						filterChanged = true;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		firmName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				App app = getApp();
				if (app != null) {
					try {
						SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
						settingDao.setSearchFirmName(firmName.getText().toString());
						filterChanged = true;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		conference.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleDataPickerOnTouch(event, DataPickerFragment.DataType.CONFERENCE);
			}
		});

		city.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				App app = getApp();
				if (app != null) {
					try {
						SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
						settingDao.setSearchCity(city.getText().toString());
						filterChanged = true;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		country.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleDataPickerOnTouch(event, DataPickerFragment.DataType.COUNTRIES);
			}
		});

		committee.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleDataPickerOnTouch(event, DataPickerFragment.DataType.COMMITTEES);
			}
		});

		areaOfPractice.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleDataPickerOnTouch(event, DataPickerFragment.DataType.AREA_OF_PRACTICES);
			}
		});
	}

	private boolean handleDataPickerOnTouch(MotionEvent event, DataPickerFragment.DataType dataType) {
		if (MotionEvent.ACTION_UP == event.getAction()) {
			searchFilterFragmentListener.dismissKeyboardIfUp();
			clearAllFocuses();
			searchFilterFragmentListener.loadDataPickerView(dataType);
		}
		return false;
	}

	private void clearAllFocuses() {
		fakeFocusView.requestFocus();
	}

	private void searchFilterHasChanged() {
		filterChanged = true;
		reCalculateIsVisible();
		searchFilterFragmentListener.setSearchButtonVisibility(isSearchFabVisible);
	}

	private void reCalculateIsVisible() {
		isSearchFabVisible = false;

		if (!TextUtils.isEmpty(firstName.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(surname.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(firmName.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(city.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(country.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(committee.getText())) {
			isSearchFabVisible = true;
			return;
		}

		if (!TextUtils.isEmpty(areaOfPractice.getText())) {
			isSearchFabVisible = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDataIfNecessary(shouldRefreshCommittee, shouldRefreshAreaOfPractices, shouldRefreshCountries, shouldRefreshConference);
	}

	public void setShouldRefreshBooleans(boolean shouldRefreshCommittee, boolean shouldRefreshAreaOfPractices, boolean shouldRefreshCountries, boolean shouldRefreshConference) {
		this.shouldRefreshCommittee = shouldRefreshCommittee;
		this.shouldRefreshAreaOfPractices = shouldRefreshAreaOfPractices;
		this.shouldRefreshCountries = shouldRefreshCountries;
		this.shouldRefreshConference = shouldRefreshConference;

		if (getApp() != null) {
			updateDataIfNecessary(shouldRefreshCommittee, shouldRefreshAreaOfPractices, shouldRefreshCountries, shouldRefreshConference);
		}
	}

	private void updateDataIfNecessary(boolean shouldRefreshCommittee, boolean shouldRefreshAreaOfPractices, boolean shouldRefreshCountries, boolean shouldRefreshConference) {
		App app = getApp();
		if (app != null) {
			try {
				SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

				if (shouldRefreshCommittee) {
					try {
						CommitteeDao committeeDao = app.getDatabaseHelper().getCommitteeDao();
						Committee savedCommittee = committeeDao.queryForId(settingDao.getSearchCommitteeId());

						String committee;
						if (savedCommittee != null) {
							committee = savedCommittee.getName();

							if (!TextUtils.isEmpty(committee)) {
								this.committee.setText(committee);
							}
						} else {
							this.committee.setText("");
						}

						this.shouldRefreshCommittee = false;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (shouldRefreshAreaOfPractices) {
					try {
						AreaOfPracticeDao areaOfPracticeDao = app.getDatabaseHelper().getAreaOfPracticeDao();
						AreaOfPractice savedAreaOfPractice = areaOfPracticeDao.queryForId(settingDao.getSearchAreaOfPracticeId());

						String areaOfPractice;
						if (savedAreaOfPractice != null) {
							areaOfPractice = savedAreaOfPractice.getName();

							if (!TextUtils.isEmpty(areaOfPractice)) {
								this.areaOfPractice.setText(areaOfPractice);
							}
						} else {
							this.areaOfPractice.setText("");
						}

						this.shouldRefreshAreaOfPractices = false;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (shouldRefreshCountries) {
					try {
						String savedCountry = settingDao.getSearchCountry();
						this.country.setText(!TextUtils.isEmpty(savedCountry) ? savedCountry : "");
						this.shouldRefreshCountries = false;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (shouldRefreshConference) {
					try {
						boolean savedCountry = settingDao.getSearchConference();
						this.conference.setText(savedCountry ? getString(R.string.data_picker_default_conference): "");
						this.shouldRefreshConference = false;
						searchFilterHasChanged();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void fillView() throws SQLException {
		App app = getApp();
		if (app != null) {
			CommitteeDao committeeDao = app.getDatabaseHelper().getCommitteeDao();
			AreaOfPracticeDao areaOfPracticeDao = app.getDatabaseHelper().getAreaOfPracticeDao();

			SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
			String firstName = settingDao.getSearchFirstName();
			String lastName = settingDao.getSearchLastName();
			String firmName = settingDao.getSearchFirmName();
			String city = settingDao.getSearchCity();
			String country = settingDao.getSearchCountry();
			boolean isConference = settingDao.getSearchConference();

			Committee savedCommittee = committeeDao.queryForId(settingDao.getSearchCommitteeId());
			AreaOfPractice savedAreaOfPractice = areaOfPracticeDao.queryForId(settingDao.getSearchAreaOfPracticeId());

			String committee = null;
			if (savedCommittee != null) {
				committee = savedCommittee.getName();
			}

			String areaOfPractice = null;
			if (savedAreaOfPractice != null) {
				areaOfPractice = savedAreaOfPractice.getName();
			}

			if (!TextUtils.isEmpty(firstName)) {
				this.firstName.setText(firstName);
				this.firstName.setSelection(firstName.length());
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(lastName)) {
				this.surname.setText(lastName);
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(firmName)) {
				this.firmName.setText(firmName);
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(city)) {
				this.city.setText(city);
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(country)) {
				this.country.setText(country);
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(committee)) {
				this.committee.setText(committee);
				this.isSearchFabVisible = true;
			}

			if (!TextUtils.isEmpty(areaOfPractice)) {
				this.areaOfPractice.setText(areaOfPractice);
				this.isSearchFabVisible = true;
			}

			if (this.isConference) {
				this.conferenceLayout.setVisibility(View.GONE);
			} else {
				this.conferenceLayout.setVisibility(View.VISIBLE);
				if (isConference) {
					this.conference.setText(getString(R.string.data_picker_default_conference));
				} else {
					this.conference.setText("");
				}
			}

			searchFilterFragmentListener.setSearchButtonVisibility(isSearchFabVisible);
		}
	}

	public void resetAllSearchFilters() {
		App app = getApp();
		if (app != null) {
			try {
				SettingDao settingDao  = app.getDatabaseHelper().getSettingDao();

				String firstName = this.firstName.getText().toString();
				String secondName = this.surname.getText().toString();
				String firmName = this.firmName.getText().toString();
				String city = this.city.getText().toString();
				String country = this.country.getText().toString();
				this.isSearchFabVisible = false;
				searchFilterFragmentListener.setSearchButtonVisibility(isSearchFabVisible);

				if (!TextUtils.isEmpty(firstName)) {
					this.firstName.setText("");
					settingDao.setSearchFirstName("");
				}

				if (!TextUtils.isEmpty(secondName)) {
					this.surname.setText("");
					settingDao.setSearchLastName("");
				}

				if (!TextUtils.isEmpty(firmName)) {
					this.firmName.setText("");
					settingDao.setSearchFirmName("");
				}

				if (!TextUtils.isEmpty(city)) {
					this.city.setText("");
					settingDao.setSearchCity("");
				}

				if (!TextUtils.isEmpty(country)) {
					this.country.setText("");
					settingDao.setSearchCountry("");
				}

				this.committee.setText("");
				settingDao.setIdSearchCommitteeId(-1);

				this.areaOfPractice.setText("");
				settingDao.setSearchAreaOfPracticeId(-1);

				this.conference.setText("");
				settingDao.setSearchConference(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasFilterChanged() {
		return filterChanged;
	}

	public void setFilterChanged(boolean filterChanged) {
		this.filterChanged = filterChanged;
	}

	public boolean isSearchFabVisible() {
		return isSearchFabVisible;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			searchFilterFragmentListener = (SearchFilterFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement SearchFilterFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		searchFilterFragmentListener = null;
	}

	public interface SearchFilterFragmentListener {

		void loadDataPickerView(DataPickerFragment.DataType dataType);

		void setSearchButtonVisibility(boolean isVisible);

		void dismissKeyboardIfUp();

		void favouritesClicked();
	}
}
