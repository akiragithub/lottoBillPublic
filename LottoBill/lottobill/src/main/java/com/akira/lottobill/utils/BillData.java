package com.akira.lottobill.utils;

import android.content.Context;

import com.akira.lottobill.R;

import org.json.JSONObject;

public class BillData {

    private String issueNumber;
    private long openDateTime;
    private String openNumbers;
    private int firstNum;
    private int secondNum;
    private int thirdNumber;
    private int result;
    private String pairStatus;
    private String sizeStatus;
    private Context context;

    public BillData(){}

    public BillData(Context c, String issueNumber, String openNumbers, long openDateTime)
    {
        this.issueNumber = issueNumber;
        this.openNumbers = openNumbers;
        this.openDateTime = openDateTime;
        context = c;
        separateNumbers(openNumbers);
        setResult();
        setStatus();
    }

    private void separateNumbers(String openNumbers)
    {
        String[] numbersList = openNumbers.split(",");
        firstNum = Integer.valueOf(numbersList[0]);
        secondNum = Integer.valueOf(numbersList[1]);
        thirdNumber = Integer.valueOf(numbersList[2]);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json = MyJsonObjectUtils.put(json,"issue",getIssueNumber());
        json = MyJsonObjectUtils.put(json, "openNum", getOpenNumbers());
        json = MyJsonObjectUtils.put(json, "openDate", getOpenDateTime());
        return json.toString();
    }

    //Setters and getters

    private void setResult() {
        result = firstNum + secondNum + thirdNumber;
    }

    public String getOpenNumbers() {
        return openNumbers;
    }

    public int getFirstNum() {
        return firstNum;
    }

    public int getSecondNum() {
        return secondNum;
    }

    public int getThirdNumber() {
        return thirdNumber;
    }

    public int getResult() {
        return result;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public long getOpenDateTime() {
        return openDateTime;
    }

    public void setFirstNum(int firstNum) {
        this.firstNum = firstNum;
    }

    public void setSecondNum(int secondNum) {
        this.secondNum = secondNum;
    }

    public void setThirdNumber(int thirdNumber) {
        this.thirdNumber = thirdNumber;
    }

    public void setOpenNumbers(String openNumbers) {
        this.openNumbers = openNumbers;
    }
    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public void setOpenDateTime(long openDateTime) {
        this.openDateTime = openDateTime;
    }

    public void setStatus()
    {
        int isPair = (getResult()%2==0?1:0);
        int isSmall = (getResult()<=13?1:0);
        pairStatus = (isPair==1?context.getString(R.string.dble):context.getString(R.string.single));
        sizeStatus = (isSmall==1?context.getString(R.string.small):context.getString(R.string.big));
    }

    public String getPairStatus() {
        return pairStatus;
    }

    public String getSizeStatus() {
        return sizeStatus;
    }
}
