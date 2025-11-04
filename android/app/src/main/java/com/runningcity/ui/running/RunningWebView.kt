package com.runningcity.ui.running

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.padding

/**
 * 🌐 RunningWebView
 * ────────────────────────────────────────────────
 * - React 웹 페이지를 WebView로 띄움
 * - WebAppInterface 연결 (React ↔ Kotlin)
 * - Compose에서 사용 가능
 * ────────────────────────────────────────────────
 */
@Composable //이 함수는 화면(UI)을 그릴 수 있는 함수라는 어노테이션. 이렇게 만든 함수를 다른 Composable 안에서 조립할 수 있음
fun RunningWebView(
    url: String,
    viewModel: RunningViewModel,
    modifierPadding: PaddingValues
) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.padding(modifierPadding),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                // ✅ 브릿지 연결
                val bridge = WebAppInterface(context, this, viewModel)
                addJavascriptInterface(bridge, "Android")

                // ✅ React 서버 로드
                loadUrl(url)
            }
        },
        update = { webView ->
            // 화면 회전 등에서 재생성 시 유지할 로직 필요시 여기에
        }
    )
}
