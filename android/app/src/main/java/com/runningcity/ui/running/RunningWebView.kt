package com.runningcity.ui.running

import android.net.http.SslError
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun RunningWebView(
    url: String,
    viewModel: RunningViewModel,
    modifierPadding: PaddingValues,
    extraHeaders: Map<String, String>? = null
): WebView {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    AndroidView(
        modifier = Modifier.padding(modifierPadding),
        factory = {
            webView.apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.userAgentString += " RunningCityApp"

                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false // 확대 버튼 UI 숨기기 (선택)
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                clearCache(true)
                clearHistory()

                // ✅ JS 콘솔 출력
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("WebViewConsole", "🧩 ${consoleMessage?.message()}")
                        return true
                    }
                }

                // ✅ SSL 무시 + 에러 로깅
                webViewClient = object : WebViewClient() {
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        Log.w("WebViewSSL", "⚠️ SSL Error ignored: ${error?.primaryError}")
                        handler?.proceed()
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e("WebViewError", "❌ ${error?.description}")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WebView", "✅ Finished loading: $url")
                    }
                }

                // ✅ React ↔ Android 브릿지 등록
                addJavascriptInterface(WebAppInterface(context, this, viewModel), "Android")

                // ✅ URL 로드
                Log.d("WebViewLoad", "🌍 Loading URL: $url")
                if (extraHeaders != null) loadUrl(url, extraHeaders) else loadUrl(url)
            }
        },
        update = { it.loadUrl(url) }
    )

    return webView
}
