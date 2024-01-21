package com.myungwoo.hipeople.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myungwoo.hipeople.view.activity.AppMainActivity
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.FragmentOneBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.adapter.CardStackAdapter
import com.myungwoo.hipeople.firebase.FBRef
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class CardsteckFragment : Fragment() {

    private lateinit var binding: FragmentOneBinding
    private lateinit var mainActivity: AppMainActivity
    private lateinit var cardStackAdapter: CardStackAdapter
    private lateinit var datalist: MutableList<UserInfoData>
    private lateinit var manager: CardStackLayoutManager
    private var currentUserUid = Firebase.auth.currentUser!!.uid

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as AppMainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardStackView = getView()?.findViewById<CardStackView>(R.id.cardStackView)
        manager = CardStackLayoutManager(requireActivity(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}

            override fun onCardSwiped(direction: Direction?) {}

            override fun onCardRewound() {}

            override fun onCardCanceled() {}

            override fun onCardAppeared(view: View?, position: Int) {}

            override fun onCardDisappeared(view: View?, position: Int) {}
        })

        datalist = mutableListOf()
        cardStackAdapter = CardStackAdapter(requireActivity(), datalist)
        cardStackView?.layoutManager = manager
        cardStackView?.adapter = cardStackAdapter
        getUserGender()

    }

    private fun getUserGender() {
        FBRef.genderDef.orderByChild("uid").equalTo(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val user = data.getValue(UserInfoData::class.java)
                        val gender = user!!.gender.toString()
                        getUserDataList(gender)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getUserDataList(gender: String) {
        var genderFlag = ""
        if (gender == "남자") {
            genderFlag = "여자"

        } else if (gender == "여자") {
            genderFlag = "남자"

        } else {
            Log.e("OneFragment - 이성 추천(성별)", "gender 오류")
        }
        FBRef.genderDef.orderByChild("gender").equalTo(genderFlag)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (dataModel in dataSnapshot.children) {
                        val user = dataModel.getValue(UserInfoData::class.java)
                        datalist.add(user!!)
                    }
                    cardStackAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }
}