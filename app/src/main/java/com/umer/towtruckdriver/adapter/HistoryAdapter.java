package com.umer.towtruckdriver.adapter;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.utils.ImageTrans_CircleTransform;
import com.umer.towtruckdriver.Models.DriverHistory;
import com.umer.towtruckdriver.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class HistoryAdapter extends BaseAdapter {
    private final FragmentActivity mActivity;
    ArrayList<DriverHistory> historyItems;
    private DriverHistory history;
    public HistoryAdapter(FragmentActivity context, ArrayList<DriverHistory> historyItems){
        this.mActivity = context;
        this.historyItems = historyItems;
    }
    static class ViewHolder {
        private DriverHistory history;
        private ImageView customerPic;
        private TextView amount,name,origin,destination;
        private RatingBar ratingBar;
    }
    @Override
    public int getCount() {
        return historyItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        DriverHistory history = historyItems.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = mActivity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.fragment_history_row, viewGroup, false);
            viewHolder.customerPic = (ImageView) convertView.findViewById(R.id.customerPic);
            viewHolder.amount = (TextView)convertView.findViewById(R.id.tvFinalAmount);
            viewHolder.name = (TextView)convertView.findViewById(R.id.customerName);
            viewHolder.origin = (TextView)convertView.findViewById(R.id.origin);
            viewHolder.destination = (TextView)convertView.findViewById(R.id.destination);
            viewHolder.ratingBar = (RatingBar)convertView.findViewById(R.id.customerRating);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder) convertView.getTag();
        }
        DecimalFormat f = new DecimalFormat("##.00");

        double total = Double.parseDouble(history.getBase_fare_amount()) + Double.parseDouble(history.getTotal_kms_fare_amount()) +
                Double.parseDouble(history.getTotal_mins_fare_amount());

        viewHolder.amount.setText("$" + f.format(total));
        viewHolder.name.setText(history.getCName());
        viewHolder.origin.setText(history.getStartAddress());
        viewHolder.destination.setText(history.getEndAddress());

        if(history.getRate()!=null) {
            viewHolder.ratingBar.setRating(Float.parseFloat(history.getRate()));
        }

        if(history.getDp() != null && !history.getDp().isEmpty()){
            Picasso.with(mActivity).load(history.getDp()).transform(new ImageTrans_CircleTransform()).into(viewHolder.customerPic);
        }else{
            Picasso.with(mActivity).load(Url.NoDpUrl).transform(new ImageTrans_CircleTransform()).into(viewHolder.customerPic);
        }


        return convertView;
    }


}
