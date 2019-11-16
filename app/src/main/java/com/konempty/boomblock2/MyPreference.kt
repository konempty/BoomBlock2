package com.konempty.boomblock2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyPreference {
    companion object {
        lateinit var SharedPreferences: SharedPreferences

        fun init(context: Context) {

            SharedPreferences = context.getSharedPreferences("BoomBlock", Context.MODE_PRIVATE)
        }

        fun setStringArrayPref(values: HashMap<Int, String>) {
            val editor = SharedPreferences.edit()
            val gson = Gson()
            val hashMapString = gson.toJson(values)

            if (values.isNotEmpty()) {
                editor?.putString("list", hashMapString)
            } else {
                editor?.putString("list", null)
            }
            editor?.apply()
        }

        fun getStringArrayPref(): HashMap<Int, String> {
            val json = SharedPreferences.getString("list", null)
            val lists: HashMap<Int, String>
            lists = if (json != null) {
                val gson = Gson()
                gson.fromJson(json, object : TypeToken<HashMap<Int, String>>() {}.type)
            } else
                HashMap()
            return lists
        }
    }
}