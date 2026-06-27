package com.example.dreamsystem.repository

import com.example.dreamsystem.data.Wish
import com.example.dreamsystem.data.WishDao
import kotlinx.coroutines.flow.Flow

class WishRepository(private val wishDao: WishDao) {

    fun getAllWishes(): Flow<List<Wish>> {
        return wishDao.getAllWishes()
    }

    suspend fun insertWish(wish: Wish): Long {
        return wishDao.insertWish(wish)
    }

    suspend fun deleteWish(wish: Wish) {
        wishDao.deleteWish(wish)
    }
}
