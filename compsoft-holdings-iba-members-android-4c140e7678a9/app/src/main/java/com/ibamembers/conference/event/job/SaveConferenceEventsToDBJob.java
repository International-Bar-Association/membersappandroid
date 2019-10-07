package com.ibamembers.conference.event.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;
import com.ibamembers.conference.event.db.ConferenceEventDao;

import java.sql.SQLException;
import java.util.List;

public class SaveConferenceEventsToDBJob extends Job implements JobConfig.RequiresApp {

	protected App app;
	protected int retryLimit;
	protected int retryBackoffMS;
	private List<ConferenceEventResponse> conferenceEventList;

	public SaveConferenceEventsToDBJob(List<ConferenceEventResponse> conferenceEventList) {
		super(new Params(JobConfig.PRIORITY_NORMAL));
		this.conferenceEventList = conferenceEventList;
	}

	@Override
	public void onRun() throws Throwable {
		try {
			ConferenceEventDao conferenceEventDao = app.getDatabaseHelper().getConferenceEventDao();
			for (ConferenceEventResponse eventResponse : conferenceEventList) {
				conferenceEventDao.saveEventResponseAsEvent(eventResponse);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
