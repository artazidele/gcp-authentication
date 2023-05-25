package com.example.gcp_auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailActivity : AppCompatActivity() {

    private lateinit var userIsNull: LinearLayout
    private lateinit var textView: TextView
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var signUpBtn: Button
    private lateinit var signInBtn: Button
    private lateinit var resetPasswordBtn: Button
    private lateinit var userIsNotNull: LinearLayout
    private lateinit var textView2: TextView
    private lateinit var newPasswordET: EditText
    private lateinit var changePasswordBtn: Button
    private lateinit var signOutBtn: Button
    private lateinit var deleteAccountBtn: Button


    // Instances FirebaseAuth deklarēšana
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)
        userIsNull = findViewById(R.id.user_is_null)
        textView = findViewById(R.id.text_view)
        emailET = findViewById(R.id.email_et)
        passwordET = findViewById(R.id.password_et)
        signUpBtn = findViewById(R.id.sign_up_btn)
        signInBtn = findViewById(R.id.sign_in_btn)
        resetPasswordBtn = findViewById(R.id.reset_password_btn)
        userIsNotNull = findViewById(R.id.user_is_not_null)
        textView2 = findViewById(R.id.text_view_2)
        newPasswordET = findViewById(R.id.new_password_et)
        changePasswordBtn = findViewById(R.id.change_password_btn)
        signOutBtn = findViewById(R.id.sign_out_btn)
        deleteAccountBtn = findViewById(R.id.delete_account_btn)


        // Instances FirebaseAuth inicializēšana
        auth = Firebase.auth

        // Pārbauda, vai lietotājs ir pierakstījies
        if (currentUser() == true) {
            userIs()
        } else {
            userIsNot()
        }

        signUpBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            if (email == "" || password == "") {
                textView.text = "You have to write email and password."
            } else {
                signUp(email, password)
            }
        }

        signInBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            if (email == "" || password == "") {
                textView.text = "You have to write email and password."
            } else {
                signIn(email, password)
            }
        }

        resetPasswordBtn.setOnClickListener {
            val email = emailET.text.toString()
            if (email == "") {
                textView.text = "You have to write your email."
            } else {
                resetPassword(email)
            }
        }

        changePasswordBtn.setOnClickListener {
            val user = Firebase.auth.currentUser
            val password = newPasswordET.text.toString()
            if (password == "") {
                textView2.text = "You have to write new password."
            } else {
                changePassword(password, user!!)
            }
        }

        signOutBtn.setOnClickListener {
            signOut()
        }

        deleteAccountBtn.setOnClickListener {
            val user = Firebase.auth.currentUser
            deleteUser(user!!)
        }
    }

    // Funkcija lietotāja saskarnei, kad lietotājs ir pierakstījies
    private fun userIs() {
        userIsNull.visibility = View.GONE
        userIsNotNull.visibility = View.VISIBLE
        textView2.text = "You can sign out, change password or delete account."
    }

    // Funkcija lietotāja saskarnei, kad lietotājs nav pierakstījies
    private fun userIsNot() {
        userIsNotNull.visibility = View.GONE
        userIsNull.visibility = View.VISIBLE
        passwordET.setText("")
        emailET.setText("")
        textView.text = "You can sign up, sign in or reset password."
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

    // Lietotāja reģistrēšanas funkcija
    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    signOut()
                    verificationEmail(user!!)
                    passwordET.setText("")
                    emailET.setText("")
                } else {
                    textView.text = "There was an error during sign up."
                }
            }
    }

    // Lietotāja pierakstīšanās funkcija
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (checkEmailVerification(user!!) == true) {
                        userIs()
                    } else {
                        textView.text = "You have to verify your email. Then you can sign in."
                    }
                } else {
                    textView.text = "There was an error during sign in."
                }
            }
    }

    // Funkcija e-pasta verifikācijas e-pasta nosūtīšanai
    private fun verificationEmail(user: FirebaseUser) {
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    textView.text = "You have to verify your email. Then you can sign in."
                } else {
                    textView.text = "There was an error while sending verification email."
                }
            }
    }

    // Funkcija, kas pārbauda, vai e-pasts ir verificēts
    private fun checkEmailVerification(user: FirebaseUser): Boolean {
        return user!!.isEmailVerified
    }

    // Funkcija paroles maiņai
    private fun changePassword(password: String, user: FirebaseUser) {
        user!!.updatePassword(password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    textView2.text = "Password is changed."
                } else {
                    textView2.text = "There was an error while changing password."
                }
            }
    }

    // Funkcija paroles atjaunošanai
    private fun resetPassword(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    textView.text = "Password reset email sent."
                } else {
                    textView.text = "There was an error while sending password reset email."
                }
            }
    }

    // Funkcija lietotāja dzēšanai
    private fun deleteUser(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userIsNot()
                } else {
                    textView2.text = "There was an error while deleting account."
                }
            }
    }

    // Izrakstīšanās funkcija
    private fun signOut() {
        Firebase.auth.signOut()
        userIsNot()
    }
}
