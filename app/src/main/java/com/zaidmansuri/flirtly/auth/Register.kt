package com.zaidmansuri.flirtly.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.zaidmansuri.flirtly.MainActivity
import com.zaidmansuri.flirtly.R
import com.zaidmansuri.flirtly.databinding.ActivityRegisterBinding
import com.zaidmansuri.flirtly.model.UserModel
import com.zaidmansuri.flirtly.utils.config


class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var imageUri: Uri? = null
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.imgProfile.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2f2e41")))

        binding.imgProfile.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.btnContinue.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        if (binding.name.text.toString().isEmpty() || binding.email.text.toString()
                .isEmpty() || binding.city.text.toString().isEmpty() || imageUri == null
        ) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show()
        } else if (!binding.termsCondition.isChecked) {
            Toast.makeText(this, "Please accept terms & conditions ", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        config.showDialog(this)
        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile.jpg")

        storageRef.putFile(imageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                storeData(it)

            }.addOnFailureListener {
                config.dialog!!.dismiss()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            config.dialog!!.dismiss()
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeData(imageURL: Uri?) {
        val data = UserModel(
            name = binding.name.text.toString(),
            image = imageURL.toString(),
            email = binding.email.text.toString(),
            city = binding.city.text.toString(),
            number = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        )
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString())
            .setValue(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    config.dialog!!.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    config.dialog!!.dismiss()
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }
}