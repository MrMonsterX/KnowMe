package com.madinaappstudio.knowme.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.Path.Direction
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.HomeActivity
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentSignInBinding
import com.madinaappstudio.knowme.utils.LoadingDialog
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog


class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val signInGoToSignUpTxt = "Don't have an account? Register"
        val spannableString = SpannableString(signInGoToSignUpTxt)

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE),
            signInGoToSignUpTxt.indexOf("Register"),
            signInGoToSignUpTxt.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.btnSignInGoSignUp.text = spannableString

        binding.btnSignInGoSignUp.setOnClickListener {
            findNavController().navigate(R.id.actionSignInToSignUp)

        }


        binding.btnSignInLogin.setOnClickListener {
            val loadingDialog = LoadingDialog(requireContext())
            loadingDialog.show()

            val uEmail = binding.etSignInEmail.editableText.toString()
            val uPass = binding.etSignInPass.editableText.toString()

            if (uEmail.isNotEmpty() && uPass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(uEmail, uPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkUNameProvided(loadingDialog){
                                startLog(it.toString())
                                when(it) {
                                    0 -> {
                                        val action =
                                            SignInFragmentDirections.actionSignInToSetUsername(true)
                                        findNavController().navigate(action)
                                    }
                                    1 -> {
                                        startActivity(
                                            Intent(
                                                requireContext(),
                                                HomeActivity::class.java
                                            )
                                        )
                                        requireActivity().finishAffinity()
                                    }
                                    3 -> {
                                        showToast(requireContext(), "Failed to check username")
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
                showToast(requireContext(), "Please enter credentials")
            }
        }

        binding.btnSignInForget.setOnClickListener {
            val uEmail = binding.etSignInEmail.editableText.toString()
            if (uEmail.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(uEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast(requireContext(), "Reset password email sent to $uEmail")
                        } else {
                            showToast(requireContext(), task.exception?.localizedMessage)
                        }
                    }
            } else {
                showToast(requireContext(), "Please enter email in the field")
            }
        }

        binding.btnSignInGoogle.setOnClickListener {
            showToast(requireContext(), "Sorry, This feature isn't available")
        }
    }

    private fun checkUNameProvided(
        loadingDialog: LoadingDialog,
        callback: (Int) -> Unit
    ) {
        val userUid = getUserUid()!!
        Firebase.firestore.collection(USER_NODE).document(userUid).get()
            .addOnCompleteListener {
                loadingDialog.hide()
            }
            .addOnSuccessListener {
                val username = it.getString("username")
                if (!username.isNullOrEmpty()) callback(1) else callback(0)
            }
            .addOnFailureListener {
                callback(2)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
