package com.zaidmansuri.flirtly.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zaidmansuri.flirtly.MainActivity
import com.zaidmansuri.flirtly.R
import com.zaidmansuri.flirtly.databinding.ActivityLoginBinding
import com.zaidmansuri.flirtly.model.UserModel
import com.zaidmansuri.flirtly.utils.config
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val auth = FirebaseAuth.getInstance()
    private var verificationID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#E30022")))

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            checkUserExist()
        }

        binding.sendOtp.setOnClickListener {
            if (binding.phoneNumber.text!!.isEmpty()) {
                binding.phoneNumber.error = "Please Enter Phone Number"
            } else {
                config.showDialog(this)
                sendOTP(binding.phoneNumber.text.toString())
            }
        }

        binding.verifyOtp.setOnClickListener {
            if (binding.otp.text.isEmpty()) {
                binding.otp.error = "Please Enter OTP Number"
            } else {
                config.showDialog(this)
                verifyOTP(binding.otp.text.toString())
            }
        }
    }

    private fun sendOTP(number: String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                config.dismissDialog()
                Log.d("MYTAG", e.toString())
                Toast.makeText(this@Login, e.toString(), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                config.dismissDialog()
                this@Login.verificationID = verificationId
                binding.cardSendOtp.visibility = GONE
                binding.cardVerifyOtp.visibility = VISIBLE
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91 $number")       // Phone number to verify
            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun verifyOTP(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationID!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    config.dismissDialog()
                    checkUserExist()
                } else {
                    Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun checkUserExist() {
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this,Register::class.java))
                    finish()
                }
            }.addOnFailureListener {
                startActivity(Intent(this,Register::class.java))
                finish()
            }
    }
}