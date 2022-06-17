package com.login.socialloginappdemo

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment

class MyInstagramDialog : DialogFragment() {
    private var instagramWebViewFragment : WebView?=null
    private var progressBar : ProgressBar?=null

    private var myAuthListener : MyAuthListener?=null
     var myAuthenticationListener : MyAuthenticationListener?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_instagram_dialog, container, false)
        setUpViews(view)
        return view
    }

    private fun setUpViews(view: View?) {
        instagramWebViewFragment = view?.findViewById(R.id.instagramWebViewFragment)
        progressBar = view?.findViewById(R.id.webViewProgressBar)
        instagramWebViewFragment!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressBar?.visibility= View.VISIBLE
                if(progress>=80){
                    progressBar?.visibility= View.GONE
                }

            }
        }
        instagramWebViewFragment!!.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                progressBar?.visibility= View.GONE
                if (url?.startsWith(myInstagramDialogWebViewCallbackURL) == true){
                    val urls = url?.split("=")?.toTypedArray()
                    myAuthListener?.onComplete(urls[1])
                    dismiss()
                    return true
                }
                return false
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility= View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility= View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                myAuthListener?.onError(error.toString())
                dismiss()
            }
        }
        instagramWebViewFragment!!.settings.javaScriptEnabled=true
        instagramWebViewFragment!!.settings.allowContentAccess=true
//        webView.settings.builtInZoomControls=true
        instagramWebViewFragment!!.settings.loadWithOverviewMode=true
        instagramWebViewFragment!!.settings.useWideViewPort=true
        instagramWebViewFragment!!.settings.loadsImagesAutomatically=true
        instagramWebViewFragment!!.loadUrl(myInstagramDialogWebViewURL)
        instagramWebViewFragment!!.isVerticalScrollBarEnabled = false
        instagramWebViewFragment!!.isHorizontalScrollBarEnabled = false
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    fun setCompleteAuthCallback(myAuthListener : MyAuthListener){
        this.myAuthListener = myAuthListener
    }

    fun setCompleteAuthenticationCallback(myAuthenticationListener : MyAuthenticationListener){
        this.myAuthenticationListener = myAuthenticationListener
    }


    interface MyAuthenticationListener{
        fun onSuccess()
        fun onFail(error: String?)
    }

    interface MyAuthListener{
        fun onComplete(accessToken: String?)
        fun onError(error: String?)
    }

    companion object{
        var myInstagramDialogWebViewURL = ""
        var myInstagramDialogWebViewCallbackURL = ""
    }

}