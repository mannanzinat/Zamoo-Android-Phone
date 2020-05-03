package com.zamoo.live.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActiveStatus {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("package_title")
    @Expose
    private String packageTitle;
    @SerializedName("expire_date")
    @Expose
    private String expireDate;
    @SerializedName("event_list")
    @Expose
    private List<String> eventList = null;

    public String getStatus() {
        return status;
    }

    public String getPackageTitle() {
        return packageTitle;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public List<String> getEventList() {
        return eventList;
    }
}
