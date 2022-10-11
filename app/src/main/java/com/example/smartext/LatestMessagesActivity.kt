package com.example.smartext

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latestMessages.adapter = adapter
        recyclerview_latestMessages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        supportActionBar?.elevation = 0F

        adapter.setOnItemClickListener{item, view->
            Log.d(TAG, "Clicked")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

//        setupDummyRows()
        listenForLatestMessage()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }

    val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerView(){
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessage(){
        val from_id = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$from_id")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?:return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?:return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

//    private fun setupDummyRows(){
//        val adapter = GroupAdapter<GroupieViewHolder>()
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        recyclerview_latestMessages.adapter = adapter
//    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessages","Current user ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        } )
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_new_message->{
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.layout.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}