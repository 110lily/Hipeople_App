package com.myungwoo.hipeople.view.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myungwoo.hipeople.adapter.RecyclerChatRoomsAdapter
import com.myungwoo.hipeople.databinding.FragmentChatBinding
import com.myungwoo.hipeople.view.activity.AppMainActivity

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var appMainActivity: AppMainActivity
    private lateinit var recylcerChatroom: RecyclerView
    private lateinit var adapter: RecyclerChatRoomsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appMainActivity = context as AppMainActivity
    }

    override fun onResume() {
        super.onResume()
        setupRecycler()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater)
        initializeView()
        setupRecycler()
        return binding.root
    }

    private fun initializeView() {
        try {
            recylcerChatroom = binding.recyclerChatroom
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(appMainActivity, "화면 초기화 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        adapter = RecyclerChatRoomsAdapter(appMainActivity)
        recylcerChatroom.adapter = adapter
        recylcerChatroom.layoutManager = LinearLayoutManager(appMainActivity)
    }
}