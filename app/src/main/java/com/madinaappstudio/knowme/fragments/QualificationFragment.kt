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
import com.madinaappstudio.knowme.databinding.FragmentQualificationBinding
import com.madinaappstudio.knowme.models.Qualification
import com.madinaappstudio.knowme.models.QualificationViewModel
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast

class QualificationFragment : Fragment() {

    private var _binding: FragmentQualificationBinding? = null
    private val binding get() = _binding!!
    private val qualificationViewModel: QualificationViewModel by viewModels()
    private val userUid = getUserUid()!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQualificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qualificationViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) showLoading(
                binding.progBarQual, binding.svQualification
            )
            else showContent(
                binding.progBarQual, binding.svQualification
            )
        })

        qualificationViewModel.qualificationState.observe(viewLifecycleOwner, Observer { state ->
            if (state.isExist) {
                if (state.qualification != null) {
                    bindViewData(state.qualification)
                } else {
                    showToast(requireContext(), "Failed to retrieve user qualification")
                }
            }
        })

        qualificationViewModel.fetchUserQualification(userUid)

        binding.btnQualEdit.setOnClickListener {
            findNavController().navigate(R.id.actionQualificationToEditQualification)
        }

    }

    private fun bindViewData(qualification: Qualification) {
        binding.etQualTitle.setText(qualification.title ?: "")
        binding.etQualInstitution.setText(qualification.institution ?: "")
        binding.etQualStartDate.setText(qualification.startDate ?: "")
        binding.etQualEndDate.setText(qualification.endDate ?: "")
        binding.etQualDegreeType.setText(qualification.degreeType ?: "")
        binding.etQualStudyField.setText(qualification.studyField ?: "")
        binding.etQualGrade.setText(qualification.grade ?: "")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}