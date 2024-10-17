package com.example.ne.foodneed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText name;
    Button location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (EditText) findViewById(R.id.editName);
        location = (Button) findViewById(R.id.btnLocation);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)) {
                    UserData.name = name.getText().toString().trim();
                    if (!UserData.name.isEmpty()) {
                        if (location.getText().toString().equals("Turn On Location Sharing")) {
                            if (UserData.servicecheck == 0) {
                                UserData.servicecheck = 1;
                                Intent i = new Intent(MainActivity.this, LocService.class);
                                startService(i);
                            }
                            location.setText("Turn Off Location Sharing");
                        } else {
                            UserData.servicecheck = 0;
                            location.setText("Turn On Location Sharing");
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Enter name first!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1 : if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                            location.callOnClick();
                        }
                        break;
        }
    }
}
