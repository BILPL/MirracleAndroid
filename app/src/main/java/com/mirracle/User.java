package com.mirracle;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;
//The sole purpose of this class is to convert json object to reqired object
public class User {
    @SerializedName(value = "Name")
    public String Name;
    @SerializedName(value = "Email")
    public String Email;
    @SerializedName(value = "MobileNumber")
    public String MobileNumber;
    @SerializedName(value = "RefferedBy")
    public String RefferedBy;
    @SerializedName(value = "DeviceType")
    public String DeviceType;
}
