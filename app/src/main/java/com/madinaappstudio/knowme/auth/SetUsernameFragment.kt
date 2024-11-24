package com.madinaappstudio.knowme.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.HomeActivity
import com.madinaappstudio.knowme.databinding.FragmentSetUsernameBinding
import com.madinaappstudio.knowme.utils.LoadingDialog
import com.madinaappstudio.knowme.utils.USERNAME_NODE
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog

class SetUsernameFragment : Fragment() {

    private var _binding: FragmentSetUsernameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetUsernameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = getUserUid()

        val args: SetUsernameFragmentArgs by navArgs()
        if (args.flag) binding.tvSetUNameWarningMsg.visibility = View.VISIBLE

        binding.btnSetUName.setOnClickListener {

            val loadingDialog = LoadingDialog(requireContext())
            loadingDialog.show()

            val username = binding.etSetUName.editableText.toString().trim()
            if (username.isNotEmpty()) {
                Firebase.firestore.collection(USERNAME_NODE).document(username).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                loadingDialog.hide()
                                showToast(requireContext(), "Username already taken")
                            } else {
                                val hashOfUName = hashMapOf("username" to username, "userId" to userId)
                                Firebase.firestore.collection(USERNAME_NODE).document(username)
                                    .set(hashOfUName)
                                    .addOnSuccessListener {
                                        Firebase.firestore.collection(USER_NODE)
                                            .document(Firebase.auth.currentUser!!.uid)
                                            .update("username", username)
                                            .addOnSuccessListener {
                                                loadingDialog.hide()
                                                startActivity(Intent(requireContext(), HomeActivity::class.java))
                                                requireActivity().finishAffinity()
                                            }
                                    }
                            }
                        } else {
                            loadingDialog.hide()
                            showToast(requireContext(), task.exception?.localizedMessage)
                        }
                    }
            } else {
                loadingDialog.hide()
                showToast(requireContext(), "Field require to be fill")
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
