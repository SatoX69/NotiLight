package com.noti.root;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.DataOutputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPerm = findViewById(R.id.btnPerm);
        Button btnTest = findViewById(R.id.btnTest);

        btnPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Notification Access Settings
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testRoot();
            }
        });
    }

    private void testRoot() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(p.getOutputStream());
                    // Simple single blink for test
                    os.writeBytes("echo 255 > /sys/class/leds/torch-light0/brightness\n");
                    os.writeBytes("sleep 0.1\n");
                    os.writeBytes("echo 0 > /sys/class/leds/torch-light0/brightness\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    p.waitFor();
                    runOnUiThread(new Runnable() {
                         public void run() { 
                             Toast.makeText(MainActivity.this, "Flashed! If no light, check path.", Toast.LENGTH_SHORT).show(); 
                         }
                    });
                } catch (Exception e) {
                    final String err = e.getMessage();
                    runOnUiThread(new Runnable() { public void run() { Toast.makeText(MainActivity.this, "Root Error: " + err, Toast.LENGTH_LONG).show(); }});
                }
            }
        }).start();
    }
}
