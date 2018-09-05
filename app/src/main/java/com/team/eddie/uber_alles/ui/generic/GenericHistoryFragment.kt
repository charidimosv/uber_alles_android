package com.team.eddie.uber_alles.ui.generic

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.team.eddie.uber_alles.adapters.HistoryAdapter
import com.team.eddie.uber_alles.databinding.FragmentGenericHistoryBinding
import com.team.eddie.uber_alles.utils.FirebaseHelper
import com.team.eddie.uber_alles.utils.FirebaseHelper.ARRIVING_TIME
import com.team.eddie.uber_alles.utils.FirebaseHelper.COST
import com.team.eddie.uber_alles.utils.FirebaseHelper.DISTANCE
import com.team.eddie.uber_alles.view.HistoryObject
import java.util.*

class GenericHistoryFragment : Fragment() {

    private lateinit var mAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var userId: String
    private var resultsHistoryList = ArrayList<HistoryObject>()

    private lateinit var mBalance: TextView
    private var balance: Double = 0.0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentGenericHistoryBinding.inflate(inflater, container, false)
        context ?: return binding.root
        setHasOptionsMenu(true)

        mBalance = binding.balance
        recyclerView = binding.historyRecyclerView

        mAdapter = HistoryAdapter(resultsHistoryList, activity!!.applicationContext)
        layoutManager = LinearLayoutManager(activity!!.applicationContext)

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)

        userId = FirebaseHelper.getUserId()
        getUserHistoryIds()

        return binding.root
    }

    private fun getUserHistoryIds() {
        val userHistoryDatabase = FirebaseHelper.getUserHistory(userId)
        userHistoryDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (history in dataSnapshot.children) fetchRideInformation(history.key)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun fetchRideInformation(rideKey: String?) {
        val historyDatabase = FirebaseHelper.getHistoryKey(rideKey!!)
        historyDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val rideId = dataSnapshot.key
                    var timestamp: Long? = 0L
                    var ridePrice: Double? = 0.0

                    if (dataSnapshot.child(ARRIVING_TIME).value != null)
                        timestamp = dataSnapshot.child(ARRIVING_TIME).value.toString().toLong()

                    if (dataSnapshot.child(COST).value != null && dataSnapshot.child("driverPaidOut").value == null) {
                        if (dataSnapshot.child(DISTANCE).value != null) {
                            ridePrice = dataSnapshot.child("price").value!!.toString().toDouble()
                            balance += ridePrice
                            mBalance.text = "balance: " + balance.toString() + " $"
                        }
                    }

                    resultsHistoryList.add(HistoryObject(rideId, getDate(timestamp)))
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getDate(time: Long?): String {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time!! * 1000
        return DateFormat.format("MM-dd-yyyy hh:mm", cal).toString()
    }
}