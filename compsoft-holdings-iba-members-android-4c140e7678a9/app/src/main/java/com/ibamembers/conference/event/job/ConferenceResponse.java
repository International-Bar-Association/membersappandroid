package com.ibamembers.conference.event.job;

import com.google.gson.annotations.SerializedName;

public class ConferenceResponse {

	@SuppressWarnings("unused")
	@SerializedName("Name")
	private String name;

	@SuppressWarnings("unused")
	@SerializedName("Venue")
	private String venue;

	@SuppressWarnings("unused")
	@SerializedName("Start")
	private String start;

	@SuppressWarnings("unused")
	@SerializedName("End")
	private String end;

	public String getName() {
		return name;
	}

	public String getVenue() {
		return venue;
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}
}