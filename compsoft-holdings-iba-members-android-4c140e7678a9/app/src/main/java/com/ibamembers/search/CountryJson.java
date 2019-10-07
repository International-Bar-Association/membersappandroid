package com.ibamembers.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CountryJson {

    @SerializedName("countries")
    private Country[] countries;

    public Country[] getCountries() {
        return countries;
    }

    public List<String> getCountryNames() {
        List<String> countryNames = new ArrayList<>();

        for (Country country : countries) {
            countryNames.add(country.getCountryName());
        }

        return countryNames;
    }

    public int getIdForCountryName(String countryName) {
        for (Country country : countries) {
            if (country.getCountryName().equals(countryName)) {
                return country.getCountryId();
            }
        }
        return -1;
    }

    public static class Country {

        @SerializedName("countryId")
        int countryId;

        @SerializedName("countryName")
        String countryName;

        public int getCountryId() {
            return countryId;
        }

        public String getCountryName() {
            return countryName;
        }
    }
}
