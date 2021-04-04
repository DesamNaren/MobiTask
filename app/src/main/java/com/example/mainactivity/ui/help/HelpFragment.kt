package com.example.mainactivity.ui.help

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.mainactivity.R


class HelpFragment : Fragment() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_help, container, false)
        val mWebView = v.findViewById<View>(R.id.webView) as WebView
        mWebView.loadUrl("https://drive.google.com/file/d/1kk_G1Fr63QyyUdq1l2Q7Nf4vRRaPs4Xe/view")
        val webSettings: WebSettings = mWebView.getSettings()
        webSettings.javaScriptEnabled = true
        mWebView.webViewClient = WebViewClient()
        return v
    }
}