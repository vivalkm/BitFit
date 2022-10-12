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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

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

        wlStatusImages = ArrayList(listOf(R.drawable.ic_baseline_favorite_border_24, R.drawable.ic_baseline_favorite_24))

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

        reFresh()

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

                // delete item from database
                lifecycleScope.launch(IO) {
                    (application as BitFitApplication).db.itemDao().delete(
                        wlItems[position].id
                    )
                }
                // reload recycler view
                reFresh()
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
            val notes = editTextNote.text.toString()

            // We need to update any ItemDao interaction to run on another thread
            // Since we are using Room and writing to files, we want to use the IO Dispatcher
            lifecycleScope.launch(IO) {
                (application as BitFitApplication).db.itemDao().insert(
                    ItemEntity(
                        name = name,
                        amount = number,
                        notes = notes,
                        isLiked = 0
                    )
                )
            }
            reFresh()

            // clean up input areas
            editTextName.text.clear()
            editTextNumber.text.clear()
            editTextNote.text.clear()
        }
    }

    // click to toggle check status
    override fun onItemClick(position: Int) {
        val item = wlItems[position]
        if (item.isLiked == 1) {
            item.isLiked = 0
        } else {
            item.isLiked = 1
        }
        lifecycleScope.launch(IO) {
            (application as BitFitApplication).db.itemDao().update(
                item.id, item.isLiked
            )
        }
        reFresh()
    }

    // long click to open url in notes, if not valid link then search for the name of item on google
    override fun onItemLongClick(position: Int) {
        val item = wlItems[position]
        if (item.note.isNotEmpty()) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.note))
                ContextCompat.startActivity(this, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + item.name)
                )
                ContextCompat.startActivity(this, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Not able to look up the item on internet.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // reload data from db and refresh recycler view list
    fun reFresh() {
        lifecycleScope.launch {
            (application as BitFitApplication).db.itemDao().getAll().collect { databaseList ->
                databaseList.map { entity ->
                    Item(
                        entity.id,
                        entity.name,
                        entity.amount,
                        entity.notes,
                        entity.isLiked
                    )
                }.also { mappedList ->
                    wlItems.clear()
                    wlItems.addAll(mappedList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}