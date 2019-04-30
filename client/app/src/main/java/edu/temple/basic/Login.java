package edu.temple.basic;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Login extends AppCompatActivity {

    Button submitButton, cancelBtn;
    TextView register;
    EditText userName, password;
    String mUsername, mPassword;
    String url = "http://ec2-34-203-104-209.compute-1.amazonaws.com/loginEndPoint.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register=findViewById(R.id.registerText);
        cancelBtn=findViewById(R.id.cancelButton);
        userName=findViewById(R.id.userRegister);
        password=findViewById(R.id.emailRegister); //INTERNAL SCREAMING!

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), Register.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton = findViewById(R.id.loginButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsername = userName.getText().toString();
                mPassword = password.getText().toString();
                //send username and password off to loginEndpoint.php
                final RequestQueue reQueue = Volley.newRequestQueue(Login.this);
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("login response", response);
                                //Log.d("cookies!?", String.valueOf(CookieHandler.getDefault().getgetCookieStore().getCookies()));

                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("fetchError", "error was:", error.getCause());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> postMap = new HashMap<>();
                        postMap.put("username", mUsername);
                        postMap.put("password", mPassword);
                        return postMap;
                    }
                };

                // Add the request to the RequestQueue.
                reQueue.add(stringRequest);

            }
        });
    }

}
