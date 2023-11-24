package com.myungwoo.datingappkotlinproject.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.myungwoo.datingappkotlinproject.view.fragment.ChatFragment
import com.myungwoo.datingappkotlinproject.view.fragment.CommunityFragment
import com.myungwoo.datingappkotlinproject.view.fragment.CardsteckFragment
import com.myungwoo.datingappkotlinproject.view.fragment.RestFragment

// fragment 연결 어댑터
class CustomAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragmentList = ArrayList<Fragment>()
    override fun getItemCount(): Int{
        return 4
    }
    override fun createFragment(position: Int): Fragment {
        // 각 위치에 맞는 프래그먼트 생성 및 반환
        return when (position) {
            0 -> CardsteckFragment()
            1 -> ChatFragment()
            2 -> RestFragment()
            3 -> CommunityFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    fun addListFragment(fragment: Fragment) {
        this.fragmentList.add(fragment)
    }



}