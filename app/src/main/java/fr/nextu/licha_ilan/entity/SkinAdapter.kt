package fr.nextu.licha_ilan.entity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.nextu.licha_ilan.R

class SkinAdapter(private val skins: Skins, private val onSkinClickListener: OnSkinClickListener) :
    RecyclerView.Adapter<SkinAdapter.SkinViewHolder>() {

    interface OnSkinClickListener {
        fun onSkinClick(skinId: String)
    }

    inner class SkinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView
        val rarityTextView: TextView

        init {
            nameTextView = view.findViewById(R.id.name_skin)
            rarityTextView = view.findViewById(R.id.rarity_skin)

            // Réagit au clic des lignes du recyclerview
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSkinClickListener.onSkinClick(skins.skins[position].id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.skin_item, parent, false)

        return SkinViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
        holder.nameTextView.text = skins.skins[position].name
        holder.rarityTextView.text = skins.skins[position].rarity.name
        holder.rarityTextView.setTextColor(Color.parseColor(skins.skins[position].rarity.color))
    }

    override fun getItemCount() = skins.skins.size
}