package com.akira.lottobill.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.akira.lottobill.Config;
import com.akira.lottobill.R;
import com.akira.lottobill.utils.BillData;
import com.makeramen.roundedimageview.RoundedImageView;

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
        private RoundedImageView firstRoundedImageView;
        private RoundedImageView secondRoundedImageView;
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
            viewHolder.firstRoundedImageView = convertView.findViewById(R.id.first_round_imv_2);
            viewHolder.secondRoundedImageView = convertView.findViewById(R.id.second_round_imv_2);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String dateTime = getFormattedDate(billData.getOpenDateTime());
        int sum = billData.getFirstNum()
                +billData.getSecondNum()
                +billData.getThirdNumber();
        String pairStatus = billData.getPairStatus();
        String smallStatus = billData.getSizeStatus();
        viewHolder.dateTimeTextView.setText(dateTime);
        viewHolder.sumTextView.setText(String.valueOf(sum));
        viewHolder.isPairTextView.setText(pairStatus);
        viewHolder.isSmallTextView.setText(smallStatus);
        if(sum%2==0){
            viewHolder.secondRoundedImageView.setImageResource(R.color.colorPair);
        }else {
            viewHolder.secondRoundedImageView.setImageResource(R.color.colorNonPair);
        }
        if(sum<=13){
            viewHolder.firstRoundedImageView.setImageResource(R.color.colorSmall);
        }else {
            viewHolder.firstRoundedImageView.setImageResource(R.color.colorBig);
        }

        return convertView;
    }

    private String getFormattedDate(long billdate)
    {
        Date date = new Date(billdate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String string = dateFormat.format(date);
        //Log.d(Config.LOG_TAG,"dateformat is : "+string);
        return string;
    }
}