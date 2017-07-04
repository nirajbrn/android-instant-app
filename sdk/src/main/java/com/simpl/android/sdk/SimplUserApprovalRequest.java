package com.simpl.android.sdk;

import com.simpl.android.sdk.model.UserApproval;
import com.simpl.android.sdk.utils.FingerPrintUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Request for asking if {@link SimplUser} is approved to use Simpl payment gateway
 *
 * @author : Akshay Deo
 * @date : 08/01/16 : 2:59 PM
 * @email : akshay@betacraft.co
 */
public final class SimplUserApprovalRequest {
  private static final String TAG = SimplUserApprovalRequest.class.getSimpleName();
  /**
   * {@link SimplUser} for which approval request is being mae
   */
  private SimplUser mUser;
  /**
   * {@link SimplParam} for extra params to be sent through this request
   */
  private ArrayList<SimplParam> mSimplQueryParams;
  /**
   * Merchant id
   */
  private String mMerchantId;
  /**
   * Listener V2
   */
  private SimplUserApprovalListenerV2 mSimplUserApprovalListenerV2;

  /**
   * Constructor
   */
  SimplUserApprovalRequest(final SimplUser user, final String merchantId) {
    mUser = user;
    mMerchantId = merchantId;
    mSimplQueryParams = new ArrayList<>();
  }

  /**
   * To add a custom param
   *
   * @param key   Key of the param
   * @param value Value of the param
   */
  public SimplUserApprovalRequest addParam(final String key, final String value) {
    mSimplQueryParams.add(SimplParam.create(key, value));
    return this;
  }

  /**
   * To execute SimplUserApprovalRequest
   *
   * @param userApprovalListener {@link SimplUserApprovalListenerV2} instance
   */
  public void execute(final SimplUserApprovalListenerV2 userApprovalListener) {
    Simpl.getInstance().getSession().setSimplUser(mUser);
    mSimplUserApprovalListenerV2 = userApprovalListener;
    Simpl.getInstance().setGlobalSimplUserApprovalListener(mSimplUserApprovalListenerV2);
    //TO set fingerprint parameter to header
    FingerPrintUtil fingerPrintUtil = new FingerPrintUtil
            (Simpl.getInstance().getCurrentApplicationContext());
    fingerPrintUtil.setSimplParams(new FingerPrintUtil.FingerPrintListener() {
      @Override
      public void onParamsAvailable(ArrayList<SimplParam> params) {
        for (SimplParam param : mSimplQueryParams) {
          switch (param.getKey()) {
            case "user_location":
              params.add(SimplParam.create("user-location", param.getValue()));

              break;
            case "order_id":
              params.add(SimplParam.create("order-id", param.getValue()));

              break;
            case "transaction_amount_in_paise":
              params.add(SimplParam.create("transaction-amount-in-paise", param.getValue()));

              break;
            case "member_since":
              params.add(SimplParam.create("member-since", param.getValue()));

              break;
            default:
              params.add(SimplParam.create(param.getKey(), param.getValue()));
              break;
          }
        }
        makeApiCallV2(params);
      }
    });
  }

    private void makeApiCallV2(final ArrayList<SimplParam> headerParams) {
    new SimplApi().isUserApproved(mUser, mMerchantId, mSimplQueryParams, headerParams,
        new SimplUserApprovalListenerV2() {
          @Override public void onSuccess(final boolean status, final String buttonText,
              final boolean showSimplIntroduction) {
            Simpl.getInstance()
                .getSession()
                .setUserApproval(new UserApproval(status, showSimplIntroduction));
            mSimplUserApprovalListenerV2.onSuccess(status, buttonText, showSimplIntroduction);
          }

          @Override public void onError(final Throwable throwable) {
            mSimplUserApprovalListenerV2.onError(throwable);
            Simpl.getInstance().getSession().setUserApproval(new UserApproval(false, false));
            final Map<String, String> metaData = new HashMap<>();
            metaData.put("user", mUser.toString());
            SimplAirbrakeNotifier.notify(throwable, metaData);
          }
        });
  }
}
