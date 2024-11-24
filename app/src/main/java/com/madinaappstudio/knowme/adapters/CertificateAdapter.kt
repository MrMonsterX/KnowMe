package com.madinaappstudio.knowme.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.madinaappstudio.knowme.HomeActivity
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.models.Certificate
import com.madinaappstudio.knowme.models.UserCertificate
import com.madinaappstudio.knowme.tabs.CertificateTab
import com.madinaappstudio.knowme.utils.CERTIFICATE_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.LoadingDialog
import com.madinaappstudio.knowme.utils.startLog
import java.io.File
import java.net.URLDecoder
import java.util.Locale

class CertificateAdapter(private var certs: List<Certificate>, private var flag: Int) :

    RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder>() {

        private var loadingDialog: LoadingDialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cert_rv_item, parent, false)
        return CertificateViewHolder(view)
    }

    override fun getItemCount(): Int {
        return certs.size
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        val context = holder.itemView.context
        loadingDialog = LoadingDialog(context)
        setCertIcon(certs[position].certType, holder.ivThumb)
        holder.tvName.text = certs[position].certName
        if (flag == 0){
            holder.llContainer.setOnLongClickListener {
                handleLongClick(
                    context,
                    certs[position].certUrl,
                    certs[position].certType
                )
                true
            }
        }
        holder.llContainer.setOnClickListener {
            loadingDialog?.show()
            viewCertificate(
                context,
                certs[position].certUrl,
                certs[position].certType
            )
        }
    }

    class CertificateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivCertItemThumb)
        val tvName: MaterialTextView = view.findViewById(R.id.tvCertItemName)
        val llContainer: LinearLayout = view.findViewById(R.id.llCertRvItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCertificate(certificates: List<Certificate>){
        this.certs = certificates
        notifyDataSetChanged()
    }

    private fun handleLongClick(context: Context, url: String, certType: String){
        val bottomActions = BottomSheetDialog(context)
        val bottomView = LayoutInflater.from(context).inflate(R.layout.bottom_cert_actions, null)
        val btnDelete = bottomView.findViewById<Button>(R.id.btnCertActionDelete)
        bottomActions.setContentView(bottomView)

        btnDelete.setOnClickListener {
            performDeletion(context, url)
            bottomActions.dismiss()
        }

        bottomActions.show()
    }

    private fun viewCertificate(context: Context, url: String, certType: String){
        val storage = Firebase.storage
        val fileRef = storage.getReferenceFromUrl(url)
        val tempDir = File.createTempFile("tempFile", null)

        fileRef.getFile(tempDir)
            .addOnSuccessListener {
                startViewIntent(context, tempDir, certType)
                tempDir.deleteOnExit()
            }
            .addOnFailureListener {
                showToast(context, it.message)
            }
    }

    private fun startViewIntent(context: Context, tempDir: File, certType: String){
        try {
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "com.madinaappstudio.knowme",
                tempDir
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, certType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            loadingDialog?.hide()
            context.startActivity(viewIntent)

        } catch (e: Exception) {
            startLog(e)
            showToast(context, "Failed to open certificate")
        }
    }

    private fun performDeletion(context: Context, url: String){
        val loadingDialog = LoadingDialog(context)
        loadingDialog.show()
        val storageRef = Firebase.storage.getReferenceFromUrl(url)
        val firestoreRef = Firebase.firestore.collection(CERTIFICATE_NODE).document(getUserUid()!!)

        storageRef.delete()
            .addOnSuccessListener {
                firestoreRef.get()
                    .addOnSuccessListener { snapshot ->
                        val list = snapshot.toObject(UserCertificate::class.java)?.certificates
                        val newList = list?.filterNot { it.certUrl == url }
                        firestoreRef.update(mapOf("certificates" to newList))
                        updateCertificate(newList!!)
                        loadingDialog.hide()
                        showToast(context, "Deleted Success")
                    }
                    .addOnFailureListener {
                        showToast(context, it.message)
                    }
            }
            .addOnFailureListener {
                showToast(context, it.message)
            }
    }

    private fun setCertIcon(certType: String, imgView: ImageView){
        when(certType.lowercase(Locale.ROOT)) {
            "image/png" -> imgView.setImageResource(R.drawable.ic_png)
            "image/jpg" -> imgView.setImageResource(R.drawable.ic_jpg)
            "image/jpeg" -> imgView.setImageResource(R.drawable.ic_jpg)
            "application/pdf" -> imgView.setImageResource(R.drawable.ic_pdf)
            "application/zip" -> imgView.setImageResource(R.drawable.ic_zip)
            else -> imgView.setImageResource(R.drawable.ic_file_empty)
        }
    }
}