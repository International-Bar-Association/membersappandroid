package com.ibamembers.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibamembers.R;
import com.ibamembers.profile.job.RefreshJob;
import com.ibamembers.search.CountryJson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DataManager {

    @SuppressWarnings("unused")
    public boolean shouldRefreshData(App app) throws SQLException {
        if (app != null) {
            Date lastUpdated = app.getDatabaseHelper().getSettingDao().getLastRefreshed();
            if (lastUpdated != null) {
                if (!isDateMoreThan1MinsOld(lastUpdated)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void refreshData(App app) throws SQLException, IOException {
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            String sessionToken = settingDao.getSessionToken();

            if (!TextUtils.isEmpty(sessionToken)) {
                app.getJobManager(App.JobQueueName.Network).addJobInBackground(new RefreshJob());
            } else{
                Log.e("DataManager", "Session token not found. Refresh failed");
            }
        }
    }

    public void resetDataRefreshedDate(App app) throws SQLException {
        if (app != null) {
            app.getDatabaseHelper().getSettingDao().setLastRefreshed(new Date());
        }
    }

    private boolean isDateMoreThan1MinsOld(Date date) {
        Calendar lastUpdated = Calendar.getInstance();
        lastUpdated.setTime(date);
        Calendar now = Calendar.getInstance();
        float differenceInMillis = now.getTimeInMillis() - lastUpdated.getTimeInMillis();
        //long differenceInHours = (differenceInMillis) / 1000L / 60L;
        long differenceInMinutes = (long)((differenceInMillis) / 1000F / 60F);

        return differenceInMinutes > 1; // refresh every minute
    }

    public String getFullName(SettingDao settingDao) throws SQLException {
        String firstName = settingDao.getCachedFirstName();
        String secondName = settingDao.getCachedLastName();
        return getFullName(firstName, secondName);
    }

    public String getFullName(String firstName, String secondName) {
        String returnString = "";

        if (!TextUtils.isEmpty(firstName)) {
            returnString += firstName;

            if (!TextUtils.isEmpty(secondName)) {
                returnString += " ";
                returnString += secondName;
            }
        }
        else {
            if (!TextUtils.isEmpty(secondName)) {
                returnString += secondName;
            }
        }

        return returnString;
    }

    public String formatAddressLines(String[] addressLines, String city, String zip, String country) {
        String formattedString = "";

        for (int i = 0; i < addressLines.length + 3; i++) {
            String pointer = "";

            if (i < addressLines.length) {
                pointer = addressLines[i];
            }
            else {
                if (i == addressLines.length) {
                    pointer = city;
                }
                else if (i == addressLines.length + 1) {
                    pointer = zip;
                }
                else if (i == addressLines.length + 2) {
                    pointer = country;
                }
            }

            if (TextUtils.isEmpty(pointer)) {
                continue;
            }

            formattedString += pointer;
            if (i != addressLines.length + 2) {
                formattedString += ", ";
            }
        }

        // removes ", " if it is the last char in the String (this could happen if an address line is null
        if (formattedString.length() > 2 && formattedString.charAt(formattedString.length()-2) == ',') {
            formattedString = formattedString.substring(0, formattedString.length()-2);
        }

        return formattedString;
    }

    public String formatFloatArray(float[] floatArray) {
        String formattedString = "";

        for (int i = 0; i < floatArray.length; i++) {
            formattedString += (int) floatArray[i];

            if (i != floatArray.length-1) {
                formattedString += ",";
            }
        }

        return formattedString;
    }

    public List<String> getCountriesList(Context context) throws IOException {
        String jsonString = context.getResources().getString(R.string.countries_list_file_name);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        List<String> countryList = gson.fromJson(jsonString, CountryJson.class).getCountryNames();

        // sort alphabetically
        Collections.sort(countryList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        return countryList;
    }

    public int getCountryIdFromName(Context context, String countryName) {
        String jsonString = context.getResources().getString(R.string.countries_list_file_name);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        return gson.fromJson(jsonString, CountryJson.class).getIdForCountryName(countryName);
    }
}
