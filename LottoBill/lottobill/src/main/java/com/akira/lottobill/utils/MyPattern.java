package com.akira.lottobill.utils;

import android.util.Log;

import org.json.JSONObject;

public class MyPattern {
    private String patternNumber;
    private String patternData;
    private Boolean alarmIsSet;

    public MyPattern()
    {
    }

    public MyPattern(String patternNumber, String patternData, Boolean alarmIsSet)
    {
        this.patternNumber = patternNumber;
        this.patternData = patternData;
        this.alarmIsSet = alarmIsSet;
    }

    public MyPattern(String patternNumber, String patternData)
    {
        new MyPattern(patternNumber, patternData, Boolean.FALSE);
    }

    public String getPatternNumber() {
        return patternNumber;
    }

    public String getPatternData() {
        return patternData;
    }

    public boolean getAlarmIsSet() {
        return alarmIsSet;
    }

    public void setPatternNumber(String patternNumber) {
        this.patternNumber = patternNumber;
    }

    public void setPatternData(String patternData) {
        this.patternData = patternData;
    }

    public void setAlarmIsSet(Boolean alarmIsSet) {
        this.alarmIsSet = alarmIsSet;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject = MyJsonObjectUtils.put(jsonObject,"patternNum",getPatternNumber());
        jsonObject = MyJsonObjectUtils.put(jsonObject,"patternData", getPatternData());
        jsonObject = MyJsonObjectUtils.put(jsonObject, "alarmIsSet", getAlarmIsSet());
        return jsonObject.toString();
    }
}
