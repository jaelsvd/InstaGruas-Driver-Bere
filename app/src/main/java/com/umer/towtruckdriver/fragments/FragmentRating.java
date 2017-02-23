package com.umer.towtruckdriver.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.umer.towtruckdriver.activty.LoginActivity;
import com.umer.towtruckdriver.R;

/**
 * Created by umer on 12/29/16.
 */

public class FragmentRating extends Fragment {

    private TextView driverName,driverRatingNum;

    private RatingBar driverRating;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rating,container,false);

        InitControls(view);

        return view;
    }

    private void InitControls(View view) {

        driverName = (TextView)view.findViewById(R.id.driverName);
        driverRating = (RatingBar)view.findViewById(R.id.ratingBar);
        driverRatingNum = (TextView)view.findViewById(R.id.driverRatingNum);

        driverName.setText(LoginActivity.driverInfo.getName());

        if(LoginActivity.driverInfo.getRating() != null){
            driverRating.setRating(Float.parseFloat(LoginActivity.driverInfo.getRating()));
            driverRatingNum.setText(LoginActivity.driverInfo.getRating());
        }else{
            driverRatingNum.setText("0");
        }

    }

}
