package com.login.socialloginappdemo

import android.app.ProgressDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.login.socialloginappdemo.InstagramDialog.Companion.TAG
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class WebViewActivity : AppCompatActivity(), AuthListener,
    OAuthAuthenticationListener {
    private var mWebView: WebView? = null
    private var mSpinner: ProgressDialog? = null
    private var mUrl : String?=null
    private var mSession: InstagramSession? = null
    val mTAG = "Instagram-WebView"

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
    private val API_URL = "https://api.instagram.com/v1"
    private var mAccessToken: String? = null
    private val userInfo = HashMap<String, String>()

    var WHAT_ERROR = 1
    private val WHAT_FETCH_INFO = 2

    private var mListener: InstagramApp.OAuthAuthenticationListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        mSession = InstagramSession(this)
        mAccessToken = mSession!!.getAccessToken()

        if (intent.extras!=null){
            mUrl = intent.extras!!.getString("url")
        }
        mSpinner = ProgressDialog(this)
        mSpinner!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mSpinner!!.setMessage("Loading...")
        mWebView = findViewById(R.id.instagramWebView)
        mWebView!!.isVerticalScrollBarEnabled = false
        mWebView!!.isHorizontalScrollBarEnabled = false
        mWebView!!.webViewClient = OAuthWebViewClient()
        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.loadUrl(mUrl!!)
        mWebView!!.layoutParams = InstagramDialog.FILL
    }

    fun fetchUserName(handler: Handler) {

        object : Thread() {
            override fun run() {
                Log.i(TAG, "Fetching user info")
                var what = InstagramApp.WHAT_FINALIZE
                try {
                    val url = URL(
                        (API_URL + "/users/" + mSession!!.getId()
                                + "/?access_token=" + mAccessToken)
                    )
                    Log.d(TAG, "Opening URL " + url.toString())
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

                    // String name = jsonObj.getJSONObject("data").getString(
                    // "full_name");
                    // String bio =
                    // jsonObj.getJSONObject("data").getString("bio");
                    // Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                    val data_obj = jsonObj.getJSONObject(TAG_DATA)
                    userInfo[TAG_ID] = data_obj.getString(TAG_ID)
                    userInfo[TAG_PROFILE_PICTURE] = data_obj.getString(TAG_PROFILE_PICTURE)
                    userInfo[TAG_USERNAME] = data_obj.getString(TAG_USERNAME)
                    userInfo[TAG_BIO] = data_obj.getString(TAG_BIO)
                    userInfo[TAG_WEBSITE] = data_obj.getString(TAG_WEBSITE)
                    val counts_obj = data_obj.getJSONObject(TAG_COUNTS)
                    userInfo[TAG_FOLLOWS] = counts_obj.getString(TAG_FOLLOWS)
                    userInfo[TAG_FOLLOWED_BY] = counts_obj.getString(TAG_FOLLOWED_BY)
                    userInfo[TAG_MEDIA] = counts_obj.getString(TAG_MEDIA)
                    userInfo[TAG_FULL_NAME] = data_obj.getString(TAG_FULL_NAME)
                    val meta_obj = jsonObj.getJSONObject(TAG_META)
                    userInfo[TAG_CODE] = meta_obj.getString(TAG_CODE)
                } catch (ex: Exception) {
                    what = WHAT_ERROR
                    ex.printStackTrace()
                }
                //mProgress!!.dismiss()
                handler.sendMessage(handler.obtainMessage(what, 2, 0))
            }
        }.start()
    }

    private inner class OAuthWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.d(mTAG, "Redirecting URL $url")
            val mListener:AuthListener = object : AuthListener {
                override fun onComplete(code: String?) {
                    getAccessToken(code)
                    //finish()
                }

                override fun onError(error: String?) {
                    mListener!!.onFail("Authorization failed")
                }
            }
            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                val urls = url.split("=").toTypedArray()
                mListener.onComplete(urls[1])
                //finish()
                return true
            }
            return false
        }

        override fun onReceivedError(
            view: WebView, errorCode: Int,
            description: String, failingUrl: String
        ) {
            Log.d(mTAG, "Page error: $description")
           /* super.onReceivedError(view, errorCode, description, failingUrl)
            mListener.onError(description)
            dismiss()*/
        }

        /*override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            Log.d(mTAG, "Loading URL: $url")
            super.onPageStarted(view, url, favicon)
            //mSpinner!!.show()
        }*/

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val title = mWebView!!.title
            /*if (title != null && title.length > 0) {
                mTitle!!.text = title
            }*/
            Log.d(InstagramDialog.TAG, "onPageFinished URL: $url")
            /*finish()
            mSpinner!!.dismiss()*/
        }
    }

    private fun getAccessToken(code: String?) {
     /*   mProgress!!.setMessage("Getting access token ...")
        mProgress!!.show()*/
        object : Thread() {
            override fun run() {
                Log.i(TAG, "Getting access token")
                var what = WHAT_FETCH_INFO
                val mHandler: Handler = object : Handler() {
                    override fun handleMessage(msg: Message) {
                        if (msg.what === WHAT_ERROR) {
                            //mProgress!!.dismiss()
                            if (msg.arg1 === 1) {
                                mListener!!.onFail("Failed to get access token")
                            } else if (msg.arg1 === 2) {
                                mListener!!.onFail("Failed to get user information")
                            }
                        } else if (msg.what === WHAT_FETCH_INFO) {
                            // fetchUserName();
                            //mProgress!!.dismiss()
                            mListener!!.onSuccess()
                        }
                    }
                }
                try {
                    val TOKEN_URL = "https://api.instagram.com/oauth/access_token"
                    val url = URL(TOKEN_URL)
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    Log.i(TAG, "Opening Token URL " + url.toString())
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
                                + "&redirect_uri=" + AppConfig.CALLBACK_URL + "&code=" + code
                    )
                    writer.flush()
                    val response: String? = Utils.streamToString(
                        urlConnection
                            .getInputStream()
                    )
                    var mAccessToken: String? = null
                    Log.i(TAG, "response $response")
                    val jsonObj = JSONTokener(response)
                        .nextValue() as JSONObject
                    mAccessToken = jsonObj.getString("access_token")
                    Log.i(TAG, "Got access token: $mAccessToken")
                    val id = jsonObj.getJSONObject("user").getString("id")
                    val user = jsonObj.getJSONObject("user").getString(
                        "username"
                    )
                    val name = jsonObj.getJSONObject("user").getString(
                        "full_name"
                    )
                    //storeAccessToken(mAccessToken, id, user, name)
                } catch (ex: Exception) {
                    what = WHAT_ERROR
                    ex.printStackTrace()
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0))
            }
        }.start()
    }

    override fun onComplete(accessToken: String?) {
        getAccessToken(accessToken)
    }

    override fun onError(error: String?) {
        mListener!!.onFail("Authorization failed")
    }

    override fun onSuccess() {
        val mHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what === WHAT_ERROR) {
                   // mProgress!!.dismiss()
                    if (msg.arg1 === 1) {
                        mListener!!.onFail("Failed to get access token")
                    } else if (msg.arg1 === 2) {
                        mListener!!.onFail("Failed to get user information")
                    }
                } else if (msg.what === WHAT_FETCH_INFO) {
                    // fetchUserName();
                    //mProgress!!.dismiss()
                    mListener!!.onSuccess()
                }
            }
        }
        fetchUserName(mHandler)
    }

    override fun onFail(error: String?) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT)
            .show()
    }
}