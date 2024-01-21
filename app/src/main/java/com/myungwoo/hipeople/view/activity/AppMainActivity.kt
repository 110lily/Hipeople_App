package com.myungwoo.hipeople.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.myungwoo.hipeople.*
import com.myungwoo.hipeople.view.fragment.ChatFragment
import com.myungwoo.hipeople.view.fragment.CommunityFragment
import com.myungwoo.hipeople.databinding.ActivityAppMainBinding
import com.myungwoo.hipeople.databinding.UsertabButtonBinding
import com.myungwoo.hipeople.view.fragment.CardsteckFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.adapter.CustomAdapter
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.view.fragment.MapFragment

class AppMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppMainBinding
    private lateinit var customAdapter: CustomAdapter
    private lateinit var tabTitleList: MutableList<String>
    private lateinit var userInfoData: UserInfoData
    private var exitFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUserUid = Firebase.auth.currentUser!!.uid
        setSupportActionBar(binding.toolbar)

        Firebase.database.reference.child("User").child("users").orderByChild("uid")
            .equalTo(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        userInfoData = data.getValue(UserInfoData::class.java)!!
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        tabTitleList = mutableListOf("이성", "채팅", "맛집", "커뮤니티")
        customAdapter = CustomAdapter(this)
        customAdapter.addListFragment(CardsteckFragment())
        customAdapter.addListFragment(ChatFragment())
        customAdapter.addListFragment(MapFragment())
        customAdapter.addListFragment(CommunityFragment())
        binding.viewPager2.adapter = customAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.setCustomView(tabCustomView(position))
        }.attach()
        binding.viewPager2.isUserInputEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navi_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("dataList", userInfoData)
                startActivity(intent)
                finish()
            }

            R.id.item_likePeople -> {
                val intent = Intent(this, LikePeopleActivity::class.java)
                intent.putExtra("dataList", userInfoData)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun tabCustomView(position: Int): View {
        val binding = UsertabButtonBinding.inflate(layoutInflater)
        when (position) {
            0 -> binding.ivIcon.setImageResource(R.drawable.baseline_camera_front_24)
            1 -> binding.ivIcon.setImageResource(R.drawable.baseline_chat_24)
            2 -> binding.ivIcon.setImageResource(R.drawable.baseline_map_24)
            3 -> binding.ivIcon.setImageResource(R.drawable.baseline_people_24)
        }
        return binding.root
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (exitFlag) {
            finishAffinity()
        } else {
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료 됩니다.", Toast.LENGTH_SHORT).show()
            exitFlag = true
            runDelayed(1500) {
                exitFlag = false
            }
        }
    }

    private fun runDelayed(millis: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, millis)
    }
}
