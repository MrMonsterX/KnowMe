package com.madinaappstudio.knowme.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentProfileBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.models.UserViewModel
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.USER_PROFILE_PIC_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userUid get() = getUserUid()!!
    private val userViewModel: UserViewModel by viewModels()
    private var media: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var profilePicUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        media = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                showProfilePicSetDialog(uri)
            } else {
                showToast(requireContext(), "Operation Cancelled by user")
            }
        }
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.btnProfilePersonal.setOnClickListener {
            navController.navigate(R.id.actionProfileToPersonal)
        }

        binding.btnProfileQualification.setOnClickListener {
            navController.navigate(R.id.actionProfileToQualification)
        }
        binding.btnProfileCertificate.setOnClickListener {
            navController.navigate(R.id.actionProfileToCertificate)
        }

        userViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) showLoading(
                binding.progBarProfile,
                binding.svProfile
            )
            else showContent(
                binding.progBarProfile,
                binding.svProfile
            )
        })

        userViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                bindViewData(user)
            } else {
                showToast(requireContext(), "Failed to retrieve user data")
            }
        })

        userViewModel.fetchUserProfile(userUid)

        binding.ivProfilePic.setOnClickListener {
            showProfilePicViewDialog()
        }

        binding.ivProfileSetPic.setOnClickListener {
            media?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


    }

    private fun showProfilePicViewDialog() {

        val builder = AlertDialog.Builder(requireContext()).create()
        val view = layoutInflater.inflate(R.layout.dialog_profile_pic_view, null)

        val ivDialogProfileView: CircleImageView = view.findViewById(R.id.ivDialogProfileViewImg)
        val tvDialogProfileWarning: MaterialButton = view.findViewById(R.id.tvDialogProfileViewWarning)

        builder.window?.setDimAmount(0.7f)
        builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.setView(view)

        lifecycleScope.launch(Dispatchers.Main) {
            if (profilePicUrl != null){
                Glide.with(requireContext())
                    .load(profilePicUrl)
                    .into(ivDialogProfileView)

                tvDialogProfileWarning.text = "Delete Profile"
                tvDialogProfileWarning.visibility = View.VISIBLE
                tvDialogProfileWarning.setOnClickListener {
                    deleteProfilePic(builder)
                }

            } else {

                ivDialogProfileView.setImageResource(R.drawable.ic_person)
                tvDialogProfileWarning.text = "No Profile"
                tvDialogProfileWarning.isEnabled = false
                tvDialogProfileWarning.visibility = View.VISIBLE
            }
        }
        builder.show()
    }

    private fun deleteProfilePic(dialog: AlertDialog) {
        val storageRef = Firebase.storage.reference.child(USER_PROFILE_PIC_NODE + userUid)
        storageRef.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Firebase.firestore.collection(USER_NODE).document(userUid)
                    .update("profilePicture", null).addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.hide()
                            userViewModel.fetchUserProfile(userUid)
                            profilePicUrl = null
                            binding.ivProfilePic.setImageResource(R.drawable.ic_person)
                            showToast(requireContext(), "Profile Picture Deleted")
                        } else {
                            showToast(requireContext(), task.exception?.message)
                        }
                    }
            } else {
                showToast(requireContext(), task.exception?.message)
            }
        }
    }

    private fun showProfilePicSetDialog(uri: Uri) {

        val builder = AlertDialog.Builder(requireContext()).create()
        val view = layoutInflater.inflate(R.layout.dialog_profile_set, null)

        val ivProfileDialogPic: CircleImageView = view.findViewById(R.id.ivDialogProfileSetImg)
        val btnCancel: MaterialButton = view.findViewById(R.id.btnDialogProfileSetCancel)
        val btnSet: MaterialButton = view.findViewById(R.id.btnDialogProfileSet)

        builder.window?.setDimAmount(0.7f)
        builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.setView(view)
        builder.setCanceledOnTouchOutside(false)

        ivProfileDialogPic.setImageURI(uri)

        btnCancel.setOnClickListener {
            builder.dismiss()
        }

        btnSet.setOnClickListener {
            builder.dismiss()
            setProfilePic(uri)
            showLoading(
                binding.progBarProfile,
                binding.svProfile
            )
        }
        builder.show()

    }

    private fun setProfilePic(uri: Uri) {
        val storageRef = Firebase.storage.reference.child(USER_PROFILE_PIC_NODE + userUid)
        storageRef.putFile(uri).addOnCompleteListener {
            storageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Firebase.firestore.collection(USER_NODE).document(userUid)
                        .update("profilePicture", task.result.toString()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                showToast(requireContext(), "Profile Picture Updated")
                                userViewModel.fetchUserProfile(userUid)
                            } else {
                                showToast(requireContext(), task.exception?.message)
                            }
                        }

                } else {
                    showToast(requireContext(), task.exception?.message)
                }
            }
        }

    }

    private fun bindViewData(user: User) {
        if (!user.profilePicture.isNullOrEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main) {
                        profilePicUrl = user.profilePicture
                        Glide.with(requireContext())
                            .load(user.profilePicture)
                            .into(binding.ivProfilePic)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            binding.progBarProfile.visibility = View.GONE
                            showToast(requireContext(), "Failed to load profile picture")
                        }
                    }
                }
            }
        }
        binding.tvProfileName.text = user.name
        binding.tvProfileAbout.text = user.about
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}