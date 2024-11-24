package com.madinaappstudio.knowme.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.madinaappstudio.knowme.utils.CERTIFICATE_NODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CertificateViewModel : ViewModel() {

    private var _certificateState = MutableLiveData<CertificateState>()
    val certificateState: LiveData<CertificateState> get() = _certificateState

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchUserCertificate(userUid: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = FirebaseFirestore.getInstance().collection(CERTIFICATE_NODE)
                    .document(userUid).get().await()

                val certificateList = document.toObject(UserCertificate::class.java)?.certificates
                _loading.postValue(false)

                if (certificateList.isNullOrEmpty()) {
                    _certificateState.postValue(CertificateState(true, null))
                } else {
                    _certificateState.postValue(CertificateState(false, certificateList))
                }

            } catch (e: Exception) {
                _certificateState.postValue(CertificateState(true, null))
                _loading.postValue(false)
            }
        }
    }
    data class CertificateState(val isCertEmpty: Boolean = false, val certificates: List<Certificate>? = null)
}

