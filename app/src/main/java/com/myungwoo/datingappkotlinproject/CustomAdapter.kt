package com.myungwoo.datingappkotlinproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.myungwoo.datingappkotlinproject.chat.ChatFragment
import com.myungwoo.datingappkotlinproject.community.CommunityFragment
import com.myungwoo.datingappkotlinproject.onefragment.OneFragment

// fragment 연결 어댑터
class CustomAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {
    val fragmentList = ArrayList<Fragment>()
    override fun getItemCount(): Int{
        return 4
    }
    override fun createFragment(position: Int): Fragment {
        // 각 위치에 맞는 프래그먼트 생성 및 반환
        return when (position) {
            0 -> OneFragment()
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