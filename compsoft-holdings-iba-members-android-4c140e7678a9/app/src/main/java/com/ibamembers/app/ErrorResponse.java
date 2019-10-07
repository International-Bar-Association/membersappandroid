package com.ibamembers.app;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    @SerializedName("code")
    private Integer code;
    @SerializedName("error_message")
    private String strMessage;

    public ErrorResponse(String strMessage, int code){
        this.strMessage = strMessage;
        this.code = code;
    }

    public String getStrMessage() {
        return strMessage;
    }

    public void setStrMessage(String strMessage) {
        this.strMessage = strMessage;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


}
