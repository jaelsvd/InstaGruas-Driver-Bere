package com.umer.towtruckdriver.activty;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.umer.towtruckdriver.Constants.Constants;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.MainActivity;
import com.umer.towtruckdriver.Models.CustomerRequestInfo;
import com.umer.towtruckdriver.Models.DriverInfo;
import com.umer.towtruckdriver.R;
import com.umer.towtruckdriver.Service.ExecuteTask;
import com.umer.towtruckdriver.interfaces.TaskListener;
import com.umer.towtruckdriver.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TaskListener {

    private Button loginButton;
    private EditText loginUserNameText,loginPasswordText;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor spEditor;

    public static DriverInfo driverInfo;
    public static CustomerRequestInfo customerRequestInfo;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedpreferences= getSharedPreferences(Constants.SHARED_PREFERENCES_PROFILE, Context.MODE_PRIVATE);
        loginButton = (Button) findViewById(R.id.my_login_button);
        loginButton.setText("LOGIN");
        loginButton.setClickable(true);
        loginUserNameText = (EditText) findViewById(R.id.editTextLoginUserName);
        loginPasswordText = (EditText) findViewById(R.id.editTextLoginPassword);

        loginButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);

        CheckLogin();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.my_login_button){
            if(!Utils.isNetworkAvailable(LoginActivity.this)){
                Toast.makeText(LoginActivity.this,Constants.NETWORK_ERROR,Toast.LENGTH_SHORT).show();
                return;
            }

            SendDataToServer();

        }
    }

    private void SendDataToServer(){
        String inputUserName,inputPassword;
        inputUserName = loginUserNameText.getText().toString().trim();
        inputPassword = loginPasswordText.getText().toString();

        if(inputUserName.equalsIgnoreCase("")){
            Toast.makeText(LoginActivity.this,Constants.ENTER_EMAIL,Toast.LENGTH_SHORT).show();
            return;
        }
        if(inputPassword.equalsIgnoreCase("")){
            Toast.makeText(LoginActivity.this,Constants.ENTER_PASSWORD,Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setText("Signing in...");
        loginButton.setOnClickListener(null);

        progressDialog.show();

        Map<String,String> params = new LinkedHashMap<>();
        params.put("email",loginUserNameText.getText().toString().trim());
        params.put("password", loginPasswordText.getText().toString().trim());

        new ExecuteTask(this,params, Url.LoginUrl,LoginActivity.this);
    }

    @Override
    public void onSuccess(String result) {

        progressDialog.dismiss();

        try
        {
            Gson gson = new Gson();

            JSONObject response = new JSONObject(result);

            if(response.getString("Status").equals("Success")) {

                JSONObject object = response.getJSONObject("data");

                driverInfo = gson.fromJson(object.toString(), DriverInfo.class);

                if (driverInfo.getDisplayPicture() == null) {
                    driverInfo.setDisplayPicture(Url.NoDpUrl);
                }

                CheckPendingRequest(response);

                spEditor = sharedpreferences.edit();

                String json = gson.toJson(driverInfo);
                spEditor.putString("driverInfo", json);
                spEditor.putBoolean(Constants.LOGGED_IN, true);
                spEditor.putString("Password", loginPasswordText.getText().toString().trim());
                spEditor.commit();

                Constants.driverId = driverInfo.getDriverId();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }else if(response.getString("Status").equals("Fail")){
                Toast.makeText(this,"Wrong mail or Password",Toast.LENGTH_SHORT).show();
            }else if(response.getString("Status").equals("Fail1")){
                Toast.makeText(this,"Account Deactivated.Contact Admin",Toast.LENGTH_SHORT).show();
            }

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        finally{
            if(!isFinishing()){
                loginButton.setText("LOGIN");
                loginButton.setOnClickListener(LoginActivity.this);
            }
        }
    }

    @Override
    public void onFailure(String result) {
        progressDialog.dismiss();
        Toast.makeText(LoginActivity.this,"Network Error Occurred.Try Again",Toast.LENGTH_SHORT).show();
        loginButton.setText("Login");
        loginButton.setOnClickListener(this);
    }

    private void CheckLogin(){

        sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_PROFILE, Context.MODE_PRIVATE);
        boolean loggedIn = sharedpreferences.getBoolean(Constants.LOGGED_IN,false);

        if(loggedIn){
            String driverData = sharedpreferences.getString("driverInfo","");

            driverInfo = new Gson().fromJson(driverData, DriverInfo.class);
            if(driverInfo != null){

                progressDialog.show();

                loginUserNameText.setText(driverInfo.getEmail().trim());
                loginPasswordText.setText(sharedpreferences.getString("Password",""));
                SendDataToServer();
            }
        }
    }

    private void CheckPendingRequest(JSONObject object){

        Gson gson = new Gson();

        try {
            JSONArray array = object.getJSONArray("request");

            int size = array.length();

            if(size != 0){

                customerRequestInfo = gson.fromJson(array.get(0).toString(),CustomerRequestInfo.class);

                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
