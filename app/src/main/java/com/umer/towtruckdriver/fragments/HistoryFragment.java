package com.umer.towtruckdriver.fragments;


import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.activty.LoginActivity;
import com.umer.towtruckdriver.Models.DriverHistory;
import com.umer.towtruckdriver.R;
import com.umer.towtruckdriver.Service.ExecuteTask;
import com.umer.towtruckdriver.interfaces.TaskListener;
import com.umer.towtruckdriver.adapter.HistoryAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class HistoryFragment extends Fragment implements TaskListener, AdapterView.OnItemClickListener {
    private ListView listView;
    private ArrayList<DriverHistory> historyArrayList;
    private HistoryAdapter historyArrayAdapter;
    private ProgressDialog progressDialog;
    public static DriverHistory history;

    private TextView noHistory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history,container,false);

        InitControls(view);

        return view;
    }

    private void InitControls(View view){

        progressDialog = ProgressDialog.show(getContext(), "Please wait.", "Loading Data..!", false);
        historyArrayList = new ArrayList<>();
        listView = (ListView)view.findViewById(R.id.fragmentHistoryListView);
        listView.setOnItemClickListener(this);

        noHistory = (TextView)view.findViewById(R.id.textview);
        noHistory.setVisibility(View.GONE);

        GetDataFromServer();
    }

    private void GetDataFromServer(){

        Map<String,String> params = new LinkedHashMap<>();
        params.put("email", LoginActivity.driverInfo.getEmail());
        new ExecuteTask(this,params, Url.DriverHistory,getActivity());
    }

    @Override
    public void onSuccess(String result) {

        progressDialog.dismiss();
        Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
        JSONObject obj;

        try {
            obj = new JSONObject(result);
            if(obj.getString("Status").equals("Success")) {
                extractData(obj);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(String result) {
        progressDialog.dismiss();
        Toast.makeText(getActivity(),"Failure",Toast.LENGTH_SHORT).show();
    }

    private void extractData(JSONObject object){

        historyArrayList = new ArrayList<>();

        Type type = new TypeToken<ArrayList<DriverHistory>>() {
        }.getType();

        try {
            historyArrayList = new GsonBuilder().create().fromJson(object.getJSONArray("History").toString(), type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(historyArrayList.size() > 0) {
            historyArrayAdapter = new HistoryAdapter(getActivity(), historyArrayList);
            listView.setDivider(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.transparent)));
            listView.setAdapter(historyArrayAdapter);
        }else{
            noHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        history = historyArrayList.get(position);

        DecimalFormat f = new DecimalFormat("##.00");
        history.setPayment(f.format(Double.parseDouble(history.getPayment())));

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content,new HistoryDetailFragment());
        ft.addToBackStack("fragment");
        ft.commit();
        return;
    }
}
