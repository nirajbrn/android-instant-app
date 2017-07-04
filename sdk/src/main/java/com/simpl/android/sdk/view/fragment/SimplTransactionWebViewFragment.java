/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk.view.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.simpl.android.sdk.BuildConfig;
import com.simpl.android.sdk.R;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplAirbrakeNotifier;
import com.simpl.android.sdk.SimplParam;
import com.simpl.android.sdk.SimplTransaction;
import com.simpl.android.sdk.SimplTransactionAuthorization;
import com.simpl.android.sdk.executor.Executor;
import com.simpl.android.sdk.receiver.SmsBroadcastReceiver;
import com.simpl.android.sdk.utils.FingerPrintUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class SimplTransactionWebViewFragment extends Fragment {

  public static final String TAG = SimplTransactionWebViewFragment.class.getSimpleName();

  private static final String TRANSACTION_KEY = "transaction";
  private static final String MERCHANT_ID_KEY = "merchant_id";
  private static final String IS_FIRST_TRANSACTION = "first_transaction";
  private static final String PARAMS = "params";
  private static final String TRANSACTION_HANDOVER_BODY_FORMAT = "{\"t\":\"%s\",\"h\":\"%s\"}";
  private static final String CONFIRM_CLICK_HANDOVER_BODY_FORMAT = "{\"t\":\"%s\",\"c\":\"%s\"}";
  private boolean mIsResultSent = false;
  /**
   * Progress dialog
   */
  private ProgressDialog mProgressDialog;
  /**
   * SMS broadcast receiver
   */
  private SmsBroadcastReceiver mSmsBroadcastReceiver;

  private SimplTransaction mTransaction;
  private String mMerchantId;
  private boolean mIsFirstTransaction;
  private ArrayList<SimplParam> mParams;

  public SimplTransactionWebViewFragment() {
    // Required empty public constructor
  }

  public static SimplTransactionWebViewFragment newInstance(final String merchantId,
      final SimplTransaction transaction, final ArrayList<SimplParam> params,
      final boolean isFirstTransaction) {
    final SimplTransactionWebViewFragment fragment = new SimplTransactionWebViewFragment();
    final Bundle bundle = new Bundle();
    bundle.putParcelable(TRANSACTION_KEY, transaction);
    bundle.putString(MERCHANT_ID_KEY, merchantId);
    bundle.putBoolean(IS_FIRST_TRANSACTION, isFirstTransaction);
    bundle.putParcelableArrayList(PARAMS, params);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.__fragment_simpl_web_view, container, false);
    mTransaction = getArguments().getParcelable(TRANSACTION_KEY);
    mMerchantId = getArguments().getString(MERCHANT_ID_KEY);
    mIsFirstTransaction = getArguments().getBoolean(IS_FIRST_TRANSACTION);
    mParams = getArguments().getParcelableArrayList(PARAMS);
    bindView(view);
    Log.d(TAG, "Package name =>" + getActivity().getPackageName());
    return view;
  }

  @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface" })
  private void bindView(final View parentView) {
    final WebView webView = (WebView) parentView.findViewById(R.id.__simpl_web_view);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    webView.setVerticalScrollBarEnabled(true);
    if (getActivity().getPackageManager()
        .checkPermission(Manifest.permission.RECEIVE_SMS, getActivity().getPackageName())
        == PackageManager.PERMISSION_GRANTED) {
      mSmsBroadcastReceiver = new SmsBroadcastReceiver();
      getActivity().registerReceiver(mSmsBroadcastReceiver,
          new IntentFilter(SmsBroadcastReceiver.SMS_RECEIVED));
      Log.d(TAG, "Broadcast receiver setting");
      // setting up the otp listener in case we are gonna use it
      mSmsBroadcastReceiver.setOtpReceivedListener(new SmsBroadcastReceiver.OtpReceivedListener() {
        @Override public void onReceive(final String otp) {
          Log.d(TAG, "Got otp =>" + otp);
          callJavaScript(webView, "fillOTP", otp);
        }
      });
    }

    webView.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.d(TAG, consoleMessage.message());
        return true;
      }
    });
    webView.addJavascriptInterface(new Android() {
      /**
       * {@link SimplTransactionAuthorization} instance
       */
      private boolean mSuccess;
      private SimplTransactionAuthorization mAuthorization;
      private Throwable mThrowable;
      private Timer mTimer;
      private int mTimeout = 5000;

      @Override @JavascriptInterface public void showToast(final String message) {
        Log.d(TAG, "Got from webview =>" + message);
        try {
          final JSONObject responseJson = new JSONObject(message);
          if (responseJson.has("cdata")) {
            Log.d(TAG, "Sending confirm call");
            sendConfirmHandoverStatus(responseJson.getJSONObject("cdata").getString("t"),
                responseJson.getJSONObject("cdata").getBoolean("c"));
          }
          if (responseJson.getString("status").contentEquals("success")) {
            mSuccess = true;
            mAuthorization =
                new SimplTransactionAuthorization(responseJson.getString("transaction_token"));
          } else {
            mSuccess = false;
            mThrowable = new Throwable(TAG + " : " + responseJson.getString("message"));
          }
          if (responseJson.has("activity_timeout")) {
            mTimeout = responseJson.getInt("activity_timeout");
          }
        } catch (final Exception e) {
          mSuccess = false;
          mThrowable = e;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
          @Override public void run() {
            if (getActivity() != null) {
              sendResult();
              return;
            }
            SimplAirbrakeNotifier.notify(
                new Throwable("Activity was null when timer " + "exposed"));
          }
        }, mTimeout);
      }

      private void sendResult() {
        mIsResultSent = true;
        if (mTimer != null) {
          mTimer.cancel();
          mTimer = null;
        }
        if (mSuccess) {
          sendTokenHandoverStatus(mAuthorization.getTransactionToken(), true);
          Simpl.getInstance().getGlobalTransactionAuthorizationListener().onSuccess(mAuthorization);
          getActivity().finish();
          return;
        }
        sendTokenHandoverStatus("", false);
        final Map<String, String> metaData = new HashMap<>();
        metaData.put("user", mTransaction.getUser().toString());
        metaData.put("transaction", mTransaction.toString());
        SimplAirbrakeNotifier.notify(mThrowable, metaData);
        Simpl.getInstance().getGlobalTransactionAuthorizationListener().onError(mThrowable);
        if (getActivity() != null && !getActivity().isFinishing()) {
          getActivity().finish();
        }
      }

      @Override @JavascriptInterface public void dismissLoader() {
        if (getActivity() != null && !getActivity().isFinishing()
                && mProgressDialog != null && mProgressDialog.isShowing())
          mProgressDialog.dismiss();
      }

      @Override @JavascriptInterface public void close() {
        sendResult();
      }
    }, "Android");

    webView.setWebViewClient(new WebViewClient() {

      @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (getActivity() != null && !getActivity().isFinishing()) {
          if (mProgressDialog != null && mProgressDialog.isShowing()) return;
          mProgressDialog = new ProgressDialog(getActivity());
          mProgressDialog.setIndeterminate(true);
          mProgressDialog.setCanceledOnTouchOutside(false);
          mProgressDialog.setCancelable(false);
          mProgressDialog.setMessage("Loading...");
          mProgressDialog.show();
        }
      }

      @Override public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        Log.d(TAG, "Loading " + url);
      }

      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (getActivity() != null && !getActivity().isFinishing()
                && mProgressDialog != null && mProgressDialog.isShowing())
          mProgressDialog.dismiss();
        mProgressDialog = null;
      }

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(TAG, "Processing webview url click..." + url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          CookieManager.getInstance().flush();
        } else {
          CookieSyncManager.getInstance().sync();
        }
        if (url.startsWith("mailto:")) {
          final MailTo mt = MailTo.parse(url);
          startActivity(newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc()));
          view.reload();
          return true;
        }
        return false;
      }

      private Intent newEmailIntent(String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
      }
    });
    final StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append("https://")
        .append(Simpl.getInstance().getAuthorizer())
        .append("authorize?")
        .append("transaction_amount=")
        .append(mTransaction.getAmountInPaise())
        .append("&merchant_id=")
        .append(mMerchantId)
        .append("&first_transaction=")
        .append(mIsFirstTransaction ? "true" : "false")
        .append("&SIMPL-RESPONSE-TIME=")
        .append(getActivity().getSharedPreferences("SIMPL_PREF", Context.MODE_PRIVATE).getString("approvalTime", ""))
        .append("&src=android");
    if (mTransaction.getUser().getEmailAddress() != null) {
      urlBuilder.append("&email=").append(mTransaction.getUser().getEmailAddress());
    }
      urlBuilder.append("&phone_number=").append(mTransaction.getUser().getPhoneNumber());
    if (mParams != null) {
      for (final SimplParam param : mParams) {
        urlBuilder.append("&").append(param.getKey()).append("=").append(param.getValue());
      }
    }
    Log.d(TAG, urlBuilder.toString());
    final Map<String, String> headers = new HashMap<>();
    //TO set fingerprint parameter to header
    /*FingerPrintUtil fingerPrintUtil = new FingerPrintUtil(getActivity());
    fingerPrintUtil.setSimplParams(new FingerPrintUtil.FingerPrintListener() {
      @Override
      public void onParamsAvailable(ArrayList<SimplParam> simplParams) {
        for (SimplParam simplParam : simplParams) {
          Log.d(TAG, "bindView(): params: " + simplParam.getKey()+":"+simplParam.getValue());
          headers.put(simplParam.getKey(), simplParam.getValue());
        }
        headers.put("SIMPL-RESPONSE-TIME",
                getActivity().getSharedPreferences("SIMPL_PREF", Context.MODE_PRIVATE).getString("approvalTime", ""));
        webView.loadUrl(urlBuilder.toString(), headers);
      }
    });*/
    webView.loadUrl(urlBuilder.toString());

    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        webView.loadUrl("javascript:JsInterface.getVerificationId(verification_id);");
        webView.addJavascriptInterface(new JsInterface() {
          @Override
          public void getVerificationId(final String id) {

            Log.d(TAG, "getVerificationId(): "+id);
          }
        }, "JsInterface");

      }
    }, 2000);
  }

  /**
   * Helper method to call a javascript function from web view
   *
   * @param view       {@link WebView} instance
   * @param methodName Javascript function name
   * @param params     Function params
   */
  private void callJavaScript(final WebView view, final String methodName, final Object... params) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("javascript:try{");
    stringBuilder.append(methodName);
    stringBuilder.append("(");
    String separator = "";
    for (Object param : params) {
      stringBuilder.append(separator);
      separator = ",";
      if (param instanceof String) {
        stringBuilder.append("'");
      }
      stringBuilder.append(param);
      if (param instanceof String) {
        stringBuilder.append("'");
      }
    }
    stringBuilder.append(")}catch(error){console.error(error.message);}");
    final String call = stringBuilder.toString();
    Log.i(TAG, "callJavaScript: call=" + call);
    view.loadUrl(call);
  }

  /**
   * Javascript interface class
   */
  private interface Android {
    void showToast(final String message);

    void dismissLoader();

    void close();
  }

  private interface JsInterface {
    void getVerificationId(final String verificationId);
  }

  /**
   * Called when the fragment is no longer in use.  This is called
   * after {@link #onStop()} and before {@link #onDetach()}.
   */
  @Override public void onDestroy() {
    super.onDestroy();
    if (getActivity() == null) {
      return;
    }
    try {
      if (mSmsBroadcastReceiver != null)
        getActivity().unregisterReceiver(mSmsBroadcastReceiver);
    } catch (final Exception exception) {
      // I know this is bad, but with inline broadcast receivers, there are a lot of
      // surprises which I don't wanna let users know. So "Screw it and let's do it" !!
      final Map<String, String> metaData = new HashMap<>();
      metaData.put("user", mTransaction.getUser().toString());
      metaData.put("transaction", mTransaction.toString());
      SimplAirbrakeNotifier.notify(exception, metaData);
    }
    if (!mIsResultSent) {
      sendTokenHandoverStatus("", false);
      final Map<String, String> metaData = new HashMap<>();
      metaData.put("user", mTransaction.getUser().toString());
      metaData.put("transaction", mTransaction.toString());
      final Throwable throwable = new Throwable("user_cancelled");
      SimplAirbrakeNotifier.notify(throwable, metaData);
      Simpl.getInstance().getGlobalTransactionAuthorizationListener().onError(throwable);
    }
    if (mProgressDialog == null) return;
    if (getActivity() != null && !getActivity().isFinishing()
            && mProgressDialog.isShowing())
      mProgressDialog.dismiss();
    mProgressDialog = null;
  }

  /**
   * To check if user is approved or not
   *
   * @param transactionToken Transaction token
   * @param success          Boolean stating the status of handover
   */
  void sendConfirmHandoverStatus(final String transactionToken, final boolean success) {
    Executor.get().execute(new Runnable() {
      @Override public void run() {
        HttpsURLConnection urlConnection = null;
        try {
          final URL url = new URL(Simpl.getInstance().getBaseUrl() + "_c");
          Log.d(TAG, "Final url is => " + url.toString());
          urlConnection = (HttpsURLConnection) url.openConnection();
          urlConnection.setRequestMethod("POST");
          urlConnection.setRequestProperty("sdk-version", BuildConfig.VERSION_NAME);
          urlConnection.setRequestProperty("Content-Type", "application/json");
          urlConnection.setReadTimeout(30000);
          urlConnection.setConnectTimeout(30000);
          final String body = String.format(CONFIRM_CLICK_HANDOVER_BODY_FORMAT, transactionToken,
              success ? "true" : "false");
          Log.d(TAG, "Sending body " + body);
          byte[] outputInBytes = body.getBytes("UTF-8");
          final OutputStream os = urlConnection.getOutputStream();
          os.write(outputInBytes);
          os.close();
          BufferedReader in =
              new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
          final StringBuilder response = new StringBuilder();
          String line;
          while ((line = in.readLine()) != null) {
            response.append(line);
          }
          Log.d(TAG, "response is =>" + response.toString());
          in.close();
          if (urlConnection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            final JSONObject jsonObject = new JSONObject(response.toString());
            final StringBuilder errorString = new StringBuilder();
            final JSONArray jsonErrorArray = jsonObject.getJSONArray("errors");
            for (int count = 0; count < jsonErrorArray.length(); ++count) {
              errorString.append(jsonErrorArray.getString(count)).append("\n");
            }
            SimplAirbrakeNotifier.notify(new Throwable(
                errorString.toString() + "\n" + jsonObject.getString("error_message")));
          }
        } catch (Exception exception) {
          Log.e(TAG, "Failed while making request", exception);
          SimplAirbrakeNotifier.notify(exception);
        } finally {
          if (urlConnection != null) urlConnection.disconnect();
        }
      }
    });
  }

  /**
   * To check if user is approved or not
   *
   * @param transactionToken Transaction token
   * @param success          Boolean stating the status of handover
   */
  void sendTokenHandoverStatus(final String transactionToken, final boolean success) {
    Executor.get().execute(new Runnable() {
      @Override public void run() {
        HttpsURLConnection urlConnection = null;
        try {
          final URL url = new URL(Simpl.getInstance().getBaseUrl() + "_h");
          Log.d(TAG, "Final url is => " + url.toString());
          urlConnection = (HttpsURLConnection) url.openConnection();
          urlConnection.setRequestMethod("POST");
          urlConnection.setRequestProperty("Sdk-Version", BuildConfig.VERSION_NAME);
          urlConnection.setRequestProperty("Content-Type", "application/json");
          urlConnection.setReadTimeout(30000);
          urlConnection.setConnectTimeout(30000);
          final String body = String.format(TRANSACTION_HANDOVER_BODY_FORMAT, transactionToken,
              success ? "true" : "false");
          Log.d(TAG, "Sending body " + body);
          byte[] outputInBytes = body.getBytes("UTF-8");
          final OutputStream os = urlConnection.getOutputStream();
          os.write(outputInBytes);
          os.close();
          BufferedReader in =
              new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
          final StringBuilder response = new StringBuilder();
          String line;
          while ((line = in.readLine()) != null) {
            response.append(line);
          }
          Log.d(TAG, "response is =>" + response.toString());
          in.close();
          if (urlConnection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            final JSONObject jsonObject = new JSONObject(response.toString());
            final StringBuilder errorString = new StringBuilder();
            final JSONArray jsonErrorArray = jsonObject.getJSONArray("errors");
            for (int count = 0; count < jsonErrorArray.length(); ++count) {
              errorString.append(jsonErrorArray.getString(count)).append("\n");
            }
            SimplAirbrakeNotifier.notify(new Throwable(
                TAG + " : " +errorString.toString() + "\n" + jsonObject.getString("error_message")));
          }
        } catch (Exception exception) {
          Log.e(TAG, "Failed while making request", exception);
          SimplAirbrakeNotifier.notify(exception);
        } finally {
          if (urlConnection != null) urlConnection.disconnect();
        }
      }
    });
  }
}
