package com.vivalkm.bitfit

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogFragment(private val itemDao: ItemDao) : Fragment(), RecyclerViewInterface {
    lateinit var wlItems: MutableList<Item>;
    lateinit var wlStatusImages: MutableList<Int>
    lateinit var adapter: ItemAdapter
    lateinit var viewLog: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wlStatusImages = ArrayList(
            listOf(
                R.drawable.ic_baseline_favorite_border_24,
                R.drawable.ic_baseline_favorite_24
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Change this statement to store the view in a variable instead of a return statement
        viewLog = inflater.inflate(R.layout.fragment_log, container, false)
        return viewLog
    }

    companion object {
        fun newInstance(itemDao: ItemDao): LogFragment {
            return LogFragment(itemDao)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val editTextNumber = view.findViewById<EditText>(R.id.editTextNumber)
        val editTextNote = view.findViewById<EditText>(R.id.editTextNote)
        val submitBtn = view.findViewById<Button>(R.id.submitBtn)
        val itemsRv = view.findViewById<RecyclerView>(R.id.itemsRv)
        wlItems = ArrayList<Item>()
        // Create adapter passing in the list of items
        adapter = ItemAdapter(wlItems, wlStatusImages, this)
        // Attach the adapter to the RecyclerView to populate items
        itemsRv.adapter = adapter
        // Set layout manager to position the items
        itemsRv.layoutManager = LinearLayoutManager(view.context)

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
                lifecycleScope.launch(Dispatchers.IO) {
                    itemDao.delete(
                        wlItems[position].id
                    )
                }
                // reload recycler view
                reFresh()
            }
        }).attachToRecyclerView(itemsRv)

//         click button to add item
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
            lifecycleScope.launch(Dispatchers.IO) {
                itemDao.insert(
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

    // reload data from db and refresh recycler view list
    fun reFresh() {
        lifecycleScope.launch {
            itemDao.getAll().collect { databaseList ->
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

    // click to toggle check status
    override fun onItemClick(position: Int) {
        val item = wlItems[position]
        if (item.isLiked == 1) {
            item.isLiked = 0
        } else {
            item.isLiked = 1
        }
        lifecycleScope.launch(Dispatchers.IO) {
            itemDao.update(
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
                ContextCompat.startActivity(viewLog.context, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + item.name)
                )
                ContextCompat.startActivity(viewLog.context, browserIntent, null)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    viewLog.context,
                    "Not able to look up the item on internet.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}