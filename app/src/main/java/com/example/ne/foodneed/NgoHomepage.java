package com.example.ne.foodneed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class NgoHomepage extends AppCompatActivity {

    TextView info;
    SharedPreferences sharedPreferences;
    String getFoodDetailsURL = IPaddress.ip+"fooddetails.php";
    String updateStatusURL = IPaddress.ip+"updatestatus.php";
    Button navigate,pickup,delivered;
    static String status = "";
    private static int count=0;
    private static double flat=0.0, flon=0.0;
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_homepage);

        info = (TextView) findViewById(R.id.textFoodAllocatedInfo);
        navigate = (Button) findViewById(R.id.btnFoodNavigation);
        pickup = (Button) findViewById(R.id.btnFoodPickedup);
        delivered = (Button) findViewById(R.id.btnFoodDelivered);

        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        navigate.setEnabled(false);
        pickup.setEnabled(false);
        delivered.setEnabled(false);

        functionGetFoodDetails();

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flat = Double.parseDouble(sharedPreferences.getString("flat","0.0"));
                flon = Double.parseDouble(sharedPreferences.getString("flon","0.0"));
                if(flat!=0.0 && flon!=0.0) {
                    Intent i = new Intent(NgoHomepage.this, NavigateActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(NgoHomepage.this, "No food allocated now!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "On the way";
                editor.putString("status",status);
                editor.commit();
                functionUpdateStatus();
            }
        });

        delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Delivered";
                editor.putString("status",status);
                editor.commit();
                functionUpdateStatus();
            }
        });

    }

    public void functionGetFoodDetails(){

        RequestParams params = new RequestParams();
        params.put("id", sharedPreferences.getString("id", ""));

        final ProgressDialog pDialog = ProgressDialog.show(NgoHomepage.this, "Processing", "connecting server...", true, false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getFoodDetailsURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String res = new String(responseBody);
                try {
                    JSONObject obj = new JSONObject(res);
                    info.setText(obj.getString("food"));
                    if (!((info.getText().toString().trim()).equals("No food allocated yet"))) {
                        editor.putString("flat", obj.getString("flat"));
                        editor.putString("flon", obj.getString("flon"));
                        editor.putString("did", obj.getString("did"));
                        editor.commit();
                        navigate.setEnabled(true);
                        pickup.setEnabled(true);
                        delivered.setEnabled(true);
                    } else {
                        navigate.setEnabled(false);
                        pickup.setEnabled(false);
                        delivered.setEnabled(false);
                    }
                } catch (Exception e) {
                    Toast.makeText(NgoHomepage.this, res, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                Toast.makeText(NgoHomepage.this, "Connectivity failed!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void functionUpdateStatus(){

        RequestParams params = new RequestParams();
        params.put("did",sharedPreferences.getString("did","0"));
        params.put("status",status);

        final ProgressDialog pDialog = ProgressDialog.show(NgoHomepage.this,"Processing","connecting server...",true,false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(updateStatusURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String res = new String(responseBody);
                if(res.equals("200")){
                    Toast.makeText(NgoHomepage.this, "Status updated!", Toast.LENGTH_SHORT).show();
                    functionGetFoodDetails();
                }else{
                    Toast.makeText(NgoHomepage.this, "Status updation failed!", Toast.LENGTH_SHORT).show();
                    functionGetFoodDetails();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                Toast.makeText(NgoHomepage.this, "Connectivity failed! Try again.", Toast.LENGTH_SHORT).show();
                functionGetFoodDetails();
            }
        });

    }

    @Override
    public void onBackPressed(){
        count ++;
        if(count==1) {
            final CountDownTimer t = new CountDownTimer(1000,100){
                @Override
                public void onTick(long millisUntilFinished) {
                    if(count>1){
                        onFinish();
                    }
                }

                @Override
                public void onFinish() {
                    if(count>1) {
                        UserData.name = "";
                        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        UserData.id = "";
                        Intent i = new Intent(NgoHomepage.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        count=0;
                        Toast.makeText(NgoHomepage.this, "Continuously press back button two times to logout!", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    }
}
