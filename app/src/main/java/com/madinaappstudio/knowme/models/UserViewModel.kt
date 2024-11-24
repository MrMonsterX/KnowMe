package com.madinaappstudio.knowme.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.madinaappstudio.knowme.utils.USER_NODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchUserProfile(userUid: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = FirebaseFirestore.getInstance().collection(USER_NODE)
                    .document(userUid).get().await()
                val userData = document.toObject<User>()
                _user.postValue(userData)
                _loading.postValue(false)
            } catch (e: Exception) {
                _loading.postValue(false)
                _user.postValue(null)
            }
        }
    }
}
