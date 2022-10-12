package com.vivalkm.bitfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private val wlItems: List<Item>,
    private val wlImages: MutableList<Int>,
    private val recyclerViewInterface: RecyclerViewInterface
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Create member variables for any view that will be set
        // as you render a row.
        var statusImageView: ImageView = itemView.findViewById(R.id.statusImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
        val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val itemView = inflater.inflate(R.layout.item, parent, false)
        val viewHolder = ViewHolder(itemView)

        itemView.setOnClickListener(View.OnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                recyclerViewInterface.onItemClick(position)
            }
        })

        itemView.setOnLongClickListener(View.OnLongClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                recyclerViewInterface.onItemLongClick(position)
            }
            true
        })

        // Return a new holder instance
        return viewHolder
    }

    // assigning values to the views we created in the RecyclerView layout file (item.xml)
    // based on the position of the recycler view
    override fun onBindViewHolder(viewHolder: ItemAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val wlItem: Item = wlItems.get(position)
        // Set item views based on your views and data model
        viewHolder.nameTextView.text = wlItem.name
        viewHolder.numberTextView.text = wlItem.number.toString()
        viewHolder.noteTextView.text = wlItem.note
        viewHolder.statusImageView.setImageResource(wlImages[wlItem.isLiked])
    }

    override fun getItemCount(): Int {
        return wlItems.size
    }
}