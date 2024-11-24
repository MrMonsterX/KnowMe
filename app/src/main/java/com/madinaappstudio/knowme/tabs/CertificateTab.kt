package com.madinaappstudio.knowme.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.adapters.CertificateAdapter
import com.madinaappstudio.knowme.databinding.FragmentCertificateTabBinding
import com.madinaappstudio.knowme.databinding.FragmentPersonalTabBinding
import com.madinaappstudio.knowme.models.Certificate
import com.madinaappstudio.knowme.models.CertificateViewModel
import com.madinaappstudio.knowme.utils.startLog

class CertificateTab : Fragment() {

    private var _binding: FragmentCertificateTabBinding? = null
    private val binding get() = _binding!!
    private val certViewModel: CertificateViewModel by viewModels()
    private var adapter: CertificateAdapter? = null
    private var certificateList: List<Certificate> = emptyList()
    private lateinit var userUid: String

    companion object{
        private const val USER_UUID = "userUid"

        fun newInstance(userUid: String): CertificateTab{
            val fragment = CertificateTab()
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
        _binding = FragmentCertificateTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userUid = it.getString(USER_UUID)!!
        }

        binding.rvCertificateMainTab.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = CertificateAdapter(emptyList(), 1)
        binding.rvCertificateMainTab.adapter = adapter

        certViewModel.loading.observe(
            viewLifecycleOwner, Observer { isLoading ->
                setViewVisibility(showProgressBar = isLoading)
            }
        )

        certViewModel.certificateState.observe(
            viewLifecycleOwner, Observer { certState ->
                if (certState.isCertEmpty){
                    setViewVisibility(showNoCertificateView = true)
                } else {
                    certificateList = certState.certificates!!
                    adapter?.updateCertificate(certificateList)
                    setViewVisibility(showRecyclerView = true)
                }
            }
        )
        certViewModel.fetchUserCertificate(userUid)

    }

    private fun setViewVisibility(
        showProgressBar: Boolean = false,
        showRecyclerView: Boolean = false,
        showNoCertificateView: Boolean = false
    ) {
        binding.progBarCertTab.visibility = if (showProgressBar) View.VISIBLE else View.GONE
        binding.llCertMainViewTab.visibility = if (showRecyclerView) View.VISIBLE else View.GONE
        binding.llCertEmptyViewTab.visibility = if (showNoCertificateView) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        certViewModel.fetchUserCertificate(userUid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}