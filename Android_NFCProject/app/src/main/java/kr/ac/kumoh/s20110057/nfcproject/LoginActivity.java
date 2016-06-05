package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void onLoginBtn(View view){
        Intent intent=new Intent(this, CheckActivity.class);
        startActivity(intent);
    }
}
