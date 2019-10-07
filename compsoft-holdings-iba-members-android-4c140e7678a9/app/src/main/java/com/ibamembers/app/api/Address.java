package com.ibamembers.app.api;

import com.google.gson.annotations.SerializedName;

public class Address {

    @SerializedName("AddressLines")
    private String[] addressLines;

    @SerializedName("City")
    private String city;

    @SerializedName("County")
    private String county;

    @SerializedName("State")
    private String state;

    @SerializedName("Country")
    private String country;

    @SerializedName("PcZip")
    private String pcZip;

    public String[] getAddressLines() {
        return addressLines;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPcZip() {
        return pcZip;
    }
}
