package com.example.lbs1;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SecondActivity extends AppCompatActivity {

    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        vibrator= (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long [] pattern = {1000,2000,1000,2000};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern,2);           //重复两次上面的pattern 如果只想震动一次，index设为-1
    }
    public void onStop(){
        super.onStop();
        vibrator.cancel();
    }
}
