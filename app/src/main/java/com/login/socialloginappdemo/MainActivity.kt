package com.login.socialloginappdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import android.widget.Toast
import com.login.socialloginappdemo.InstagramApp.OAuthAuthenticationListener
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.login.socialloginappdemo.MyInstagramDialog.Companion.myInstagramDialogWebViewURL
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


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
    private var loginButtonInstagram : Button?=null
    private var mApp: InstagramApp? = null
    private var userInfoHashmap = HashMap<String, String>()
    var myUserName = ""

    private val AUTH_URL = "https://api.instagram.com/oauth/authorize/"
    private val TOKEN_URL = "https://api.instagram.com/oauth/access_token"
    private val API_URL = "https://api.instagram.com/v1"
    private var mAuthUrl: String? = null
    private var mAccessToken: String? = null
    private var mSession: InstagramSession? = null

    private var myInstagramDialog : MyInstagramDialog?=null
    private var mHandler : Handler?=null
    var WHAT_FINALIZE = 0
    private val userInfo = HashMap<String, String>()


    val TAG_DATA = "data"
    val TAG_ID = "id"
    val TAG_PROFILE_PICTURE = "profile_picture"
    val TAG_USERNAME = "username"
    val TAG_BIO = "bio"
    val TAG_WEBSITE = "website"
    val TAG_COUNTS = "counts"
    val TAG_FOLLOWS = "follows"
    val TAG_FOLLOWED_BY = "followed_by"
    val TAG_MEDIA = "media"
    val TAG_FULL_NAME = "full_name"
    val TAG_META = "meta"
    val TAG_CODE = "code"

    private var myAccessToken = ""
    private var myId = ""
    private var myUser = ""
    private var myName = ""



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

        val myInstagramDialog = MyInstagramDialog()
        myInstagramDialog.isCancelable = false

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
                    tvUsername?.text =myId
                    tvName?.text = myId
                }
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
            if (mApp!!.hasAccessToken()) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(
                    this@MainActivity
                )
                builder.setMessage("Disconnect from Instagram?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, id ->
                            mApp!!.resetAccessToken()
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
                    // urlConnection.connect();
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


                   // fetchUserName(handler, mAccessToken!!, id,user,name)

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
                    val myToken = "IGQVJVSnpHS3JFSEJnN2pFRFEzOWJhNTlKR2ZAsYkhpR2xQSERiTV9kT3JRd1p0VzZAzR1hqV3kxTm9JNmNwcjE1dTNCOHZApcm9OMVB2eVRhOXVZAN015c1E5NjhHLWZADRmZAFMmJQcEpRMWpjLURrMlVwdAZDZD"
                    val url = URL(
                        (API_URL + "/users/" + id
                                + "/?access_token=" + myToken)
                    )
                    Log.d("TAG", "Opening URL " + url.toString())
                    val urlConnection: HttpURLConnection = url
                        .openConnection() as HttpURLConnection
                    urlConnection.setRequestMethod("GET")
                    urlConnection.setDoInput(true)
                    urlConnection.connect()
                    val response: String? = Utils.streamToString(urlConnection.getInputStream())
                    println(response)
                    val jsonObj = JSONTokener(response)
                        .nextValue() as JSONObject

                    // String name = jsonObj.getJSONObject("data").getString(
                    // "full_name");
                    // String bio =
                    // jsonObj.getJSONObject("data").getString("bio");
                    // Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                    val data_obj = jsonObj.getJSONObject(TAG_DATA)
                    userInfo[TAG_ID] = data_obj.getString(TAG_ID)
                    userInfo[TAG_PROFILE_PICTURE] = data_obj.getString(TAG_PROFILE_PICTURE)
                    userInfo[TAG_USERNAME] = data_obj.getString(TAG_USERNAME)
                    myUserName = data_obj.getString(TAG_USERNAME)
                    userInfo[TAG_BIO] = data_obj.getString(TAG_BIO)
                    userInfo[TAG_WEBSITE] = data_obj.getString(TAG_WEBSITE)
                    val counts_obj = data_obj.getJSONObject(TAG_COUNTS)
                    userInfo[TAG_FOLLOWS] = counts_obj.getString(TAG_FOLLOWS)
                    userInfo[TAG_FOLLOWED_BY] = counts_obj.getString(TAG_FOLLOWED_BY)
                    userInfo[TAG_MEDIA] = counts_obj.getString(TAG_MEDIA)
                    userInfo[TAG_FULL_NAME] = data_obj.getString(TAG_FULL_NAME)
                    val meta_obj = jsonObj.getJSONObject(TAG_META)
                    userInfo[TAG_CODE] = meta_obj.getString(TAG_CODE)
                    myInstagramDialog?.dismiss()
                } catch (ex: Exception) {
                    what = WHAT_ERROR
                    ex.printStackTrace()
                }
                mHandler?.sendMessage(mHandler!!.obtainMessage(WHAT_FINALIZE, 2, 0))
            }
        }.start()
    }
}