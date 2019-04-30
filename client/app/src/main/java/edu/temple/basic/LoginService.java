package edu.temple.basic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginService extends Service {

    private IBinder mBinder = new LocalBinder();
    //stores the raw response from the server
    private JSONObject mJson;
    private boolean loggedIn = false;

    static final String LOGIN_BROADCAST = "NEW_LOGIN";
    static final String LOGIN_EXTRA = "LOGIN_JSON";

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra(LOGIN_EXTRA);
            try {
                logIn(new JSONObject(temp));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("loginService", "Service onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter iFil = new IntentFilter(LOGIN_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, iFil);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }

    public void logIn(JSONObject obj){
        loggedIn = true;
        mJson = obj;
    }

    public String getUsername(){
        if(loggedIn) {
            try {
                return mJson.getString("username");
            } catch (JSONException e) {
                return "";
            }
        }
        else
            return "";
    }
    public boolean getLoggedIn(){
        return loggedIn;
    }

    public class LocalBinder extends Binder
    {
        LoginService getService()
        {
            return LoginService.this;
        }
    }

}
