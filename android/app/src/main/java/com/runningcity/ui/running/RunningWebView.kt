package com.runningcity.ui.running

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 🌐 RunningWebView
 * ────────────────────────────────────────────────
 * - React 웹앱을 Compose 내부에서 렌더링
 * - WebAppInterface 연결 (React ↔ Kotlin)
 * - ngrok 보안 경고 자동 우회
 * ────────────────────────────────────────────────
 */
@Composable
fun RunningWebView(
    url: String,
    viewModel: RunningViewModel,
    modifierPadding: PaddingValues,
    extraHeaders: Map<String, String>? = null // ✅ 추가
): WebView {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    AndroidView(
        modifier = Modifier.padding(modifierPadding),
        factory = {
            webView.apply {
                // ⚙️ WebView 설정
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                // 🔗 React ↔ Android 통신 브릿지 등록
                addJavascriptInterface(
                    WebAppInterface(context, this, viewModel),
                    "Android"
                )

                // 🌍 React 서버 로드 (ngrok 헤더 포함)
                if (url.isNotEmpty()) {
                    if (extraHeaders != null) {
                        loadUrl(url, extraHeaders)
                    } else {
                        loadUrl(url)
                    }
                }
            }
        },
        update = {
            it.evaluateJavascript("console.log('🔁 WebView Updated');", null)
        }
    )

    return webView
}


/*
//React 쪽 대응 코드 예시
// Android → React
window.receiveFromAndroid = (data) => {
  const run = typeof data === "string" ? JSON.parse(data) : data;
  console.log("📡 from Android:", run);
};

// React → Android
function startRun() {
  window.Android.startRunning();
}

function stopRun() {
  window.Android.stopRunning();
}
*/