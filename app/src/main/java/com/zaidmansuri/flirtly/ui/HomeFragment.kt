package com.zaidmansuri.flirtly.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.R
import com.zaidmansuri.flirtly.adapter.HomeAdapter
import com.zaidmansuri.flirtly.databinding.FragmentHomeBinding
import com.zaidmansuri.flirtly.model.UserModel

class HomeFragment : Fragment() {
    private lateinit var adapter: HomeAdapter
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    private fun getData() {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = arrayListOf<UserModel>()
                    for (data in snapshot.children) {
                        val model = data.getValue(UserModel::class.java)
                        list.add(model!!)
                    }
                    list.shuffle()
                    if (snapshot.exists()) {
                        binding.cardStack.adapter = HomeAdapter(requireContext(), list)
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}