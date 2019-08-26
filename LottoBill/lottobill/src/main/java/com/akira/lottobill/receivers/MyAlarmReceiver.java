package com.akira.lottobill.receivers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.akira.lottobill.Config;
import com.akira.lottobill.utils.MyDataFetcher;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(Config.LOG_TAG,"AlarmBroadcoast received ");
        //MyDataFetcher.fetchData(context,0);
    }
}
