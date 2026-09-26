package cn.ppps.forwarder.widget

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import cn.ppps.forwarder.adapter.spinner.AppListAdapterItem

object AppPickerDialog {
    fun show(context: Context, apps: List<AppListAdapterItem>, select: (AppListAdapterItem) -> Unit) {
        val pad = (16 * context.resources.displayMetrics.density).toInt()
        val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, 0, pad, pad) }
        val search = EditText(context).apply { hint = "앱 이름 또는 패키지명 검색"; maxLines = 1 }
        val list = ListView(context)
        val adapter = object : ArrayAdapter<AppListAdapterItem>(context, android.R.layout.simple_list_item_2, android.R.id.text1, apps.toMutableList()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val row = super.getView(position, convertView, parent)
                val item = getItem(position)!!
                row.findViewById<TextView>(android.R.id.text1).apply { text = item.name; textSize = 16f }
                row.findViewById<TextView>(android.R.id.text2).apply { text = item.packageName; textSize = 12f }
                return row
            }
        }
        list.adapter = adapter
        content.addView(search)
        content.addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (320 * context.resources.displayMetrics.density).toInt()))
        val dialog = AlertDialog.Builder(context).setTitle("전달할 앱 선택").setView(content).setNegativeButton("취소", null).create()
        list.setOnItemClickListener { _, _, position, _ -> adapter.getItem(position)?.let { select(it); dialog.dismiss() } }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                adapter.clear(); adapter.addAll(apps.filter { it.name.contains(query, true) || it.packageName.orEmpty().contains(query, true) }); adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) { }
        })
        dialog.show()
    }
}
