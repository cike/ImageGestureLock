package com.example.cike.imagegesturelockview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private ImageGestureLockView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (ImageGestureLockView) findViewById(R.id.gesture_view);
        view.setOnGestureDrawListener(new ImageGestureLockView.GestureDrawLisenter() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(String gesturePassword) {
                Log.e("手势密码结果", gesturePassword);
            }
        });
    }
}
