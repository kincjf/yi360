package com.telnetproject.pastelplus.telnetproject.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016-03-14.
 */
public class JsonParsingContainer {



    public CheckConnect mCheckConnect;

    public JsonParsingContainer(){



        mCheckConnect = new CheckConnect();
    }


    public class CheckConnect {

        public boolean mJsonError;
        public String mMsg_id;
        public String mParam;

        public void setJsonParsingOfData(JSONObject jo){
            try{
                this.mMsg_id =  jo.getString("msg_id");
                this.mParam = jo.getString("param");
                this.mJsonError = false;
            }catch(JSONException e) {
                this.mJsonError = true;
            }
        }
    }


}
