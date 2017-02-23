package com.umer.towtruckdriver.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umer.towtruckdriver.utils.ImageTrans_CircleTransform;
import com.umer.towtruckdriver.Models.DriverHistory;
import com.umer.towtruckdriver.R;

import java.text.DecimalFormat;


/**
 * Created by MuhammadUmer on 12/7/2016.
 */
public class HistoryDetailFragment extends Fragment {

    private ImageView userImage;

    private TextView tvTotalFare,tvDistance,tvTime,tvDate,tvOrigin,tvDestination,tvCustomerName,
                        ratePerKm,minimumRate,timeRateValue,totalValue;

    private DriverHistory history;

    private RatingBar ratingBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_detail_layout,container,false);

        InitControls(view);

        return view;
    }

    private void InitControls(View view){

        String dollar = "$ ";

        history = HistoryFragment.history;

        userImage = (ImageView)view.findViewById(R.id.customerPic);

        tvTotalFare = (TextView)view.findViewById(R.id.tvTotalFare);

        tvDistance = (TextView)view.findViewById(R.id.tvTotalDistance);
        tvTime = (TextView)view.findViewById(R.id.tvTime);
        tvDate = (TextView)view.findViewById(R.id.tvDate);
        tvOrigin = (TextView)view.findViewById(R.id.tvOrigin);
        tvDestination = (TextView)view.findViewById(R.id.tvDestination);
        tvCustomerName = (TextView)view.findViewById(R.id.customerName);

        ratePerKm = (TextView)view.findViewById(R.id.ratePerKmValue);
        minimumRate = (TextView)view.findViewById(R.id.minimumRateValue);
        timeRateValue = (TextView)view.findViewById(R.id.timeRateValue);
        totalValue = (TextView)view.findViewById(R.id.totalValue);

        ratingBar = (RatingBar)view.findViewById(R.id.customerRating);
        if(history.getRate()!=null){
            ratingBar.setRating(Float.parseFloat(history.getRate()));
        }

        double total = Double.parseDouble(history.getBase_fare_amount()) + Double.parseDouble(history.getTotal_kms_fare_amount()) +
        Double.parseDouble(history.getTotal_mins_fare_amount());

        DecimalFormat f = new DecimalFormat("##.00");

        tvTotalFare.setText(dollar.concat(f.format(total)));
        tvDistance.setText(history.getDistance());

        String time[] = history.getDateTimeRequested().split(" ");

        tvTime.setText(time[1]);

        tvDate.setText(time[0]);

        tvOrigin.setText(history.getStartAddress());
        tvDestination.setText(history.getEndAddress());
        tvCustomerName.setText(history.getCName());

        if(history.getDp() != null && !history.getDp().isEmpty()){
            Picasso.with(getContext()).load(history.getDp()).transform(new ImageTrans_CircleTransform()).into(userImage);
        }

        ratePerKm.setText(dollar.concat(f.format(Double.parseDouble(history.getTotal_kms_fare_amount()))));
        minimumRate.setText(dollar.concat(f.format(Double.parseDouble(history.getBase_fare_amount()))));
        timeRateValue.setText(dollar.concat(f.format(Double.parseDouble(history.getTotal_mins_fare_amount()))));
        totalValue.setText(dollar.concat(f.format(total)));

    }

}
