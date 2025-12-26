package com.noti.root;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import android.os.PowerManager;
import java.io.DataOutputStream;
import android.util.Log;

public class NotificationService extends NotificationListenerService {

    private static final String LED_PATH = "/sys/class/leds/torch-light0/brightness";
    private long lastTime = 0;
    private PowerManager powerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // 1. Ignore ongoing events (downloads, music players)
        if ((sbn.getNotification().flags & android.app.Notification.FLAG_ONGOING_EVENT) != 0) return;

        // 2. Ignore if screen is ON (save battery/eyes)
        if (powerManager != null && powerManager.isInteractive()) return;

        // 3. Debounce (prevent spamming if multiple notis come at once)
        long now = System.currentTimeMillis();
        if (now - lastTime < 1500) return; 
        lastTime = now;

        blink();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

    private void blink() {
        // Use a WakeLock to ensure the CPU sleeps AFTER the blink, not during
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FlashNoti:Blink");
        wakeLock.acquire(2000); // Safety timeout 2s

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process p = null;
                DataOutputStream os = null;
                try {
                    p = Runtime.getRuntime().exec("su");
                    os = new DataOutputStream(p.getOutputStream());
                    
                    // Your requested pattern
                    String cmd = "echo 255 > " + LED_PATH + "\n" +
                                 "sleep 0.08\n" +
                                 "echo 0 > " + LED_PATH + "\n" +
                                 "sleep 0.06\n" +
                                 "echo 255 > " + LED_PATH + "\n" +
                                 "sleep 0.06\n" +
                                 "echo 0 > " + LED_PATH + "\n" +
                                 "exit\n";
                    
                    os.writeBytes(cmd);
                    os.flush();
                    p.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (os != null) os.close(); } catch (Exception ignored) {}
                    try { if (p != null) p.destroy(); } catch (Exception ignored) {}
                    // Release lock
                    if (wakeLock.isHeld()) wakeLock.release();
                }
            }
        }).start();
    }
}
