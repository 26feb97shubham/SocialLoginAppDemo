package com.login.socialloginappdemo

interface OAuthAuthenticationListener {
    fun onSuccess()
    fun onFail(error: String?)
}