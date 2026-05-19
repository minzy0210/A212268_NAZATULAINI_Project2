package com.example.a212268_nazatulaini_lab1

import android.app.Application
import com.example.a212268_nazatulaini_lab1.data.ReServeDatabase
import com.example.a212268_nazatulaini_lab1.data.ReServeRepository

class ReServeApplication : Application() {

    val database by lazy { ReServeDatabase.getInstance(this) }

    val repository by lazy {
        ReServeRepository(
            userListedItemDao = database.userListedItemDao(),
            cartItemDao       = database.cartItemDao(),
            chatMessageDao    = database.chatMessageDao(),
            reservationDao    = database.reservationDao(),
            borrowedItemDao   = database.borrowedItemDao()
        )
    }
}