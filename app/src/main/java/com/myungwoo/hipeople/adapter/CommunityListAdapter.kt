package com.myungwoo.hipeople.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.dao.PostingDAO
import com.myungwoo.hipeople.data.AnonymousPostingData
import com.myungwoo.hipeople.databinding.PictureLayoutBinding
import com.myungwoo.hipeople.view.activity.PostingActivity
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class CommunityListAdapter(val context: Context, val pictureList: MutableList<AnonymousPostingData>) :
    RecyclerView.Adapter<CommunityListAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = PictureLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount() = pictureList.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val binding = holder.binding
        val pictureData = pictureList.get(position)
        val database = Firebase.database.reference.child("posting").child(pictureData.key)
        val currentUserUid = Firebase.auth.currentUser!!.uid

        database.child("likePeople").child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value == true) {
                        binding.ivLike.setImageResource(R.drawable.favorite_pink)
                    } else {
                        binding.ivLike.setImageResource(R.drawable.baseline_favorite_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.tvAuthor.text = pictureData.nickName
        binding.tvTitle.text = pictureData.title
        binding.tvContent.text = pictureData.content
        binding.tvLike.text = pictureData.tvLike.toString()
        binding.tvEye.text = pictureData.tvHits.toString()
        binding.tvDate.text = getLastMessageTimeString(pictureData.tvDate)

        val commentCountRef =
            Firebase.database.reference.child("posting").child(pictureData.key).child("comment")
        commentCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                val commentCountMap = mutableMapOf<String, Any>()
                commentCountMap[pictureData.key] = count
                binding.tvComment.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PictureAdapter", "Failed to read comment count.", error.toException())
            }
        })
        val postingDAO = PostingDAO()
        val pictureRef = postingDAO.storage!!.reference.child("images/${pictureData.key}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(context).load(it.result).into(binding.ivPicture3)
            }
        }

        binding.root.setOnClickListener {
            val tvHits = mutableMapOf<String, Any>()
            val hits = pictureData.tvHits + 1
            tvHits["tvHits"] = hits
            database.updateChildren(tvHits)

            val intent = Intent(binding.root.context, PostingActivity::class.java)
            intent.putExtra("dataList", pictureList as ArrayList<Serializable>)
            intent.putExtra("position", position)
            intent.putExtra("key", pictureData.key)
            binding.root.context.startActivity(intent)

        }

        binding.ivLike.setOnClickListener {
            val database = Firebase.database.reference.child("posting").child(pictureData.key)
            val currentUserUid = Firebase.auth.currentUser!!.uid

            database.child("likePeople").child(currentUserUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.value == true) {
                            database.child("likePeople").child(currentUserUid).setValue(false)
                            binding.ivLike.setImageResource(R.drawable.baseline_favorite_24)
                            database.child("tvLike").setValue(pictureData.tvLike - 1)
                        } else {
                            database.child("likePeople").child(currentUserUid).setValue(true)
                            binding.ivLike.setImageResource(R.drawable.favorite_pink)
                            database.child("tvLike").setValue(pictureData.tvLike + 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessageTimeString(lastTimeString: String): String {
        try {
            val currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId())
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val messageMonth = lastTimeString.substring(4, 6).toInt()
            val messageDate = lastTimeString.substring(6, 8).toInt()
            val messageHour = lastTimeString.substring(8, 10).toInt()
            val messageMinute = lastTimeString.substring(10, 12).toInt()
            val formattedCurrentTimeString = currentTime.format(dateTimeFormatter)
            val currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            val currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            val currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            val currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()
            val monthAgo = currentMonth - messageMonth
            val dayAgo = currentDate - messageDate
            val hourAgo = currentHour - messageHour
            val minuteAgo = currentMinute - messageMinute
            if (monthAgo > 0) {
                return monthAgo.toString() + "개월 전"
            } else {
                if (dayAgo > 0) {
                    if (dayAgo == 1) {
                        return "어제"
                    } else {
                        return dayAgo.toString() + "일 전"
                    }
                } else {
                    if (hourAgo > 0) {
                        return hourAgo.toString() + "시간 전"
                    } else {
                        if (minuteAgo > 0) {
                            return minuteAgo.toString() + "분 전"
                        } else {
                            return "방금"
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        }
    }

    class CustomViewHolder(val binding: PictureLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}