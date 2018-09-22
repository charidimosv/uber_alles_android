package com.team.eddie.uber_alles.ui.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.team.eddie.uber_alles.adapters.RequestAdapter
import com.team.eddie.uber_alles.databinding.FragmentGenericRequestListBinding
import com.team.eddie.uber_alles.utils.firebase.FirebaseHelper
import com.team.eddie.uber_alles.utils.firebase.Request
import java.util.*

class GenericRequestListFragment : Fragment() {

    private lateinit var binding: FragmentGenericRequestListBinding

    private lateinit var mAdapter: RequestAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var userId: String
    private var resultsRequestList = ArrayList<Request>()
    private var resultsRequestIdList = ArrayList<String>()

    private lateinit var mBalanceValue: TextView
    private lateinit var mTotalTripsValue: TextView

    private var balanceValue: Double = 0.0
    private var totalTripsValue: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGenericRequestListBinding.inflate(inflater, container, false)
        context ?: return binding.root
        setHasOptionsMenu(true)

        mAdapter = RequestAdapter()
        mAdapter.submitList(resultsRequestList)

        mBalanceValue = binding.balanceValue
        mTotalTripsValue = binding.totalTripsValue

        recyclerView = binding.requestRecyclerView
        recyclerView.adapter = mAdapter

        mBalanceValue.text = "0€"
        mTotalTripsValue.text = "0"

        userId = FirebaseHelper.getUserId()
        getUserHistoryIds()

        return binding.root
    }

    private fun getUserHistoryIds() {
        val userHistoryDatabase = FirebaseHelper.getUserRequestList(userId)
        userHistoryDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0)
                    for (request in dataSnapshot.children) fetchRideInformation(request.key)
                else {
                    binding.layout.visibility = View.GONE
                    binding.noRequest.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun fetchRideInformation(requestId: String?) {
        val requestDatabase = FirebaseHelper.getRequestKey(requestId!!)
        requestDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val request = dataSnapshot.getValue(Request::class.java)
                    if (request != null && !resultsRequestIdList.contains(request.requestId)) {
                        balanceValue += request.amount
                        totalTripsValue++

                        mBalanceValue.text = balanceValue.toString() + "€"
                        mTotalTripsValue.text = totalTripsValue.toString()

                        resultsRequestIdList.add(request.requestId)
                        resultsRequestList.add(request)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}