package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AppDatabase
import com.example.data.TestProgressRepository
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color(0xFF07090E)
        ) {
          TestifyAppScreen()
        }
      }
    }
  }
}

/**
 * Native JavaScript Bridge exposed to the web view as window.AndroidBridge.
 * Integrates Room Database for saving active test state and session resumption.
 */
class AndroidBridge(
  private val context: Context,
  private val webView: WebView,
  private val repository: TestProgressRepository,
  private val scope: CoroutineScope
) {

  @JavascriptInterface
  fun showToast(message: String) {
    webView.post {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }

  @JavascriptInterface
  fun vibrate(durationMs: Long) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs.coerceAtLeast(15), VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(durationMs.coerceAtLeast(15), VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(durationMs.coerceAtLeast(15))
        }
      }
    } catch (_: Exception) {
      // Gracefully ignore if vibration permission not available
    }
  }

  @JavascriptInterface
  fun shareResult(title: String, scoreSummary: String) {
    webView.post {
      val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n$scoreSummary\n\nPracticed on TESTIFY CBT Exam Platform")
        type = "text/plain"
      }
      val shareIntent = Intent.createChooser(sendIntent, "Share Test Result")
      shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(shareIntent)
    }
  }

  /**
   * Saves active test state (answers, time remaining, question index) to local Room Database.
   */
  @JavascriptInterface
  fun saveTestProgressToRoom(
    testId: String,
    testTitle: String,
    studentId: String,
    totalDurationSeconds: Long,
    remainingSeconds: Long,
    activeQuestionIndex: Int,
    userAnswersJson: String
  ) {
    scope.launch {
      repository.saveProgress(
        testId = testId,
        testTitle = testTitle,
        studentId = studentId,
        totalDurationSeconds = totalDurationSeconds,
        remainingSeconds = remainingSeconds,
        activeQuestionIndex = activeQuestionIndex,
        userAnswersJson = userAnswersJson,
        isCompleted = false
      )
    }
  }

  /**
   * Retrieves saved test progress from Room Database to resume an interrupted session.
   */
  @JavascriptInterface
  fun getTestProgressFromRoom(testId: String): String? {
    return runBlocking {
      val entity = repository.getTestProgress(testId)
      if (entity != null && !entity.isCompleted) {
        """{"testId":"${entity.testId}","remainingSeconds":${entity.remainingSeconds},"activeQuestionIndex":${entity.activeQuestionIndex},"userAnswers":${entity.userAnswersJson}}"""
      } else {
        null
      }
    }
  }

  /**
   * Deletes test state from Room Database upon final test submission.
   */
  @JavascriptInterface
  fun clearTestProgressFromRoom(testId: String) {
    scope.launch {
      repository.deleteProgress(testId)
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TestifyAppScreen() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val database = remember { AppDatabase.getInstance(context) }
  val repository = remember { TestProgressRepository(database.testProgressDao()) }

  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }

  // Intercept Android hardware Back button
  BackHandler(enabled = true) {
    webViewInstance?.let { webView ->
      // Try to navigate backward inside single-page app or history
      webView.evaluateJavascript(
        "(function() { if (window.app && (window.app.activeChapter || window.app.activeSubject || window.app.activeExam || window.app.activeCategory)) { window.app.navigateHome(); return true; } return false; })();"
      ) { result ->
        if (result == "false" || result == null) {
          if (webView.canGoBack()) {
            webView.goBack()
          } else {
            (context as? ComponentActivity)?.finish()
          }
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF07090E))
      .windowInsetsPadding(WindowInsets.statusBars)
  ) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          setBackgroundColor(0xFF07090E.toInt())

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            displayZoomControls = false
            builtInZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
          }

          addJavascriptInterface(
            AndroidBridge(ctx, this, repository, coroutineScope),
            "AndroidBridge"
          )

          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              super.onPageStarted(view, url, favicon)
              canGoBack = view?.canGoBack() == true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              canGoBack = view?.canGoBack() == true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
              return false
            }
          }

          webChromeClient = WebChromeClient()

          // Load bundled responsive single-page examination platform
          loadUrl("file:///android_asset/testify.html")
          webViewInstance = this
        }
      },
      update = { webView ->
        webViewInstance = webView
      }
    )
  }

  DisposableEffect(Unit) {
    onDispose {
      webViewInstance?.destroy()
      webViewInstance = null
    }
  }
}
