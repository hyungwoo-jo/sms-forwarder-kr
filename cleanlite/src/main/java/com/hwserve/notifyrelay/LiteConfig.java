package com.hwserve.notifyrelay;

import android.content.Context;
import android.content.SharedPreferences;

final class LiteConfig {
    private static final String PREFS = "notify_relay";

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean ready(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs.getBoolean("enabled", false)
            && validPackages(prefs.getString("package", ""))
            && (prefs.getBoolean("webhook_enabled", false) || prefs.getBoolean("telegram_enabled", false));
    }

    static boolean validPackages(String selected) {
        if (selected.trim().isEmpty()) return false;
        for (String item : selected.split(",", -1)) {
            if (!item.trim().matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) return false;
        }
        return true;
    }

    static boolean matchesPackage(String selected, String candidate) {
        for (String item : selected.split(",")) {
            if (item.trim().equals(candidate)) return true;
        }
        return false;
    }

    private LiteConfig() {}
}
