package com.noti.root;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import java.io.DataOutputStream;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        
        findViewById(R.id.btnPerm).setOnClickListener(v -> 
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

        findViewById(R.id.btnTest).setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(p.getOutputStream());
                    os.writeBytes("echo 255 > /sys/class/leds/torch-light0/brightness\n");
                    os.writeBytes("sleep 0.2\n");
                    os.writeBytes("echo 0 > /sys/class/leds/torch-light0/brightness\n");
                    os.flush();
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        });
    }
}