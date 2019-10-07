package com.ibamembers.search.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;
import com.ibamembers.search.favourites.ProfileSnippet;

import java.util.ArrayList;
import java.util.List;

public class ConvertProfileSnippetsJob extends Job implements JobConfig.RequiresApp {

	private ProfileSnippetModel[] profileSnippetModels;
	protected App app;
	protected int retryLimit;
	protected int retryBackoffMS;
	private int skip;

	public ConvertProfileSnippetsJob(ProfileSnippetModel[] profileSnippetModels, int skip) {
		super(new Params(JobConfig.PRIORITY_LOW));
		this.skip = skip;
		this.profileSnippetModels = profileSnippetModels;
	}

	@Override
	public void onRun() throws Throwable {
		List<ProfileSnippet> profileSnippets = new ArrayList<>();

		for (int i = 0; i < profileSnippetModels.length; i++) {
			ProfileSnippetModel psm = profileSnippetModels[i];
			ProfileSnippet profileSnippet = new ProfileSnippet(psm.getUserId());
			profileSnippet.setFirstName(psm.getFirstName());
			profileSnippet.setLastName(psm.getLastName());
			profileSnippet.setFirmName(psm.getFirmName());
			profileSnippet.setJobPosition(psm.getJobPosition());
			profileSnippet.setCurrentlyAttendingConference(psm.getCurrentlyAttendingConference());
			profileSnippet.setProfilePicture(psm.getProfilePictureUrl());

			String[] addressLines = psm.getAddress().getAddressLines();
			String city = psm.getAddress().getCity();
			String zip = psm.getAddress().getPcZip();
			String country = psm.getAddress().getCountry();
			String formattedAddress = app.getDataManager().formatAddressLines(addressLines, city, zip, country);

			profileSnippet.setAddress(formattedAddress);

			profileSnippets.add(profileSnippet);
		}

//		Collections.sort(profileSnippets, new Comparator<ProfileSnippet>() {
//			@Override
//			public int compare(ProfileSnippet ps1, ProfileSnippet ps2) {
//				String x1 = !TextUtils.isEmpty(ps1.getFirstName()) ? ps1.getFirstName() : "";
//				String x2 = !TextUtils.isEmpty(ps2.getFirstName()) ? ps2.getFirstName() : "";
//				int sComp = x1.compareTo(x2);
//
//				if (sComp != 0) {
//					return sComp;
//				} else {
//					String s1 = !TextUtils.isEmpty(ps1.getLastName()) ? ps1.getLastName() : "";
//					String s2 = !TextUtils.isEmpty(ps2.getLastName()) ? ps2.getLastName() : "";
//					return s1.compareTo(s2);
//				}
//			}
//		});

		app.getEventBus().post(new Success(profileSnippets, skip));
	}

	public static class Success {

		private List<ProfileSnippet> profileSnippetList;
		private int skip;

		public Success(List<ProfileSnippet> profileSnippetList, int skip) {
			this.profileSnippetList = profileSnippetList;
			this.skip = skip;
		}

		public List<ProfileSnippet> getProfileSnippetList() {
			return profileSnippetList;
		}

		public int getSkip() {
			return skip;
		}
	}


	@Override
	public void onAdded() {

	}

	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		return null;
	}

	@Override
	public void setApp(App app, int retryLimit, int retryBackoffMS) {
		this.app = app;
		this.retryLimit = retryLimit;
		this.retryBackoffMS = retryBackoffMS;
	}
}
