package com.example.ne.foodneed;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class Verification extends AppCompatActivity {

    EditText otpverification;
    Button verify;
    String EncKey="",ukey="";
    String registerURL = IPaddress.ip+"userregister.php";
    private String sharedPrefFile = "com.example.ne.foodneed";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        otpverification = (EditText) findViewById(R.id.editVerificationPin);
        verify = (Button) findViewById(R.id.btnVerify);

        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);

        sendSMSOTP();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ukey = otpverification.getText().toString().trim();
                if(EncKey.equals(ukey)){
                    functionRegister();
                }
            }
        });
    }

    private void sendSMSOTP(){
        if(ActivityCompat.checkSelfPermission(Verification.this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Verification.this,new String[]{Manifest.permission.SEND_SMS},1);
        }else{
            int randpin = (int)(Math.random()*900000)+100000;
            EncKey = ""+randpin;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sharedPreferences.getString("mobile",""),null,"Your OTP for verification is "+EncKey,null,null);
        }
    }

    private void functionRegister(){
        RequestParams params = new RequestParams();
        params.put("name",sharedPreferences.getString("name",""));
        params.put("mobile",sharedPreferences.getString("mobile",""));
        params.put("email",sharedPreferences.getString("email",""));
        params.put("aadhar",sharedPreferences.getString("aadhar",""));
        params.put("password",sharedPreferences.getString("password",""));

        final ProgressDialog pDialog = ProgressDialog.show(Verification.this,"Prcoessing","connecting server...",true,false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(registerURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String res = new String(responseBody);
                if(res.equals("200")){
                    Toast.makeText(Verification.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.commit();
                    Intent i = new Intent(Verification.this,Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(Verification.this, res, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                Toast.makeText(Verification.this, "Connectivity failed with server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1: {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    sendSMSOTP();
                }else{
                    ActivityCompat.requestPermissions(Verification.this,new String[]{Manifest.permission.SEND_SMS},1);
                }
            }
        }
    }
}
