/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.simpl.android.sdk.utils.FingerPrintUtil;
import com.simpl.android.sdk.view.activity.BaseSimplScreen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.simpl.android.sdk.utils.FingerPrintUtil.SIMPL_Ac;
import static com.simpl.android.sdk.utils.FingerPrintUtil.SIMPL_Cl;
import static com.simpl.android.sdk.utils.FingerPrintUtil.SIMPL_Lnt;
import static com.simpl.android.sdk.utils.FingerPrintUtil.SIMPL_Ps;

/**
 * This is the one point access class for all the functionality associated with the SDK.
 *
 * @author : Akshay Deo
 * @date : 14/10/15 : 5:55 PM
 * @email : akshay@betacraft.co
 */
public final class Simpl {
  /**
   * TAG for logging
   */
  private static final String TAG = "###Simpl-SDK###";
  /**
   * Singleton instance
   */
  private static Simpl instance;
  /**
   * Merchant ID from Simpl dashboard
   */
  private String mMerchantId;
  /**
   * Global {@link com.simpl.android.sdk.SimplAuthorizeTransactionListener}
   */
  private SimplAuthorizeTransactionListener mGlobalTransactionAuthorizationListener;
  /**
   * Global {@link com.simpl.android.sdk.SimplUserApprovalListenerV2}
   */
  private SimplUserApprovalListenerV2 mGlobalSimplUserApprovalListener;
  /**
   * Current {@link SimplSession}
   */
  private SimplSession mSimplSession;
  /**
   * Is in Sandbox mode
   */
  private boolean mIsInSandboxMode;

  /**
   * Is in staging
   */
  private boolean mIsInStaging;

  /**
   * Is in testing
   */
  private boolean mIsInDebug;
  /**
   * Current application {@link Context}
   */
  private Context mApplicationContext;
  /**
   * Flags
   */
  private ArrayList<String> mMerchantPermFlag;

  /**
   * APi End point for testing
   */
  private String mApiEndPoint;

  /**
   * Front End point for testing
   */
  private String mFrontEndPoint;

  /**
   * Approval API
   */
  private String mApprovalApi;

  /**
   * Getter for {@link SimplSession} associated with current {@link Simpl} instance
   *
   * @return {@link SimplSession} instance
   */
  public SimplSession getSession() {
    return mSimplSession;
  }

  /**
   * Private constructor
   *
   * @param context       Current {@link Context} instance
   * @param inSandboxMode Boolean flag to indicate if the SDK should work in Sandbox mode or
   *                      not.
   */
  private Simpl(final Context context, final boolean inSandboxMode) {
    mMerchantPermFlag = new ArrayList<>();
    mApplicationContext = context.getApplicationContext();
    mIsInSandboxMode = inSandboxMode;
    SimplAirbrakeNotifier.register(mApplicationContext.getApplicationContext(),
        "fb68fde12f8d24307fa351f463d75d12", mIsInSandboxMode ? "sandbox" : "production");
    final Map<String, String> extraData = new HashMap<>();
    extraData.put("sdk-version", BuildConfig.VERSION_NAME);
    SimplAirbrakeNotifier.setExtraData(extraData);
    mSimplSession = new SimplSession();
    /*try {
      ApplicationInfo ai = context.getApplicationContext()
          .getPackageManager()
          .getApplicationInfo(context.getApplicationContext().getPackageName(),
              PackageManager.GET_META_DATA);
      Bundle bundle = ai.metaData;
      mMerchantId = bundle.getString("com.simpl.android.sdk.merchant_id");
      if (mMerchantId == null) {
        SimplAirbrakeNotifier.notify(new Throwable(TAG + " Simpl(): " + "Merchant Id is not added"));
        throw new RuntimeException("Please add\n<meta-data\n" +
            "    android:name=\"com.simpl.android.sdk.merchant_id\"\n" +
            "    android:value=\"CLIENT_ID_FROM_SIMPL_DASHBOARD\" />\n\nto your " +
            "AndroidManifest.xml");
      }
    } catch (PackageManager.NameNotFoundException e) {
      SimplAirbrakeNotifier.notify(new Throwable(TAG + " : "+e));
      throw new RuntimeException("Please add\n<meta-data\n" +
          "    android:name=\"com.simpl.android.sdk.merchant_id\"\n" +
          "    android:value=\"CLIENT_ID_FROM_SIMPL_DASHBOARD\" />\n\nto your " +
          "AndroidManifest.xml");
    } catch (NullPointerException e) {
      SimplAirbrakeNotifier.notify(new Throwable(TAG + " : "+e));
      throw new RuntimeException("There was some issue while reading the meta-data from "
          + "AndroidManifest.xml, contact ping@getsimpl.com.");
    }*/
  }

  /**
   * To initialize Simpl SDK
   *
   * @param application   Current {@link Application} instance
   * @param inSandboxMode Boolean flag to indicate if the SDK should work in Sandbox mode or
   *                      not.
   */
  @Deprecated public static void init(@NonNull final Application application,
      final boolean inSandboxMode) {
    Log.e(TAG, ".\n++++++++++++++++++++++\n" + "This method is deprecated, please use " +
        "Simpl.init(application)\n++++++++++++++++++++++\\n\" +");
    Log.d(TAG, "Initializing Simpl SDK");
    if (instance != null) {
      Log.w(TAG, "Simpl is already initialized");
      return;
    }
    instance = new Simpl(application, inSandboxMode);
  }

  /**
   * To initialize Simpl SDK
   *
   * @param application Current {@link Application} instance
   */
  public static void init(@NonNull final Application application) {
    Log.d(TAG, "Initializing Simpl SDK");
    if (instance != null) {
      Log.w(TAG, "Simpl is already initialized");
      return;
    }
    instance = new Simpl(application, false);
  }

  /**
   * To initialize Simpl SDK
   *
   * @param applicationContext Current {@link Context} instance
   */
  public static void init(@NonNull final Context applicationContext) {
    Log.d(TAG, "Initializing Simpl SDK");
    if (instance != null) {
      Log.w(TAG, "Simpl is already initialized");
      return;
    }
    instance = new Simpl(applicationContext, false);
  }

  /**
   * To run the Simpl SDK in sandbox mode
   */
  public void runInSandboxMode() {
    mIsInSandboxMode = true;
    SimplAirbrakeNotifier.updateEnvironmentName("sandbox");
  }

  /**
   * To run in stagin mode
   * Make it private before release
   */
  public void runInStagingMode() {
    mIsInStaging = true;
    SimplAirbrakeNotifier.updateEnvironmentName("staging");
  }

  /**
   * To run in testing mode
   * Make it private before release
   */
  public void runInDebugMode() {
    mIsInDebug = true;
    SimplAirbrakeNotifier.updateEnvironmentName("testing");
  }

    /**
     * to add flag to simpl config
     * @param s flag code to be added
     */
  public void setDisable(@NonNull String s) {
    switch (s) {
      case SIMPL_Ac:
        mMerchantPermFlag.add(FingerPrintUtil.ACCOUNT_PERM_CODE);
        break;
      case SIMPL_Cl:
        mMerchantPermFlag.add(FingerPrintUtil.CALL_PERM_CODE);
        break;
      case SIMPL_Lnt:
        mMerchantPermFlag.add(FingerPrintUtil.LOCATION_PERM_CODE);
        break;
      case SIMPL_Ps:
        mMerchantPermFlag.add(FingerPrintUtil.PHONE_PERM_CODE);
        break;
      default:
        break;
    }
  }

    /**
     * getter for disable flag
     * @return
     */
  public ArrayList<String> getDisabled() {
    return mMerchantPermFlag;
  }

  /**
   * Getter for the Simpl instance
   */
  public static Simpl getInstance() {
    if (instance == null) {
      throw new RuntimeException("Please call init() before accessing the instance" +
          ".\n++++++++++++++++++++++\n" +
          "Make sure you have called Simpl.init(getApplicationContext(),inSandboxMode) " +
          "in your Application class. If you don't have an Application class, please " +
          "create a class extending android.app.Application. And override onCreate() " +
          "method to add this call." +
          "\n++++++++++++++++++++++\\n\" +");
    }
    return instance;
  }

  /**
   * Method to check if the current User is approved for using Simpl
   *
   * @param phoneNumber  Phone number of the user
   * @param emailAddress Email address of the user
   */
  public SimplUserApprovalRequest isUserApproved(@NonNull final String phoneNumber,
      final String emailAddress) {
    return new SimplUserApprovalRequest(new SimplUser(emailAddress, phoneNumber), mMerchantId);
  }

  /**
   * Method to check if the current {@link SimplUser} is approved for using Simpl
   *
   * @param user {@link SimplUser} instance
   */
  public SimplUserApprovalRequest isUserApproved(@NonNull final SimplUser user) {
    return new SimplUserApprovalRequest(user, mMerchantId);
  }

  /**
   * To authorize a transaction
   *
   * @param context                  Current {@link Context}
   * @param transactionAmountInPaise Transaction amount in paise
   */
  public SimplAuthorizeTransactionRequest authorizeTransaction(@NonNull final Context context,
      final long transactionAmountInPaise) {
    if (mSimplSession.getUserApproval() == null) {
      throw new RuntimeException(
          "Please call Simpl.isUserApproved() before calling this " + "method");
    }
    if (!mSimplSession.getUserApproval().isApproved()) {
      throw new RuntimeException("Current user is not allowed to use Simpl");
    }
    return new SimplAuthorizeTransactionRequest(context, mSimplSession.getSimplUser(),
        transactionAmountInPaise, mMerchantId);
  }

  /**
   * To start web view SDK flow
   *
   * @param context               Current {@link Context}
   * @param showSimplIntroduction Boolean indicating if the Simpl introduction to be shown or not
   * @param transaction           {@link SimplTransaction} instance
   */
  private void startWebView(final Context context, final boolean showSimplIntroduction,
      final SimplTransaction transaction) {
    context.startActivity(new Intent(context, BaseSimplScreen.class).addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .putExtra(BaseSimplScreen.MERCHANT_ID, mMerchantId)
        .putExtra(BaseSimplScreen.FIRST_TRANSACTION, showSimplIntroduction)
        .putExtra(BaseSimplScreen.TRANSACTION, transaction));
  }

  /**
   * Setter for global {@link com.simpl.android.sdk.SimplAuthorizeTransactionListener}
   *
   * @param listener {@link com.simpl.android.sdk
   *                 .SimplAuthorizeTransactionListener} instance
   */
  public void setGlobalSimplTransactionAuthorizationListener(
      @NonNull final SimplAuthorizeTransactionListener listener) {
    mGlobalTransactionAuthorizationListener = listener;
  }

  /**
   * Getter for global {@link com.simpl.android.sdk
   * .SimplAuthorizeTransactionListener}
   *
   * @return Gloabl {@link com.simpl.android.sdk.SimplAuthorizeTransactionListener}
   * instance
   */
  @Nullable public SimplAuthorizeTransactionListener getGlobalTransactionAuthorizationListener() {
    return mGlobalTransactionAuthorizationListener;
  }

  /**
   * Setter for global {@link com.simpl.android.sdk.SimplUserApprovalListenerV2}
   * @param listener {@link com.simpl.android.sdk
   *                 .SimplUserApprovalListenerV2} instance
   */

  public void setGlobalSimplUserApprovalListener(
          @NonNull final SimplUserApprovalListenerV2 listener) {
    mGlobalSimplUserApprovalListener = listener;
  }

  /**
   * Getter for global {@link com.simpl.android.sdk.SimplUserApprovalListenerV2}
   * @return Global {@link com.simpl.android.sdk.SimplUserApprovalListenerV2}
   * instance
   */
  @NonNull
  public SimplUserApprovalListenerV2 getGlobalSimplUserApprovalListener() {
    return mGlobalSimplUserApprovalListener;
  }

  /**
   * Getter for {@link Simpl#mApplicationContext}
   *
   * @return {@link Context} instance
   */
  Context getCurrentApplicationContext() {
    return mApplicationContext;
  }

  /**
   * To get base URL for api calls
   *
   * @return BASE URL
   */
  public String getBaseUrl() {
    if (mIsInStaging) {
      return "https://staging-api.getsimpl.com/api/v1/";
    } else if (mIsInSandboxMode) {
      return "https://sandbox-api.getsimpl.com/api/v1/";
    } else if (mIsInDebug) {
      return mApiEndPoint;
    }
    return "https://api.getsimpl.com/api/v1/";
  }

  /**
   * To get the authorizer
   *
   * @return Authorizer
   */
  public String getAuthorizer() {
    if (mIsInStaging) {
      return "staging.getsimpl.com/api/v1/";
    } else if (mIsInSandboxMode) {
      return "sandbox.getsimpl.com/api/v1/";
    } else if (mIsInDebug) {
      return mFrontEndPoint;
    }
    return "getsimpl.com/api/v1/";
  }

  /**
   * To get the base url for approvals api
   */
  protected String getApprovalsApiBaseUrl() {
    if (mIsInStaging) {
      return "https://staging-approvals-api.getsimpl.com/api/v1/";
    } else if (mIsInSandboxMode) {
      return "https://sandbox-approvals-api.getsimpl.com/api/v1/";
    } else if (mIsInDebug) {
      return mApprovalApi;
    }
    return "https://approvals-api.getsimpl.com/api/v1/";
  }

  /**
   * To check either in current session user is approved or not
   * @return
   */
  public boolean isSimplApproved() {
    return getSession().getUserApproval() != null && getSession().getUserApproval().isApproved();
  }

  public boolean isUserFirstTransaction() {
    return getSession().getUserApproval() != null && getSession().getUserApproval().isFirstTransaction();
  }

  public void setMerchantId(String merchantId) {
    if (!TextUtils.isEmpty(merchantId))
      mMerchantId = merchantId;
  }

  public void setApiEndPoint(String apiEndPoint) {
    this.mApiEndPoint = apiEndPoint;
  }

  public void setFrontEndPoint(String frontEndPoint) {
    this.mFrontEndPoint = frontEndPoint;
  }

  public void setApprovalApi(String approvalApi) {
    this.mApprovalApi = approvalApi;
  }

}
