package com.madinaappstudio.knowme.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.firestore.PersistentCacheIndexManager
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.adapters.ProfileViewPagerAdapter
import com.madinaappstudio.knowme.databinding.FragmentProfileViewBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast
import com.madinaappstudio.knowme.utils.startLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewFragment : Fragment() {

    private var _binding: FragmentProfileViewBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(
            binding.progBarProfileView,
            binding.llProfileView
        )

        val args: ProfileViewFragmentArgs by navArgs()
        val userUid = args.userUid

        val adapter = ProfileViewPagerAdapter(this@ProfileViewFragment, userUid)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Personal"
                1 -> tab.text = "Qualifications"
                2 -> tab.text = "Certificate"
            }
        }.attach()

        fetchUserData(userUid) { userData ->
            binding.tvProfileViewName.text = userData.name
            binding.tvProfileViewUName.text = userData.username
            if (userData.profilePicture != null) {
                viewLifecycleOwner.lifecycleScope
                    .launch(Dispatchers.Main) {
                    Glide.with(requireContext())
                        .applyDefaultRequestOptions(RequestOptions().override(100,100))
                        .load(userData.profilePicture)
                        .into(binding.ivProfileViewPic)
                }
            }
            showContent(
                binding.progBarProfileView,
                binding.llProfileView
            )
        }


    }

    private fun fetchUserData(userUid: String, user: (User) -> Unit){

        Firebase.firestore.collection(USER_NODE).document(userUid).get()
            .addOnSuccessListener {
                val userData = it.toObject(User::class.java)!!
                user(userData)
            }
            .addOnFailureListener {
                showToast(requireContext(), it.message)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}