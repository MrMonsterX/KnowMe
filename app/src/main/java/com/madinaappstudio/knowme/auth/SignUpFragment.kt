package com.madinaappstudio.knowme.auth

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentSignUpBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.utils.LoadingDialog
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val signUpGoToSignInTxt = "Already have an account? Login"
        val spannableString = SpannableString(signUpGoToSignInTxt)

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE),
            signUpGoToSignInTxt.indexOf("Login"),
            signUpGoToSignInTxt.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.btnSignUpGoSignIn.text = spannableString

        binding.btnSignUpGoSignIn.setOnClickListener {
            findNavController().navigate(R.id.actionSignUpToSignIn)
        }

        binding.btnSignUpRegister.setOnClickListener {

            val loadingDialog = LoadingDialog(requireContext())
            loadingDialog.show()

            val uName = binding.etSignUpName.editableText.toString()
            val uEmail = binding.etSignUpEmail.editableText.toString()
            val uPass = binding.etSignUpPass.editableText.toString()

            if (uName.isNotEmpty() && uEmail.isNotEmpty() && uPass.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(uEmail, uPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = User()
                            user.name = uName
                            user.email = uEmail
                            Firebase.firestore.collection(USER_NODE)
                                .document(firebaseAuth.currentUser!!.uid)
                                .set(user)
                                .addOnSuccessListener {
                                    loadingDialog.hide()
                                    findNavController().navigate(R.id.actionSignUpToSignSetUsername)
                                }
                        } else {
                            loadingDialog.hide()
                            showToast(requireContext(), task.exception?.localizedMessage)
                        }
                    }
            } else {
                loadingDialog.hide()
                showToast(requireContext(), "All fields are required")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
