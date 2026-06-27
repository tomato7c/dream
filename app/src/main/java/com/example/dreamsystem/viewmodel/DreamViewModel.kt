package com.example.dreamsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamsystem.data.AppDatabase
import com.example.dreamsystem.data.Wish
import com.example.dreamsystem.repository.WishRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WishRepository

    init {
        val wishDao = AppDatabase.getDatabase(application).wishDao()
        repository = WishRepository(wishDao)
    }

    val wishes: Flow<List<Wish>> = repository.getAllWishes()

    private val _showAddWishDialog = MutableStateFlow(false)
    val showAddWishDialog: StateFlow<Boolean> = _showAddWishDialog.asStateFlow()

    private val _newWishDescription = MutableStateFlow("")
    val newWishDescription: StateFlow<String> = _newWishDescription.asStateFlow()

    private val _newWishPoints = MutableStateFlow(0)
    val newWishPoints: StateFlow<Int> = _newWishPoints.asStateFlow()

    fun showAddWishDialog() {
        _showAddWishDialog.value = true
    }

    fun hideAddWishDialog() {
        _showAddWishDialog.value = false
        _newWishDescription.value = ""
        _newWishPoints.value = 0
    }

    fun updateWishDescription(description: String) {
        _newWishDescription.value = description
    }

    fun updateWishPoints(points: String) {
        _newWishPoints.value = points.toIntOrNull() ?: 0
    }

    fun addWish() {
        viewModelScope.launch {
            val wish = Wish(
                description = _newWishDescription.value,
                points = _newWishPoints.value
            )
            repository.insertWish(wish)
            hideAddWishDialog()
        }
    }

    fun deleteWish(wish: Wish) {
        viewModelScope.launch {
            repository.deleteWish(wish)
        }
    }
}
