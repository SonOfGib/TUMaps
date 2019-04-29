package edu.temple.basic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationsFetchService extends Service {

    private IBinder mBinder = new LocalBinder();
    private RequestQueue reQueue;
    private final String url="http://ec2-34-203-104-209.compute-1.amazonaws.com/listLocations.php";
    private String mJson;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //setup runnanble to fetch locations every 2 minutes
        mHandlerTask.run();
    }

    private final static int INTERVAL = 1000 * 60 * 1; //1 minutes
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            fetchLocations();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mHandlerTask);
    }
    /**
     * Might be null, only call after you have recieved a broadcast!
     * @return Json Object
     */
    public JSONArray getLocationJson(){
        try {
            return new JSONArray(mJson);
        } catch (JSONException e) {
            Log.e("json error", "getLocationJson()", e);
            return null;
        }
    }

    public void fetchLocations(){
        reQueue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("gettrack","Response is: "+ response);
                        //broadcast that we have fetched locations
                        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(mContext);
                        Intent intent =  new Intent("fetched_markers");
                        bm.sendBroadcast(intent);
                        mJson = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("fetchError","error was:",error.getCause());
            }
        });
        // Add the request to the RequestQueue.
        reQueue.add(stringRequest);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("fetchService", "Service onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder
    {
        LocationsFetchService getService()
        {
            return LocationsFetchService.this;
        }
    }
}
