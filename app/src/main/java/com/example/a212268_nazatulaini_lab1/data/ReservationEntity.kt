package com.example.a212268_nazatulaini_lab1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val itemName: String,
    val quantity: Int
)

@Entity(tableName = "borrowed_items")
data class BorrowedItemEntity(
    @PrimaryKey val itemName: String
)