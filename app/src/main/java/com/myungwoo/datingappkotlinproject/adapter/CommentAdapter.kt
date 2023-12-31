package com.myungwoo.datingappkotlinproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myungwoo.datingappkotlinproject.databinding.CommentItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.datingappkotlinproject.data.CommentItemData

class CommentAdapter(val data: MutableList<CommentItemData>, val key: String) :
    RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        binding.tvAuthor.text = data.get(position).tvAuthor
        binding.tvComment2.text = data.get(position).tvComment

        val userUid = Firebase.auth.currentUser!!.uid
        val writerUid = data.get(position).tvWriterUid

        if (userUid.equals(writerUid)) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.INVISIBLE
        }
        binding.btnDelete.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle("댓글 삭제")
                .setMessage("삭제 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->

                    try {
                        // RealTimeDatabase 에서 댓글 삭제
                        Firebase.database.getReference("posting").child(key)
                            .child(data.get(position).key).setValue(null)
                        Toast.makeText(binding.root.context, "댓글 삭제 완료", Toast.LENGTH_SHORT).show()
                        // 완료 후 dialog 닫기
                        dialog.dismiss()

                    } catch (e: java.lang.Exception) {
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소") {
                        dialog, id ->
                    dialog.dismiss()
                }
            builder.show()
        }
    }

    inner class CustomViewHolder(val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}