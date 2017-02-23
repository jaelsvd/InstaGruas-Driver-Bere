package com.umer.towtruckdriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umer.towtruckdriver.Constants.Constants;
import com.umer.towtruckdriver.activty.LoginActivity;

/**
 * Created by umer on 12/28/16.
 */

public class FragmentProfile extends Fragment implements View.OnClickListener {

    private TextView name,phone,email,license;
    private ImageView imageView;
    private Button logOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        initControls(view);

        return view;
    }

    private void initControls(View view){

        name = (TextView)view.findViewById(R.id.driverName);
        phone = (TextView)view.findViewById(R.id.driverPhone);
        email = (TextView)view.findViewById(R.id.driverEmail);
        license = (TextView)view.findViewById(R.id.driverLicense);
        logOut = (Button)view.findViewById(R.id.signOut);

        logOut.setOnClickListener(this);

        imageView = (ImageView)view.findViewById(R.id.profilePic);

    }

    @Override
    public void onStart() {
        super.onStart();

        UpdateControls();
    }

    private void UpdateControls(){

        name.setText(LoginActivity.driverInfo.getName());
        phone.setText(LoginActivity.driverInfo.getPhone());
        email.setText(LoginActivity.driverInfo.getEmail());
        license.setText(LoginActivity.driverInfo.getLiscenseno());

        if(LoginActivity.driverInfo.getDisplayPicture() != null) {
            Picasso.with(getActivity()).load(LoginActivity.driverInfo.getDisplayPicture()).into(imageView);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.signOut:
                KillUSerSession();
                break;
        }
    }

    private void KillUSerSession(){

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_PROFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedpreferences.edit();
        editor.putBoolean(Constants.LOGGED_IN,false);
        editor.putString("driverInfo","");
        editor.commit();

        ((MainActivity)getActivity()).UpdateDriverLocation(0);

        ((MainActivity)getActivity()).stopRepeatingTask();

        Intent i = new Intent(getActivity(),LoginActivity.class);
        startActivity(i);
    }
}
