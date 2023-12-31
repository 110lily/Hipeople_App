package com.myungwoo.datingappkotlinproject.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.myungwoo.datingappkotlinproject.view.activity.AppMainActivity
import com.myungwoo.datingappkotlinproject.databinding.FragmentCommunityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.myungwoo.datingappkotlinproject.data.AnonyPostingData
import com.myungwoo.datingappkotlinproject.view.activity.InputActivity
import com.myungwoo.datingappkotlinproject.adapter.PictureAdapter
import com.myungwoo.datingappkotlinproject.dao.PostingDAO


class CommunityFragment : Fragment() {
    lateinit var mainActivity: AppMainActivity
    lateinit var binding: FragmentCommunityBinding
    lateinit var postingData: MutableList<AnonyPostingData>
    lateinit var adapter: PictureAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as AppMainActivity
    }

    override fun onResume() {
        super.onResume()
        connentAdapter()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater)

        pictureDataLoading()

        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(mainActivity.applicationContext, InputActivity::class.java)
            startActivity(intent)
        }

        postingData = mutableListOf()
        connentAdapter()
        return binding.root

    }

    // 사진 게시판의 정보 얻기
    private fun pictureDataLoading() {
        val postingDAO = PostingDAO()
        postingDAO.pictureSelect()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postingData.clear()
                postingData.reverse()

                for (data in snapshot.children) {
                    val pictureData = data.getValue(AnonyPostingData::class.java)
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

    private fun connentAdapter() {
        adapter = PictureAdapter(mainActivity, postingData)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
    }
}