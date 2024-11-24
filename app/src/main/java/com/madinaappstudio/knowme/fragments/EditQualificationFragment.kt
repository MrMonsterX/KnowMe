package com.madinaappstudio.knowme.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.databinding.FragmentEditQualificationBinding
import com.madinaappstudio.knowme.models.Qualification
import com.madinaappstudio.knowme.models.QualificationViewModel
import com.madinaappstudio.knowme.utils.QUALIFICATION_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EditQualificationFragment : Fragment() {

    private var _binding: FragmentEditQualificationBinding? = null
    private val binding get() = _binding!!
    private val userUid = getUserUid()!!
    private val qualificationViewModel: QualificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditQualificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qualificationViewModel.loading.observe(
            viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) showLoading(
                    binding.progBarEQual,
                    binding.svEQualification
                )
                else showContent(
                    binding.progBarEQual,
                    binding.svEQualification
                )
            }
        )

        qualificationViewModel.qualificationState.observe(
            viewLifecycleOwner, Observer { state ->
                if (state.isExist){
                    state.qualification?.let { qualification ->
                        bindViewData(qualification)
                    } ?: showToast(requireContext(), "Failed to retrieve user qualification")
                }
            }
        )

        qualificationViewModel.fetchUserQualification(userUid)

        binding.btnEQualCancel.setOnClickListener {
            showCancelDialog()
        }

        binding.ilEQualStartDate.setEndIconOnClickListener {
            showDatePicker { startDate ->
                binding.etEQualStartDate.setText(startDate)
            }
        }

        binding.ilEQualEndDate.setEndIconOnClickListener {
            showDatePicker { endDate ->
                binding.etEQualEndDate.setText(endDate)
            }
        }

        binding.btnEQualSave.setOnClickListener {
            if (validateInputField()){
                val qualification = getUpdatedQualification()
                Firebase.firestore.collection(QUALIFICATION_NODE).document(userUid).set(qualification)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            showToast(requireContext(), "Qualification Updated")
                            activity?.supportFragmentManager?.popBackStack()
                        } else {
                            showToast(requireContext(), task.exception?.localizedMessage)
                        }
                    }
            } else {
                showToast(requireContext(), "All field required")
            }
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

    private fun getUpdatedQualification() : Qualification {
        val qualification = Qualification()

        qualification.title = binding.etEQualTitle.text.toString()
        qualification.institution = binding.etEQualInstitution.text.toString()
        qualification.startDate = binding.etEQualStartDate.text.toString()
        qualification.endDate = binding.etEQualEndDate.text.toString()
        qualification.degreeType = binding.etEQualDegreeType.text.toString()
        qualification.studyField = binding.etEQualStudyField.text.toString()
        qualification.grade = binding.etEQualGrade.text.toString()

        return qualification
    }

    private fun validateInputField(): Boolean {
        return binding.etEQualTitle.text.toString().isNotEmpty() &&
                binding.etEQualInstitution.text.toString().isNotEmpty() &&
                binding.etEQualStartDate.text.toString().isNotEmpty() &&
                binding.etEQualEndDate.text.toString().isNotEmpty() &&
                binding.etEQualDegreeType.text.toString().isNotEmpty() &&
                binding.etEQualStudyField.text.toString().isNotEmpty()

    }

    private fun bindViewData(qualification: Qualification) {
        binding.etEQualTitle.setText(qualification.title ?: "")
        binding.etEQualInstitution.setText(qualification.institution ?: "")
        binding.etEQualStartDate.setText(qualification.startDate ?: "")
        binding.etEQualEndDate.setText(qualification.endDate ?: "")
        binding.etEQualDegreeType.setText(qualification.degreeType ?: "")
        binding.etEQualStudyField.setText(qualification.studyField ?: "")
        binding.etEQualGrade.setText(qualification.grade ?: "")
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Start Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.show(activity?.supportFragmentManager!!, "datePicker")

        datePicker.addOnPositiveButtonClickListener {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
                .withZone(ZoneId.systemDefault())
            val formatedDate = formatter.format(Instant.ofEpochMilli(it))
            onDateSelected(formatedDate)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}