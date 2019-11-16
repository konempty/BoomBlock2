package com.konempty.boomblock2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import android.webkit.URLUtil.isValidUrl
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var lastTimeBackPressed = 0L
    val FILECHOOSER_RESULTCODE = 100
    val PERM_RESULTCODE = 200
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    var mUploadMessage: ValueCallback<Uri>? = null

    var list = HashMap<Int, String>()
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0)
                for (i in list.keys)
                    webview.loadUrl("javascript:$('a[href$=\"/profile/home.nhn?userNo=$i\"]').parent().parent().remove();")
            else
                for (i in list.keys)
                    webview.loadUrl("javascript:$('a[href$=\"/profile/home.nhn?userNo=$i\"]').parent().parent().parent().parent().remove();")
        }

    }


    @SuppressLint("AddJavascriptInterface", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyPreference.init(this@MainActivity)
        try {

            webview.settings.javaScriptEnabled = true
            webview.webViewClient = MyWebViewClient()
            webview.webChromeClient = MyChromeClient()
            webview.addJavascriptInterface(WebBridge(), "bboomblock")
            //registerForContextMenu(webview)
            if (Build.VERSION.SDK_INT >= 21) {
                webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webview.setOnLongClickListener {
                val hitTestResult = webview.hitTestResult

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERM_RESULTCODE
                    )
                    false
                } else {

                    when (hitTestResult.type) {
                        WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> AlertDialog.Builder(
                            this@MainActivity
                        )
                            .setMessage("선택한 이미지를 저장하시겠습니까?")
                            .setPositiveButton(
                                "예"
                            ) { dialogInterface, i ->
                                val DownloadImageURL = hitTestResult.extra

                                if (isValidUrl(DownloadImageURL)) {

                                    val mRequest =
                                        DownloadManager.Request(Uri.parse(DownloadImageURL))
                                    mRequest.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        "bboom/" + DownloadImageURL.substring(
                                            DownloadImageURL.lastIndexOf('/') + 1,
                                            DownloadImageURL.length
                                        )
                                    )
                                    mRequest.allowScanningByMediaScanner()
                                    mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    val mDownloadManager =
                                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    mDownloadManager.enqueue(mRequest)

                                    Toast.makeText(
                                        this@MainActivity,
                                        "다운로드완료",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "다운로드실패",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }.setNegativeButton(
                                "아니요"
                            ) { dialogInterface, i -> }.create().show()

                    }
                }
                false
            }
            webview.loadUrl("https://m.bboom.naver.com")


        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        list = MyPreference.getStringArrayPref()
    }

    internal inner class WebBridge {
        @JavascriptInterface
        fun delete(no: Int) {
            handler.sendEmptyMessage(no)
        }

        @JavascriptInterface
        fun addUser(no: Int, name: String, isPost: Int) {
            list[no] = name
            MyPreference.setStringArrayPref(list)
            delete(isPost)
        }

    }


    internal inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                return if (url.contains("m.bboom.naver.com"))
                    false
                else {
                    Toast.makeText(applicationContext, "다른페이지로는 갈수 없습니다.", Toast.LENGTH_LONG).show()
                    true
                }
            }
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {

            if (url != null) {
                // tv.text=url
                webview.loadUrl("javascript:\$(\"<style type='text/css'> .blockbtn{ background-color: red;color:#ffffff; font-weight:bold;border: 0; padding: 0.25rem 0.5rem;} </style>\").appendTo(\"head\");")
                if (url == "https://m.bboom.naver.com/" || url == "http://m.bboom.naver.com/" || url.contains(
                        "list.nhn"
                    )
                ) {

                    webview.loadUrl(
                        "javascript:\$(function(){" +
                                "var addBtns=function(){" +
                                "\$('.blockbtn').remove();" +
                                "\$('.sc_usr').each(function(){" +
                                "bboomblock.delete(1);" +
                                "var userno = \$(this).children('a').attr('href').split('=')[1];" +
                                "var usernm = \$(this).children('a').text().trim();" +
                                "\$(this).append('<input class=\"blockbtn\" type=\"button\" value=\"차단\" onclick=\"bboomblock.addUser('+userno +',\\''+usernm+'\\',1)\"/>');" +
                                "});" +
                                "};" +
                                "oPostList.getMorePostList = oPostList.__getMoreList;" +
                                "oPostList.__getMoreList = function(a,b){" +
                                "oPostList.getMorePostList(a,b);" +
                                "addBtns();" +
                                "};" +
                                "addBtns();" +
                                "});"
                    )
                } else if (url.contains("get.nhn")) {
                    webview.loadUrl(
                        "javascript:\$(function(){ " +
                                "try{" +
                                "if(b)" +
                                "return;" +
                                "}catch{" +
                                "b=true;}" +
                                "var addBtns=function(){" +
                                "\$('.blockbtn').remove();" +
                                "\$('.nick').each(function(){" +
                                "bboomblock.delete(0);" +
                                "var userno = \$(this).attr('href').split('=')[1];" +
                                "var usernm = \$(this).text().trim();" +
                                "\$(this).after('<input class=\"blockbtn\" type=\"button\" value=\"차단\" onclick=\"bboomblock.addUser('+userno +',\\''+usernm+'\\',0)\"/>');" +
                                "});" +
                                "};" +
                                "oPostView.tmp = oPostView.CommentList = oPostView.__callbackCommentList;" +
                                "oPostView.__callbackCommentList = function(a,b){" +
                                "oPostView.CommentList = oPostView.tmp;" +
                                "oPostView.CommentList(a,b);" +
                                "addBtns();" +
                                "};" +
                                "addBtns();" +
                                "});"
                    )
                }
            }
            super.onPageFinished(view, url)
        }
    }

    internal inner class MyChromeClient : WebChromeClient() {
        /*override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            if (!consoleMessage.message().contains("insecure"))
                tv.text =
                    tv.text.toString() + ("\nMyApplication" + consoleMessage.message() + " -- From line "
                            + consoleMessage.lineNumber()) + " of " + consoleMessage.sourceId()
            return super.onConsoleMessage(consoleMessage)
        }*/
//The undocumented magic method override
        //Eclipse will swear at you if you try to put @Override here
        // For Android 3.0+
        fun openFileChooser(uploadMsg: ValueCallback<Uri>) {

            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            startActivityForResult(
                Intent.createChooser(i, "File Chooser"),
                FILECHOOSER_RESULTCODE
            )

        }

        // For Android 3.0+
        fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
            mUploadMessage = uploadMsg as ValueCallback<Uri>
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            startActivityForResult(
                Intent.createChooser(i, "File Browser"),
                FILECHOOSER_RESULTCODE
            )
        }

        //For Android 4.1
        fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            startActivityForResult(
                Intent.createChooser(i, "File Chooser"),
                FILECHOOSER_RESULTCODE
            )

        }

        //For Android 5.0+
        override fun onShowFileChooser(
            webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ): Boolean {
            mFilePathCallback?.onReceiveValue(null)
            mFilePathCallback = filePathCallback

            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            val intentArray: Array<Intent?> = arrayOfNulls(0)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
            return true
        }

    }

    override fun onBackPressed() {
        if (webview.canGoBack())
            webview.goBack()
        else {
            if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
                finish()
                return
            }
            lastTimeBackPressed = System.currentTimeMillis()
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /*override fun onCreateContextMenu(
        contextMenu: ContextMenu,
        view: View,
        contextMenuInfo: ContextMenu.ContextMenuInfo
    ) {
        val webViewHitTestResult = webview.hitTestResult
        Toast.makeText(this, webViewHitTestResult.type, Toast.LENGTH_LONG).show()
        val webViewHitTestResult = webview.hitTestResult

         if (webViewHitTestResult.type == WebView.HitTestResult.IMAGE_TYPE || webViewHitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

             contextMenu.setHeaderTitle("파일다운로드")

             contextMenu.add(0, 1, 0, "다운로드")
                 .setOnMenuItemClickListener {
                     val DownloadImageURL = webViewHitTestResult.extra
                     Toast.makeText(this,DownloadImageURL,Toast.LENGTH_LONG).show()

                     if (isValidUrl(DownloadImageURL)) {

                         val mRequest = DownloadManager.Request(Uri.parse(DownloadImageURL))
                         mRequest.allowScanningByMediaScanner()
                         mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                         val mDownloadManager =
                             getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                         mDownloadManager.enqueue(mRequest)

                         Toast.makeText(
                             this@MainActivity,
                             "다운로드완료",
                             Toast.LENGTH_LONG
                         ).show()
                     } else {
                         Toast.makeText(
                             this@MainActivity,
                             "다운로드실패",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                     false
                 }
         }
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu1 -> startActivity(Intent(this@MainActivity, BlockListActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            mFilePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            )
            mFilePathCallback = null

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this, "다시 다운로드를 시도해주세요.", Toast.LENGTH_LONG).show()

        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_LONG).show()
        }
        return
    }


}
