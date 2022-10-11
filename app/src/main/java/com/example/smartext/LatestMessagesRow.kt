package com.example.smartext

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null
    override fun bind(p0: GroupieViewHolder, p1: Int) {
        p0.itemView.message_latestMessageRow.text = chatMessage.text

        val chatPartnerId: String
        if(chatMessage.from_id == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.to_id
        }
        else chatPartnerId = chatMessage.from_id

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                p0.itemView.username_latestMessageRow.text = chatPartnerUser?.username

                val targetImageView = p0.itemView.dp_latestMessageRow
                Picasso.get().load(chatPartnerUser?.profileImageURL).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    override fun getLayout(): Int {
        return R.layout.latest_messages_row
    }
}