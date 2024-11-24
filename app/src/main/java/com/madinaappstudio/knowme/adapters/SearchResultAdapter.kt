package com.madinaappstudio.knowme.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.knowme.R
import com.madinaappstudio.knowme.fragments.SearchFragmentDirections
import com.madinaappstudio.knowme.models.User
import com.madinaappstudio.knowme.utils.showToast
import de.hdodenhof.circleimageview.CircleImageView

class SearchResultAdapter(private var users: List<User>) :
    RecyclerView.Adapter<SearchResultAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_rv_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        if (!users[position].profilePicture.isNullOrEmpty()){
            Glide.with(holder.itemView.context)
                .load(users[position].profilePicture)
                .override(52,52)
                .into(holder.ivItemDP)
        } else {
            holder.ivItemDP.setImageResource(R.drawable.ic_person)
        }

        holder.tvItemName.text = users[position].name
        holder.tvItemUName.text = users[position].username

        holder.llContainer.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchToProfileView(users[position].userUid!!)
            holder.itemView.findNavController().navigate(action)
        }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llContainer: LinearLayout = view.findViewById(R.id.llSearchItemContainer)
        val ivItemDP: CircleImageView = view.findViewById(R.id.ivSearchItemDP)
        val tvItemName: MaterialTextView = view.findViewById(R.id.tvSearchItemName)
        val tvItemUName: MaterialTextView = view.findViewById(R.id.tvSearchItemUName)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(users: List<User>){
        this.users = users
        notifyDataSetChanged()
    }
}