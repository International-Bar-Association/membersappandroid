package com.ibamembers.search.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.service.BaseJob;
import com.ibamembers.search.database.Attendee;
import com.ibamembers.search.database.AttendeeDao;

import java.util.List;

public class GetConferenceProfileJob extends BaseJob {

	public GetConferenceProfileJob() {
		super(JobConfig.PRIORITY_NORMAL);
	}

	@Override
	public void onRun() throws Throwable {
		List<Attendee> attendeeList;
		AttendeeDao attendeeDao = app.getDatabaseHelper().getAttendeeeDao();
		attendeeList = attendeeDao.queryForAll();


		app.getEventBus().post(new Success(attendeeList));
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		return null;
	}

	public static class Success {

		private List<Attendee> attendeeList;

		public Success(List<Attendee> attendeeList) {
			this.attendeeList = attendeeList;
		}

		public List<Attendee> getAttendeeList() {
			return attendeeList;
		}
	}
}
