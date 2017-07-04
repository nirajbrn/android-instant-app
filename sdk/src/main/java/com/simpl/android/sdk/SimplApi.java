/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk;

import android.content.Context;
import android.util.Log;
import com.simpl.android.sdk.executor.Executor;
import com.simpl.android.sdk.utils.FingerPrintUtil;
import com.simpl.android.sdk.utils.NoSSLv3SocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Simpl API implementation
 *
 * @author : Akshay Deo
 * @date : 11/11/15 : 2:19 PM
 * @email : akshay@betacraft.co
 */
final class SimplApi {
  /**
   * TAG for logging
   */
  private static final String TAG = "##SimplApi##";

  /**
   * Constructor
   */
  SimplApi() {
  }

  /**
   * To check if user is approved or not
   *
   * @param user                 {@link SimplUser} instance
   * @param merchantId           Current merchant Id
   * @param simplQueryParams          Extra params to be sent along with main params
   * @param userApprovalListener {@link SimplUserApprovalListenerV2} instance
   */
  void isUserApproved(final SimplUser user, final String merchantId,
        final ArrayList<SimplParam> simplQueryParams, final ArrayList<SimplParam> simplHeaderParams,
      final SimplUserApprovalListenerV2 userApprovalListener) {
    Executor.get().execute(new Runnable() {
      @Override public void run() {
        SSLContext sslcontext = null;
        try {
          sslcontext = SSLContext.getInstance("TLSv1");
          sslcontext.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
          SimplAirbrakeNotifier.notify(e);
          Simpl.getInstance().getGlobalSimplUserApprovalListener().onError(e);
          return;
        }
        SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
        HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
        HttpsURLConnection urlConnection = null;
        try {
          final HashMap<String, String> params = new HashMap<>();
          if (user.getEmailAddress() != null) params.put("email", user.getEmailAddress());
          params.put("phone_number", user.getPhoneNumber());
          params.put("merchant_id", merchantId);
          if (simplQueryParams != null) {
            for (SimplParam param : simplQueryParams) {
              params.put(param.getKey(), param.getValue());
            }
          }
          Log.d(TAG, "Final url params is =>" + getQueryString(params));
          URL url = new URL(Simpl.getInstance().getApprovalsApiBaseUrl() +
              "simpl_buy/approved?" + getQueryString(params));
          Log.d(TAG, "Final url is => " + url.toString());
          urlConnection = (HttpsURLConnection) url.openConnection();

          if (simplHeaderParams != null) {
            for (SimplParam simplParam : simplHeaderParams) {
              Log.d(TAG, "isUserApproved(): params: " + simplParam.getKey()+":"+simplParam.getValue());
              urlConnection.setRequestProperty(simplParam.getKey(), simplParam.getValue());
            }
          }
          urlConnection.setReadTimeout(30000);
          urlConnection.setConnectTimeout(30000);
          final long startTime = System.currentTimeMillis();
          if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            Log.d(TAG, "isUserApproved(): approvalTime: "+String.valueOf(System.currentTimeMillis() - startTime));
            Simpl.getInstance()
                    .getCurrentApplicationContext().getSharedPreferences("SIMPL_PREF", Context.MODE_PRIVATE)
                    .edit().putString("approvalTime", String.valueOf(System.currentTimeMillis() - startTime)).apply();
            BufferedReader in =
                new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            final StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
              response.append(line);
            }
            Log.d(TAG, "response is =>" + response.toString());
            in.close();
            final JSONObject jsonObject = new JSONObject(response.toString());
            userApprovalListener.onSuccess(jsonObject.getJSONObject("data").getBoolean("approved"),
                jsonObject.getJSONObject("data").getString("button_text"),
                jsonObject.getJSONObject("data").getBoolean("first_transaction"));
          } else if (urlConnection.getResponseCode() >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            BufferedReader in =
                new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            final StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
              response.append(line);
            }
            Log.d(TAG, "response is =>" + response.toString());
            in.close();
            final JSONObject jsonObject = new JSONObject(response.toString());
            final StringBuilder errorString = new StringBuilder();
            final JSONArray jsonErrorArray = jsonObject.getJSONArray("errors");
            for (int count = 0; count < jsonErrorArray.length(); ++count) {
              errorString.append(jsonErrorArray.getString(count)).append("\n");
            }
            Log.e(TAG, "Error while approving user => " + errorString.toString());
            userApprovalListener.onError(new Throwable(
                errorString.toString() + "\n" + jsonObject.getString("error_message")));
          } else {
            BufferedReader in =
                new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            final StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
              response.append(line);
            }
            Log.d(TAG, "response is =>" + response.toString());
            in.close();
            final JSONObject jsonObject = new JSONObject(response.toString());
            final StringBuilder errorString = new StringBuilder();
            final JSONArray jsonErrorArray = jsonObject.getJSONArray("errors");
            for (int count = 0; count < jsonErrorArray.length(); ++count) {
              errorString.append(jsonErrorArray.getString(count)).append("\n");
            }
            userApprovalListener.onError(new Throwable(
                errorString.toString() + "\n" + jsonObject.getString("error_message")));
          }
        } catch (Exception exception) {
          Log.e(TAG, "Failed while making request", exception);
          SimplAirbrakeNotifier.notify(exception);
          userApprovalListener.onError(exception);
        } finally {
          if (urlConnection != null) urlConnection.disconnect();
        }
      }
    });
  }

  /**
   * To create query params
   *
   * @param params Params {@link Map}
   * @return Query string
   * @throws UnsupportedEncodingException
   */
  private String getQueryString(HashMap<String, String> params)
      throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (entry.getValue() == null
          || entry.getKey() == null
          || entry.getKey().length() == 0
          || entry.getValue().length() == 0) {
        continue;
      }
      if (first) {
        first = false;
      } else {
        result.append("&");
      }
      Log.d(TAG, "Adding " + entry.getKey() + "  " + entry.getValue());
      result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
      result.append("=");
      result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
    }

    return result.toString();
  }

  void destroy() {
    Executor.get().shutdownNow();
  }
}
