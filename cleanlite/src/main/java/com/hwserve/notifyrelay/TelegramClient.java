package com.hwserve.notifyrelay;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class TelegramClient {
    static boolean send(Context context, String title, String body) {
        SharedPreferences prefs = LiteConfig.prefs(context);
        String token = prefs.getString("telegram_token", "").trim();
        String chat = prefs.getString("telegram_chat", "").trim();
        String thread = prefs.getString("telegram_thread", "").trim();
        if (!validToken(token) || chat.isEmpty()) {
            prefs.edit().putString("telegram_result", "전송 실패: Telegram 설정을 확인하세요").apply();
            return false;
        }
        String message = title.isEmpty() ? body : body.isEmpty() ? title : title + "\n" + body;
        if (message.length() > 4000) {
            int end = 4000;
            if (Character.isHighSurrogate(message.charAt(end - 1))) end--;
            message = message.substring(0, end) + "…";
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage");
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            String payload = "chat_id=" + encode(chat) + "&text=" + encode(message);
            if (!thread.isEmpty()) payload += "&message_thread_id=" + encode(thread);
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = connection.getOutputStream()) { out.write(bytes); }
            int status = connection.getResponseCode();
            boolean success = false;
            if (status >= 200 && status < 300) {
                try (InputStream in = connection.getInputStream()) {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    while (response.size() < 8192 && (count = in.read(buffer)) != -1) {
                        response.write(buffer, 0, Math.min(count, 8192 - response.size()));
                    }
                    success = new JSONObject(response.toString("UTF-8")).optBoolean("ok", false);
                }
            }
            prefs.edit().putString("telegram_result", success ? "전송 성공" : "전송 실패: HTTP " + status).apply();
            return success;
        } catch (Exception ignored) {
            // The URL contains a bot token, so exception text is never logged or shown.
            prefs.edit().putString("telegram_result", "전송 실패: 연결 또는 설정을 확인하세요").apply();
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    static boolean validToken(String token) {
        return token.matches("[0-9]+:[A-Za-z0-9_-]{20,}");
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    private TelegramClient() {}
}
