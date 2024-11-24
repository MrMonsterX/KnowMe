package com.madinaappstudio.knowme.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.knowme.HomeActivity
import com.madinaappstudio.knowme.R

fun loadFragment(fragmentManager: FragmentManager, fragment: Fragment, container: Int, addToStack: Boolean) {
    fragmentManager.beginTransaction().apply {
        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        replace(container, fragment)
        if (addToStack){
            addToBackStack(null)
        } else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        commit()
    }
}

fun getUserUid() : String? {
    return FirebaseAuth.getInstance().currentUser?.uid
}

fun showToast(context: Context, msg: Any?){
    Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show()
}

fun showLoading(progressBar: ProgressBar, view: View) {
    progressBar.visibility = View.VISIBLE
    view.visibility = View.GONE
}

fun showContent(progressBar: ProgressBar, view: View) {
    progressBar.visibility = View.GONE
    view.visibility = View.VISIBLE
}

class LoadingDialog(context: Context){

    private val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
    private val builder: AlertDialog =  AlertDialog.Builder(context).create().apply {
        setView(dialogView)
        setCancelable(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setDimAmount(0.85f)
    }

    fun show(){
        builder.show()
    }

    fun hide() {
        if (builder.isShowing) {
            builder.dismiss()
        }
    }

}

//fun getUserUsername(userUid: String){
//
//    val username = task.result.getString("username")
//    if (!username.isNullOrEmpty()) {
//        binding.progBarMain.visibility = View.GONE
//        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
//        finishAffinity()
//}

fun startLog(msg: Any) {
    Log.d("KnowMe-Log", "simpleLog: ${msg}")
}




