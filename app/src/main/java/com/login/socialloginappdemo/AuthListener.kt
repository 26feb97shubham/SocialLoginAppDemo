package com.login.socialloginappdemo

interface AuthListener {
    fun onComplete(accessToken: String?)
    fun onError(error: String?)
}