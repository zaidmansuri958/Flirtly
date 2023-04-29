package com.zaidmansuri.flirtly.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.zaidmansuri.flirtly.activity.MessageActivity
import com.zaidmansuri.flirtly.databinding.UserCardBinding
import com.zaidmansuri.flirtly.model.UserModel

class HomeAdapter(val context: Context, val arrayList: ArrayList<UserModel>,val onClick: () -> Unit) :
    RecyclerView.Adapter<viewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = UserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val data = arrayList[position]
        holder.binding.name.text = data.name
        holder.binding.email.text = data.email
        Glide.with(holder.itemView).load(data.image).into(holder.binding.userImage)
        holder.binding.userMessage.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("userID", data.number)
            intent.putExtra(
                "chatID",
                FirebaseAuth.getInstance().currentUser!!.phoneNumber + data.number
            )
            holder.itemView.context.startActivity(intent)
        }
        holder.binding.userVideocall.setOnClickListener {
            onClick()
        }

    }

}

class viewHolder(val binding: UserCardBinding) : ViewHolder(binding.root)