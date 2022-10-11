package com.example.smartext

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*


class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Select user"

        val adapter = GroupAdapter<GroupieViewHolder>()

//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())

        recyclerView_newmessage.adapter = adapter
        fetchUsers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach{
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null)
                        adapter.add(UserItem(user))
                }
                adapter.setOnItemClickListener{item, view->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
//                    intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
                recyclerView_newmessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(p0: GroupieViewHolder, p1: Int) {
        p0.itemView.username_newMessage.text = user.username
        Picasso.get().load(user.profileImageURL).into(p0.itemView.dp_newMessage)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}


//
//class CustomAdapter: RecyclerView.Adapter<ViewHolder>{
//    override fun dummy() {}
//}