package com.example.appportfolio.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    NOTFOUND

}
enum class requestType {
    getMail,
    getPlatform,
    getAccount,
    logout
}

class SignManager (context:Context){
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var lastSignedGoogleAccount:GoogleSignInAccount?

    init {
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient=GoogleSignIn.getClient(context,gso)
        lastSignedGoogleAccount=GoogleSignIn.getLastSignedInAccount(context)
    }
    fun getCurAccountInfo(applyResult:(Status, String?, String?)->Unit)
    {
        if(isGoogleLoggedin())
        {
            applyResult(Status.SUCCESS,"GOOGLE",lastSignedGoogleAccount!!.id)
        }
        else
        {
            applyResult(Status.NOTFOUND,"google not found",null)
        }
    }
    fun getGoogleMail():String?
    {
        return lastSignedGoogleAccount!!.email
    }
    private fun isGoogleLoggedin():Boolean{
        return lastSignedGoogleAccount!=null
    }
    fun signout(applyResult:(Status, String?, String?)->Unit)
    {
        if(isGoogleLoggedin())
            googleSignout(applyResult)
        else
            applyResult(Status.NOTFOUND,"google notfound",null)

    }
    private fun googleSignout(applyResult: (Status, String?, String?) -> Unit){
        mGoogleSignInClient.signOut().addOnCompleteListener {
            applyResult(Status.SUCCESS,null,null)
        }
            .addOnFailureListener {
                applyResult(Status.ERROR,it.message,null)
            }
    }


}