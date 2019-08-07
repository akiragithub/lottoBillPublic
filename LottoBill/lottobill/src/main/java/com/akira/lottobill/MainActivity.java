package com.akira.lottobill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akira.lottobill.adapters.MyDataAdapter;
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
    BillData lastBill = new BillData();
    ArrayList<MyPattern> patterns = new ArrayList<>();
    static String DATA_LINK = "https://api.kai58a.com/data/jndpc28/last.json";

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

        //Lists and adapters
        arrayAdapter = new MyDataAdapter(this,R.layout.data_row,bills);
        dataListView.setAdapter(arrayAdapter);

        //Allowing (all) servers certificate
        // Because the server certificate was already expired at that time
        NukeSSLCerts.nuke();

        //fetching data;
        loadLottoData();

        // filling patternList with default data
        fillPatternsList();

        //Listeners
        pullSwipeRefreshLayout.setOnRefreshListener(refreshListener);
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

    private void fillPatternsList()
    {
        ArrayList<String> patternData = new ArrayList<>();
        patternData = generatePatternData();

        for(int i=0;i<10;i++)
        {
            MyPattern myPattern = new MyPattern();
            myPattern.setPatternNumber(String.valueOf(i));
            myPattern.setPatternData(patternData.get(i));
            myPattern.setAlarmIsSet(false);
            patterns.add(myPattern);
        }
    }

    private ArrayList<String> generatePatternData()
    {
        ArrayList<String> patternDataList = new ArrayList<>();
        Random random = new Random();
        String single = getString(R.string.single);
        String dble = getString(R.string.dble);
        String big = getString(R.string.big);
        String small = getString(R.string.small);
        String[] pairStatus = {single,dble};
        String[] sizeStatus = {small,big};
        int statusChoice = random.nextInt(1);

        // Launching a set of 10 rows
        for(int i=0; i<10; i++)
        {
            String currentPatternData = "";
            for(int j=0; j<4; j++)
            {
                int intChoice = random.nextInt(1);
                //If statuschoice is 0, so the pattern set line is for pairsStatus
                // if not, it is sizeStatus
                Log.d(Config.LOG_TAG,"choice is "+intChoice);
                String choice = (statusChoice==0?pairStatus[intChoice]:sizeStatus[intChoice]);
                currentPatternData+=choice;
            }
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
               BillData billData = new BillData(issue, openNum, openDateTime);
               bills.add(billData);
            }
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
