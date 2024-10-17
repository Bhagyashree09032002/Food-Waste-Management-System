package com.example.ne.foodneed;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ne.foodneed.firebase.DeleteTokenService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Login extends AppCompatActivity {

    Button ngo,hotel,user,reg,login;
    EditText editusername,editpassword;
    LinearLayout ll;
    String usertype="",username="",password="";
    String loginURL = IPaddress.ip+"login.php";
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ngo = (Button) findViewById(R.id.btnSelectNGO);
        hotel = (Button) findViewById(R.id.btnSelectHotel);
        user = (Button) findViewById(R.id.btnSelectUser);

        reg = (Button) findViewById(R.id.btnRegister);
        login = (Button) findViewById(R.id.btnLogin);

        editusername = (EditText) findViewById(R.id.editUserNameLogin);
        editpassword = (EditText) findViewById(R.id.editPasswordLogin);

        ll = (LinearLayout) findViewById(R.id.layoutUsertype);

        ngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usertype = "ngo";
                ngo.setBackgroundColor(Color.parseColor("#5bf4da"));
                hotel.setBackgroundColor(Color.TRANSPARENT);
                user.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usertype = "hotel";
                hotel.setBackgroundColor(Color.parseColor("#5bf4da"));
                ngo.setBackgroundColor(Color.TRANSPARENT);
                user.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usertype = "user";
                user.setBackgroundColor(Color.parseColor("#5bf4da"));
                ngo.setBackgroundColor(Color.TRANSPARENT);
                hotel.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = editusername.getText().toString().trim();
                password = editpassword.getText().toString().trim();
                if(!usertype.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    functionLogin();
                }else{
                    if(usertype.isEmpty()){
                        ObjectAnimator anim = ObjectAnimator.ofInt(ll,"backgroundColor",Color.parseColor("#5bf4da"),Color.TRANSPARENT);
                        anim.setDuration(500);
                        anim.setEvaluator(new ArgbEvaluator());
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        anim.setRepeatCount(2);
                        anim.start();
                        Toast.makeText(Login.this, "Please select a user type!", Toast.LENGTH_SHORT).show();
                    }else if(username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(Login.this, "Enter credentials!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usertype.isEmpty()){
                    ObjectAnimator anim = ObjectAnimator.ofInt(ll,"backgroundColor",Color.parseColor("#5bf4da"),Color.TRANSPARENT);
                    anim.setDuration(500);
                    anim.setEvaluator(new ArgbEvaluator());
                    anim.setRepeatMode(ValueAnimator.REVERSE);
                    anim.setRepeatCount(2);
                    anim.start();
                    Toast.makeText(Login.this, "Please select a user type!", Toast.LENGTH_SHORT).show();
                }else if(usertype.equals("ngo") || usertype.equals("hotel")) {
                    Toast.makeText(Login.this, "NGO / Hotels must register from website!", Toast.LENGTH_SHORT).show();
                }else if(usertype.equals("user")) {
                    Intent i = new Intent(Login.this, Register.class);
                    startActivity(i);
                }else{
                    Toast.makeText(Login.this, "NGO / Hotels must register from website!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void functionLogin(){
        RequestParams params = new RequestParams();
        params.put("usertype",usertype);
        params.put("username",username);
        params.put("password",password);

        final ProgressDialog pDialog = ProgressDialog.show(Login.this,"Processing","connecting to server...",true,false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(loginURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String res = new String(responseBody);
                try{
                    JSONObject obj = new JSONObject(res);
                    SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("id", obj.getString("id"));
                    UserData.id = obj.getString("id");
                    editor.putString("name", obj.getString("name"));
                    editor.putString("email", obj.getString("email"));
                    editor.putString("mobile", obj.getString("mobile"));
                    editor.putString("usertype",usertype);
                    editor.putString("password",password);
                    if(usertype.equals("ngo")) {
                        editor.putString("lat",obj.getString("lat"));
                        editor.putString("lon",obj.getString("lon"));
                        editor.putString("persons",obj.getString("persons"));
                        editor.putString("regno",obj.getString("regno"));

                        Intent in = new Intent(Login.this, DeleteTokenService.class);
                        startService(in);

                        editor.commit();
                        Intent i = new Intent(Login.this,NgoHomepage.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else if(usertype.equals("hotel")){
                        editor.putString("lat",obj.getString("lat"));
                        editor.putString("lon",obj.getString("lon"));
                        editor.putString("licenseno",obj.getString("licenseno"));
                        editor.commit();
                        Intent i = new Intent(Login.this,HotelHomepage.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else if(usertype.equals("user")){
                        editor.commit();
                        Intent i = new Intent(Login.this,UserHomepage.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }
                }catch(Exception e){
                    Toast.makeText(Login.this, res, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                Toast.makeText(Login.this, "Connectivity failed! Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
