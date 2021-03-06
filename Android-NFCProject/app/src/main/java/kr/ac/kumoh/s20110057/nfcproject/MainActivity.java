package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onLoginBtn(View view){
        Intent intent=new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void onExitBtn(View view){
        programShutdown();
    }
    private void programShutdown() {
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
