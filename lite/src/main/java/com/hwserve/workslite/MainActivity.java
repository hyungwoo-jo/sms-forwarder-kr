package com.hwserve.workslite;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import cn.ppps.forwarder.service.NotificationService;
import org.json.JSONObject;
import java.util.Locale;

public final class MainActivity extends Activity {
    private EditText packageInput;
    private EditText urlInput;
    private EditText bodyInput;
    private EditText methodInput;
    private EditText headersInput;
    private EditText responseInput;
    private Switch enabled;
    private Switch hideSource;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LiteConfig.importLegacyWebhook(this);
        SharedPreferences prefs = LiteConfig.prefs(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);
        scroll.addView(box);
        setContentView(scroll);

        TextView heading = new TextView(this);
        heading.setText("WORKS → Webhook");
        heading.setTextSize(24);
        box.addView(heading);
        TextView explanation = new TextView(this);
        explanation.setText("선택한 앱의 알림 제목과 본문만 지정한 HTTPS 주소로 보냅니다. Telegram 알림은 서버 설정에서 관리하세요.");
        box.addView(explanation);

        packageInput = field(box, "WORKS 앱 패키지", prefs.getString("package", ""));
        button(box, "최근 알림 앱 패키지 채우기", v -> {
            String value = prefs.getString("recent_package", "");
            if (value.isEmpty()) Toast.makeText(this, "먼저 WORKS 알림을 한 번 받아 주세요", Toast.LENGTH_LONG).show();
            else {
                packageInput.setText(value);
                Toast.makeText(this, "선택한 패키지가 WORKS인지 확인하세요: " + value, Toast.LENGTH_LONG).show();
            }
        });
        urlInput = field(box, "HTTPS Webhook 주소", prefs.getString("url", ""));
        methodInput = field(box, "HTTP 방식 (POST 또는 GET)", prefs.getString("method", "POST"));
        bodyInput = field(box, "기존 Webhook 본문 템플릿(선택)", prefs.getString("body_template", ""));
        bodyInput.setMinLines(3);
        headersInput = field(box, "요청 헤더 JSON (선택)", prefs.getString("headers", "{}"));
        responseInput = field(box, "성공 응답에 포함될 문구 (선택)", prefs.getString("response", ""));
        enabled = new Switch(this);
        enabled.setText("WORKS 알림 전달 사용");
        enabled.setChecked(prefs.getBoolean("enabled", false));
        box.addView(enabled);
        hideSource = new Switch(this);
        hideSource.setText("Webhook 성공 후 폰의 원본 WORKS 알림 지우기");
        hideSource.setChecked(prefs.getBoolean("hide_source", false));
        box.addView(hideSource);
        TextView hideNote = new TextView(this);
        hideNote.setText("켜면 WORKS 알림 기록도 사라집니다. 서버에서 Telegram을 못 보내면 알림을 놓칠 수 있어 기본값은 꺼짐입니다.");
        box.addView(hideNote);
        button(box, "저장", v -> save());
        button(box, "알림 접근 권한 열기", v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        status = new TextView(this);
        box.addView(status);
        updateStatus();
    }

    private EditText field(LinearLayout box, String hint, String value) {
        TextView label = new TextView(this);
        label.setText(hint);
        box.addView(label);
        EditText input = new EditText(this);
        input.setSingleLine(!hint.contains("템플릿"));
        input.setText(value);
        box.addView(input);
        return input;
    }

    private Button button(LinearLayout box, String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(listener);
        box.addView(button);
        return button;
    }

    private void save() {
        String app = packageInput.getText().toString().trim();
        String url = urlInput.getText().toString().trim();
        String method = methodInput.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (enabled.isChecked() && (app.isEmpty() || !url.startsWith("https://"))) {
            Toast.makeText(this, "앱 패키지와 HTTPS Webhook 주소를 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        if (!method.equals("POST") && !method.equals("GET")) {
            Toast.makeText(this, "HTTP 방식은 POST 또는 GET만 지원합니다", Toast.LENGTH_LONG).show();
            return;
        }
        try { new JSONObject(headersInput.getText().toString()); }
        catch (Exception e) {
            Toast.makeText(this, "요청 헤더 JSON을 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        LiteConfig.prefs(this).edit().putString("package", app).putString("url", url)
            .putString("method", method)
            .putString("body_template", bodyInput.getText().toString())
            .putString("headers", headersInput.getText().toString())
            .putString("response", responseInput.getText().toString().trim())
            .putBoolean("enabled", enabled.isChecked())
            .putBoolean("hide_source", hideSource.isChecked()).apply();
        Toast.makeText(this, "저장했습니다", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    @Override protected void onResume() {
        super.onResume();
        if (status != null) updateStatus();
    }

    private void updateStatus() {
        String listeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean access = listeners != null && listeners.contains(new ComponentName(this, NotificationService.class).flattenToString());
        status.setText("알림 접근: " + (access ? "허용" : "필요") + "\n마지막 전송: "
            + LiteConfig.prefs(this).getString("last_result", "기록 없음"));
    }
}
