package com.madinaappstudio.knowme.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentPersonalTabBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.models.UserViewModel
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog

class PersonalTab : Fragment() {

    private var _binding: FragmentPersonalTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var userUid: String
    private val userViewModel: UserViewModel by viewModels()

    companion object {
        private const val USER_UUID = "userUid"

        fun newInstance(userUid: String): PersonalTab {
            val fragment = PersonalTab()
            val args = Bundle().apply {
                putString(USER_UUID, userUid)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userUid = it.getString(USER_UUID)!!
        }

        userViewModel.loading.observe(
            viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) showLoading(
                    binding.progBarPersonalTab,
                    binding.svPersonalTab
                )
                else showContent(
                    binding.progBarPersonalTab,
                    binding.svPersonalTab
                )
            }
        )

        userViewModel.user.observe(
            viewLifecycleOwner, Observer { user ->
                if (user != null) {
                    bindViewData(user)
                } else {
                    showToast(requireContext(), "Failed to retrieve user data")
                }
            }
        )

        userViewModel.fetchUserProfile(userUid)

    }

    private fun bindViewData(user: User) {
        binding.etPersonalTabUName.setText(user.username)
        binding.etPersonalTabName.setText(user.name)
        binding.etPersonalTabEmail.setText(user.email)
        binding.etPersonalTabAge.setText(user.age?.toString() ?: "Age not set")
        binding.etPersonalTabAbout.setText(user.about ?: "About not provided")
        binding.etPersonalTabLocation.setText(user.location ?: "Location not provided")
    }

    override fun onResume() {
        super.onResume()
        userViewModel.fetchUserProfile(userUid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}