package com.akira.lottobill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.akira.lottobill.utils.MyJsonObjectUtils;
import com.akira.lottobill.utils.MyPattern;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    static String DATA_LINK = "https://api.kai58a.com/data/jndpc28/last.json";
    Boolean alarmIsMuted = false;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initializing data
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
        mediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        //Lists and adapters
        arrayAdapter = new MyDataAdapter(this,R.layout.data_row,bills);
        dataListView.setAdapter(arrayAdapter);

        patternListAdapter = new MyPatternAdapter(this, patterns);
        patternListView.setAdapter(patternListAdapter);

        //Allowing (all) servers certificate
        // Because the server certificate was already expired at that time
        NukeSSLCerts.nuke();

        //fetching data;
        loadLottoData();

        // filling patternList with default data
        fillPatternsList(5);

        //Listeners
        muteAlarmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
                //muteAlarm(audioManager);
                if(mediaPlayer!=null)
                {
                    mediaPlayer.stop();
                }
            }
        });
        addPatternImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogBoxView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_dialog_box,null);
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setView(dialogBoxView);
                alertDialog.show();
                final EditText patternEditText = dialogBoxView.findViewById(R.id.pattern_hint_et);
                final Button pairsButton = dialogBoxView.findViewById(R.id.pairs_bt);
                final Button sizesButton = dialogBoxView.findViewById(R.id.sizes_bt);
                final Button pairButton = dialogBoxView.findViewById(R.id.pair_bt);
                final Button sizeButton = dialogBoxView.findViewById(R.id.size_bt);
                Button addPatternButton = dialogBoxView.findViewById(R.id.add_pattern_bt);
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
                            MyPattern pattern = new MyPattern(patternNumber,patternData,false);
                            patterns.add(pattern);
                            patternListAdapter.notifyDataSetChanged();
                            checkPatternMatch();
                            alertDialog.dismiss();
                        }
                    }
                });

            }
        });


        alarmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
                if (alarmIsMuted)
                {
                    unmuteAlarm(audioManager);
                }
                else {
                    muteAlarm(audioManager);
                }

            }
        });

        pullSwipeRefreshLayout.setOnRefreshListener(refreshListener);
        resetPatternImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillPatternsList(5);
                Toast.makeText(MainActivity.this,R.string.patterns_list_reset,Toast.LENGTH_LONG).show();
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
        //On destory stop and release the media player
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    private void muteAlarm(AudioManager audioManager)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,AudioManager.ADJUST_MUTE,0);
        }else {
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        }
        alarmImageView.setImageResource(R.drawable.ic_alarm_off_black_24dp);
        alarmIsMuted = true;
    }

    private void unmuteAlarm(AudioManager audioManager)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,AudioManager.ADJUST_UNMUTE,0);
        }else {
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        }
        alarmImageView.setImageResource(R.drawable.ic_alarm_black_24dp);
        alarmIsMuted = false;
    }


    private void fillPatternsList(int numberOfItems)
    {
        ArrayList<String> patternData = new ArrayList<>();
        patternData = generatePatternData(numberOfItems);
        patterns.clear();
        for(int i=1;i<numberOfItems;i++)
        {
            MyPattern myPattern = new MyPattern();
            myPattern.setPatternNumber(String.valueOf(i));
            myPattern.setPatternData(patternData.get(i));
            myPattern.setAlarmIsSet(false);
            patterns.add(myPattern);
        }
        checkPatternMatch();
        patternListAdapter.notifyDataSetChanged();
    }

    private void checkPatternMatch()
    {

        String mergedPairs = mergedPairsStatus();
        String mergedSize = mergedSizeStatus();
        String mergedStatus = mergedPairs+mergedSize;
        for (MyPattern pattern: patterns) {
            Log.d(Config.LOG_TAG, "merged status is : "+mergedStatus);
            Log.d(Config.LOG_TAG, "current pattern is : "+pattern.getPatternData());
            if (mergedStatus.contains(pattern.getPatternData()))
            {
                //Trigerring alarm
                Log.d(Config.LOG_TAG,"Pattern Match found : "+pattern.getPatternData());
                if(mediaPlayer!=null)
                {
                    //mediaPlayer.prepareAsync();
                    mediaPlayer.start();
                }

                //updating UI
                pattern.setAlarmIsSet(true);
                patternListAdapter.notifyDataSetChanged();
            }
        }
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
                Log.d(Config.LOG_TAG,"status choice is "+statusChoice);
                String choice = (statusChoice==0?pairStatus[intChoice]:sizeStatus[intChoice]);
                currentPatternData+=choice;
            }
            Log.d(Config.LOG_TAG,"current data is : "+currentPatternData);
            patternDataList.add(currentPatternData);
        }
        return patternDataList;
    }

    private void loadLottoData() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, DATA_LINK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //DOTO refresList
                        Log.d(Config.LOG_TAG, "new response : " + response);
                        formatToDataAndRefreshView(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingTextView.setText(R.string.error_fetching);
                        Log.d(Config.LOG_TAG, "error is : " + error.toString());
                        Toast.makeText(MainActivity.this, R.string.error_fetching_data, Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void formatToDataAndRefreshView(String apiResult)
    {
        JSONArray resultJsonArray = MyJsonObjectUtils.stringToJSONArray(apiResult);
        int arrayLength = resultJsonArray.length();

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
            //BillData testBillData = new BillData("458745123","4,3,6",45124L);
            //bills.add(testBillData);
            checkPatternMatch();
            arrayAdapter.notifyDataSetChanged();
            updateUIData(bills);
            loadingTextView.setVisibility(View.GONE);
        }

    }

    private void updateUIData(ArrayList<BillData> bills)
    {
        lastBill = bills.get(0);
        if (lastBill!=null)
        {
            int firstNum = lastBill.getFirstNum();
            int secondNum = lastBill.getSecondNum();
            int thirdNum = lastBill.getThirdNumber();
            int sum = firstNum + secondNum + thirdNum;
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
    private String getFormattedDate(long billdate)
    {
        Date date = new Date(billdate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
        String string = dateFormat.format(date);
        Log.d(Config.LOG_TAG,"dateformat is : "+string);
        return string;
    }

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadingTextView.setVisibility(View.VISIBLE);
            loadLottoData();
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
}
