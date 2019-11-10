package com.konempty.boomblock2

import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    var SharedPreferences = getSharedPreferences("BoomBlock", Context.MODE_PRIVATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webview.loadUrl("https://m.bboom.naver.com/")

        webview.addJavascriptInterface(WebBridge(), "android")
        webview.webViewClient = MyWebViewClient()
    }

    internal inner class WebBridge {
        var list = getStringArrayPref()
        @JavascriptInterface
        fun delete() {
            for (i in list)
                webview.loadUrl("javascript:$(a[href$=\"/profile/home.nhn?userNo=$i\"]).parent().parent().parent().remove();")
        }

        fun addUser(no: Int) {
            list.add(no)
            setStringArrayPref(list)
            delete()
        }

    }

    private fun setStringArrayPref(values: ArrayList<Int>) {
        val editor = SharedPreferences.edit()
        val a = JSONArray()
        for (i in 0 until values.size) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString("list", a.toString())
        } else {
            editor.putString("list", null)
        }
        editor.apply()
    }

    private fun getStringArrayPref(): ArrayList<Int> {
        val json = SharedPreferences.getString("list", null)
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
                if (url.contains("list.nhn")) {

                    webview.loadUrl(
                        "javascript:function(){" +
                                "var addBtns=function(){" +
                                    "\$('#blockbtn').remove();"+
                                    "\$(\".sc_usr\").each(function(){" +
                                    "var userno = \$(this).children('a').attr('href').split('=')[1];" +
                                    "\$(this).append('<input id=\"blockbtn\" type=\"button\" value=\"차단\" onclick=\"window.android.addUser('+userno +')\"/>');" +
                                    "});" +
                                "};"+
                                "var getMorePostList = oPostList.__getMoreList;"+
                                "oPostList.__getMoreList = function(){" +
                                "getMorePostList();" +
                                "window.android.delete();" +
                                "addBtns()"+
                                "};" +

                                "}"
                    )
                } else if (url.contains("get.nhn")) {

                    webview.loadUrl(
                        "javascript:function(){" +
                                "var addBtns=function(){" +
                                "\$('#blockbtn').remove();"+
                                "\$(\".sc_usr\").each(function(){" +
                                "var userno = \$(this).attr('href').split('=')[1];" +
                                "\$(this).append('<input id=\"blockbtn\" type=\"button\" value=\"차단\" onclick=\"window.android.addUser('+userno +')\"/>');" +
                                "});" +
                                "};"+

                                "var getMoreBestCommentList = oPostView.__callbackBestCommentList;"+
                                "oPostView.__callbackBestCommentList = function(){" +
                                "getMoreBestCommentList();" +
                                "window.android.delete();" +
                                "addBtns();"+
                                "}" +

                                "var getMoreCommentList = oPostView.__callbackCommentList;"+
                                "oPostView.__callbackCommentList = function(){" +
                                "getMoreCommentList();" +
                                "window.android.delete();" +
                                "addBtns();"+
                                "}" +
                                "}"
                    )
                }
            }
            return false
        }
    }
}
