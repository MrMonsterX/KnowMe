package com.madinaappstudio.knowme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentPersonalBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.models.UserViewModel
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast

class PersonalFragment : Fragment() {

    private var _binding: FragmentPersonalBinding? = null
    private val binding get() = _binding!!
    private val userUid = getUserUid()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.loading.observe(
            viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) showLoading(
                    binding.progBarPersonal,
                    binding.svPersonal
                )
                else showContent(
                    binding.progBarPersonal,
                    binding.svPersonal
                )
        })

        userViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                bindViewData(user)
            } else {
                showToast(requireContext(), "Failed to retrieve user data")
            }
        })

        userViewModel.fetchUserProfile(userUid!!)

        binding.btnPersonalEdit.setOnClickListener {
            findNavController().navigate(R.id.actionPersonalToEditPersonal)
        }
    }

    private fun bindViewData(user: User) {
        binding.etPersonalUName.setText(user.username)
        binding.etPersonalName.setText(user.name)
        binding.etPersonalEmail.setText(user.email)
        binding.etPersonalAge.setText(user.age?.toString() ?: "Age not set")
        binding.etPersonalAbout.setText(user.about ?: "About not provided")
        binding.etPersonalLocation.setText(user.location ?: "Location not provided")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
