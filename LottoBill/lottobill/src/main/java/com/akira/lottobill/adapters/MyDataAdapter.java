package com.akira.lottobill.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.akira.lottobill.Config;
import com.akira.lottobill.R;
import com.akira.lottobill.utils.BillData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

public class MyDataAdapter extends ArrayAdapter<BillData> {

    private String date;
    private int sum;
    private int isSmall;
    private int isPair;

    public MyDataAdapter(Context context, int layoutID, ArrayList<BillData> bills){
        super(context, layoutID, bills);
    }

    private static class ViewHolder{
        private TextView dateTimeTextView;
        private TextView sumTextView;
        private TextView isSmallTextView;
        private TextView isPairTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BillData billData = getItem(position);
        ViewHolder viewHolder = null;
        if(convertView==null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.data_row,parent,false);
            viewHolder.dateTimeTextView = convertView.findViewById(R.id.time_tv);
            viewHolder.sumTextView = convertView.findViewById(R.id.sum_tv);
            viewHolder.isSmallTextView = convertView.findViewById(R.id.first_status_tv);
            viewHolder.isPairTextView = convertView.findViewById(R.id.second_status_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String dateTime = getFormattedDate(billData.getOpenDateTime());
        int sum = billData.getFirstNum()
                +billData.getSecondNum()
                +billData.getThirdNumber();
        int isPair = (sum%2==0?1:0);
        int isSmall = (sum<13?1:0);
        viewHolder.dateTimeTextView.setText(dateTime);
        Log.d(Config.LOG_TAG,"sum is : "+sum);
        viewHolder.sumTextView.setText(String.valueOf(sum));
        viewHolder.isPairTextView.setText(String.valueOf(isPair));
        viewHolder.isSmallTextView.setText(String.valueOf(isSmall));

        return convertView;
    }

    private String getFormattedDate(long billdate)
    {
        Date date = new Date(billdate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String string = dateFormat.format(date);
        Log.d(Config.LOG_TAG,"dateformat is : "+string);
        return string;
    }


}