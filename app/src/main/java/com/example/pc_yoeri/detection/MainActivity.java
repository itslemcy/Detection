package com.example.pc_yoeri.detection;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private ImageView mLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogo = (ImageView) findViewById(R.id.Logo);
        mLogo.setImageResource(R.drawable.logo);





    }

    public void loginClick(View view){
        //Switch to Camera Activity if login is succesful
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
