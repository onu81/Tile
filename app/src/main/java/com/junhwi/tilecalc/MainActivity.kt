package com.junhwi.tilecalc

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }
        
        // "Android"라는 이름으로 브릿지 연결
        webView.addJavascriptInterface(WebAppInterface(), "Android")
        
        webView.webViewClient = WebViewClient()
        
        // 2. WebChromeClient 구현 (JS 팝업 해결)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("확인")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                    .setCancelable(false)
                    .show()
                return true
            }
        }

        webView.loadUrl("file:///android_asset/index.html")

        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    /**
     * 자바스크립트 브릿지 인터페이스
     */
    inner class WebAppInterface {

        @JavascriptInterface
        fun saveImage(base64Data: String) {
            try {
                val bitmap = decodeBase64(base64Data)
                val filename = "TileCalc_${System.currentTimeMillis()}.png"
                
                val outputStream: OutputStream?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TileCalculator")
                    }
                    val item = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    outputStream = item?.let { contentResolver.openOutputStream(it) }
                } else {
                    val imagesDir = File(getExternalFilesDir(null), "Images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val image = File(imagesDir, filename)
                    outputStream = FileOutputStream(image)
                }

                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    runOnUiThread { Toast.makeText(this@MainActivity, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }

        @JavascriptInterface
        fun shareImage(base64Data: String) {
            try {
                val bitmap = decodeBase64(base64Data)
                // FileProvider 설정을 위해 외부 캐시 디렉토리 사용
                val cachePath = File(externalCacheDir, "images")
                if (!cachePath.exists()) cachePath.mkdirs()
                
                val file = File(cachePath, "shared_result.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                // file_paths.xml에 정의된 external-cache-path를 통해 접근
                val contentUri = FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", file)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "결과 공유하기"))
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "공유 실패: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }

        private fun decodeBase64(base64Str: String): Bitmap {
            val cleanBase64 = base64Str.substringAfter(",")
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
    }
}
