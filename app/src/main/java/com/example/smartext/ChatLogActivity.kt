package com.example.smartext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerview_chatLog.adapter = adapter
        supportActionBar?.elevation = 0F


        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        //setupDummyData()
        listenForMessages()

        sendButton_chatLog.setOnClickListener{
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }

    }

    private fun listenForMessages(){
        val from_id = FirebaseAuth.getInstance().uid
        val to_id = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$from_id/$to_id")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null){
                    Log.d(TAG, chatMessage.text)
                    if (chatMessage.from_id == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    }
                    else{
                        val to_user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))

                    }
                }
                recyclerview_chatLog.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }


    private fun performSendMessage(){
        val text = editText_chatLog.text.toString()
        val from_id = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val to_id = user?.uid
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$from_id/$to_id").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$to_id/$from_id").push()
        val chatMessage = ChatMessage(reference.key!!, text, from_id!!, to_id!!, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                editText_chatLog.text.clear()
                recyclerview_chatLog.scrollToPosition(adapter.itemCount -1)
            }
        toReference.setValue(chatMessage)
        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$from_id/$to_id")
        latestMessageFromRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$to_id/$from_id")
        latestMessageToRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(p0: GroupieViewHolder, p1: Int) {
        p0.itemView.from_textView.text = text
        val uri = user.profileImageURL
        val targetImageView = p0.itemView.from_dp_newMessage
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(p0: GroupieViewHolder, p1: Int) {
        p0.itemView.to_textView.text = text
        val uri = user.profileImageURL
        val targetImageView = p0.itemView.to_dp_newMessage
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}