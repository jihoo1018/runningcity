package com.runningcity.ui.running

import android.net.http.SslError
import android.util.Log
import android.view.ViewGroup
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
    modifier: Modifier = Modifier,
    extraHeaders: Map<String, String>? = null
): WebView {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    AndroidView(
        modifier = modifier,
        factory = {
            webView.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    allowFileAccess = true
                    allowContentAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    
                    // ✅ CSS 및 리소스 로딩을 위한 중요 설정
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    
                    // ✅ 리소스 로딩 최적화
                    blockNetworkImage = false
                    blockNetworkLoads = false
                    loadsImagesAutomatically = true
                    
                    // ✅ User Agent 설정
                    userAgentString += " RunningCityApp"

                    // HMR 등 타이밍 이슈 완화
                    setSupportMultipleWindows(false)
                }
                CookieManager.getInstance().setAcceptCookie(true)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

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

                // ✅ SSL 무시 + 에러 로깅 + 리소스 로딩 추적
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
                        val url = request?.url?.toString() ?: "unknown"
                        val errorCode = error?.errorCode ?: -1
                        val description = error?.description ?: "unknown error"
                        Log.e("WebViewError", "❌ Failed to load: $url | Code: $errorCode | $description")

                        // CSS 파일 로딩 실패 시 특별 로깅
                        if (url.contains(".css") || url.contains("styles")) {
                            Log.e("WebViewCSS", "🚨 CSS 파일 로딩 실패: $url")
                        }
                    }
                    
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        val url = request?.url?.toString() ?: "unknown"
                        val statusCode = errorResponse?.statusCode ?: -1
                        Log.w("WebViewHTTP", "⚠️ HTTP Error: $url | Status: $statusCode")
                    }
                    
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: ""
                        // CSS 파일 로딩 추적
                        if (url.contains(".css") || url.contains("styles")) {
                            Log.d("WebViewCSS", "📦 CSS 파일 로딩 시도: $url")
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WebView", "✅ Finished loading: $url")
                        
                        // 페이지 로드 후 CSS가 제대로 적용되었는지 확인
                        view?.evaluateJavascript("""
                            (function() {
                                var styles = document.querySelectorAll('link[rel="stylesheet"], style');
                                console.log('📦 Loaded stylesheets: ' + styles.length);
                                for (var i = 0; i < styles.length; i++) {
                                    console.log('  - ' + (styles[i].href || styles[i].innerHTML.substring(0, 50)));
                                }
                            })();
                        """.trimIndent(), null)
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
