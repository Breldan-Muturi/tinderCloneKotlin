package turi.practice.tinderclone.activities

import com.google.firebase.database.DatabaseReference

interface TinderCallBack {
    fun onSignout()
    fun onGetUserId(): String
    fun getUserDatabase(): DatabaseReference
    fun getChatDatabase(): DatabaseReference
    fun profileComplete()
    fun startActivityForPhoto()
}