package com.example.smartext

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(getSupportActionBar() != null){
            getSupportActionBar()?.hide()
        }

        login_button.setOnClickListener {
            val email = email_login_editText.text.toString()
            val password = password_login_editText.text.toString()

            Log.d("Login", "Attempt login with email/password: $email/***")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Main", "Successfully signed in!")
                        Toast.makeText(this, "Successfully signed in!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }

                    else {
                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener {
                    Log.w("Main", "Authentication failed")
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
        }
        backToReg_textView.setOnClickListener { finish() }
    }
}