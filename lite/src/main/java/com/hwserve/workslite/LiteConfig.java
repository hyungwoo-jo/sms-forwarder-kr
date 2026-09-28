package com.hwserve.workslite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

final class LiteConfig {
    private static final String PREFS = "works_webhook_lite";
    private static final String LEGACY_DATABASE = "sms_forwarder.db";

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void importLegacyWebhook(Context context) {
        SharedPreferences prefs = prefs(context);
        if (prefs.getBoolean("legacy_checked", false)) return;
        File file = context.getDatabasePath(LEGACY_DATABASE);
        if (!file.exists()) {
            prefs.edit().putBoolean("legacy_checked", true).apply();
            return;
        }
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
             Cursor rows = db.rawQuery("SELECT id, json_setting FROM Sender WHERE type = 3 AND status = 1", null)) {
            if (rows.getCount() == 1 && rows.moveToFirst()) {
                long senderId = rows.getLong(0);
                JSONObject setting = new JSONObject(rows.getString(1));
                String url = setting.optString("webServer", "");
                String method = setting.optString("method", "POST").toUpperCase(Locale.ROOT);
                String proxyType = setting.optString("proxyType", "DIRECT");
                if (url.startsWith("https://") && (method.equals("POST") || method.equals("GET"))
                    && setting.optString("secret", "").isEmpty() && proxyType.equals("DIRECT")) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("url", url);
                    edit.putString("method", method);
                    edit.putString("body_template", setting.optString("webParams", ""));
                    edit.putString("headers", setting.optJSONObject("headers") == null ? "{}" : setting.optJSONObject("headers").toString());
                    edit.putString("response", setting.optString("response", ""));
                    String oldPackage = findSinglePackage(db, senderId);
                    if (!oldPackage.isEmpty()) edit.putString("package", oldPackage);
                    edit.apply();
                }
            }
            // Multiple old channels are ambiguous; the user selects the destination in the UI.
        } catch (Exception ignored) {
            // Old data stays untouched. Manual configuration is still available.
        } finally {
            prefs.edit().putBoolean("legacy_checked", true).apply();
        }
    }

    private static String findSinglePackage(SQLiteDatabase db, long senderId) {
        String selected = "";
        try (Cursor rules = db.rawQuery(
            "SELECT value, sender_list FROM Rule WHERE type = 'app' AND status = 1 AND filed = 'package_name' AND `check` = 'is'",
            null)) {
            while (rules.moveToNext()) {
                String value = rules.getString(0);
                if (value == null || !value.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) continue;
                boolean linked = false;
                String senderList = rules.getString(1);
                if (senderList == null) continue;
                for (String id : senderList.split(",")) {
                    if (id.trim().equals(Long.toString(senderId))) linked = true;
                }
                if (!linked) continue;
                if (!selected.isEmpty() && !selected.equals(value)) return "";
                selected = value;
            }
        } catch (Exception ignored) { return ""; }
        return selected;
    }

    static boolean ready(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs.getBoolean("enabled", false)
            && !TextUtils.isEmpty(prefs.getString("package", ""))
            && prefs.getString("url", "").startsWith("https://");
    }

    private LiteConfig() {}
}
