package com.madinaappstudio.knowme.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentPersonalTabBinding
import com.madinaappstudio.knowme.databinding.FragmentQualificationTabBinding
import com.madinaappstudio.knowme.models.Qualification
import com.madinaappstudio.knowme.models.QualificationViewModel
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading

class QualificationTab : Fragment() {

    private var _binding: FragmentQualificationTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var userUid: String
    private val qualificationViewModel: QualificationViewModel by viewModels()

    companion object {
        private const val USER_UUID = "userUid"

        fun newInstance(userUid: String) : QualificationTab {
            val fragment = QualificationTab()
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
        _binding = FragmentQualificationTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userUid = it.getString(USER_UUID)!!
        }

        qualificationViewModel.loading.observe(
            viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) showLoading(
                    binding.progBarQualTab,
                    binding.svQualificationTab
                ) else showContent(
                    binding.progBarQualTab,
                    binding.svQualificationTab
                )
            }
        )

        qualificationViewModel.qualificationState.observe(
            viewLifecycleOwner, Observer { state ->

                if (state.isExist){
                    if (state.qualification != null){
                        bindViewData(state.qualification)
                    } else {
                        binding.svQualificationTab.visibility = View.GONE
                        binding.tvQualTabNoData.visibility = View.VISIBLE
                    }
                }
            }
        )

        qualificationViewModel.fetchUserQualification(userUid)
    }

    private fun bindViewData(qualification: Qualification) {
        binding.etQualTabTitle.setText(qualification.title ?: "")
        binding.etQualTabInstitution.setText(qualification.institution ?: "")
        binding.etQualTabStartDate.setText(qualification.startDate ?: "")
        binding.etQualTabEndDate.setText(qualification.endDate ?: "")
        binding.etQualTabDegreeType.setText(qualification.degreeType ?: "")
        binding.etQualTabStudyField.setText(qualification.studyField ?: "")
        binding.etQualTabGrade.setText(qualification.grade ?: "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        qualificationViewModel.fetchUserQualification(userUid)
    }

}