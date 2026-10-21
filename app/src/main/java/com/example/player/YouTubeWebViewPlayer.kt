package com.example.player

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class YouTubeBridge(
    private val onReadyCallback: (Float) -> Unit,
    private val onStateChangeCallback: (Int, Float, Float) -> Unit,
    private val onTimeUpdateCallback: (Float, Float) -> Unit,
    private val onErrorCallback: (Int) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onReady(duration: Float) {
        mainHandler.post { onReadyCallback(duration) }
    }

    @JavascriptInterface
    fun onStateChange(state: Int, currentTime: Float, duration: Float) {
        mainHandler.post { onStateChangeCallback(state, currentTime, duration) }
    }

    @JavascriptInterface
    fun onTimeUpdate(currentTime: Float, duration: Float) {
        mainHandler.post { onTimeUpdateCallback(currentTime, duration) }
    }

    @JavascriptInterface
    fun onError(errorCode: Int) {
        mainHandler.post { onErrorCallback(errorCode) }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWebViewPlayer(
    videoId: String,
    startPositionSeconds: Int,
    modifier: Modifier = Modifier,
    onReady: (durationSeconds: Int) -> Unit = {},
    onPositionUpdate: (positionSeconds: Int, durationSeconds: Int) -> Unit = { _, _ -> },
    onVideoEnded: () -> Unit = {},
    onPlayerError: (errorCode: Int) -> Unit = {}
) {
    val context = LocalContext.current

    val bridge = remember(videoId) {
        YouTubeBridge(
            onReadyCallback = { duration ->
                onReady(duration.toInt())
            },
            onStateChangeCallback = { state, current, duration ->
                val playerState = YTPlayerState.fromCode(state)
                onPositionUpdate(current.toInt(), duration.toInt())
                if (playerState == YTPlayerState.ENDED) {
                    onVideoEnded()
                }
            },
            onTimeUpdateCallback = { current, duration ->
                onPositionUpdate(current.toInt(), duration.toInt())
            },
            onErrorCallback = { errorCode ->
                onPlayerError(errorCode)
            }
        )
    }

    val webView = remember(videoId) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_DEFAULT
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()

            addJavascriptInterface(bridge, "AndroidBridge")

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                  <style>
                    * { margin:0; padding:0; box-sizing:border-box; background-color:#000000; }
                    html, body { width:100%; height:100%; overflow:hidden; }
                    #player { width:100%; height:100%; }
                  </style>
                </head>
                <body>
                  <div id="player"></div>
                  <script>
                    var tag = document.createElement('script');
                    tag.src = "https://www.youtube.com/iframe_api";
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                    var player;
                    var startSec = $startPositionSeconds;

                    function onYouTubeIframeAPIReady() {
                      player = new YT.Player('player', {
                        videoId: '$videoId',
                        playerVars: {
                          'playsinline': 1,
                          'autoplay': 1,
                          'controls': 1,
                          'rel': 0,
                          'modestbranding': 1,
                          'fs': 1,
                          'start': startSec
                        },
                        events: {
                          'onReady': onPlayerReady,
                          'onStateChange': onPlayerStateChange,
                          'onError': onPlayerError
                        }
                      });
                    }

                    var timer = null;

                    function onPlayerReady(event) {
                      if (startSec > 0) {
                        try { player.seekTo(startSec, true); } catch(e){}
                      }
                      var dur = (player && player.getDuration) ? player.getDuration() : 0;
                      if (window.AndroidBridge) {
                        AndroidBridge.onReady(dur);
                      }
                    }

                    function onPlayerStateChange(event) {
                      var state = event.data;
                      var cur = (player && player.getCurrentTime) ? player.getCurrentTime() : 0;
                      var dur = (player && player.getDuration) ? player.getDuration() : 0;
                      if (window.AndroidBridge) {
                        AndroidBridge.onStateChange(state, cur, dur);
                      }
                      if (state == 1) { // PLAYING
                        if (!timer) {
                          timer = setInterval(function() {
                            if (player && player.getCurrentTime) {
                              AndroidBridge.onTimeUpdate(player.getCurrentTime(), player.getDuration() || 0);
                            }
                          }, 3000);
                        }
                      } else {
                        if (timer) {
                          clearInterval(timer);
                          timer = null;
                        }
                      }
                    }

                    function onPlayerError(event) {
                      if (window.AndroidBridge) {
                        AndroidBridge.onError(event.data);
                      }
                    }
                  </script>
                </body>
                </html>
            """.trimIndent()

            loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
        }
    }

    DisposableEffect(videoId) {
        onDispose {
            try {
                webView.loadUrl("about:blank")
                webView.stopLoading()
                webView.destroy()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
