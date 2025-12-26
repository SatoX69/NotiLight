package com.noti.root;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import android.os.PowerManager;
import java.io.DataOutputStream;

public class NotiService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if ((sbn.getNotification().flags & android.app.Notification.FLAG_ONGOING_EVENT) != 0) return;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && pm.isInteractive()) return;

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Flash:Wakelock");
        wl.acquire(3000);

        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                String cmd = "echo 255 > /sys/class/leds/torch-light0/brightness\n" +
                             "sleep 0.08\n" +
                             "echo 0 > /sys/class/leds/torch-light0/brightness\n" +
                             "sleep 0.06\n" +
                             "echo 255 > /sys/class/leds/torch-light0/brightness\n" +
                             "sleep 0.06\n" +
                             "echo 0 > /sys/class/leds/torch-light0/brightness\n" +
                             "exit\n";
                os.writeBytes(cmd);
                os.flush();
                p.waitFor();
            } catch (Exception e) {} 
            finally { if (wl.isHeld()) wl.release(); }
        }).start();
    }
    @Override public void onNotificationRemoved(StatusBarNotification s) {}
}