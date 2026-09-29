package com.hwserve.notifyrelay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.Locale;

public final class MainActivity extends Activity {
    private EditText packageInput;
    private EditText urlInput;
    private EditText bodyInput;
    private EditText methodInput;
    private EditText headersInput;
    private EditText responseInput;
    private EditText tokenInput;
    private EditText chatInput;
    private EditText threadInput;
    private Switch enabled;
    private Switch webhookEnabled;
    private Switch telegramEnabled;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences prefs = LiteConfig.prefs(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);
        box.setOnApplyWindowInsetsListener((view, insets) -> {
            int top = Build.VERSION.SDK_INT >= 30 ? insets.getInsets(WindowInsets.Type.systemBars()).top : insets.getSystemWindowInsetTop();
            int bottom = Build.VERSION.SDK_INT >= 30 ? insets.getInsets(WindowInsets.Type.systemBars()).bottom : insets.getSystemWindowInsetBottom();
            view.setPadding(pad, pad + top, pad, pad + bottom);
            return insets;
        });
        scroll.addView(box);
        setContentView(scroll);

        TextView heading = new TextView(this);
        heading.setText("앱 알림 → Telegram · Webhook");
        heading.setTextSize(24);
        box.addView(heading);
        TextView explanation = new TextView(this);
        explanation.setText("선택한 앱의 알림 제목과 본문만 지정한 곳으로 보냅니다. 원본 앱 알림은 지우지 않습니다. 기존 알림 전달 Light와는 별도 앱이므로 설정을 새로 입력해야 합니다. Webhook 서버도 Telegram을 보내면 두 경로를 함께 켜지 마세요.");
        box.addView(explanation);

        packageInput = field(box, "전달할 앱 패키지 (여러 개면 쉼표로 구분)", prefs.getString("package", ""));
        button(box, "최근 알림 앱 패키지 채우기", v -> {
            String value = prefs.getString("recent_package", "");
            if (value.isEmpty()) Toast.makeText(this, "먼저 해당 앱 알림을 한 번 받아 주세요", Toast.LENGTH_LONG).show();
            else {
                packageInput.setText(value);
                Toast.makeText(this, "선택할 앱 패키지를 확인하세요: " + value, Toast.LENGTH_LONG).show();
            }
        });
        webhookEnabled = new Switch(this);
        webhookEnabled.setText("Webhook 전송");
        webhookEnabled.setChecked(prefs.getBoolean("webhook_enabled", false));
        box.addView(webhookEnabled);
        urlInput = field(box, "HTTPS Webhook 주소", prefs.getString("url", ""));
        methodInput = field(box, "HTTP 방식 (POST 또는 GET)", prefs.getString("method", "POST"));
        bodyInput = field(box, "기존 Webhook 본문 템플릿(선택)", prefs.getString("body_template", ""));
        bodyInput.setMinLines(3);
        headersInput = field(box, "요청 헤더 JSON (선택)", prefs.getString("headers", "{}"));
        responseInput = field(box, "성공 응답에 포함될 문구 (선택)", prefs.getString("response", ""));
        telegramEnabled = new Switch(this);
        telegramEnabled.setText("Telegram 직접 전송");
        telegramEnabled.setChecked(prefs.getBoolean("telegram_enabled", false));
        box.addView(telegramEnabled);
        tokenInput = field(box, "Telegram Bot 토큰", prefs.getString("telegram_token", ""));
        tokenInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        chatInput = field(box, "Telegram 채팅 ID", prefs.getString("telegram_chat", ""));
        threadInput = field(box, "Telegram 토픽 ID (선택)", prefs.getString("telegram_thread", ""));
        enabled = new Switch(this);
        enabled.setText("앱 알림 전달 사용");
        enabled.setChecked(prefs.getBoolean("enabled", false));
        box.addView(enabled);
        button(box, "저장", v -> save());
        button(box, "알림 접근 권한 열기", v -> showNotificationAccessDisclosure());
        button(box, "개인정보 처리방침", v -> startActivity(new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/hyungwoo-jo/sms-forwarder-kr/blob/main/docs/PRIVACY-LITE-ko.md"))));
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

    private void showNotificationAccessDisclosure() {
        new AlertDialog.Builder(this)
            .setTitle("알림 접근 안내")
            .setMessage("이 권한을 허용하면 앱은 기기에 표시되는 모든 앱의 알림을 읽을 수 있습니다. 앱은 저장한 패키지 목록과 일치하는 알림의 제목과 본문만 선택한 Telegram 채팅 및 HTTPS Webhook으로 전송합니다. 알림 내용은 기기 밖으로 전달되며, 원본 알림은 삭제하지 않습니다. 앱에서 전달 사용을 끄거나 Android 설정에서 알림 접근을 해제할 수 있습니다.")
            .setNegativeButton("취소", null)
            .setPositiveButton("계속", (dialog, which) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
            .show();
    }

    private void save() {
        String app = packageInput.getText().toString().trim();
        String url = urlInput.getText().toString().trim();
        String method = methodInput.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (enabled.isChecked() && (!LiteConfig.validPackages(app)
            || (!webhookEnabled.isChecked() && !telegramEnabled.isChecked()))) {
            Toast.makeText(this, "앱 패키지와 전송 경로를 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        if (webhookEnabled.isChecked() && !url.startsWith("https://")) {
            Toast.makeText(this, "HTTPS Webhook 주소를 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        if (webhookEnabled.isChecked() && !method.equals("POST") && !method.equals("GET")) {
            Toast.makeText(this, "HTTP 방식은 POST 또는 GET만 지원합니다", Toast.LENGTH_LONG).show();
            return;
        }
        if (telegramEnabled.isChecked() && (!TelegramClient.validToken(tokenInput.getText().toString().trim())
            || chatInput.getText().toString().trim().isEmpty())) {
            Toast.makeText(this, "Telegram Bot 토큰과 채팅 ID를 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        try { if (webhookEnabled.isChecked()) new JSONObject(headersInput.getText().toString()); }
        catch (Exception e) {
            Toast.makeText(this, "요청 헤더 JSON을 확인하세요", Toast.LENGTH_LONG).show();
            return;
        }
        LiteConfig.prefs(this).edit().putString("package", app).putString("url", url)
            .putString("method", method)
            .putString("body_template", bodyInput.getText().toString())
            .putString("headers", headersInput.getText().toString())
            .putString("response", responseInput.getText().toString().trim())
            .putString("telegram_token", tokenInput.getText().toString().trim())
            .putString("telegram_chat", chatInput.getText().toString().trim())
            .putString("telegram_thread", threadInput.getText().toString().trim())
            .putBoolean("enabled", enabled.isChecked())
            .putBoolean("webhook_enabled", webhookEnabled.isChecked())
            .putBoolean("telegram_enabled", telegramEnabled.isChecked()).apply();
        Toast.makeText(this, "저장했습니다", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    @Override protected void onResume() {
        super.onResume();
        if (status != null) updateStatus();
    }

    private void updateStatus() {
        String listeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean access = listeners != null && listeners.contains(new ComponentName(this, WorksNotificationService.class).flattenToString());
        status.setText("알림 접근: " + (access ? "허용" : "필요")
            + "\nWebhook: " + LiteConfig.prefs(this).getString("last_result", "기록 없음")
            + "\nTelegram: " + LiteConfig.prefs(this).getString("telegram_result", "기록 없음"));
    }
}
