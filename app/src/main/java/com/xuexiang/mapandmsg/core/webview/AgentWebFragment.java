/*
 * Copyright (C) 2019 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xuexiang.mapandmsg.core.webview;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.just.agentweb.action.PermissionInterceptor;
import com.just.agentweb.core.AgentWeb;
import com.just.agentweb.core.client.MiddlewareWebChromeBase;
import com.just.agentweb.core.client.MiddlewareWebClientBase;
import com.just.agentweb.core.client.WebListenerManager;
import com.just.agentweb.core.web.AbsAgentWebSettings;
import com.just.agentweb.core.web.AgentWebConfig;
import com.just.agentweb.core.web.IAgentWebSettings;
import com.just.agentweb.download.AgentWebDownloader;
import com.just.agentweb.download.DefaultDownloadImpl;
import com.just.agentweb.download.DownloadListenerAdapter;
import com.just.agentweb.download.DownloadingService;
import com.just.agentweb.utils.LogUtils;
import com.just.agentweb.widget.IWebLayout;
import com.xuexiang.mapandmsg.MyApplication;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xutil.net.JsonUtil;

import java.util.HashMap;

/**
 * ??????WebView??????
 *
 * @author xuexiang
 * @since 2019/1/4 ??????11:13
 */
public class AgentWebFragment extends Fragment implements FragmentKeyDown {
    public static final String KEY_URL = "com.xuexiang.xuidemo.base.webview.key_url";

    private ImageView mBackImageView;
    private View mLineView;
    private ImageView mFinishImageView;
    private TextView mTitleTextView;
    private AgentWeb mAgentWeb;
    private ImageView mMoreImageView;
    private PopupMenu mPopupMenu;
    public static final String TAG = AgentWebFragment.class.getSimpleName();
    private DownloadingService mDownloadingService;

    public static AgentWebFragment getInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, url);
        return getInstance(bundle);
    }

    public static AgentWebFragment getInstance(Bundle bundle) {
        AgentWebFragment fragment = new AgentWebFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agentweb, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAgentWeb = AgentWeb.with(this)
                //??????AgentWeb???????????????
                .setAgentWebParent((LinearLayout) view, -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                //?????????????????????????????????-1????????????????????????2????????????dp???
                .useDefaultIndicator(-1, 3)
                //?????? IAgentWebSettings???
                .setAgentWebWebSettings(getSettings())
                //WebViewClient ??? ??? WebView ???????????? ?????????????????????WebView??????setWebViewClient(xx)?????????,?????????AgentWeb DefaultWebClient,???????????????????????????????????????
                .setWebViewClient(mWebViewClient)
                //WebChromeClient
                .setWebChromeClient(mWebChromeClient)
                //??????WebChromeClient????????????????????????WebChromeClient???AgentWeb 3.0.0 ?????????
                .useMiddlewareWebChrome(getMiddlewareWebChrome())
                //??????WebViewClient????????????????????????WebViewClient??? AgentWeb 3.0.0 ?????????
                .useMiddlewareWebClient(getMiddlewareWebClient())
                //???????????? 2.0.0 ?????????
                .setPermissionInterceptor(mPermissionInterceptor)
                //???????????? Android 4.2.2 ??????????????????????????? ?????????AgentWebView????????????
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                //?????????UI  AgentWeb3.0.0 ?????????
                .setAgentWebUIController(new UIController(getActivity()))
                //??????1?????????????????????????????????2??????????????????ID -1???????????????????????????????????? AgentWeb 3.0.0 ?????????
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setWebLayout(getWebLayout())
                .interceptUnkownUrl()
                //??????AgentWeb???
                .createAgentWeb()
                .ready()//?????? WebSettings???
                //WebView?????????url???????????????????????????
                .go(getUrl());

        if (MyApplication.isDebug()) {
            AgentWebConfig.debug();
        }

        // ?????? AgentWeb ??????????????????
        addBackgroundChild(mAgentWeb.getWebCreator().getWebParentLayout());

        initView(view);

        // AgentWeb ?????????WebView????????????????????? ????????????????????? AgentWeb ?????????????????????WebView?????????????????????
        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    }

    protected IWebLayout getWebLayout() {
        return new WebLayout(getActivity());
    }

    protected void initView(View view) {
        mBackImageView = view.findViewById(R.id.iv_back);
        mLineView = view.findViewById(R.id.view_line);
        mFinishImageView = view.findViewById(R.id.iv_finish);
        mTitleTextView = view.findViewById(R.id.toolbar_title);
        mBackImageView.setOnClickListener(mOnClickListener);
        mFinishImageView.setOnClickListener(mOnClickListener);
        mMoreImageView = view.findViewById(R.id.iv_more);
        mMoreImageView.setOnClickListener(mOnClickListener);
        pageNavigator(View.GONE);
    }

    protected void addBackgroundChild(FrameLayout frameLayout) {
        TextView textView = new TextView(frameLayout.getContext());
        textView.setText("????????? AgentWeb ??????");
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#727779"));
        frameLayout.setBackgroundColor(Color.parseColor("#272b2d"));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        final float scale = frameLayout.getContext().getResources().getDisplayMetrics().density;
        params.topMargin = (int) (15 * scale + 0.5f);
        frameLayout.addView(textView, 0, params);
    }


    private void pageNavigator(int tag) {
        mBackImageView.setVisibility(tag);
        mLineView.setVisibility(tag);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:
                    // true??????AgentWeb??????????????????
                    if (!mAgentWeb.back()) {
                        AgentWebFragment.this.getActivity().finish();
                    }
                    break;
                case R.id.iv_finish:
                    AgentWebFragment.this.getActivity().finish();
                    break;
                case R.id.iv_more:
                    showPoPup(v);
                    break;
                default:
                    break;

            }
        }

    };

    //========================================//

    /**
     * ?????????????????????
     */
    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {
        /**
         * PermissionInterceptor ????????? url1 ??????????????? url2 ????????????????????????
         * @param url
         * @param permissions
         * @param action
         * @return true ???Url???????????????????????????????????? ???false ??????????????????
         */
        @Override
        public boolean intercept(String url, String[] permissions, String action) {
            Log.i(TAG, "mUrl:" + url + "  permission:" + JsonUtil.toJson(permissions) + " action:" + action);
            return false;
        }
    };

    //=====================??????============================//

    /**
     * ????????? AgentWeb 4.0.0???????????????
     */
    protected DownloadListenerAdapter mDownloadListenerAdapter = new DownloadListenerAdapter() {
        /**
         *
         * @param url                ????????????
         * @param userAgent          UserAgent
         * @param contentDisposition ContentDisposition
         * @param mimetype           ?????????????????????
         * @param contentLength      ????????????
         * @param extra              ???????????? ??? ?????????????????? Extra ????????????icon ??? ??????????????? ??? ?????????????????????
         * @return true ???????????????????????????????????? ??? false ?????? AgentWeb ??????
         */
        @Override
        public boolean onStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, AgentWebDownloader.Extra extra) {
            LogUtils.i(TAG, "onStart:" + url);
            // ????????????????????????
            extra.setOpenBreakPointDownload(true)
                    //???????????????icon
                    .setIcon(R.drawable.ic_file_download_black_24dp)
                    // ?????????????????????
                    .setConnectTimeOut(6000)
                    // ???8KB??????????????????60s ?????????60s??????????????????????????????8KB????????????????????????
                    .setBlockMaxTime(10 * 60 * 1000)
                    // ?????????????????????
                    .setDownloadTimeOut(Long.MAX_VALUE)
                    // ??????????????????????????????
                    .setParallelDownload(false)
                    // false ??????????????????
                    .setEnableIndicator(true)
                    // ??????????????????
                    .addHeader("Cookie", "xx")
                    // ????????????????????????
                    .setAutoOpen(true)
                    // ???????????????????????????????????????
                    .setForceDownload(true);
            return false;
        }

        /**
         *
         * ????????????????????????????????????????????????????????????
         * @param url
         * @param downloadingService  ?????????????????? DownloadingService#shutdownNow ????????????
         */
        @Override
        public void onBindService(String url, DownloadingService downloadingService) {
            super.onBindService(url, downloadingService);
            mDownloadingService = downloadingService;
            LogUtils.i(TAG, "onBindService:" + url + "  DownloadingService:" + downloadingService);
        }

        /**
         * ??????onUnbindService??????????????????????????? DownloadingService???
         * @param url
         * @param downloadingService
         */
        @Override
        public void onUnbindService(String url, DownloadingService downloadingService) {
            super.onUnbindService(url, downloadingService);
            mDownloadingService = null;
            LogUtils.i(TAG, "onUnbindService:" + url);
        }

        /**
         *
         * @param url  ????????????
         * @param loaded  ?????????????????????
         * @param length    ??????????????????
         * @param usedTime   ?????? ?????????ms
         * ????????????????????????????????? ???????????? AsyncTask #XX ?????? AgentWeb # XX
         */
        @Override
        public void onProgress(String url, long loaded, long length, long usedTime) {
            int mProgress = (int) ((loaded) / Float.valueOf(length) * 100);
            LogUtils.i(TAG, "onProgress:" + mProgress);
            super.onProgress(url, loaded, length, usedTime);
        }

        /**
         *
         * @param path ?????????????????????
         * @param url  ????????????
         * @param throwable    ????????????????????????????????????
         * @return true ???????????????????????????????????????????????? ???false ????????????AgentWeb ??????
         */
        @Override
        public boolean onResult(String path, String url, Throwable throwable) {
            //????????????
            if (null == throwable) {
                //do you work
            } else {//????????????

            }
            // true  ????????????????????????????????? , ??????????????????
            return false;
        }
    };

    /**
     * @return IAgentWebSettings
     */
    public IAgentWebSettings getSettings() {
        return new AbsAgentWebSettings() {
            private AgentWeb mAgentWeb;

            @Override
            protected void bindAgentWebSupport(AgentWeb agentWeb) {
                this.mAgentWeb = agentWeb;
            }

            /**
             * AgentWeb 4.0.0 ??????????????? DownloadListener ?????? ???????????????API ?????? Download ??????????????????????????????????????????
             * ????????????????????? AgentWeb Download ?????? ??? ???????????? compile 'com.just.agentweb:download:4.0.0 ???
             * ???????????????????????????????????????????????? AgentWebSetting ??? New ??? DefaultDownloadImpl?????????DownloadListenerAdapter
             * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? setDownloader ????????????????????????
             * @param webView
             * @param downloadListener
             * @return WebListenerManager
             */
            @Override
            public WebListenerManager setDownloader(WebView webView, android.webkit.DownloadListener downloadListener) {
                return super.setDownloader(webView,
                        DefaultDownloadImpl
                                .create(getActivity(),
                                        webView,
                                        mDownloadListenerAdapter,
                                        mDownloadListenerAdapter,
                                        this.mAgentWeb.getPermissionInterceptor()));
            }
        };
    }

    //===================WebChromeClient ??? WebViewClient===========================//
    /**
     * ????????????????????????scheme??????????????? scheme://host:port/path?query&query ???
     *
     * @return mUrl
     */
    public String getUrl() {
        String target = "";
        Bundle bundle = getArguments();
        if (bundle != null) {
            target = bundle.getString(KEY_URL);
        }

        if (TextUtils.isEmpty(target)) {
            target = "https://github.com/xuexiangjys";
        }
        return target;
    }

    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.i(TAG, "onProgressChanged:" + newProgress + "  view:" + view);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mTitleTextView != null && !TextUtils.isEmpty(title)) {
                if (title.length() > 10) {
                    title = title.substring(0, 10).concat("...");
                }
                mTitleTextView.setText(title);
            }
        }
    };

    protected WebViewClient mWebViewClient = new WebViewClient() {

        private HashMap<String, Long> timer = new HashMap<>();

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        //
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            //intent:// scheme????????? ????????????false ??? ????????? DefaultWebClient ?????? ??? ??????????????????Activity  ??? ??????Activity????????????????????????????????????.  true ????????????
            //???????????????????????? ???intent://play?...package=com.youku.phone;end;
            //?????????????????????????????????????????? ??? ???????????????????????? true  ?????????????????? H5 ?????? ??????????????????????????????????????? ???????????? false ??? DefaultWebClient  ?????????intent ???????????? ????????? ??? ????????????????????????????????? ??????????????? ??? ????????????????????? ??? ??????????????? ??? ???????????????????????????????????? .
            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
                return true;
            }

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i(TAG, "mUrl:" + url + " onPageStarted  target:" + getUrl());
            timer.put(url, System.currentTimeMillis());
            if (url.equals(getUrl())) {
                pageNavigator(View.GONE);
            } else {
                pageNavigator(View.VISIBLE);
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (timer.get(url) != null) {
                long overTime = System.currentTimeMillis();
                Long startTime = timer.get(url);
                Log.i(TAG, "  page mUrl:" + url + "  used time:" + (overTime - startTime));
            }

        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    //========================????????????================================//

    /**
     * ???????????????
     *
     * @param targetUrl ??????????????????????????????
     */
    private void openBrowser(String targetUrl) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            XToastUtils.toast(targetUrl + " ???????????????????????????????????????");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(targetUrl);
        intent.setData(uri);
        startActivity(intent);
    }


    /**
     * ??????????????????
     *
     * @param view ??????????????????View??????
     */
    private void showPoPup(View view) {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getContext(), view);
            mPopupMenu.inflate(R.menu.menu_toolbar_web);
            mPopupMenu.setOnMenuItemClickListener(mOnMenuItemClickListener);
        }
        mPopupMenu.show();
    }

    /**
     * ????????????
     */
    private PopupMenu.OnMenuItemClickListener mOnMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.refresh:
                    if (mAgentWeb != null) {
                        mAgentWeb.getUrlLoader().reload(); // ??????
                    }
                    return true;

                case R.id.copy:
                    if (mAgentWeb != null) {
                        toCopy(getContext(), mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                case R.id.default_browser:
                    if (mAgentWeb != null) {
                        openBrowser(mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                case R.id.share:
                    if (mAgentWeb != null) {
                        shareWebUrl(mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                default:
                    return false;
            }

        }
    };

    /**
     * ??????????????????
     *
     * @param url ????????????
     */
    private void shareWebUrl(String url) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        //???????????????????????????????????????????????????????????????
        startActivity(Intent.createChooser(shareIntent, "?????????"));
    }


    /**
     * ???????????????
     *
     * @param context
     * @param text
     */
    private void toCopy(Context context, String text) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.setPrimaryClip(ClipData.newPlainText(null, text));
    }

    //===================??????????????????===========================//

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();//??????
        super.onResume();
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause(); //?????????????????????WebView ??? ??????mWebView.resumeTimers();/mAgentWeb.getWebLifeCycle().onResume(); ?????????
        super.onPause();
    }

    @Override
    public boolean onFragmentKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb.handleKeyEvent(keyCode, event);
    }

    @Override
    public void onDestroyView() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroyView();
    }

    //===================?????????===========================//


    /**
     * MiddlewareWebClientBase ??? AgentWeb 3.0.0 ??????????????????????????????
     * ???????????????????????? AgentWeb ?????????????????? ???????????? WebClientView???
     * ?????????AgentWeb???????????????????????? MiddlewareWebClientBase ?????????
     * ??????????????? ???
     *
     * @return
     */
    protected MiddlewareWebClientBase getMiddlewareWebClient() {
        return new MiddlewareWebViewClient() {
            /**
             *
             * @param view
             * @param url
             * @return
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // ?????? url???????????? DefaultWebClient#shouldOverrideUrlLoading
                if (url.startsWith("agentweb")) {
                    Log.i(TAG, "agentweb scheme ~");
                    return true;
                }
                // ?????? DefaultWebClient#shouldOverrideUrlLoading
                if (super.shouldOverrideUrlLoading(view, url)) {
                    return true;
                }
                // do you work
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        };
    }

    protected MiddlewareWebChromeBase getMiddlewareWebChrome() {
        return new MiddlewareChromeClient() {
        };
    }
}
