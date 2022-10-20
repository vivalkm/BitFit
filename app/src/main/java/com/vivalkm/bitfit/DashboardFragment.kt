package com.vivalkm.bitfit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class DashboardFragment(private val itemDao: ItemDao) : Fragment() {
    lateinit var wlItems: MutableList<Item>;
    lateinit var viewDashboard: View
    lateinit var avgTextView: TextView
    lateinit var minTextView: TextView
    lateinit var maxTextView: TextView
    lateinit var countTextView: TextView
    var min = Float.MAX_VALUE
    var max = Float.MIN_VALUE
    var sum = 0F
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Change this statement to store the view in a variable instead of a return statement
        viewDashboard = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return viewDashboard
    }

    companion object {
        fun newInstance(itemDao: ItemDao): LogFragment {
            return LogFragment(itemDao)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wlItems = ArrayList<Item>()
        countTextView = view.findViewById<TextView>(R.id.countNumTextView)
        avgTextView = view.findViewById<TextView>(R.id.avgCaloriesNumTextView)
        minTextView = view.findViewById<TextView>(R.id.minCaloriesNumTextView)
        maxTextView = view.findViewById<TextView>(R.id.maxCaloriesNumTextView)
        reFresh()
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

                    count = 0
                    min = Float.MAX_VALUE
                    max = Float.MIN_VALUE
                    sum = 0F
                    for (item in wlItems) {
                        count++
                        sum += item.number
                        if (min > item.number) {
                            min = item.number
                        }

                        if (max < item.number) {
                            max = item.number
                        }
                    }
                    countTextView.text = count.toString()
                    avgTextView.text = if (count > 0) String.format("%.0f", sum / count) else "0"
                    minTextView.text = if (min == Float.MAX_VALUE) "0" else String.format("%.0f", min)
                    maxTextView.text = if (max == Float.MIN_VALUE) "0" else String.format("%.0f", max)
                }
            }
        }
    }
}