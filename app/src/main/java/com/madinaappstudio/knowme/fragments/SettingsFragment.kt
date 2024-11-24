package com.madinaappstudio.knowme.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.knowme.MainActivity
import com.madinaappstudio.knowme.databinding.FragmentSettingsBinding
import com.madinaappstudio.knowme.utils.startLog

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSettingSignOut.setOnClickListener {
            showSignOutDialog()
        }

        val pm = requireContext().packageManager
        val version = pm.getPackageInfo(requireContext().packageName, 0).versionName
        binding.tvSettingsVersion.text = "v$version"
    }

    private fun showSignOutDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Are you sure want to log out?")
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                activity?.finishAffinity()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}