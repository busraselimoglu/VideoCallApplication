package com.example.videocallapp.firebase

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videocallapp.activities.IncomingInvivationActivity
import com.example.videocallapp.utilities.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("bk", "MessagingService")

        val type: String? = remoteMessage.data[Constants.REMOTE_MSG_TYPE]
        if (type != null) {
            if (type == Constants.REMOTE_MSG_INVITATION) {
                val intent = Intent(applicationContext, IncomingInvivationActivity::class.java)
                intent.putExtra(
                    Constants.REMOTE_MSG_MEETING_TYPE,
                    remoteMessage.data[Constants.REMOTE_MSG_MEETING_TYPE]
                )
                intent.putExtra(
                    Constants.KEY_FIRST_NAME,
                    remoteMessage.data[Constants.KEY_FIRST_NAME]
                )
                intent.putExtra(
                    Constants.KEY_LAST_NAME,
                    remoteMessage.data[Constants.KEY_LAST_NAME]
                )
                intent.putExtra(
                    Constants.KEY_EMAIL,
                    remoteMessage.data[Constants.KEY_EMAIL]
                )
                intent.putExtra(
                    Constants.REMOTE_MSG_INVITER_TOKEN,
                    remoteMessage.data[Constants.REMOTE_MSG_INVITER_TOKEN]
                )
                intent.putExtra(
                    Constants.REMOTE_MSG_MEETING_ROOM,
                    remoteMessage.data[Constants.REMOTE_MSG_MEETING_ROOM]
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (type == Constants.REMOTE_MSG_INVITATION_RESPONSE) {
                val intent = Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                intent.putExtra(
                    Constants.REMOTE_MSG_INVITATION_RESPONSE,
                    remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE]
                )
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

    }
}