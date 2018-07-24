package com.dataScrapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.datScrapping.R;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


@SuppressWarnings("unused")
public class Main extends AppCompatActivity implements OnClickListener {

    String previewUrl = "";
    SwipeRefreshLayout mySwipeRefreshLayout;
    ImageView previewProduct, clearTextButton, forwardButton, backwardButton;
    int currentUrlIndex=-1;
    RelativeLayout serachContainer, bottomBar;
    PopupWindow popupWindow;
    int searchContianerWidth, searchContianerHeight, bottombarWidth, bottombarHeight;
    private EditText searchEditText, editTextTitlePost, editTextDescriptionPost;
    private Button postButton, randomButton;
    private ImageButton submitButton;
    private Context context;
    private TextCrawler textCrawler;
    private TextView previewAreaTitle, postAreaTitle;
    private Bitmap[] currentImageSet;
    private Bitmap currentImage;
    private int currentItem = 0;
    private int countBigImages = 0;
    private boolean noThumb;
    private WebView mWebview;
    private ArrayList<String> listOfUrls = new ArrayList<>();
    private View mainView;
    private LinearLayout linearLayout;
    private View loading;
    private ImageView imageView;
    /**
     * You can customize this to update your view
     */
    private LinkPreviewCallback callback = new LinkPreviewCallback() {
        /**
         * This view is used to be updated or added in the layout after getting
         * the result
         */
        private View mainView;
        private LinearLayout linearLayout;
        private ImageView imageView;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onPre() {
            hideSoftKeyboard();
            currentImageSet = null;
            currentItem = 0;
            currentImage = null;
            noThumb = false;
            submitButton.setEnabled(false);
            CommonUtility.showProgressDailog(Main.this);


        }

        @Override
        public void onPos(final SourceContent sourceContent, boolean isNull) {

            /** Removing the loading layout */
            if (linearLayout != null)
                linearLayout.removeAllViews();
            CommonUtility.hideProgressDailog(Main.this);
            mainView = getLayoutInflater().inflate(R.layout.main_view, null);

            linearLayout = (LinearLayout) mainView.findViewById(R.id.external);

            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);


            boolean focusable = true;

            if (isNull || sourceContent.getFinalUrl().equals("") || sourceContent.getImage().equals("")) {
                /**
                 * Inflating the content layout into Main View LinearLayout
                 */
                View failed = getLayoutInflater().inflate(R.layout.failed,
                        linearLayout);

                TextView titleTextView = (TextView) failed
                        .findViewById(R.id.text);
                titleTextView.setText(getString(R.string.failed_preview));

                failed.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        releasePreviewArea();
                    }
                });

            } else {


                final View content = getLayoutInflater().inflate(
                        R.layout.preview_content, linearLayout);

                /** Fullfilling the content layout */
                final LinearLayout infoWrap = (LinearLayout) content
                        .findViewById(R.id.info_wrap);
                final LinearLayout titleWrap = (LinearLayout) content
                        .findViewById(R.id.title_wrap);


                final ImageView imageSet = (ImageView) infoWrap
                        .findViewById(R.id.image_post_set);

                final TextView close = (TextView) titleWrap
                        .findViewById(R.id.close);
                final TextView titleTextView = (TextView) titleWrap
                        .findViewById(R.id.title);

                final TextView urlTextView = (TextView) content
                        .findViewById(R.id.url);
                final TextView descriptionTextView = (TextView) content
                        .findViewById(R.id.description);
                final TextView priceTextView = (TextView) content.findViewById(R.id.price);

                descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                titleTextView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        titleTextView.setVisibility(View.GONE);


                    }
                });


                close.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        releasePreviewArea();

                    }
                });

                if (sourceContent.getTitle().equals(""))
                    sourceContent.setTitle(getString(R.string.enter_title));

                if (sourceContent.getDescription().equals(""))
                    sourceContent
                            .setDescription(getString(R.string.enter_description));
                titleTextView.setText(getString(R.string.title_label) + sourceContent.getTitle());
                urlTextView.setText(sourceContent.getCannonicalUrl());
                descriptionTextView.setText(getString(R.string.description_label) + sourceContent.getDescription());
                priceTextView.setText(getString(R.string.price_label) + sourceContent.getPrice());
                if (!sourceContent.getImage().equals(""))
                    Picasso.with(context).load(sourceContent.getImage()).into(imageSet);

            }

            /** Inflating the preview layout */
            popupWindow = new PopupWindow(linearLayout, LayoutParams.MATCH_PARENT,
                    (LayoutParams.WRAP_CONTENT), focusable);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    previewProduct.setImageResource(R.drawable.baseline_favorite_border_black_18dp);
                }
            });
            // show the popup window
            popupWindow.showAtLocation(mainView, Gravity.CENTER, 0, 150);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.main);
        initiLayout();
    }

    /**
     * initialize view
     */
    private void initiLayout() {
        searchEditText = (EditText) findViewById(R.id.edt_search_text);
        editTextTitlePost = null;
        editTextDescriptionPost = null;
        submitButton = (ImageButton) findViewById(R.id.action_go);
        previewProduct = (ImageView) findViewById(R.id.preview);
        clearTextButton = (ImageView) findViewById(R.id.iv_clear_text);
        forwardButton = (ImageView) findViewById(R.id.forward);
        backwardButton = (ImageView) findViewById(R.id.backward);
        serachContainer = (RelativeLayout) findViewById(R.id.search_Container);
        bottomBar = (RelativeLayout) findViewById(R.id.bottomBar);
        mainView = getLayoutInflater().inflate(R.layout.main_view, null);
        textCrawler = new TextCrawler();
        previewProduct.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        clearTextButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        backwardButton.setOnClickListener(this);
        mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(mWebview!=null)
                        {
                            mWebview.reload();
                        }

                    }
                }
        );
        //handling keyboard search button
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    initSubmitButton();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Opening web view on submission of url
     */
    public void initSubmitButton() {
        if (!searchEditText.getText().toString().matches("")) {
            hideSoftKeyboard(searchEditText);
            if (CommonUtility.isNetworkConnected(this)) {
                openWebView();
            } else {
                CommonUtility.showSimpleDialog(Main.this,getString(R.string.internet_connection_check));
            }
        } else {
            if(mWebview!=null)
            {
                //removing webpage from webview on clearing url from search box
                mWebview.loadUrl(getString(R.string.blank_url));
            }
            CommonUtility.showSimpleDialog(Main.this,getString(R.string.enter_url_check));
        }

    }

    /**
     * Click on  for product preview
     */
    private void previewProduct() {
        if (popupWindow == null || (popupWindow != null && !popupWindow.isShowing())) {
            if (!previewUrl.equals("")&& !previewUrl.equals(getString(R.string.blank_url))) {
                if (CommonUtility.isNetworkConnected(Main.this)) {

                    if(previewUrl.contains("www.frys.com")||previewUrl.contains("ebay.com")||
                            previewUrl.contains("www.walmart.com")||previewUrl.contains("www.target.com"))
                    {
                        textCrawler
                                .makePreview(previewUrl, callback, previewUrl
                                        , TextCrawler.NONE);
                        previewProduct.setImageResource(R.drawable.baseline_favorite_black_18dp);
                    }else
                    {
                       CommonUtility.showSimpleDialog(Main.this,getString(R.string.website_not_supported));
                    }

                    previewProduct.setImageResource(R.drawable.baseline_favorite_black_18dp);

                } else {
                    CommonUtility.showSimpleDialog(Main.this,getString( R.string.internet_connection_check));

                }
            } else {
                CommonUtility.showSimpleDialog(Main.this,getString(R.string.no_product_found));

            }


        }
    }

    /**
     * remvoing url from search box
     */
    private void clearSearchText() {
        searchEditText.setText("");
        previewUrl = "";
        listOfUrls.clear();//clearing the url stack
    }

    /**
     * For moving to the web page or url from where we have come back
     */
    private void moveForward() {
        if (listOfUrls.size() > 1) {

            currentUrlIndex = listOfUrls.indexOf(previewUrl);
            if (currentUrlIndex != listOfUrls.size() - 1) {
                CommonUtility.showProgressDailog(Main.this);
                currentUrlIndex++;
                mWebview.loadUrl(listOfUrls.get(currentUrlIndex));
            }
        }
    }

    /**
     * For moving to previous web page or url from where we have came to the current page
     */
    private void moveBackward() {

        if (listOfUrls.size() > 1) {

            currentUrlIndex = listOfUrls.indexOf(previewUrl);
            if (currentUrlIndex != 0 ) {
                CommonUtility.showProgressDailog(Main.this);
                currentUrlIndex--;
                mWebview.loadUrl(listOfUrls.get(currentUrlIndex));
            } else if (currentUrlIndex == 0) {
                currentUrlIndex--;
                CommonUtility.hideProgressDailog(Main.this);
            }
        }


    }

    /**
     * Show or hide the image layout according to the "No Thumbnail" ckeckbox
     */
    private void showHideImage(View image, View parent, boolean show) {
        if (show) {
            image.setVisibility(View.VISIBLE);
            parent.setPadding(5, 5, 5, 5);
            parent.setLayoutParams(new LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, 2f));
        } else {
            image.setVisibility(View.GONE);
            parent.setPadding(5, 5, 5, 5);
            parent.setLayoutParams(new LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, 3f));
        }
    }

    /**
     * Hide keyboard
     */
    private void hideSoftKeyboard() {
        hideSoftKeyboard(searchEditText);

    }

    private void hideSoftKeyboard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager
                .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * removing preview popup and updating the preview icon
     */
    private void releasePreviewArea() {
        previewProduct.setImageResource(R.drawable.baseline_favorite_border_black_18dp);
        popupWindow.dismiss();
        submitButton.setEnabled(true);

    }

    private void openWebView() {
        CommonUtility.showProgressDailog(this);
        mWebview = (WebView) findViewById(R.id.webView);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setLoadWithOverviewMode(true);
        mWebview.getSettings().setUseWideViewPort(true);
        mWebview.getSettings().setDomStorageEnabled(true);
        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                CommonUtility.showProgressDailog(Main.this);
                view.loadUrl(url);

                listOfUrls.add(url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                CommonUtility.hideProgressDailog(Main.this);
                mySwipeRefreshLayout.setRefreshing(false);
                previewUrl = url;
                if(listOfUrls.size()==0)
                {
                    listOfUrls.add(previewUrl);
                }



            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Ignore SSL certificate errors
                handler.proceed();
            }
        });

        mWebview.loadUrl("https://" + searchEditText.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.preview:
                previewProduct();
                break;
            case R.id.action_go:
                initSubmitButton();
                break;
            case R.id.iv_clear_text:
                clearSearchText();
                break;
            case R.id.forward:
                moveForward();
                break;
            case R.id.backward:
                moveBackward();
                break;
        }
    }

    /**
     * back button overriding for handling url or webpage stack(moving backward)
     */
    @Override
    public void onBackPressed() {
        moveBackward();
        if (currentUrlIndex == -1) {
            CommonUtility.hideProgressDailog(Main.this);
            currentUrlIndex--;
            finish();

        }
    }
}





