package com.telnetproject.pastelplus.telnetproject.connect;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.telnetproject.pastelplus.telnetproject.MainActivity;
import com.telnetproject.pastelplus.telnetproject.model.JsonParsingContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016-03-15.
 */

public class GetParam_Thread extends Thread {
    Handler mHandler;
    JsonParsingContainer mJsonUtils;
    String mJsonResult;
    MainActivity mMainActivity;
    int mThreadNum = 0;
    public GetParam_Thread(Handler handler, JsonParsingContainer jsonUtils, Context context, Activity activity, int threadnum) {
        mHandler = handler;
        mJsonUtils = jsonUtils;
        mMainActivity =(MainActivity) activity;
        mThreadNum = threadnum;
    }

    public void run(){

        int retryCount = 0;



        do{
            mHandler.sendEmptyMessage(0);




            mJsonResult =mMainActivity.getarrParamResult(mThreadNum);

            if(mJsonResult.length() <= 0){
                retryCount++;
            }
//
            if(retryCount > 5){
                mHandler.sendEmptyMessage(1);
                return;
            }

        }while(mJsonResult.length() <= 0);

        try {

            JSONArray ja = new JSONArray("["+mJsonResult+"]");
            for(int i = 0; i < ja.length(); i++)
            {
                mHandler.sendEmptyMessage(2);
                JSONObject jo = ja.getJSONObject(i);
                mJsonUtils.mCheckConnect.setJsonParsingOfData(jo);
                mMainActivity.initarrParamResult();
            }
        } catch (JSONException e) {
            mHandler.sendEmptyMessage(3);
            return;
        }

        mHandler.sendEmptyMessage(4);
    }

}