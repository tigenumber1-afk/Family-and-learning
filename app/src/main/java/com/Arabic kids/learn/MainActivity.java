package com.arabickids.learn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_BACKUP = 4102;
    private WebView web;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale arabic = new Locale("ar", "SA");
                int available = tts.isLanguageAvailable(arabic);
                if (available >= TextToSpeech.LANG_AVAILABLE) {
                    tts.setLanguage(arabic);
                    ttsReady = true;
                } else {
                    int fallback = tts.setLanguage(new Locale("ar"));
                    ttsReady = fallback >= TextToSpeech.LANG_AVAILABLE;
                }
                tts.setSpeechRate(0.82f);
                tts.setPitch(1.05f);
            }
        });

        web = new WebView(this);
        web.setWebViewClient(new WebViewClient());
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.setSafeBrowsingEnabled(true);
        }
        web.addJavascriptInterface(new AndroidBridge(), "AndroidTTS");
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    @Override public void onBackPressed() {
        if (web != null) {
            web.evaluateJavascript("(function(){var bg=document.getElementById('akxBg');if(bg&&getComputedStyle(bg).display!=='none'){bg.style.display='none';if(window.AndroidTTS&&AndroidTTS.stop)AndroidTTS.stop();return 'closed';}return 'none';})()", value -> {
                if (!"\"closed\"".equals(value)) {
                    if (web.canGoBack()) web.goBack(); else MainActivity.super.onBackPressed();
                }
            });
        } else super.onBackPressed();
    }

    @Override protected void onPause() {
        stopSpeech();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (web != null) {
            web.loadUrl("about:blank");
            web.stopLoading();
            web.destroy();
            web = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            ttsReady = false;
        }
        super.onDestroy();
    }

    private void stopSpeech() {
        if (tts != null) tts.stop();
    }

    private void pickBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, PICK_BACKUP);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_BACKUP || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                if (out.length() > 300000) throw new IllegalArgumentException("backup too large");
            }
            String json = out.toString();
            String escaped = org.json.JSONObject.quote(json);
            if (web != null) web.evaluateJavascript("window.restoreBackupFromAndroid && window.restoreBackupFromAndroid(" + escaped + ")", null);
        } catch (Exception e) {
            if (web != null) web.evaluateJavascript("window.restoreBackupError && window.restoreBackupError()", null);
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void speak(String text) {
            if (tts == null || !ttsReady || text == null) return;
            runOnUiThread(() -> tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "arabic-kids"));
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(MainActivity.this::stopSpeech);
        }

        @JavascriptInterface
        public void pickBackup() {
            runOnUiThread(MainActivity.this::pickBackupFile);
        }

        @JavascriptInterface
        public void shareBackup(String text) {
            if (text == null) return;
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - تعلم العربية للأطفال");
                send.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(send, "مشاركة النسخة الاحتياطية"));
            });
        }
    }
}
