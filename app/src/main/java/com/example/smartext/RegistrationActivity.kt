package com.example.smartext

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.    MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        if(getSupportActionBar() != null){
            getSupportActionBar()?.hide()
        }

        val email = email_reg_editText.text.toString()
        val password = password_reg_editText.text.toString()
        Log.d("RegisterActivity", "Email is: " + email)
        Log.d("RegisterActivity", "Password is: $password")

        register_button.setOnClickListener{
            performRegistration()
        }

        alreadyhaveaccount_textView.setOnClickListener{
            Log.d("RegisterActivity", "Try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphotoReg_button.setOnClickListener {
            Log.d("RegisterActivity", "Try to show a photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null){
            Log.d("RegisterActivity", "Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphotoReg_imageView.setImageBitmap(bitmap)
            selectphotoReg_button.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectphotoReg_button.setBackgroundDrawable(bitmapDrawable)
        }
    }


    private fun performRegistration(){
        val email = email_reg_editText.text.toString()
        val password = password_reg_editText.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email & password to register!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: "+email)
        Log.d("RegisterActivity", "Password is: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Register", "Successfully created new user!")
                    Toast.makeText(this, "Successfully created new user!", Toast.LENGTH_SHORT).show()
                    uploadImageInFirebase()
                }
                else return@addOnCompleteListener
            }

            .addOnFailureListener {
                Log.d("Register", "Failed to create user: ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageInFirebase(){
        if (selectedPhotoUri == null)
            return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener{
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File loacation: $it")
                    saveUserInFirebase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Image upload failed: ${it.message}")
            }
    }

    private fun saveUserInFirebase(profileImageURL: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_reg_editText.text.toString(), profileImageURL)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "The user is finally saved in Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}



