package com.example.gcp_auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var phoneBtn: Button
    private lateinit var phoneET: EditText
    private lateinit var phoneTV: TextView
    private lateinit var signOutBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var signedTV: TextView
    private lateinit var signedInLL: LinearLayout
    private lateinit var notSignedInLL: LinearLayout
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    var codeIsSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        auth = Firebase.auth

        phoneET = findViewById(R.id.phone_et)
        phoneTV = findViewById(R.id.phone_tv)
        phoneBtn = findViewById(R.id.phone_btn)
        signedInLL = findViewById(R.id.signed_in_ll)
        notSignedInLL = findViewById(R.id.not_signed_in_ll)
        deleteBtn = findViewById(R.id.delete_user_btn)
        signOutBtn = findViewById(R.id.sign_out_btn)
        signedTV = findViewById(R.id.signedtv)

        if (currentUser() == true) {
            isSignedIn()
        } else {
            isNotSignedIn()
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@PhoneActivity, "Verification failed.", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                codeIsSentToUser()
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        phoneBtn.setOnClickListener {
            if (codeIsSent == false) {
                val phoneNumber = phoneET.text.toString()
                if (phoneNumber == "") {
                    Toast.makeText(this, "Please enter phone number.", Toast.LENGTH_LONG).show()
                } else {
                    sendVerificationCode(phoneNumber)
                }
            } else {
                val code = phoneET.text.trim().toString()
                if (code == "") {
                    Toast.makeText(this, "Please enter code.", Toast.LENGTH_LONG).show()
                } else {
                    val credentials : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        storedVerificationId.toString(), code)
                    signInWithPhoneAuthCredential(credentials)
                }

            }
        }
        deleteBtn.setOnClickListener {
            val currentUser = auth.currentUser
            deleteUser(currentUser!!)
        }
        signOutBtn.setOnClickListener {
            signOut()
        }
    }

    // Funkcija, kas pārbauda, vai šobrīd lietotājs ir pierakstījies
    private fun currentUser(): Boolean {
        val currentUser = auth.currentUser
        var trueUser = true
        if (currentUser == null) {
            trueUser = false
        }
        return trueUser
    }

    // Funkcija, kas izveido lietotāja saskarni, kad kods ir nosūtīts
    private fun codeIsSentToUser() {
        codeIsSent = true
        phoneET.setText("")
        phoneTV.text = "Please type verification code."
    }

    // Funkcija, kas izveido lietotāja saskarni, kad kods nav nosūtīts
    private fun codeIsNotSentToUser() {
        codeIsSent = false
        phoneTV.text = "Please type phone number."
    }

    // Funkcija, kas izveido lietotāja saskarni, kad lietotājs ir pierakstījies
    private fun isSignedIn() {
        signedTV.text = "You are successfully signed in."
        notSignedInLL.visibility = View.GONE
        signedInLL.visibility = View.VISIBLE
    }

    // Funkcija, kas izveido lietotāja saskarni, kad lietotājs nav pierakstījies
    private fun isNotSignedIn() {
        phoneET.setText("")
        signedInLL.visibility = View.GONE
        notSignedInLL.visibility = View.VISIBLE
        codeIsNotSentToUser()
    }

    // Funkcija, kas nosūta verifikācijas kodu
    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+371" + phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Funkcija lietotāja pierakstīšanai
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    isSignedIn()
                } else {
                    phoneTV.text = "Sign in was not successful. Please try again."
                }
            }
    }

    // Funkcija lietotāja dzēšanai
    private fun deleteUser(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isNotSignedIn()
                } else {
                    signedTV.text = "User was not deleted successfully. Please try again."
                }
            }
    }

    // Izrakstīšanās funkcija
    private fun signOut() {
        Firebase.auth.signOut()
        isNotSignedIn()
    }
}
