package com.hwserve.workslite;

import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorksNotificationService extends NotificationListenerService {
    private final ExecutorService queue = Executors.newSingleThreadExecutor();
    private final Map<String, String> recent = new LinkedHashMap<String, String>() {
        @Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100;
        }
    };

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || getPackageName().equals(sbn.getPackageName())) return;
        SharedPreferences prefs = LiteConfig.prefs(this);
        String selected = prefs.getString("package", "");
        if (selected.isEmpty()) {
            // Save the package identifier only; never cache another app's notification text.
            prefs.edit().putString("recent_package", sbn.getPackageName()).apply();
            return;
        }
        if (!selected.equals(sbn.getPackageName()) || !LiteConfig.ready(this)) return;
        Notification note = sbn.getNotification();
        if (note == null || note.extras == null) return;
        if ((note.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
        CharSequence titleValue = note.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence bigValue = note.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        CharSequence textValue = bigValue != null && bigValue.length() > 0
            ? bigValue : note.extras.getCharSequence(Notification.EXTRA_TEXT);
        String title = titleValue == null ? "" : titleValue.toString().trim();
        String body = textValue == null ? "" : textValue.toString().trim();
        if (title.isEmpty() && body.isEmpty()) return;
        String key = sbn.getKey();
        String fingerprint = title + "\n" + body;
        synchronized (recent) {
            if (fingerprint.equals(recent.get(key))) return;
            recent.put(key, fingerprint);
        }
        queue.execute(() -> {
            boolean delivered = WebhookClient.send(getApplicationContext(), title, body);
            if (delivered && LiteConfig.prefs(this).getBoolean("hide_source", false)) {
                try {
                    // A conversation can update while the HTTP request is in flight.
                    for (StatusBarNotification active : getActiveNotifications()) {
                        if (!key.equals(active.getKey())) continue;
                        Notification current = active.getNotification();
                        CharSequence currentTitle = current.extras.getCharSequence(Notification.EXTRA_TITLE);
                        CharSequence currentBig = current.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                        CharSequence currentText = currentBig != null && currentBig.length() > 0
                            ? currentBig : current.extras.getCharSequence(Notification.EXTRA_TEXT);
                        String activeTitle = currentTitle == null ? "" : currentTitle.toString().trim();
                        String activeBody = currentText == null ? "" : currentText.toString().trim();
                        if (fingerprint.equals(activeTitle + "\n" + activeBody)) cancelNotification(key);
                        break;
                    }
                } catch (RuntimeException ignored) { }
            }
        });
    }

    @Override public void onDestroy() {
        queue.shutdown();
        super.onDestroy();
    }
}
