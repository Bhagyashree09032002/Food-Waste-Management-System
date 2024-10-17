package com.example.ne.foodneed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class Register extends AppCompatActivity {

    EditText editname,editemail,editmobile,editaadhar,editpassword,editconfirmpassword;
    Button register;
    String name="",email="",mobile="",aadhar="",password="",confirmpassword="";
    String registerURL = IPaddress.ip+"userregister.php";
    private String sharedPrefFile = "com.example.ne.foodneed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editname = (EditText) findViewById(R.id.editRegUsername);
        editemail = (EditText) findViewById(R.id.editRegEmail);
        editmobile = (EditText) findViewById(R.id.editRegMobile);
        editaadhar = (EditText) findViewById(R.id.editAadhaarNo);
        editpassword = (EditText) findViewById(R.id.editRegPassword);
        editconfirmpassword = (EditText) findViewById(R.id.editRegConfirmPassword);
        register = (Button) findViewById(R.id.btnRegisterDone);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editname.getText().toString().trim();
                email = editemail.getText().toString().trim();
                mobile = editmobile.getText().toString().trim();
                aadhar = editaadhar.getText().toString().trim();
                password = editpassword.getText().toString().trim();
                confirmpassword = editconfirmpassword.getText().toString().trim();
                if(!name.isEmpty()){
                    if(!email.isEmpty() && Pattern.matches("^([A-Za-z0-9_\\.\\$])+\\@([A-Za-z0-9_])+\\.+com$",email)){
                        if(!mobile.isEmpty() && mobile.length()==10) {
                            if (!aadhar.isEmpty() && aadhar.length()==12) {
                                if(!password.isEmpty() && password.equals(confirmpassword)){
                                    SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
                                    SharedPreferences.Editor editor= sharedPreferences.edit();
                                    editor.putString("name",name);
                                    editor.putString("email",email);
                                    editor.putString("mobile",mobile);
                                    editor.putString("aadhar",aadhar);
                                    editor.putString("password",password);
                                    editor.commit();
                                    Intent i = new Intent(Register.this,Verification.class);
                                    startActivity(i);
                                }else{
                                    Toast.makeText(Register.this, "Confirm password!", Toast.LENGTH_SHORT).show();
                                    editconfirmpassword.setError("Invalid password!");
                                }
                            }else{
                                Toast.makeText(Register.this, "Enter valid aadhar no!", Toast.LENGTH_SHORT).show();
                                editaadhar.setError("Invalid Aadhaar No!");
                            }
                        }else{
                            Toast.makeText(Register.this, "Enter valid mobile no!", Toast.LENGTH_SHORT).show();
                            editmobile.setError("Invalid mobile no!");
                        }
                    }else{
                        Toast.makeText(Register.this, "Enter valid email address!", Toast.LENGTH_SHORT).show();
                        editemail.setError("Invalid email!");
                    }
                }else{
                    Toast.makeText(Register.this, "Enter valid name!", Toast.LENGTH_SHORT).show();
                    editname.setError("Invalid name!");
                }
            }
        });
    }


}
