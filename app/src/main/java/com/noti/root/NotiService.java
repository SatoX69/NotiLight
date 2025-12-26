package com.noti.root;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.io.DataOutputStream;
public class NotiService extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("echo 255 > /sys/class/leds/torch-light0/brightness\nsleep 0.08\necho 0 > /sys/class/leds/torch-light0/brightness\nexit\n");
                os.flush();
            } catch (Exception e) {}
        }).start();
    }
}