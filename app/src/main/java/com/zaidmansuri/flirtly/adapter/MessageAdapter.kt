package com.zaidmansuri.flirtly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.R
import com.zaidmansuri.flirtly.model.MessageModel
import com.zaidmansuri.flirtly.model.UserModel

class MessageAdapter(val context: Context, val list: ArrayList<MessageModel>) :
    RecyclerView.Adapter<MessageViewHolder>() {
    val MSG_TYPE_RIGHT = 0
    val MSG_TYPE_LEFT = 1

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderID == FirebaseAuth.getInstance().currentUser!!.phoneNumber) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            MessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_sender_message, parent, false)
            )
        } else {
            MessageViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_receiver_message, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.message.text = list[position].message
        FirebaseDatabase.getInstance().getReference("users").child(list[position].senderID!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.getValue(UserModel::class.java)
                        Glide.with(holder.itemView).load(data!!.image).into(holder.image)
                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }

            })
    }
}

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val message = itemView.findViewById<TextView>(R.id.message)
    val image = itemView.findViewById<ImageView>(R.id.image)
}