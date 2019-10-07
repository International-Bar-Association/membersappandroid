package com.ibamembers.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.birbit.android.jobqueue.TagConstraint;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.search.favourites.ProfileSnippet;
import com.ibamembers.search.job.ConvertProfileSnippetsJob;
import com.ibamembers.search.job.SearchJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultFragment extends ProfileSnippetFragment {

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_result_recycler)
	protected RecyclerView searchResultRecycler;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_progress_bar)
	protected ProgressBar progressBar;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.search_no_profiles_found)
	protected TextView noProfilesFound;

	private SearchResultFragmentListener searchResultFragmentListener;
	private boolean isLandscape;

	/*
	NOTE: Profile snippet is loaded from api for first time and kept in memory. Snippets are not saved in DB for future use
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.search_result_fragment, container, false);
		ButterKnife.bind(this, view);

		setRetainInstance(true);

		View layoutIsLandscape = view.findViewById(R.id.search_layout_is_landscape);
		isLandscape = layoutIsLandscape != null;

		List<ProfileSnippet> profileSnippets = getSnippetsIfPresent();
		setUpRecycler(profileSnippets, SearchHandlerFragment.TAG_SEARCH_JOB, !isConference);

		if (profileSnippets.size() <= 0) {
			searchForProfiles();
		}

		return view;
	}

	@Override
	protected RecyclerView getRecyclerView() {
		return searchResultRecycler;
	}

	private void searchForProfiles() {
		searchForProfiles(0);
	}

	/**
	 *	PAGING not used, take is set to max value (keeping code incase it it used again)
	 */
	private void searchForProfiles(int skip) {
		progressBar.setVisibility(View.VISIBLE);
		getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new SearchJob(skip, TAKE_COUNT));
		skipIndex += TAKE_COUNT;
	}

	@Override
	public void getProfile() {
		searchForProfiles(skipIndex);
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(ConvertProfileSnippetsJob.Success profileSnippets) {
		progressBar.setVisibility(View.INVISIBLE);

		if (profileSnippets.getProfileSnippetList().size() > 0) {
			noProfilesFound.setVisibility(View.INVISIBLE);
			if (profileSnippets.getSkip() > 0 ) {
				appendProfileSnippets(profileSnippets.getProfileSnippetList());
			} else {
				setProfileSnippets(profileSnippets.getProfileSnippetList());
			}
		} else {
			noProfilesFound.setVisibility(View.VISIBLE);
		}

		if (isLandscape) {
			getProfileSnippetAdapter().selectFirstItemIfNoneSelected();
		}

		if (profileSnippets.getProfileSnippetList().size() < TAKE_COUNT) {
			loadMore = false;
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SearchJob.SearchJobError error) {
		App app = getApp();
		Activity activity = getActivity();
		if (app != null && activity != null) {
			if (error.getStatus() == activity.getResources().getInteger(R.integer.session_token_invalid_code)) {
				app.getConnectionManager().sessionExpired(activity, app);
			} else {
				showErrorDialog();
			}
		}
	}

	private void showErrorDialog() {
		Resources resources = this.getResources();
		String title = resources.getString(R.string.search_result_failed_error_title);
		String message = resources.getString(R.string.search_result_failed_error_message);
		String positiveButton = resources.getString(R.string.search_result_failed_error_positive_button);
		showErrorDialog(title, message, positiveButton, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				searchResultFragmentListener.finishedSearching();
			}
		});
	}

	protected void showErrorDialog(String title, String message, String positiveButton, boolean isCancellable, DialogInterface.OnClickListener onClickListener) {
		Activity activity = getActivity();
		if (activity != null) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
			alertDialog.setTitle(title);
			alertDialog.setMessage(message);
			alertDialog.setPositiveButton(positiveButton, onClickListener);

			if (!isCancellable) {
				alertDialog.setCancelable(false);
			}

			alertDialog.show();
		}
	}

	public void cancelSearching() {
		App app = getApp();
		if (app != null) {
			app.getJobManager(App.JobQueueName.Network).cancelJobsInBackground(null, TagConstraint.ANY, SearchJob.SEARCH_JOB_TAG);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			searchResultFragmentListener = (SearchResultFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement SearchResultFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		searchResultFragmentListener = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		App app = getApp();
		if (app != null) {
			if (!app.getEventBus().isRegistered(this)) {
				app.getEventBus().register(this);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		App app = getApp();
		if (app != null) {
			app.getEventBus().unregister(this);
		}
	}

	public interface SearchResultFragmentListener {
		void finishedSearching();
	}
}
