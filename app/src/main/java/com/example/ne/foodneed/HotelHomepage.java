package com.example.ne.foodneed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class HotelHomepage extends AppCompatActivity {

    Button donate,track,selectloc,submit;
    EditText ricequantity,sabjiquantity,chapatinos,description;
    TextView location;
    static int count =0;
    static String loc="",lat="",lon="",rice="0",sabji="0",roti="0",describe="none";

    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    private String submitFoodURL = IPaddress.ip+"donatefoodhotel.php";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_homepage);

        donate = (Button) findViewById(R.id.btnHotelDonateFood);
        track = (Button) findViewById(R.id.btnHotelTrack);

        selectloc = (Button) findViewById(R.id.selectLocationMap);
        submit = (Button) findViewById(R.id.btnSubmitFoodDetails);

        ricequantity = (EditText) findViewById(R.id.editRiceQuantity);
        sabjiquantity = (EditText) findViewById(R.id.editSabjiQuantity);
        chapatinos = (EditText) findViewById(R.id.editChapatiNo);
        description = (EditText) findViewById(R.id.editFoodDescription);
        location = (TextView) findViewById(R.id.textSelectedLocationAddress);

        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);

        UserData.address = "";

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HotelHomepage.this,HotelTrack.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });

        findViewById(R.id.selectLocationMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HotelHomepage.this,HotelAddLocation.class);
                startActivity(i);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rice = ricequantity.getText().toString().trim();
                sabji = sabjiquantity.getText().toString().trim();
                roti = chapatinos.getText().toString().trim();
                describe = description.getText().toString().trim();
                if((rice.isEmpty() && sabji.isEmpty() && roti.isEmpty()) || (rice.equals("0") && sabji.equals("0") && roti.equals("0"))) {
                    Toast.makeText(HotelHomepage.this, "Enter some food to donate!", Toast.LENGTH_SHORT).show();
                }else{
                    if(loc.isEmpty() || lat.isEmpty() || lon.isEmpty()){
                        Toast.makeText(HotelHomepage.this, "Select location of food!", Toast.LENGTH_SHORT).show();
                    }else {
                        functionSendDonation();
                    }
                }
            }
        });
    }

    private void functionSendDonation(){
        RequestParams params = new RequestParams();
        params.put("rice",rice);
        params.put("sabji",sabji);
        params.put("roti",roti);
        params.put("description",describe);
        params.put("lat",lat);
        params.put("lon",lon);
        params.put("location",loc);
        params.put("usertype",sharedPreferences.getString("usertype",""));
        params.put("uid",sharedPreferences.getString("id",""));
        params.put("uemail",sharedPreferences.getString("email",""));
        params.put("umobile",sharedPreferences.getString("mobile",""));

        final ProgressDialog pDialog = ProgressDialog.show(HotelHomepage.this,"Processing","connecting server...",true,false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(submitFoodURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String res = new String(responseBody);
                if(res.equals("200")){
                    Toast.makeText(HotelHomepage.this, "Donation successful!", Toast.LENGTH_SHORT).show();
                    ricequantity.setText("");
                    ricequantity.setHint("0 Kg");
                    sabjiquantity.setText("");
                    sabjiquantity.setHint("0 Kg");
                    chapatinos.setText("");
                    chapatinos.setHint("0 Nos");
                    description.setText("");
                    description.setHint("Description");
                    location.setText("");
                    loc = "";
                    lat="";
                    lon="";
                    rice="0";
                    sabji="0";
                    roti="0";
                    describe="none";
                    Intent i = new Intent(HotelHomepage.this,Track.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                Toast.makeText(HotelHomepage.this, "Connectivity failed! Try again.", Toast.LENGTH_SHORT).show();
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
                        UserData.fid = "0";
                        Intent i = new Intent(HotelHomepage.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        count=0;
                        Toast.makeText(HotelHomepage.this, "Continuously press back button two times to logout!", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    }

    @Override
    public void onResume(){
        try{
            if(UserData.address.isEmpty() || UserData.address.contains("none")){

            }else{
                ((TextView)findViewById(R.id.textSelectedLocationAddress)).setText(UserData.address);
                ((TextView)findViewById(R.id.textSelectedLocationAddress)).setMovementMethod(new ScrollingMovementMethod());
            }
        }catch(Exception e){}
        super.onResume();
    }
}
