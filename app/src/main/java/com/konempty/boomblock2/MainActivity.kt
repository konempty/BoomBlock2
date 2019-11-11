package com.konempty.boomblock2

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    var SharedPreferences: SharedPreferences?=null

    var list = ArrayList<Int>()
    @SuppressLint("AddJavascriptInterface", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {

            SharedPreferences=applicationContext.getSharedPreferences("BoomBlock", Context.MODE_PRIVATE)
            list = getStringArrayPref()
            webview.settings.javaScriptEnabled=true
            webview.addJavascriptInterface(WebBridge(), "android")
            webview.webViewClient = MyWebViewClient()
            webview.webChromeClient=MyChromeClient()
            webview.loadUrl("https://m.bboom.naver.com/")

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    internal inner class WebBridge {
        @JavascriptInterface
        fun delete() {
            for (i in list)
                webview.loadUrl("javascript:$(a[href$=\"/profile/home.nhn?userNo=$i\"]).parent().parent().parent().remove();")
        }

        @JavascriptInterface
        fun addUser(no: Int) {
            list.add(no)
            setStringArrayPref(list)
            delete()
        }

    }

    private fun setStringArrayPref(values: ArrayList<Int>) {
        val editor = SharedPreferences?.edit()
        val a = JSONArray()
        for (i in 0 until values.size) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor?.putString("list", a.toString())
        } else {
            editor?.putString("list", null)
        }
        editor?.apply()
    }

    private fun getStringArrayPref(): ArrayList<Int> {
        val json = SharedPreferences?.getString("list", null)
        val lists = ArrayList<Int>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optInt(i)
                    lists.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return lists
    }

    internal inner class MyWebViewClient : WebViewClient() {
        @Override
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                tv.text=url
                if (url.contains("list.nhn")) {

                    webview.loadUrl(
                        "javascript:\$(function(){" +
                                    "var addBtns=function(){" +
                                       "\$('#blockbtn').remove();" +
                                       "\$(\".sc_usr\").each(function(){" +
                                            "var userno = \$(this).children('a').attr('href').split('=')[1];" +
                                            "\$(this).append('<input id=\"blockbtn\" type=\"button\" value=\"차단\" onclick=\"window.android.addUser('+userno +')\"/>');" +
                                        "});" +
                                    "};" +
                                    "var getMorePostList = oPostList.__getMoreList;" +
                                    "oPostList.__getMoreList = function(){" +
                                        "getMorePostList();" +
                                        "window.android.delete();" +
                                        "addBtns();" +
                                    "};" +

                                "})"
                    )
                } else if (url.contains("get.nhn")) {

                    webview.loadUrl(
                        "javascript:$(function(){" +
                                "var addBtns=function(){" +
                                "$('#blockbtn').remove();" +
                                "$('.nick').each(function(){" +
                                "var userno = $(this).attr('href').split('=')[1];" +
                                "$(this).append('<input id='blockbtn' type='button' value='차단' onclick='window.android.addUser('+userno +')'/>');" +
                                "});" +
                                "};" +

                                "oPostView.BestCommentList = oPostView.__callbackBestCommentList;" +
                                "oPostView.__callbackBestCommentList = function(a,b){" +
                                "oPostView.BestCommentList(a,b);" +
                                "window.android.delete();" +
                                "addBtns();" +
                                "}" +

                                "oPostView.CommentList = oPostView.__callbackCommentList;" +
                                "oPostView.__callbackCommentList = function(a,b){" +
                                "oPostView.CommentList(a,b);" +
                                "window.android.delete();" +
                                "addBtns();" +
                                "}" +
                                "})"
                    )
                }
            }
            return false
        }

    }

    internal inner class MyChromeClient : WebChromeClient(){
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            tv.text=("MyApplication"+consoleMessage.message() + " -- From line "
                    + consoleMessage.lineNumber()) + " of "+ consoleMessage.sourceId()
             return super.onConsoleMessage(consoleMessage)
        }
    }
}
