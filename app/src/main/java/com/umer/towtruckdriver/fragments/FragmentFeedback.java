package com.umer.towtruckdriver.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.utils.ImageTrans_CircleTransform;
import com.umer.towtruckdriver.activty.LoginActivity;
import com.umer.towtruckdriver.R;
import com.umer.towtruckdriver.Service.ExecuteTask;
import com.umer.towtruckdriver.interfaces.TaskListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by umer on 12/29/16.
 */

public class FragmentFeedback extends Fragment implements View.OnClickListener, TaskListener {

    private TextView userName,userRatingNum,total;
    private ImageView userImage;

    private Button submit;

    private RatingBar userRating,driverRating;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rating_feedback,container,false);

        InitControls(view);

        return view;
    }

    private void InitControls(View view) {

        total = (TextView)view.findViewById(R.id.total);
        userName = (TextView)view.findViewById(R.id.userContactDetailsName);
        userRatingNum = (TextView)view.findViewById(R.id.userRatingNum);
        userRating = (RatingBar)view.findViewById(R.id.userRating);
        userImage = (ImageView)view.findViewById(R.id.userContactDetailsPic);
        driverRating = (RatingBar)view.findViewById(R.id.driverRating);
        submit = (Button)view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        userName.setText(LoginActivity.customerRequestInfo.getName());

        Bundle b = getArguments();

        if(b != null){
            total.setText("$ " + b.getString("total")+" MX");
        }

        if(LoginActivity.customerRequestInfo.getRating() != null){
            userRating.setRating(Float.parseFloat(LoginActivity.customerRequestInfo.getRating()));
            userRatingNum.setText(LoginActivity.customerRequestInfo.getRating());
        }

        if(LoginActivity.customerRequestInfo.getDisplayPicture() != null && LoginActivity.customerRequestInfo.getDisplayPicture().isEmpty()){
            Picasso.with(getActivity()).load(LoginActivity.customerRequestInfo.getDisplayPicture()).transform(new ImageTrans_CircleTransform()).into(userImage);
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.submit:
                sendRatingToServer();
                break;
        }
    }

    private void sendRatingToServer(){

        if(driverRating.getRating()>0) {

            progressDialog.show();
            Map<String, String> params = new LinkedHashMap<>();
            params.put("rid", LoginActivity.customerRequestInfo.getRequestId());
            params.put("cid", LoginActivity.customerRequestInfo.getCustomerID());
            params.put("rating", Float.toString(driverRating.getRating()));
            params.put("feedback", "");

            new ExecuteTask(this, params, Url.FeedBack,getActivity());
        }else{
            Toast.makeText(getActivity(),"User Rating is Necessary",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(String result) {

        JSONObject jsonObject;
        try {
            progressDialog.dismiss();
            jsonObject = new JSONObject(result);
            if(jsonObject.getString("Status").equals("Success")){
                Toast.makeText(getActivity(),"Rating Success",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(),"Failed to Rate",Toast.LENGTH_SHORT).show();
            }

            getActivity().onBackPressed();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(String result) {

        Toast.makeText(getActivity(),"Network Error.",Toast.LENGTH_SHORT).show();
    }
}
