package com.login.socialloginappdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity

import android.app.ProgressDialog

import android.webkit.WebView
import android.content.Intent
import android.graphics.Bitmap

import android.net.Uri
import android.util.Log

import android.webkit.WebViewClient
import java.lang.Exception


class TwitterAuthenticationActivity : Activity() {
    companion object{
        const val EXTRA_URL = "extra_url"
        private val TAG = TwitterAuthenticationActivity::class.java
            .simpleName
        private var mWebView: WebView? = null
        private var mDialog: ProgressDialog? = null
        private var mActivity: Activity? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this;
        setContentView(R.layout.activity_twitter_authentication)
        mWebView = findViewById(R.id.webView)
        val url = this.intent.getStringExtra(EXTRA_URL)
        if (url==null){
            finish()
        }else{
            mWebView?.webViewClient = MyWebViewClient()
            mWebView?.loadUrl(url)
        }

    }
    override fun onStop() {
        cancelProgressDialog()
        super.onStop()
    }

    override fun onPause() {
        cancelProgressDialog()
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        this.onRestart()
    }

    private class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            try {
                if (mDialog != null && mDialog!!.isShowing) {
                    mDialog!!.dismiss()
                    mDialog = null
                }
            } catch (exception: Exception) {
            }
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (mDialog == null) mDialog = ProgressDialog(mActivity)
            mDialog!!.setMessage("Loading..")
            if (!mActivity!!.isFinishing) {
                mDialog!!.show()
            }
        }

        override fun onLoadResource(view: WebView, url: String) {
            Log.i(TAG, "Loading Resources")
            Log.i(
                TAG,
                "Resource Loading Progress : " + view.progress
            )
            if (view.progress >= 70) {
                if (mDialog != null) {
                    mDialog!!.dismiss()
                    mDialog!!.cancel()
                    mDialog = null
                }
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri: Uri = Uri.parse(url)
            val verifier: String = uri.getQueryParameter("oauth_verifier")!!
            val resultIntent = Intent()
            resultIntent.putExtra("oauth_verifier", verifier)
            mActivity?.setResult(RESULT_OK, resultIntent)
            mActivity?.finish()
            return true
        }
    }


    private fun cancelProgressDialog() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog!!.cancel()
            mDialog = null
        }
    }

}