package com.ibamembers.app.gcm.job;

import com.google.gson.annotations.SerializedName;

public class DevicePushTokenModel {

    @SerializedName("DeviceUUID")
    private String deviceUUID;

    @SerializedName("DeviceType")
    private String deviceType;

    @SerializedName("PushToken")
    private String pushToken;

    public DevicePushTokenModel(String deviceUUID, String deviceType, String pushToken) {
        this.deviceUUID = deviceUUID;
        this.deviceType = deviceType;
        this.pushToken = pushToken;
    }
}
