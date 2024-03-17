package com.myungwoo.hipeople.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.myungwoo.hipeople.adapter.CommunityListAdapter
import com.myungwoo.hipeople.dao.PostingDAO
import com.myungwoo.hipeople.data.AnonymousPostingData
import com.myungwoo.hipeople.databinding.FragmentCommunityBinding
import com.myungwoo.hipeople.view.activity.AppMainActivity
import com.myungwoo.hipeople.view.activity.InputActivity

class CommunityFragment : Fragment() {

    private lateinit var mainActivity: AppMainActivity
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var postingData: MutableList<AnonymousPostingData>
    private lateinit var adapter: CommunityListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as AppMainActivity
    }

    override fun onResume() {
        super.onResume()
        connectAdapter()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater)

        pictureDataLoading()

        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(mainActivity.applicationContext, InputActivity::class.java)
            startActivity(intent)
        }

        postingData = mutableListOf()
        connectAdapter()
        return binding.root
    }

    private fun pictureDataLoading() {
        val postingDAO = PostingDAO()
        postingDAO.pictureSelect()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postingData.clear()
                postingData.reverse()

                for (data in snapshot.children) {
                    val pictureData = data.getValue(AnonymousPostingData::class.java)
                    if (pictureData != null) {
                        postingData.add(pictureData)
                        adapter.notifyDataSetChanged()
                    }
                    postingData.reverse()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PictureActivity", "${error.message}")
            }
        })
    }

    private fun connectAdapter() {
        adapter = CommunityListAdapter(mainActivity, postingData)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
    }
}