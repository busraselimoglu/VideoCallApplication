package com.example.videocallapp.listeners

import com.example.videocallapp.models.User

interface UsersListener {

    fun initiateVideoMeeting(user : User)

    fun initiateAudioMeeting(user: User)
}