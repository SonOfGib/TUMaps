package edu.temple.basic;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    Button cancelButton, submitButton;
    EditText userName, password, passwordConf, realName, email;
    TextView login;
    String mUsername, mPassword, mEmail, mPasswordConf, mRealName;
    String url = "http://ec2-34-203-104-209.compute-1.amazonaws.com/registerEndPoint.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName=findViewById(R.id.userRegister);
        password=findViewById(R.id.passwordRegister);
        passwordConf = findViewById(R.id.passwordConfRegister);
        realName=findViewById(R.id.nameRegister);
        email=findViewById(R.id.emailRegister);

        cancelButton=findViewById(R.id.cancelButton);
        login = findViewById(R.id.registerText);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
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
                if (TextUtils.isEmpty(mUsername)) userName.setError("required");
                mPassword = password.getText().toString();
                if (TextUtils.isEmpty(mPassword)) password.setError("required");
                mEmail = email.getText().toString();
                if (TextUtils.isEmpty(mEmail)) email.setError("required");
                mPasswordConf = passwordConf.getText().toString();
                if (TextUtils.isEmpty(mPasswordConf)) passwordConf.setError("required");
                mRealName = realName.getText().toString();
                if (TextUtils.isEmpty(mUsername)) mRealName ="";
                //send username and password off to loginEndpoint.php
                RequestQueue reQueue = Volley.newRequestQueue(Register.this);
                // Request a string response from the provided URL.

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("login response", response);
                                try{
                                    JSONObject json = new JSONObject(response);
                                    if(json.has("username")){
                                        Intent intent = new Intent(LoginService.LOGIN_BROADCAST);
                                        intent.putExtra(LoginService.LOGIN_EXTRA, json.toString());
                                        LocalBroadcastManager.getInstance(Register.this).sendBroadcast(intent);
                                        //logged in, back to the mainActivity!
                                        finish();
                                    }
                                }catch (JSONException e){
                                    Log.e("jsonExceptio","Failed to register!", e);
                                    Toast.makeText(Register.this, "Failed to register!",
                                            Toast.LENGTH_LONG).show();
                                }
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
                        postMap.put("email", mEmail);
                        postMap.put("confPassword", mPasswordConf);
                        postMap.put("fullName", mRealName);
                        return postMap;
                    }

                };

                // Add the request to the RequestQueue.
                reQueue.add(stringRequest);
            }
        });
    }
}
