package com.example.videocallapp.models

import java.io.Serializable

class User : Serializable {
    lateinit var firstName : String
    lateinit var lastName : String
    lateinit var email : String
    lateinit var token : String

}