package com.login.socialloginappdemo

import android.content.Context
import android.content.SharedPreferences

class InstagramSession(private val context: Context) {
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var instance: InstagramSession? = null

    private val SHARED = "Instagram_Preferences"
    private val API_USERNAME = "username"
    private val API_ID = "id"
    private val API_NAME = "name"
    private val API_ACCESS_TOKEN = "access_token"

    init {
        instance = this
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE)
        editor = sharedPref!!.edit()
    }
    /**
     *
     * @param accessToken
     * @param username
     */
    fun storeAccessToken(accessToken: String?, id: String?, username: String?, name: String?) {
        editor!!.putString(API_ID, id)
        editor!!.putString(API_NAME, name)
        editor!!.putString(API_ACCESS_TOKEN, accessToken)
        editor!!.putString(API_USERNAME, username)
        editor!!.commit()
    }

    fun storeAccessToken(accessToken: String?) {
        editor!!.putString(API_ACCESS_TOKEN, accessToken)
        editor!!.commit()
    }

    /**
     * Reset access token and user name
     */
    fun resetAccessToken() {
        editor!!.putString(API_ID, null)
        editor!!.putString(API_NAME, null)
        editor!!.putString(API_ACCESS_TOKEN, null)
        editor!!.putString(API_USERNAME, null)
        editor!!.commit()
    }

    /**
     * Get user name
     *
     * @return User name
     */
    fun getUsername(): String? {
        return sharedPref!!.getString(API_USERNAME, null)
    }

    /**
     *
     * @return
     */
    fun getId(): String? {
        return sharedPref!!.getString(API_ID, null)
    }

    /**
     *
     * @return
     */
    fun getName(): String? {
        return sharedPref!!.getString(API_NAME, null)
    }

    /**
     * Get access token
     *
     * @return Access token
     */
    fun getAccessToken(): String? {
        return sharedPref!!.getString(API_ACCESS_TOKEN, null)
    }

    @Synchronized
    fun getInstance(): InstagramSession {
        if (instance == null) {
            instance = InstagramSession(context)
        }
        return instance as InstagramSession
    }
}