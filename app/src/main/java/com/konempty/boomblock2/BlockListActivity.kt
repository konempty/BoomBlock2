package com.konempty.boomblock2

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_block_list.*

class BlockListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_list)
        val blocklist = MyPreference.getStringArrayPref()
        val LIST_MENU = ArrayList<String>(blocklist.values)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, LIST_MENU)
        list.adapter = adapter
        list.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                AlertDialog.Builder(this)
                    .setTitle("차단해제")
                    .setMessage(LIST_MENU[position] + "님을 차단해제하시겠습니까?")
                    .setPositiveButton("예") { dialog, _ ->
                        blocklist.remove(ArrayList<Int>(blocklist.keys)[position])
                        MyPreference.setStringArrayPref(blocklist)
                        adapter.remove(LIST_MENU[position])
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this,"앱을 재시작하면 적용됩니다.",Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("아니요") { dialog, _ ->
                        blocklist.remove(ArrayList<Int>(blocklist.keys)[position])
                        dialog.dismiss()
                    }
                    .create().show()
            }
    }
}
