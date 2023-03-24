package com.zaidmansuri.flirtly.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.adapter.ChatAdapter
import com.zaidmansuri.flirtly.adapter.HomeAdapter
import com.zaidmansuri.flirtly.databinding.FragmentChatBinding
import com.zaidmansuri.flirtly.model.UserModel

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    private fun getData() {
        val currentID = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        FirebaseDatabase.getInstance().getReference("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var list = arrayListOf<String>()
                    var newList = arrayListOf<String>()
                    for (data in snapshot.children) {
                        if (data.key!!.contains(currentID)) {
                            list.add(data!!.key!!.replace(currentID, ""))
                            newList.add(data.key!!)
                        }
                    }
                    try {
                        binding.chatRecylce.adapter = ChatAdapter(requireContext(), list, newList)
                    } catch (e: Exception) {

                    }

                    binding.chatRecylce.layoutManager = LinearLayoutManager(requireContext())


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}