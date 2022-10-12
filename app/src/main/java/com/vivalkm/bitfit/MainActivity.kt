package com.vivalkm.bitfit

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), RecyclerViewInterface {
    lateinit var wlItems: MutableList<Item>;
    lateinit var wlStatusImages: MutableList<Int>
    lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // animated background
        val constraintLayout = findViewById<ConstraintLayout>(R.id.mainLayout)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2500)
        animationDrawable.setExitFadeDuration(2500)
        animationDrawable.start()

        wlStatusImages = ArrayList(listOf(R.drawable.ic_check_box_blank, R.drawable.ic_check_box))

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextNumber = findViewById<EditText>(R.id.editTextNumber)
        val editTextNote = findViewById<EditText>(R.id.editTextNote)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val itemsRv = findViewById<RecyclerView>(R.id.itemsRv)
        wlItems = ArrayList<Item>()
        // Create adapter passing in the list of items
        adapter = ItemAdapter(wlItems, wlStatusImages, this)
        // Attach the adapter to the RecyclerView to populate items
        itemsRv.adapter = adapter
        // Set layout manager to position the items
        itemsRv.layoutManager = LinearLayoutManager(this)

        // swipe left to remove item
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                val position = viewHolder.adapterPosition
                wlItems.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }).attachToRecyclerView(itemsRv)

        // click button to add item
        submitBtn.setOnClickListener {
            // get data from textEditViews, and create new item
            val name = editTextName.text.toString()
            val number = try {
                editTextNumber.text.toString().toFloat()
            } catch (e: NumberFormatException) {
                0.0F
            }
            val note = editTextNote.text.toString()
            val item = Item(name, number, note, 0)
            // update itemList
            wlItems.add(item)

            // notify adaptor
            adapter.notifyItemInserted(wlItems.size - 1)
            // clean up input areas
            editTextName.text.clear()
            editTextNumber.text.clear()
            editTextNote.text.clear()
        }
    }

    // click to toggle check status
    override fun onItemClick(position: Int) {
        val item = wlItems[position]
        if (item.isDone == 1) {
            item.isDone = 0
        } else {
            item.isDone = 1
        }
        adapter.notifyItemChanged(position)
    }

    // long click to open url in notes
    override fun onItemLongClick(position: Int) {
        val item = wlItems[position]
        if (item.note.isNotEmpty()) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.note))
                ContextCompat.startActivity(this, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + item.note))
                ContextCompat.startActivity(this, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Not able to look up the item on internet.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}