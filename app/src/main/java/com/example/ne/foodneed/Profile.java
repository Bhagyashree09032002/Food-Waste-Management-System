package com.example.ne.foodneed;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class Profile extends AppCompatActivity {

    private static int count = 0;
    TextView name,email;
    Button password,editname,editemail;
    String pass="",nam="",mail="";
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    String updateUserURL = IPaddress.ip+"updateuser.php";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = (TextView) findViewById(R.id.editEditName);
        email = (TextView) findViewById(R.id.editEditDob);
        editname = (Button) findViewById(R.id.btnEditName);
        editemail = (Button) findViewById(R.id.btnEditDob);
        password = (Button) findViewById(R.id.btnChangePassword);

        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        name.setText(sharedPreferences.getString("name",""));
        email.setText(sharedPreferences.getString("email",""));

        findViewById(R.id.btnHomepageDonateFood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Profile.this,UserHomepage.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });

        findViewById(R.id.btnHomepageTrack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Profile.this,Track.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });

        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Profile.this);
                alert.setTitle("Edit name");
                alert.setMessage("Type your name");
                final EditText ed = new EditText(Profile.this);
                ed.setPadding(8,8,8,8);
                ed.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                ed.setMaxLines(1);
                ed.setMaxEms(10);
                ed.setGravity(View.TEXT_ALIGNMENT_CENTER);
                alert.setView(ed);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nam = ed.getText().toString().trim();
                        name.setText(nam);
                        updateInfo();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.create();
                alert.show();
            }
        });

        editemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Profile.this);
                alert.setTitle("Edit email address");
                alert.setMessage("Type your email address");
                final EditText ed = new EditText(Profile.this);
                ed.setPadding(8,8,8,8);
                ed.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                ed.setMaxLines(1);
                ed.setMaxEms(10);
                ed.setGravity(View.TEXT_ALIGNMENT_CENTER);
                alert.setView(ed);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mail = ed.getText().toString().trim();
                        email.setText(mail);
                        updateInfo();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.create();
                alert.show();
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.setContentView(R.layout.changepassword);
                dialog.setTitle("Chnage Password");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                Button cancel = (Button) dialog.findViewById(R.id.btnChangePasswordCancel);
                Button save = (Button) dialog.findViewById(R.id.btnChangePasswordSave);
                final EditText editold = (EditText) dialog.findViewById(R.id.textChangePasswordOldPassword);
                final EditText editnew = (EditText) dialog.findViewById(R.id.textChangePasswordNewPassword);
                final EditText confirmnew = (EditText) dialog.findViewById(R.id.textChangePasswordConfirmPassword);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String oldpass = editold.getText().toString().trim();
                        String newpass = editnew.getText().toString().trim();
                        String confirmpass = confirmnew.getText().toString().trim();

                        if(oldpass!=null && newpass!=null && confirmpass!=null){
                            if(oldpass.equals(sharedPreferences.getString("password",""))){
                                if(newpass.equals(confirmpass)){
                                    pass = newpass;
                                    updateInfo();
                                    dialog.cancel();
                                }else{
                                    Toast.makeText(Profile.this, "Confirm password!", Toast.LENGTH_SHORT).show();
                                    confirmnew.setError("Please confrim password!");
                                }
                            }else{
                                Toast.makeText(Profile.this, "Failed to change password!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void updateInfo(){
        nam = name.getText().toString().trim();
        mail = email.getText().toString().trim();
        if(nam.isEmpty()){
            if(mail.isEmpty()){
                RequestParams params = new RequestParams();
                params.put("id",UserData.id);
                params.put("pass",pass);
                params.put("name",nam);
                params.put("email",mail);

                final ProgressDialog pDialog = ProgressDialog.show(Profile.this,"Processing","please wait...",true,false);

                AsyncHttpClient client = new AsyncHttpClient();
                client.post(updateUserURL, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        pDialog.dismiss();
                        String res = new String(responseBody);
                        if(res.equals("200")){
                            Toast.makeText(Profile.this, "Details saved!", Toast.LENGTH_SHORT).show();
                            editor.putString("name", nam);
                            editor.putString("email", mail);
                            editor.putString("password",pass);
                            editor.commit();
                        }else{
                            Toast.makeText(Profile.this, res, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        pDialog.dismiss();
                        Toast.makeText(Profile.this, "connectivity failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                email.setError("Invalid email address!");
                Toast.makeText(Profile.this, "Invalid mobile number!", Toast.LENGTH_SHORT).show();
            }
        }else{
            name.setError("Invalid name!");
            Toast.makeText(Profile.this, "Enter your full name!", Toast.LENGTH_SHORT).show();
        }
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
                        Intent i = new Intent(Profile.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        count=0;
                        Toast.makeText(Profile.this, "Continuously press back button two times to logout!", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    }
}
