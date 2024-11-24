package com.madinaappstudio.knowme.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.adapters.CertificateAdapter
import com.madinaappstudio.knowme.databinding.BottomCertPickerBinding
import com.madinaappstudio.knowme.databinding.FragmentCertificateBinding
import com.madinaappstudio.knowme.models.Certificate
import com.madinaappstudio.knowme.models.CertificateViewModel
import com.madinaappstudio.knowme.models.UserCertificate
import com.madinaappstudio.knowme.utils.CERTIFICATE_NODE
import com.madinaappstudio.knowme.utils.LoadingDialog
import com.madinaappstudio.knowme.utils.USER_DOCUMENT_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog

class CertificateFragment : Fragment() {

    private var _binding: FragmentCertificateBinding? = null
    private val binding get() = _binding!!
    private val certViewModel: CertificateViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private val userUid = getUserUid()!!
    private var pickFileLauncher: ActivityResultLauncher<String>? = null
    private val selectedFile = mutableListOf<Uri>()
    private var _certPickerBinding: BottomCertPickerBinding? = null
    private val certPickerBinding get() = _certPickerBinding!!
    private var certificateList: List<Certificate> = emptyList()
    
    private var adapter: CertificateAdapter? = null
    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificateBinding.inflate(inflater, container, false)
        _certPickerBinding = BottomCertPickerBinding.inflate(inflater, container, false)

        pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                handleFileUri(uri)
            } else {
                showToast(requireContext(), "Operation Cancelled by User")
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding.rvCertificateMain.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = CertificateAdapter(emptyList(), 0)
        binding.rvCertificateMain.adapter = adapter

        certViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            setViewVisibility(showProgressBar = isLoading)
        })

        certViewModel.certificateState.observe(viewLifecycleOwner, Observer { certState ->
            if (certState.isCertEmpty) {
                setViewVisibility(showNoCertificateView = true)
            } else {
                certificateList = certState.certificates!!
                adapter?.updateCertificate(certState.certificates)
                setViewVisibility(showRecyclerView = true)
            }
        })

        certViewModel.fetchUserCertificate(userUid)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(certPickerBinding.root)

        bottomSheetDialog.setOnDismissListener {
            selectedFile.clear()
            certPickerBinding.tvCertPickerFileName.text = String.format(R.string.no_file_chosen.toString())
            certPickerBinding.etCertPickerName.text?.clear()
        }

        binding.btnCertUploadCert.setOnClickListener {
            bottomSheetDialog.show()
        }

        binding.fabCertUpload.setOnClickListener {
            bottomSheetDialog.show()
        }

        certPickerBinding.btnCertPickerPickFile.setOnClickListener {
            pickFileLauncher?.launch("*/*")
        }

        certPickerBinding.btnCertPickerUpload.setOnClickListener {
            handleUploadClick(bottomSheetDialog)
        }

        startLog("me $certificateList")
    }

    private fun handleFileUri(uri: Uri) {
        selectedFile.clear()
        selectedFile.add(uri)
        updateSelectedFilesUI()
    }

    private fun updateSelectedFilesUI() {
        val fileName = selectedFile.firstOrNull()?.let {
            getFileNameFromUri(it)
        } ?: String.format(R.string.no_file_chosen.toString())
        certPickerBinding.tvCertPickerFileName.text = fileName
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "Unknown"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun handleUploadClick(bottomSheetDialog: BottomSheetDialog) {
        val certName = certPickerBinding.etCertPickerName.text.toString()

        if (certName.isNotEmpty()) {
            val uri = selectedFile.firstOrNull()
            if (uri != null) {
                val isNameExist = certificateList.any {it.certName == certName}
                if (!isNameExist){
                    loadingDialog?.show()
                    uploadCertificate(uri, certName)
                    bottomSheetDialog.dismiss()
                } else {
                    showToast(requireContext(), "Name Must be different")
                }
            } else {
                showToast(requireContext(), "No file selected")
            }
        } else {
            showToast(requireContext(), "Enter certificate name")
        }
    }

    private fun uploadCertificate(uri: Uri, name: String) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("$USER_DOCUMENT_NODE$userUid/$name")

        storageRef.putFile(uri).addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                    saveCertificate(name, downloadUri.toString()) }
                    .addOnFailureListener { handleUploadResult(false, it.message) }
            } else {
                handleUploadResult(false, uploadTask.exception?.message)
            }
        }
    }

    private fun saveCertificate(name: String, url: String) {
        val docRef = FirebaseFirestore.getInstance().collection("Certificate").document(userUid)

        val ref = Firebase.storage.getReferenceFromUrl(url)
        ref.metadata
            .addOnSuccessListener { metaData ->
                val certType = metaData.contentType ?: "Unknown"
                val newList = listOf(Certificate(name, url, certType))
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val existingList = document.toObject(UserCertificate::class.java)?.certificates ?: emptyList()
                            val updatedList = existingList + newList
                            docRef.update("certificates", updatedList)
                                .addOnSuccessListener { handleUploadResult(true) }
                                .addOnFailureListener { handleUploadResult(false, it.message) }
                        } else {
                            docRef.set(hashMapOf("certificates" to newList))
                                .addOnSuccessListener { handleUploadResult(true) }
                                .addOnFailureListener { handleUploadResult(false, it.message) }
                        }
                    }
                    .addOnFailureListener {
                        handleUploadResult(false, it.message) }
            }
            .addOnFailureListener {
                showToast(requireContext(), it.message)
            }
    }

    private fun handleUploadResult(success: Boolean, error: String? = null) {
        loadingDialog?.hide()
        certViewModel.fetchUserCertificate(userUid)
        if (success) {
            showToast(requireContext(), "Upload Successful")
        } else {
            showToast(requireContext(), error ?: "Upload Failed")
        }
    }

    private fun setViewVisibility(
        showProgressBar: Boolean = false,
        showRecyclerView: Boolean = false,
        showNoCertificateView: Boolean = false
    ) {
        binding.progBarCert.visibility = if (showProgressBar) View.VISIBLE else View.GONE
        binding.llCertMainView.visibility = if (showRecyclerView) View.VISIBLE else View.GONE
        binding.llCertEmptyView.visibility = if (showNoCertificateView) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _certPickerBinding = null
    }
}
