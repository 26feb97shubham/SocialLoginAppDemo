package com.login.socialloginappdemo

import android.R.attr
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.AccessToken.Companion.getCurrentAccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.login.socialloginappdemo.InstagramApp.OAuthAuthenticationListener
import com.login.socialloginappdemo.MyInstagramDialog.Companion.myInstagramDialogWebViewURL
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import com.twitter.sdk.android.core.TwitterConfig

import com.twitter.sdk.android.core.TwitterAuthConfig
import android.content.SharedPreferences
import android.net.Uri
import com.facebook.CustomTabMainActivity.Companion.EXTRA_URL
import android.app.ProgressDialog
import android.R.attr.data
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy


class MainActivity : AppCompatActivity() {
    private var callbackManager : CallbackManager?=null
    private var loginButton : LoginButton?=null
    private val EMAIL = "email"
    private val INSTAGRAM_BASIC = "instagram_basic"
    private val PAGES_SHOW_LIST = "pages_show_list"
    private var accessTokenTracker : AccessTokenTracker?=null
    private var accessToken : com.facebook.AccessToken?=null
    private var profileTracker : ProfileTracker?=null
    private var tvUsername : TextView?=null
    private var tvName : TextView?=null
    private var request1 : GraphRequest?=null
    private var login : Button?=null
    private var loginButtonInstagram : Button?=null
    private var mApp: InstagramApp? = null
    private var userInfoHashmap = HashMap<String, String>()
    var myUserName = ""

    private val AUTH_URL = "https://api.instagram.com/oauth/authorize/"
    private val TOKEN_URL = "https://api.instagram.com/oauth/access_token"
    private val API_URL = "https://api.instagram.com/v1"
    private val GRAPH_API_URL = "https://graph.instagram.com/v14.0"
    private var mAuthUrl: String? = null
    private var mAccessToken: String? = null
    private var mSession: InstagramSession? = null

    private var myStatusCode = ""

    private var myInstagramDialog : MyInstagramDialog?=null
    private var mHandler : Handler?=null
    var WHAT_FINALIZE = 0
    private val userInfo = HashMap<String, String>()

    private var twitterLoginButton : Button?=null

    private var myAccessToken = ""
    private var myId = ""
    private var myUser = ""
    private var myName = ""

    private var twitter_consumer_key = "oHKTv6Mw2TZYlNgZJ45XYoMHY"
    private var twitter_consumer_secret_key = "vcJqddAcjoIpdlcETozOXuSMauyDh2PgFT5JbL9Q53vXB25nzu"
    private var mAuthVerifier = "oauth_verifier"
    private var isTwitterAuthenticated = false

    val PREF_NAME = "sample_twitter_pref"
    val PREF_USER_NAME = "twitter_user_name"
    val WEBVIEW_REQUEST_CODE = 100
    private val PREF_KEY_OAUTH_TOKEN = "oauth_token"
    private val PREF_KEY_OAUTH_SECRET = "oauth_token_secret"
    private val PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin"

    private var mConsumerKey: String? = null
    private var mConsumerSecret: String? = null
    private val mCallbackUrl : String? =null
    private var mTwitterVerifier: String? = null
    private var mTwitter: Twitter? = null
    private var mRequestToken: RequestToken? = null
    private var mSharedPreferences: SharedPreferences? = null
    private var mPostProgress: ProgressDialog? = null



    var WHAT_ERROR = 1
    private val WHAT_FETCH_INFO = 2

    private val handler: Handler = Handler(object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            if (msg.what === WHAT_FINALIZE) {
                userInfoHashmap = mApp!!.getUserInfo()!!
            } else if (msg.what === WHAT_FINALIZE) {
                Toast.makeText(
                    this@MainActivity, "Check your network.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
    })

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.e("resultCode", it.resultCode.toString())
        if (it.resultCode== RESULT_OK){
            Log.e("data", it.data.toString())
        }else{
            Log.e("data", it.data.toString())

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_main)

        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button)
        login = findViewById(R.id.loginButton)
        loginButtonInstagram = findViewById(R.id.loginButtonInstagram)
        tvUsername = findViewById(R.id.tvUsername)
        tvName = findViewById(R.id.tvName)
        mSession = InstagramSession(this)
        mAccessToken = InstagramSession(this).getAccessToken()
        twitterLoginButton = findViewById(R.id.twitterLoginButton)

        val myInstagramDialog = MyInstagramDialog()
        myInstagramDialog.isCancelable = false

        initSDK()

        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what === WHAT_ERROR) {
                    if (msg.arg1 === 1) {
                        myInstagramDialog.myAuthenticationListener!!.onFail("Failed to get access token")
                    } else if (msg.arg1 === 2) {
                        myInstagramDialog.myAuthenticationListener!!.onFail("Failed to get user information")
                    }
                } else if (msg.what === WHAT_FETCH_INFO) {
                    myInstagramDialog.myAuthenticationListener!!.onSuccess()
                }else if(msg.what === WHAT_FINALIZE){
                    tvUsername?.text =myUserName
                    tvName?.text = myUserName
                }
            }
        }


        twitterLoginButton?.setOnClickListener {
            if (isAuthenticated()) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            } else {
                loginToTwitter()
            }
        }




        setWidgetReference()
        bindEventHandlers()

        mApp = InstagramApp(
            this, AppConfig.CLIENT_ID,
            AppConfig.CLIENT_SECRET, AppConfig.CALLBACK_URL
        )
        mApp?.setListener(object : OAuthAuthenticationListener {
            override fun onSuccess() {
                // tvSummary.setText("Connected as " + mApp.getUserName());
                loginButtonInstagram?.setText("Disconnect")
                // userInfoHashmap = mApp!!.getUserInfo()
                mApp?.fetchUserName(handler)
                myInstagramDialog.dismiss()
            }

            override fun onFail(error: String?) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT)
                    .show()
                myInstagramDialog.dismiss()
            }
        })

        if (mApp?.hasAccessToken()!!) {
            // tvSummary.setText("Connected as " + mApp.getUserName());
            loginButtonInstagram?.setText("Disconnect")
            mApp?.fetchUserName(handler)
            myInstagramDialog.dismiss()

        }



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

        loginButtonInstagram?.setOnClickListener {
            if (myAccessToken.isNotEmpty()) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(
                    this@MainActivity
                )
                builder.setMessage("Disconnect from Instagram?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, id ->
                            myAccessToken = ""
                            // btnConnect.setVisibility(View.VISIBLE);
                            loginButtonInstagram?.setText("Connect")
                            // tvSummary.setText("Not connected");
                        })
                    .setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert: AlertDialog = builder.create()
                alert.show()
            } else {
                mAuthUrl = (AUTH_URL
                        + "?client_id="
                        + AppConfig.CLIENT_ID
                        + "&redirect_uri="
                        + AppConfig.CALLBACK_URL
                        + "&response_type=code&display=touch&scope=user_profile,user_media")
                myInstagramDialogWebViewURL = mAuthUrl!!

                myInstagramDialog?.setCompleteAuthCallback(object : MyInstagramDialog.MyAuthListener{
                    override fun onComplete(accessToken: String?) {
                        getAccessToken(accessToken)
                    }

                    override fun onError(error: String?) {
                        myInstagramDialog.myAuthenticationListener!!.onFail("Authorization failed")
                        myInstagramDialog.dismiss()
                    }

                })

                myInstagramDialog?.setCompleteAuthenticationCallback(object : MyInstagramDialog.MyAuthenticationListener{
                    override fun onSuccess() {
                        loginButtonInstagram?.setText("Disconnect")
                        tvUsername?.text =myUser
                        tvName?.text = myName
                        // userInfoHashmap = mApp!!.getUserInfo()
                        fetchUserName(mHandler as Handler, myAccessToken, myId, "", "")

                        /*request1 = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), object : GraphRequest.GraphJSONObjectCallback{
                            override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                                try {
                                    var email_id = obj?.getString("email")
                                    var gender = obj?.getString("gender")
                                    var profile_name = obj?.getString("name")
                                    var fb_id = obj?.getLong("id")

                                    Log.e("data", obj.toString())
                                    Log.e("graphresponse", response.toString())
                                    tvUsername?.text = obj?.getString("name")
                                    tvName?.text = obj?.getString("name")

                                }catch (e : JSONException){

                                }
                            }

                        })

                        *//*var bundle = Bundle()
                        bundle.putString("fields")
                        bundle.putString("name")
                        bundle.putString("gender")
                        bundle.putString("email")
                        bundle.putString("first_name")
                        bundle.putString("last_name")*//*

                        request1?.executeAsync()*/

                        /*  GraphRequest(
                              AccessToken.getCurrentAccessToken(),
                              "/{instagram-user-id}",
                              null, HttpMethod.GET,
                              object : GraphRequest.Callback {
                                  override fun onCompleted(response: GraphResponse) {
                                      *//* handle the result *//*
                                    Log.e("graphresponse", response.toString())
                                }
                            }
                        ).executeAsync()*/


                    }

                    override fun onFail(error: String?) {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT)
                            .show()
                        myInstagramDialog.dismiss()
                    }

                })
                myInstagramDialog?.show(supportFragmentManager, "MyInstagramDialog")
            }
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
                oldAccessToken: com.facebook.AccessToken?,
                currentAccessToken: com.facebook.AccessToken?
            ) {
                TODO("Not yet implemented")
            }
        }
        // If the access token is available already assign it.
        // If the access token is available already assign it.
//        accessToken = com.facebook.AccessToken.getCurrentAccessToken()!!

        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(
                oldProfile: Profile?,
                currentProfile: Profile?
            ) {
                // App code
            }
        }
    }

    private fun bindEventHandlers() {

    }

    private fun setWidgetReference() {

    }

    private fun displayUserInfo(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Profile Information: ")
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (myStatusCode=="twitter"){
            if (data != null) mTwitterVerifier = data?.extras?.getString(mAuthVerifier)

            val accessToken: AccessToken
            try {
                accessToken = mTwitter!!.getOAuthAccessToken(
                    mRequestToken,
                    mTwitterVerifier
                )
                tvName?.text = accessToken.screenName
                tvUsername?.text = accessToken.screenName
                //saveTwitterInformation(accessToken)
            } catch (e: java.lang.Exception) {
            }
        }else{
            callbackManager?.onActivityResult(requestCode, resultCode, data)
            request1 = GraphRequest.newMeRequest(com.facebook.AccessToken.getCurrentAccessToken(), object : GraphRequest.GraphJSONObjectCallback{
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

            request1?.executeAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker!!.stopTracking()
        profileTracker?.stopTracking();
    }


    private fun getAccessToken(code: String?) {
        object : Thread() {
            override fun run() {
                var what = WHAT_FETCH_INFO
                try {
                    val url = URL(TOKEN_URL)
                    val urlConnection: HttpURLConnection = url
                        .openConnection() as HttpURLConnection
                    urlConnection.setRequestMethod("POST")
                    urlConnection.setDoInput(true)
                    urlConnection.setDoOutput(true)
                    urlConnection.connect()
                    val writer = OutputStreamWriter(
                        urlConnection.getOutputStream()
                    )
                    writer.write(
                        "client_id=" + AppConfig.CLIENT_ID + "&client_secret="
                                + AppConfig.CLIENT_SECRET + "&grant_type=authorization_code"
                                + "&redirect_uri=" + AppConfig.CALLBACK_URL+ "&code=" + code
                    )
                    writer.flush()
                    val response: String? = Utils.streamToString(
                        urlConnection
                            .getInputStream()
                    )
                    Log.i("TAG", "response $response")
                    val jsonObj = JSONTokener(response)
                        .nextValue() as JSONObject
                    mAccessToken = jsonObj.getString("access_token")
                    Log.i("TAG", "Got access token: $mAccessToken")
                    val id = jsonObj.getString("user_id")
                    myAccessToken = mAccessToken!!
                    myId = id
                    myUser = ""
                    myName = ""


                    //fetchUserName(handler, mAccessToken!!, id,"","")

//                    mSession!!.getInstance().storeAccessToken(mAccessToken, id, user, name)
                    // myInstagramDialog?.dismiss()
                } catch (ex: Exception) {
                    what = WHAT_ERROR
                    ex.printStackTrace()
                }
                mHandler?.sendMessage(mHandler!!.obtainMessage(WHAT_FETCH_INFO, 1, 0))
            }
        }.start()
    }

    fun fetchUserName(
        handler: Handler,
        mAccessToken: String,
        id: String,
        user: String,
        name: String
    ) {
        object : Thread() {
            override fun run() {
                Log.i("TAG", "Fetching user info")
                var what = WHAT_FINALIZE
                try {
                    val url = URL(
                        (GRAPH_API_URL + "/${id}" + "?fields=id,username,media"
                                + "&access_token=" + mAccessToken)
                    )
                    Log.d("TAG", "Opening URL " + url.toString())
                    val urlConnection: HttpURLConnection = url
                        .openConnection() as HttpURLConnection
                    urlConnection.setRequestMethod("GET")
                    urlConnection.setDoInput(true)
                    urlConnection.connect()
                    val response: String? = Utils.streamToString(
                        urlConnection
                            .getInputStream()
                    )
                    println(response)
                    val jsonObj = JSONTokener(response)
                        .nextValue() as JSONObject
                    myUserName = jsonObj.getString("username")

                } catch (ex: java.lang.Exception) {
                    what = WHAT_ERROR
                    ex.printStackTrace()
                }


                mHandler?.sendMessage(mHandler!!.obtainMessage(WHAT_FINALIZE, 2, 0))
            }
        }.start()
    }

    //TwitterAuthentication
    fun initSDK() {
        mConsumerKey = resources.getString(R.string.twitter_consumer_key)
        mConsumerSecret = resources.getString(R.string.twitter_consumer_secret_key)
        mAuthVerifier = "oauth_verifier"
        if (TextUtils.isEmpty(mConsumerKey)
            || TextUtils.isEmpty(mConsumerSecret)
        ) {
            return
        }
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0)
        if (isAuthenticated()) {
            Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
            //hide login button here and show tweet

            mSharedPreferences?.getString(PREF_USER_NAME, "")
            tvUsername?.setText("Welcome " + mSharedPreferences?.getString(PREF_USER_NAME, ""))
            tvName?.setText("Welcome " + mSharedPreferences?.getString(PREF_USER_NAME, ""))
        } else {
            val uri: Uri? = intent.data
            if (uri != null && uri.toString().startsWith(mCallbackUrl!!)) {
                val verifier: String = uri.getQueryParameter(mAuthVerifier)!!
                try {
                    val accessToken = mTwitter!!.getOAuthAccessToken(
                        mRequestToken, verifier
                    )
                    saveTwitterInformation(accessToken)
                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                } catch (e: java.lang.Exception) {
                    Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    Log.d(
                        "Failed to login ",
                        e.message!!
                    )
                }
            }
        }
    }

    protected fun isAuthenticated(): Boolean {
        return mSharedPreferences!!.getBoolean(PREF_KEY_TWITTER_LOGIN, false)
    }

    private fun saveTwitterInformation(accessToken: AccessToken) {
        val userID = accessToken.userId
        val user: User
        try {
            user = mTwitter!!.showUser(userID)
            val username = user.name
            val e = mSharedPreferences!!.edit()
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.token)
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.tokenSecret)
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true)
            e.putString(PREF_USER_NAME, username)
            e.commit()
        } catch (e1: TwitterException) {
            Log.d("Failed to Save", e1.message!!)
        }
    }

    private fun loginToTwitter() {
        val isLoggedIn = mSharedPreferences!!.getBoolean(
            PREF_KEY_TWITTER_LOGIN, false
        )
        if (!isLoggedIn) {
            val builder = ConfigurationBuilder()
            builder.setOAuthConsumerKey(mConsumerKey)
            builder.setOAuthConsumerSecret(mConsumerSecret)
            val configuration = builder.build()
            val factory = TwitterFactory(configuration)
            mTwitter = factory.instance
            try {
                mRequestToken = mTwitter?.getOAuthRequestToken(mCallbackUrl)
                startWebAuthentication()
            } catch (e: TwitterException) {
                e.printStackTrace()
            }
        }
    }

    private fun closeProgress() {
        if (mPostProgress != null && mPostProgress!!.isShowing()) {
            mPostProgress!!.dismiss()
            mPostProgress = null
        }
    }

    protected fun startWebAuthentication() {
        val intent = Intent(
            this@MainActivity,
            TwitterAuthenticationActivity::class.java
        )
        intent.putExtra(
            TwitterAuthenticationActivity.EXTRA_URL,
            mRequestToken!!.authenticationURL
        )
        myStatusCode = "twitter"
        startActivityForResult(intent, WEBVIEW_REQUEST_CODE)
    }


}