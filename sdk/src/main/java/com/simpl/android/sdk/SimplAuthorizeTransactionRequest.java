package com.simpl.android.sdk;

import android.content.Context;
import android.content.Intent;
import com.simpl.android.sdk.view.activity.BaseSimplScreen;
import java.util.ArrayList;

/**
 * To authorize a give transaction
 *
 * @author : Akshay Deo
 * @date : 08/01/16 : 2:59 PM
 * @email : akshay@betacraft.co
 */
public final class SimplAuthorizeTransactionRequest {
  /**
   * {@link Context} context
   */
  private Context mContext;
  /**
   * {@link SimplUser} for which approval request is being mae
   */
  private SimplUser mUser;
  /**
   * Transaction amount
   */
  private long mTransactionAmount;
  /**
   * {@link SimplParam} for extra params to be sent through this request
   */
  private ArrayList<SimplParam> mSimplParams;
  /**
   * Merchant id
   */
  private String mMerchantId;

  /**
   * Constructor
   *
   * @param user              {@link SimplUser} instance
   * @param transactionAmount Transaction amount
   * @param merchantId        Merchant ID
   */
  SimplAuthorizeTransactionRequest(final Context context, final SimplUser user,
      final long transactionAmount, final String merchantId) {
    mContext = context;
    mUser = user;
    mMerchantId = merchantId;
    mTransactionAmount = transactionAmount;
    mSimplParams = new ArrayList<>();
  }

  /**
   * To add a custom param
   *
   * @param key   Key of the param
   * @param value Value of the param
   */
  public SimplAuthorizeTransactionRequest addParam(final String key, final String value) {
    mSimplParams.add(SimplParam.create(key, value));
    return this;
  }

  /**
   * To execute {@link SimplAuthorizeTransactionRequest}
   *
   * @param authorizeTransactionListener {@link SimplAuthorizeTransactionListener} instance
   */
  public void execute(final SimplAuthorizeTransactionListener authorizeTransactionListener) {
    Simpl.getInstance()
        .setGlobalSimplTransactionAuthorizationListener(authorizeTransactionListener);
    mContext.startActivity(new Intent(mContext, BaseSimplScreen.class).addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .putExtra(BaseSimplScreen.MERCHANT_ID, mMerchantId)
        .putExtra(BaseSimplScreen.FIRST_TRANSACTION,
            Simpl.getInstance().getSession().getUserApproval().isFirstTransaction())
        .putExtra(BaseSimplScreen.TRANSACTION, new SimplTransaction(mUser, mTransactionAmount))
        .putExtra(BaseSimplScreen.PARAMS, mSimplParams));
  }
}
