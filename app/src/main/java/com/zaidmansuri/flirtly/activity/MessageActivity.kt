package com.zaidmansuri.flirtly.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.adapter.MessageAdapter
import com.zaidmansuri.flirtly.databinding.ActivityMessageBinding
import com.zaidmansuri.flirtly.model.MessageModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            if (binding.txtMessage.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                storeData(binding.txtMessage.text.toString())
            }
        }

//        getData(intent.getStringExtra("chatID").toString())

        verifyChatID()
    }

    private var senderID: String? = null
    private var chatID: String? = null
    private fun verifyChatID() {
        val receiverID = intent.getStringExtra("userID")
        senderID = FirebaseAuth.getInstance().currentUser!!.phoneNumber

        chatID = senderID + receiverID
        val reverseChatID = receiverID + senderID


        val ref = FirebaseDatabase.getInstance().getReference("chats")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(chatID!!)) {
                    getData(chatID!!)
                } else if (snapshot.hasChild(reverseChatID!!)) {
                    chatID = reverseChatID
                    getData(chatID!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun getData(chatID: String) {
        FirebaseDatabase.getInstance().getReference("chats").child(chatID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var list = ArrayList<MessageModel>()

                    for (data in snapshot.children) {
                        val message = data.getValue(MessageModel::class.java)
                        list.add(message!!)
                    }

                    if (snapshot.exists()) {
                        binding.messageRecycle.adapter = MessageAdapter(this@MessageActivity, list)
                        binding.messageRecycle.layoutManager =
                            LinearLayoutManager(this@MessageActivity)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessageActivity, "something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }


    private fun storeData(msg: String) {
        val currentDate = SimpleDateFormat("dd-mm-yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val map = hashMapOf<String, String>()
        map["message"] = msg
        map["senderID"] = senderID!!
        map["currentDate"] = currentDate
        map["currentTime"] = currentTime
        val ref = FirebaseDatabase.getInstance().getReference("chats").child(chatID!!)
        ref.child(ref.push().key!!).setValue(map).addOnCompleteListener {
            if (it.isSuccessful) {
                binding.txtMessage.setText("")
                Toast.makeText(this, "Message send successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }
}