package com.madinaappstudio.knowme.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.madinaappstudio.knowme.tabs.CertificateTab
import com.madinaappstudio.knowme.tabs.PersonalTab
import com.madinaappstudio.knowme.tabs.QualificationTab

class ProfileViewPagerAdapter(fragment: Fragment, private val userUid: String) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> PersonalTab.newInstance(userUid)
            1 -> QualificationTab.newInstance(userUid)
            2 -> CertificateTab.newInstance(userUid)
            else -> throw IllegalStateException("Invalid Tab")
        }
    }
}