package com.madinaappstudio.knowme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.databinding.FragmentEditPersonalBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.models.UserViewModel
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast

class EditPersonalFragment : Fragment() {

    private var _binding: FragmentEditPersonalBinding? = null
    private val binding get() = _binding!!
    private val userUid = getUserUid()
    private var user: User? = null
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) showLoading(
                binding.progBarEPersonal,
                binding.svEPersonal
            )
            else showContent(
                binding.progBarEPersonal,
                binding.svEPersonal
            )
        })

        userViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                this.user = user
                bindViewData(user)
            } else {
                showToast(requireContext(), "Failed to retrieve user data")
            }
        })

        userViewModel.fetchUserProfile(userUid!!)

        binding.btnEPersonalSave.setOnClickListener {
            if (validateInputField()){
                user = getUpdatedProfile()
                Firebase.firestore.collection(USER_NODE).document(userUid).set(user!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            showToast(requireContext(), "Profile Updated")
                            activity?.supportFragmentManager?.popBackStack()
                        } else {
                            showToast(requireContext(),task.exception?.localizedMessage)
                        }
                    }
            } else {
                showToast(requireContext(), "All field are required")
            }
        }

        binding.btnEPersonalCancel.setOnClickListener {
            showCancelDialog()
        }
    }

    private fun showCancelDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Profile edit")
            .setMessage("Are you sure want to discard")
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack()
            }
            .show()
    }

    private fun validateInputField() : Boolean {
        return binding.etEPersonalUName.text.toString().isNotEmpty() &&
                binding.etEPersonalName.text.toString().isNotEmpty() &&
                binding.etEPersonalEmail.text.toString().isNotEmpty() &&
                binding.etEPersonalAge.text.toString().isNotEmpty() &&
                binding.etEPersonalAbout.text.toString().isNotEmpty() &&
                binding.etEPersonalLocation.text.toString().isNotEmpty()
    }

    private fun getUpdatedProfile() : User {
        val user = User()
        user.username = binding.etEPersonalUName.text.toString()
        user.name = binding.etEPersonalName.text.toString()
        user.email = binding.etEPersonalEmail.text.toString()
        user.age = Integer.valueOf(binding.etEPersonalAge.text.toString())
        user.about = binding.etEPersonalAbout.text.toString()
        user.location = binding.etEPersonalLocation.text.toString()
        if (!this.user?.profilePicture.isNullOrEmpty()){
            user.profilePicture = this.user?.profilePicture
        }
        return user
    }
    private fun bindViewData(user: User) {
        binding.etEPersonalUName.setText(user.username)
        binding.etEPersonalName.setText(user.name)
        binding.etEPersonalEmail.setText(user.email)
        binding.etEPersonalAge.setText(user.age?.toString() ?: "")
        binding.etEPersonalAbout.setText(user.about ?: "")
        binding.etEPersonalLocation.setText(user.location ?: "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
