package com.akira.lottobill.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyJsonObjectUtils {

    public static JSONObject stringToJson(String str)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(str);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray stringToJSONArray(String str)
    {
        JSONArray jsonArray = new JSONArray();
        try{
            jsonArray = new JSONArray(str);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static Object getObjectFromJsonArray(JSONArray jsonArray, int index)
    {
        Object object = new Object();
        try{
            object = jsonArray.get(index);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return object;
    }

    public static String getString(JSONObject jsonObject, String key)
    {
        String str = new String();
        try{
            str = jsonObject.getString(key);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return str;
    }

    public static long getLong(JSONObject jsonObject, String key)
    {
        Long lng = -1L;
        try{
            lng = jsonObject.getLong(key);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return lng;
    }

    public static JSONObject put(JSONObject json, String key, Object value)
    {
        try
        {
            json.put(key,value);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return json;
    }



}
