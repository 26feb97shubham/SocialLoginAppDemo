package com.login.socialloginappdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import java.util.*
import com.facebook.ProfileTracker
import com.facebook.login.LoginManager
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var callbackManager : CallbackManager?=null
    private var loginButton : LoginButton?=null
    private val EMAIL = "email"
    private val INSTAGRAM_BASIC = "instagram_basic"
    private val PAGES_SHOW_LIST = "pages_show_list"
    private var accessTokenTracker : AccessTokenTracker?=null
    private var accessToken : AccessToken?=null
    private var profileTracker : ProfileTracker?=null
    private var tvUsername : TextView?=null
    private var tvName : TextView?=null
    private var request1 : GraphRequest?=null
    private var login : Button?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_main)

        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button)
        login = findViewById(R.id.loginButton)
        tvUsername = findViewById(R.id.tvUsername)
        tvName = findViewById(R.id.tvName)
        loginButton?.setReadPermissions(Arrays.asList(EMAIL, INSTAGRAM_BASIC, PAGES_SHOW_LIST))

        login?.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(EMAIL, INSTAGRAM_BASIC, PAGES_SHOW_LIST))
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onCancel() {
                    Log.e("cancel", "cancel")
                }

                override fun onError(error: FacebookException) {
                    Log.e("error", error.message.toString())
                }

                override fun onSuccess(result: LoginResult) {
                    Log.e("result", "success")
                    Log.e("result_1", result.accessToken.userId)


                    val profile = Profile.getCurrentProfile()
                    if (profile!=null){
                        tvUsername?.text = profile.firstName+" "+profile.lastName
                        tvName?.text = profile.firstName+" "+profile.lastName
                    }
                }

            })
        }


        loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onCancel() {
                Log.e("cancel", "cancel")
            }

            override fun onError(error: FacebookException) {
                Log.e("error", error.message.toString())
            }

            override fun onSuccess(result: LoginResult) {
                Log.e("result", "success")
                Log.e("result_1", result.accessToken.userId)

                val profile = Profile.getCurrentProfile()
                if (profile!=null){
                    tvUsername?.text = profile.firstName+" "+profile.lastName
                    tvName?.text = profile.firstName+" "+profile.lastName


                    request1 = GraphRequest.newMeRequest(result.accessToken, object : GraphRequest.GraphJSONObjectCallback{
                        override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                            try {
                                var email_id = obj?.getString("email")
                                var gender = obj?.getString("gender")
                                var profile_name = obj?.getString("name")
                                var fb_id = obj?.getLong("id")

                            }catch (e : JSONException){

                            }
                        }

                    })
                    request1?.executeAsync()
                }
            }
        })

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
               if (currentAccessToken==null){
                   LoginManager.getInstance().logOut()
                   tvUsername?.text = ""
                   tvName?.text = ""
               }
            }
        }
        // If the access token is available already assign it.
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken()

        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(
                oldProfile: Profile?,
                currentProfile: Profile?
            ) {
                // App code
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        request1 = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), object : GraphRequest.GraphJSONObjectCallback{
            override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                try {
                    var email_id = obj?.getString("email")
                    var gender = obj?.getString("gender")
                    var profile_name = obj?.getString("name")
                    var fb_id = obj?.getLong("id")

                    Log.e("data", obj.toString())
                    tvUsername?.text = obj?.getString("name")
                    tvName?.text = obj?.getString("name")

                }catch (e : JSONException){

                }
            }

        })

        /*var bundle = Bundle()
        bundle.putString("fields")
        bundle.putString("name")
        bundle.putString("gender")
        bundle.putString("email")
        bundle.putString("first_name")
        bundle.putString("last_name")*/

        request1?.executeAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker!!.stopTracking()
        profileTracker?.stopTracking();
    }


}