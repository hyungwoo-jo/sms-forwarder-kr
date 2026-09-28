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
        // Cache only an identifier so the user can select an app without app-list access.
        prefs.edit().putString("recent_package", sbn.getPackageName()).apply();
        String selected = prefs.getString("package", "");
        if (selected.isEmpty()) return;
        if (!LiteConfig.matchesPackage(selected, sbn.getPackageName()) || !LiteConfig.ready(this)) return;
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
            if (prefs.getBoolean("webhook_enabled", false)) {
                WebhookClient.send(getApplicationContext(), sbn.getPackageName(), title, body);
            }
            if (prefs.getBoolean("telegram_enabled", false)) {
                TelegramClient.send(getApplicationContext(), title, body);
            }
        });
    }

    @Override public void onDestroy() {
        queue.shutdown();
        super.onDestroy();
    }
}
