package com.akira.lottobill.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.akira.lottobill.R;
import com.akira.lottobill.utils.MyPattern;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MyPatternAdapter extends ArrayAdapter<MyPattern> {

    public MyPatternAdapter(Context context, ArrayList<MyPattern> patternsList)
    {
        super(context, R.layout.pattern_row,patternsList);
    }

    private static class ViewHolder
    {
        private TextView patternNumberTextView;
        private TextView patternDataTextView;
        private ImageView patternStatusImageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView==null)
            {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pattern_row,parent,false);
                viewHolder.patternNumberTextView = convertView.findViewById(R.id.pattern_num_tv);
                viewHolder.patternDataTextView = convertView.findViewById(R.id.pattern_tv);
                viewHolder.patternStatusImageView = convertView.findViewById(R.id.pattern_status_imv);
                convertView.setTag(viewHolder);
            }else
                {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
            MyPattern pattern = getItem(position);
            viewHolder.patternNumberTextView.setText(pattern.getPatternNumber());
            viewHolder.patternDataTextView.setText(pattern.getPatternData());
            viewHolder.patternStatusImageView.setVisibility(pattern.getAlarmIsSet()?View.VISIBLE:View.INVISIBLE);
            convertView.setBackgroundResource(pattern.getAlarmIsSet()?R.color.colorAccent:R.color.colorWhite);
            return convertView;
    }
}
