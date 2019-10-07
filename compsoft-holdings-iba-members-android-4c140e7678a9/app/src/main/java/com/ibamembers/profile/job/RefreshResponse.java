package com.ibamembers.profile.job;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class RefreshResponse {

    @SerializedName("AreasOfPracticeDict")
    private Map<String, String> areasOfPractice;

    @SerializedName("CommitteesDict")
    private Map<String, String> committees;

    @SerializedName("Conference")
    private Conference conference;

    public Map<String, String> getAreasOfPractice() {
        return areasOfPractice;
    }

    public Map<String, String> getCommittees() {
        return committees;
    }

    public Conference getConference() {
        return conference;
    }

    public class Conference {

        @SerializedName("ShowDetails")
        private boolean showDetails;

        @SerializedName("ConferenceId")
        private int conferenceId;

        @SerializedName("Url")
        private String url;

        @SerializedName("StartDate")
        private String start;

        @SerializedName("FinishDate")
        private String finish;

        @SerializedName("Attendees")
        private List<AttendeeModel> attendees;

        public boolean isShowDetails() {
            return showDetails;
        }

        public String getUrl() {
            return url;
        }

        public String getStart() {
            return start;
        }

        public String getFinish() {
            return finish;
        }

        public int getConferenceId() {
            return conferenceId;
        }

        public List<AttendeeModel> getAttendees() {
            return attendees;
        }
    }

    public class AttendeeModel {

        @SerializedName("Id")
        private float id;

        @SerializedName("FirstName")
        private String firstName;

        @SerializedName("LastName")
        private String lastName;

        @SerializedName("FirmName")
        private String firmName;

        @SerializedName("ProfilePicture")
        private String profilePicture;

        public float getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirmName() {
            return firmName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }
    }
}
