package fr.nextu.licha_ilan.entity

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.nextu.licha_ilan.R

class RaritySpinnerListAdapter(
    context: Context,
    items: List<Rarity>
) : ArrayAdapter<Rarity>(context, R.layout.rarity_spinner_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.rarity_spinner_item, parent, false)
        val item = getItem(position)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.text = item?.name
        textView.setTextColor(Color.parseColor(item?.color))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}