package com.madinaappstudio.knowme.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.protobuf.Struct
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.adapters.SearchResultAdapter
import com.madinaappstudio.knowme.databinding.FragmentSearchBinding
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.utils.USERNAME_NODE
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val firestore = Firebase.firestore
    private var adapter: SearchResultAdapter? = null
    private var currentQuery = ""
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSearchResult.layoutManager = LinearLayoutManager(requireContext())
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        adapter = SearchResultAdapter(emptyList())
        binding.rvSearchResult.addItemDecoration(divider)
        binding.rvSearchResult.adapter = adapter

        binding.svSearch.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currentQuery = query.toString().trim()
                searchJob?.cancel()

                if (currentQuery.isNotEmpty()){
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        searchUsers(currentQuery)
                    }
                } else {
                    adapter?.updateResults(emptyList())
                    showContent(
                        binding.progBarSearch,
                        binding.rvSearchResult
                    )
                }

            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        handleBackPress()
    }

    fun searchUsers(query: String){

        if (query.isEmpty()){
            adapter?.updateResults(emptyList())
            return
        }
        showLoading(
            binding.progBarSearch,
            binding.rvSearchResult
        )
        firestore.collection(USER_NODE)
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get(Source.SERVER)
            .addOnSuccessListener { documents ->
                if (query != currentQuery){
                    return@addOnSuccessListener
                }
                val users = documents.mapNotNull { it.toObject(User::class.java) }
                adapter?.updateResults(users)
                showContent(
                    binding.progBarSearch,
                    binding.rvSearchResult
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity?.baseContext, exception.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                showContent(
                    binding.progBarSearch,
                    binding.rvSearchResult
                )
            }
    }

    private fun handleBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.svSearch.isShowing) {
                        binding.svSearch.hide()
                    } else {
                        isEnabled = false
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
