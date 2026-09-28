package com.hwserve.workslite;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class WebhookClient {
    static boolean send(Context context, String title, String body) {
        SharedPreferences prefs = LiteConfig.prefs(context);
        if (!LiteConfig.ready(context)) return false;
        String url = prefs.getString("url", "");
        String method = prefs.getString("method", "POST").toUpperCase(Locale.ROOT);
        String message = title.isEmpty() ? body : body.isEmpty() ? title : title + "\n" + body;
        HttpURLConnection connection = null;
        try {
            URL target = new URL(url);
            if (!"https".equalsIgnoreCase(target.getProtocol())) throw new IllegalArgumentException("HTTPS required");
            if (!method.equals("POST") && !method.equals("GET")) {
                method = "POST";
            }
            String template = prefs.getString("body_template", "");
            JSONObject headers = new JSONObject(prefs.getString("headers", "{}"));
            String declaredType = "";
            java.util.Iterator<String> headerNames = headers.keys();
            while (headerNames.hasNext()) {
                String name = headerNames.next();
                if (name.equalsIgnoreCase("Content-Type")) declaredType = headers.optString(name);
            }
            boolean json = declaredType.toLowerCase(Locale.ROOT).contains("application/json")
                || (declaredType.isEmpty() && template.trim().startsWith("{"));
            boolean plain = declaredType.toLowerCase(Locale.ROOT).contains("text/plain");
            String contentType = declaredType.isEmpty()
                ? (json ? "application/json; charset=utf-8" : "application/x-www-form-urlencoded; charset=utf-8")
                : declaredType;
            String from = prefs.getString("package", "");
            String payload;
            if (template.isEmpty()) {
                payload = plain ? message : "from=" + encode(from) + "&content=" + encode(message);
            } else if (json) {
                payload = substitute(template, escapeJson(message), escapeJson(title), escapeJson(from));
            } else if (plain) {
                payload = substitute(template, message, title, from);
            } else {
                payload = substitute(template, encode(message), encode(title), encode(from));
            }
            if (method.equals("GET")) {
                target = new URL(url + (url.contains("?") ? "&" : "?") + payload);
            }
            connection = (HttpURLConnection) target.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod(method);
            if (!method.equals("GET")) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", contentType);
            }
            java.util.Iterator<String> keys = headers.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                if (name.equalsIgnoreCase("Host") || name.equalsIgnoreCase("Content-Length")) continue;
                connection.setRequestProperty(name, headers.optString(name));
            }
            if (!method.equals("GET")) {
                byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream out = connection.getOutputStream()) { out.write(bytes); }
            }
            int status = connection.getResponseCode();
            boolean success = status >= 200 && status < 300;
            String expected = prefs.getString("response", "");
            if (success && !expected.isEmpty()) {
                java.io.InputStream stream = connection.getInputStream();
                byte[] response = new byte[4096];
                int count = stream.read(response);
                stream.close();
                success = count >= 0 && new String(response, 0, count, StandardCharsets.UTF_8).contains(expected);
            }
            prefs.edit().putString("last_result", success ? "전송 성공" : "전송 실패: HTTP " + status).apply();
            return success;
        } catch (Exception e) {
            prefs.edit().putString("last_result", "전송 실패: 연결 또는 설정을 확인하세요").apply();
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    private static String escapeJson(String value) {
        String quoted = JSONObject.quote(value);
        return quoted.substring(1, quoted.length() - 1);
    }

    private static String substitute(String template, String message, String title, String from) {
        return template.replace("[msg]", message).replace("[content]", message)
            .replace("[org_content]", message).replace("[title]", title).replace("[from]", from);
    }

    private WebhookClient() {}
}
