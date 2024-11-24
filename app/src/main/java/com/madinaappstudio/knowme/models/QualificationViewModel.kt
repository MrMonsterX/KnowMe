package com.madinaappstudio.knowme.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.madinaappstudio.knowme.utils.QUALIFICATION_NODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QualificationViewModel : ViewModel() {

    private var _qualificationState = MutableLiveData<QualificationState>()
    val qualificationState: LiveData<QualificationState> get() = _qualificationState

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchUserQualification(userUid: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = FirebaseFirestore.getInstance().collection(QUALIFICATION_NODE)
                    .document(userUid).get().await()
                if (document.exists()) {
                    val userQualification = document.toObject<Qualification>()
                    _qualificationState.postValue(QualificationState(true, userQualification))
                } else {
                    _qualificationState.postValue(QualificationState(false, null))
                }
            } catch (e: Exception) {
                _qualificationState.postValue(QualificationState(false, null))
            } finally {
                _loading.postValue(false)
            }
        }
    }
    data class QualificationState(val isExist: Boolean, val qualification: Qualification?)
}
