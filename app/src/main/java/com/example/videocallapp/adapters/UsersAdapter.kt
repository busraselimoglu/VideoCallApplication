package com.example.videocallapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videocallapp.R
import com.example.videocallapp.listeners.UsersListener
import com.example.videocallapp.models.User
import kotlinx.android.synthetic.main.item_container_user.view.*

class UsersAdapter(private var users: ArrayList<User>, private var usersListener: UsersListener) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_container_user, parent, false))
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.itemView.textFirstChar.text = users[position].firstName.substring(0,1)
        holder.itemView.textUserName.text = String.format("%s %s",users[position].firstName,users[position].lastName)
        holder.itemView.textEmail.text = users[position].email
        holder.itemView.imageAudioMeeting.setOnClickListener { usersListener.initiateAudioMeeting(users[position]) }
        holder.itemView.imageVideoMeeting.setOnClickListener { usersListener.initiateVideoMeeting(users[position]) }
    }


}

