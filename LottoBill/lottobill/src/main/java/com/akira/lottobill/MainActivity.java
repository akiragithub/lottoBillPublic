package com.akira.lottobill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akira.lottobill.adapters.MyDataAdapter;
import com.akira.lottobill.adapters.MyPatternAdapter;
import com.akira.lottobill.utils.BillData;
import com.akira.lottobill.utils.MyDataFetcher;
import com.akira.lottobill.utils.MyJsonObjectUtils;
import com.akira.lottobill.utils.MyPattern;
import com.akira.lottobill.utils.MySharedPreferences;
import com.akira.lottobill.utils.MyStringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    TextView curNumTextView;
    TextView cureTimeTextView;
    ImageView alarmImageView;
    TextView firstNumTextView;
    TextView secondNumTextView;
    TextView thirdNewTextView;
    TextView resultTextView;
    ImageView copyImageView;
    TextView loadingTextView;
    ListView dataListView;
    ListView patternListView;
    SwipeRefreshLayout pullSwipeRefreshLayout;
    ImageView addPatternImageView;
    ImageView resetPatternImageView;
    ImageView muteAlarmImageView;
    ArrayList<BillData> bills = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    ArrayAdapter patternListAdapter;
    BillData lastBill = new BillData();
    ArrayList<MyPattern> patterns = new ArrayList<>();
    Boolean alarmIsMuted = false;
    MediaPlayer mediaPlayer;
    AlarmManager alarmManager ;
    PendingIntent alarmPendingIntent ;
    TextView countDownTextView;
    private long newDataInterval = (4*60)*1000L;
    private  long lastBillTime;
    private MySharedPreferences mySharedPreferences;
    private MyCountDown countDownTimer;
    private static String PATTERNS_LIST_PREF_KEY = "patterns.key";
    private static long MINIMAL_TIME_FOR_ALARM = 2*1000L;
    private long timeToSleep = 2000L;
    private AudioManager audioManager ;
    private Boolean haveToCheck = true;
    static AlertDialog alertDialogStat = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing data
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        //SharedPreferences
        mySharedPreferences = new MySharedPreferences(this);

        //Initializing UI Components;
        curNumTextView = findViewById(R.id.cur_num_tv);
        cureTimeTextView = findViewById(R.id.time_tv);
        alarmImageView = findViewById(R.id.alarm_imv);
        firstNumTextView = findViewById(R.id.first_num_tv);
        secondNumTextView = findViewById(R.id.second_num_tv);
        thirdNewTextView = findViewById(R.id.third_num_tv);
        resultTextView = findViewById(R.id.result_num_tv);
        loadingTextView = findViewById(R.id.loading_tv);
        copyImageView = findViewById(R.id.copy_imv);
        dataListView = findViewById(R.id.data_lv);
        patternListView = findViewById(R.id.pattern_lv);
        pullSwipeRefreshLayout = findViewById(R.id.pull_to_refresh);
        addPatternImageView = findViewById(R.id.add_pattern);
        resetPatternImageView = findViewById(R.id.reset_pattern);
        muteAlarmImageView = findViewById(R.id.mute_alarm);
        countDownTextView = findViewById(R.id.count_down_tv);

        //Ask for optimization
        requestOptimization();

        //recover previous state
        try{
            alarmIsMuted = (Boolean) mySharedPreferences.getBoolean("mute");
            Log.d(Config.LOG_TAG,"mutedAlarm previous state is : "+alarmIsMuted);
            if (alarmIsMuted){muteAlarm();} else unmuteAlarm();
        }catch (Exception e){
            e.printStackTrace();
        }

        //BroadCoast Management
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //countDownTimer.start();
                if(Config.INTENT_FILTER_ACTION.equals(intent.getAction()))
                {
                    Log.d(Config.LOG_TAG,"broadcoast received");
                    String response = intent.getStringExtra("response");
                    if (response!=null)
                    {
                        formatToDataAndRefreshView(response);
                    }else{
                        loadingTextView.setText(R.string.error_fetching);
                        dataListView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, R.string.error_fetching_data, Toast.LENGTH_LONG).show();
                    }
                }
            }
        },new IntentFilter(Config.INTENT_FILTER_ACTION));

        //Lists and adapters
        arrayAdapter = new MyDataAdapter(this,R.layout.data_row,bills);
        dataListView.setAdapter(arrayAdapter);

        patternListAdapter = new MyPatternAdapter(this, patterns);
        patternListView.setAdapter(patternListAdapter);
        patternListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
               final int position = i;
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setItems(R.array.long_click_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton(R.string.add_pattern, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                patterns.remove(position);
                                savePatternsBeforeDestroy();
                                patternListAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel_pattern, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            //Do nothing
                            }
                        });
                        final AlertDialog alertDialog1 = builder.create();
                        alertDialog1.setMessage(getString(R.string.confirm_delete_item_message));
                        alertDialog1.show();
                    }
                }).create();
                alertDialog.show();
                return true;
            }
        });

        //Allowing (all) servers certificate
        //Because the server certificate was already expired at that time
        NukeSSLCerts.nuke();

        //fetching data;
        loadLottoData(timeToSleep);

        // filling patternList with default data
        restorePatterns(mySharedPreferences.getString(PATTERNS_LIST_PREF_KEY));
        //fillPatternsList(30);

        //Listeners
        muteAlarmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //muteAlarm(audioManager);
                if(mediaPlayer!=null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
        addPatternImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogBoxView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_dialog_box,null);
                //final Dialog alertDialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar).create();
                alertDialog.setView(dialogBoxView);
                //alertDialog.getWindow().setBackgroundDrawable(null);
                //alertDialog.setContentView(dialogBoxView);
                alertDialog.show();
                final EditText patternEditText = dialogBoxView.findViewById(R.id.pattern_hint_et);
                final Button pairsButton = dialogBoxView.findViewById(R.id.pairs_bt);
                final Button sizesButton = dialogBoxView.findViewById(R.id.sizes_bt);
                final Button pairButton = dialogBoxView.findViewById(R.id.pair_bt);
                final Button sizeButton = dialogBoxView.findViewById(R.id.size_bt);
                Button addPatternButton = dialogBoxView.findViewById(R.id.add_pattern_bt);
                Button cancelPatternButton = dialogBoxView.findViewById(R.id.cancel_pattern_bt);
                Button deletePatternButton = dialogBoxView.findViewById(R.id.delete_pattern_bt);
                patternEditText.setKeyListener(null);
                pairsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        patternEditText.setText("");
                        pairsButton.setClickable(false);
                        sizesButton.setClickable(true);
                        pairButton.setText(R.string.single);
                        sizeButton.setText(R.string.dble);
                    }
                });
                sizesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        patternEditText.setText("");
                        sizesButton.setClickable(false);
                        pairsButton.setClickable(true);
                        pairButton.setText(R.string.small);
                        sizeButton.setText(R.string.big);
                    }
                });
                pairButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String oldPattern = String.valueOf(patternEditText.getText());
                        patternEditText.setText(oldPattern+pairButton.getText());
                    }
                });

                sizeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String oldPattern = String.valueOf(patternEditText.getText());
                        patternEditText.setText(oldPattern+sizeButton.getText());
                    }
                });

                addPatternButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String patternData = String.valueOf(patternEditText.getText());
                        if (!TextUtils.isEmpty(patternData))
                        {
                            String patternNumber = String.valueOf(patterns.size()+1);
                            MyPattern pattern = new MyPattern(patternData,false);
                            patterns.add(pattern);
                            patternListAdapter.notifyDataSetChanged();
                            savePatternsBeforeDestroy();
                            alertDialog.dismiss();
                        }
                    }
                });
                cancelPatternButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                deletePatternButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String oldText = String.valueOf(patternEditText.getText());
                        if(!TextUtils.isEmpty(oldText))
                        {
                            String newText = oldText.substring(0,oldText.length()-1);
                            patternEditText.setText(newText);
                        }
                    }
                });

            }
        });


        alarmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alarmIsMuted)
                {
                    unmuteAlarm();
                }
                else {
                    muteAlarm();
                }
                saveMuteState();

            }
        });

        pullSwipeRefreshLayout.setOnRefreshListener(refreshListener);
        resetPatternImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.confirm_reset_message);
                builder.setPositiveButton(R.string.add_pattern, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fillPatternsList(0);
                        Toast.makeText(MainActivity.this,R.string.patterns_list_reset,Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(R.string.cancel_pattern,null);
                builder.create().show();
            }
        });
        copyImageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText
                                (getString(R.string.last_bill_clip_data), lastBill.toString());
                        try{
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(getApplicationContext(),R.string.last_bill_clip_data,Toast.LENGTH_SHORT).show();
                        }catch (NullPointerException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Config.LOG_TAG,"activity being destroyed");
        saveAndReleaseEverything();

    }

    private void requestOptimization(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
            String packageName = getPackageName();
            if(!pm.isIgnoringBatteryOptimizations(packageName));
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+packageName));
                startActivity(intent);
            }
        }
    }

    private void saveAndReleaseEverything()
    {
        Log.d(Config.LOG_TAG,"we are in tne destroy");
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //handle countDown
        try{
            countDownTimer.cancel();
        }catch (Exception e){e.printStackTrace();}
        //saveAndReleaseEverything();
        //save patternsState;
        savePatternsBeforeDestroy();
        //save alarmMuted state
        saveMuteState();
        Log.d(Config.LOG_TAG,"we saved everything");
    }

    private void saveMuteState(){
        mySharedPreferences.putBoolean("mute",alarmIsMuted);
    }



    private void muteAlarm()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_MUTE,0);
        }else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
        alarmImageView.setImageResource(R.drawable.ic_alarm_off_black_24dp);
        alarmIsMuted = true;
    }

    private void unmuteAlarm()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_UNMUTE,0);
        }else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
        alarmImageView.setImageResource(R.drawable.ic_alarm_black_24dp);
        alarmIsMuted = false;
    }


    private void fillPatternsList(int numberOfItems)
    {
        ArrayList<String> patternData = new ArrayList<>();
        patternData = generatePatternData(numberOfItems);
        restorePatterns(patternData);
    }

    private void restorePatterns(ArrayList<String> patternData)
    {
        patterns.clear();
        if (patternData!=null&&!patternData.isEmpty())
        {
            for(int j=0;j<patternData.size();j++)
            {
                String currentData = patternData.get(j);
                if(!TextUtils.isEmpty(currentData))
                {
                    MyPattern myPattern = new MyPattern();
                    myPattern.setPatternData(currentData.trim());
                    myPattern.setAlarmIsSet(false);
                    patterns.add(myPattern);
                }
            }
        }
        patternListAdapter.notifyDataSetChanged();
        savePatternsBeforeDestroy();
    }

    private void restorePatterns(String stringValueOfPattersDataList)
    {
        ArrayList<String> patternData = MyStringUtils.stringToArrayList(stringValueOfPattersDataList);
        restorePatterns(patternData);
    }

    private void savePatternsBeforeDestroy()
    {
        ArrayList<String> patternData = new ArrayList<>();
     if (patterns!=null&&!patterns.isEmpty()){
         for (MyPattern pattern:patterns) {
             patternData.add(pattern.getPatternData());
         }
     }
        mySharedPreferences.putString(PATTERNS_LIST_PREF_KEY,String.valueOf(patternData));
        Log.d(Config.LOG_TAG,"PatternsList saved");
    }

    private void checkPatternMatch()
    {
        String mergedPairs = mergedPairsStatus();
        String mergedSize = mergedSizeStatus();
        String reversePairs = MyStringUtils.reverseString(mergedPairs);
        String reverseSize = MyStringUtils.reverseString(mergedSize);
        //String mergedStatus = mergedPairs+mergedSize;
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext())
                .setMessage(R.string.new_alarm)
                .setNeutralButton(R.string.text_ok,null);
        alertDialogStat = builder.create();
        for (MyPattern pattern: patterns) {
            String patternData = pattern.getPatternData().trim();
            Log.d(Config.LOG_TAG,"pattern is : "+patternData);
            if (reversePairs.endsWith(patternData)
                    ||reverseSize.endsWith(patternData)
            )
            {
                Log.d(Config.LOG_TAG,"pattern matched : "+patternData);
                //showing dialog
                /*try{
                    try{alertDialogStat.dismiss();}catch (Exception e){e.printStackTrace();}
                        alertDialogStat.show();
                }catch (Exception e){e.printStackTrace();}
                */
                //Trigerring alarm
                try {
                    if(mediaPlayer==null)
                    {
                        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
                    }else{
                        if(!mediaPlayer.isPlaying()){mediaPlayer.start();}
                        //mediaPlayer.stop();
                        //mediaPlayer.prepareAsync();
                        //mediaPlayer.start();
                    }
                }catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });

                //updating UI
                pattern.setAlarmIsSet(true);
                //break;
            }
            else {
                pattern.setAlarmIsSet(false);
            }
        }
        patternListAdapter.notifyDataSetChanged();
        savePatternsBeforeDestroy();
    }

    private String mergedPairsStatus()
    {
        String mergedPairs = "";
        for (BillData billData:bills) {
            mergedPairs+=billData.getPairStatus();
        }
        return mergedPairs;
    }


    private String mergedSizeStatus()
    {
        String mergedSize = "";
        for (BillData billData:bills) {
            mergedSize+=billData.getSizeStatus();
        }
        return mergedSize;
    }

    private ArrayList<String> generatePatternData(int numberOfItems)
    {
        ArrayList<String> patternDataList = new ArrayList<>();
        Random random = new Random();
        String single = getString(R.string.single);
        String dble = getString(R.string.dble);
        String big = getString(R.string.big);
        String small = getString(R.string.small);
        String[] pairStatus = {single,dble};
        String[] sizeStatus = {small,big};
        int statusChoice = random.nextInt(2);

        // Launching a set of 10 rows
        for(int i=0; i<numberOfItems; i++)
        {
            String currentPatternData = "";
            for(int j=0; j<4; j++)
            {
                int intChoice = random.nextInt(2);
                //If statuschoice is 0, so the pattern set line is for pairsStatus
                // if not, it is sizeStatus
                String choice = (statusChoice==0?pairStatus[intChoice]:sizeStatus[intChoice]);
                currentPatternData+=choice;
            }
            patternDataList.add(currentPatternData);
        }
        return patternDataList;
    }

    private void loadLottoData(final long timeToSleep) {
        //countDownTimer.start();
        loadingTextView.setText(R.string.loading);
        loadingTextView.setVisibility(View.VISIBLE);
        MyDataFetcher myDataFetcher = new MyDataFetcher(this);
        myDataFetcher.execute(String.valueOf(timeToSleep));

    }

    private void formatToDataAndRefreshView(String apiResult)
    {
        JSONArray resultJsonArray = MyJsonObjectUtils.stringToJSONArray(apiResult);
        int arrayLength = resultJsonArray.length();
        bills.clear();
        for (int i=0; i<arrayLength;i++)
        {
            JSONObject currentBill = (JSONObject)MyJsonObjectUtils.getObjectFromJsonArray(
                    resultJsonArray,i);
            if (currentBill.length()>0)
            {
               String issue = MyJsonObjectUtils.getString(currentBill,"issue");
               String openNum = MyJsonObjectUtils.getString(currentBill,"openNum");
               long openDateTime = MyJsonObjectUtils.getLong(currentBill, "openDateTime");
               BillData billData = new BillData(this, issue, openNum, openDateTime);
               bills.add(billData);
            }
        }
        try{
            countDownTimer.cancel();
        }catch (Exception e){e.printStackTrace();}
        countDownTimer = new MyCountDown(getRemaining(),1000);
        countDownTimer.start();
        long tempsRestant = MINIMAL_TIME_FOR_ALARM;
        try{
            tempsRestant = countDownTimer.getTotalTime();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.d(Config.LOG_TAG,"temps restant est : "+tempsRestant);
        if ((tempsRestant >= MINIMAL_TIME_FOR_ALARM) &&haveToCheck)
        {
            timeToSleep = 0;
            checkPatternMatch();
        }
        haveToCheck = true;
        arrayAdapter.notifyDataSetChanged();
        updateUIData(bills);
        loadingTextView.setVisibility(View.GONE);
        dataListView.setVisibility(View.VISIBLE);
        if (tempsRestant < MINIMAL_TIME_FOR_ALARM){
            timeToSleep = 2000L;
        }
    }

    private long getRemaining()
    {
        lastBillTime = mySharedPreferences.getLong("lastBillTime");
        long currentTime = System.currentTimeMillis();
        long rem = (lastBillTime + newDataInterval)-currentTime;
        Log.d(Config.LOG_TAG,"last bill time is "+String.valueOf(new Date(lastBillTime)));
        return rem;
    }

    private void updateUIData(ArrayList<BillData> bills)
    {
        if(bills!=null&&bills.size()>0)
        {
            lastBill = bills.get(0);
            if (lastBill!=null)
            {
                int firstNum = lastBill.getFirstNum();
                int secondNum = lastBill.getSecondNum();
                int thirdNum = lastBill.getThirdNumber();
                int sum = firstNum + secondNum + thirdNum;
                lastBillTime = lastBill.getOpenDateTime();
                mySharedPreferences.putLong("lastBillTime",lastBillTime);
                String date = getFormattedDate(lastBill.getOpenDateTime());
                String billIssueNumber = lastBill.getIssueNumber();

                firstNumTextView.setText(String.valueOf(firstNum));
                secondNumTextView.setText(String.valueOf(secondNum));
                thirdNewTextView.setText(String.valueOf(thirdNum));
                resultTextView.setText(String.valueOf(sum));
                cureTimeTextView.setText(date);
                curNumTextView.setText(billIssueNumber);
            }

        }
    }
    private String getFormattedDate(long billdate)
    {
        Date date = new Date(billdate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
        String string = dateFormat.format(date);
        return string;
    }

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //loadingTextView.setText(R.string.loading);
            //loadingTextView.setVisibility(View.VISIBLE);
            haveToCheck = false;
            loadLottoData(timeToSleep);
            //to stop animation
            //the android paging library I am using requires to do it manually
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pullSwipeRefreshLayout.setRefreshing(false);
                }
            },3_000);
        }
    };


        public static class NukeSSLCerts {
        public static void nuke() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    private class MyCountDown extends CountDownTimer
    {
        long totalTime;
        public MyCountDown(long totalTime,long interval)
        {
            super(totalTime,interval);
            this.totalTime = totalTime;
        }

        @Override
        public void onTick(long l) {

            long minutes = l/(1000*60);
            long secondes = l/1000-60*minutes;
            String toShow = minutes+":"+secondes;
            countDownTextView.setText(toShow);

        }

        @Override
        public void onFinish() {
            //countDownTimer.start();
            //MyDataFetcher.fetchData(MainActivity.this);
            loadLottoData(timeToSleep);
        }

        public long getTotalTime() {
            return totalTime;
        }
    }
}
