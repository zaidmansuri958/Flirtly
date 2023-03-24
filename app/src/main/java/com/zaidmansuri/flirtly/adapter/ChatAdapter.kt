package com.zaidmansuri.flirtly.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.activity.MessageActivity
import com.zaidmansuri.flirtly.databinding.ChatCardBinding
import com.zaidmansuri.flirtly.model.UserModel

class ChatAdapter(
    val context: Context,
    val list: ArrayList<String>,
    val newList: ArrayList<String>
) :
    RecyclerView.Adapter<chatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): chatViewHolder {
        val view = ChatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return chatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: chatViewHolder, position: Int) {
        FirebaseDatabase.getInstance().getReference("users").child(list[position])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.getValue(UserModel::class.java)
                        holder.binding.userName.text = data!!.name
                        Glide.with(holder.itemView).load(data.image).into(holder.binding.userImage)
                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }

            })

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("chatID", newList[position])
            intent.putExtra("userID", list[position])
            holder.itemView.context.startActivity(intent)
        }
    }
}

class chatViewHolder(val binding: ChatCardBinding) : ViewHolder(binding.root)