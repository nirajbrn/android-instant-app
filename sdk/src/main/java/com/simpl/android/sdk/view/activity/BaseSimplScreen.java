/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk.view.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.simpl.android.sdk.R;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplParam;
import com.simpl.android.sdk.SimplTransaction;
import com.simpl.android.sdk.SimplUser;
import com.simpl.android.sdk.view.fragment.SimplTransactionWebViewFragment;
import java.util.ArrayList;

public class BaseSimplScreen extends FragmentActivity {
  /**
   * TAG for logging
   */
  private static final String TAG = "##SimplScreen##";
  // KEYS for bundle
  public static final String MERCHANT_ID = "merchant_id";
  public static final String TRANSACTION = "transaction";
  public static final String PARAMS = "params";
  public static final String FIRST_TRANSACTION = "first_transaction";

  /**
   * {@link SimplTransaction} instance got from intent
   */
  private SimplTransaction mSimplTransaction;
  private String mMerchantId;
  private Fragment mFragment;
  private boolean mIsFirstTransaction;
  private ArrayList<SimplParam> mParams;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.__activity_simpl);
    Simpl.init(getApplication());
    Simpl.getInstance().runInStagingMode();
    SimplUser user = new SimplUser("nirajbrn@mail.com", "7204261097");
    mSimplTransaction = new SimplTransaction(user, 2500);//getIntent().getExtras().getParcelable(TRANSACTION);
    mMerchantId = "cc253a6252f4472dee9bd3539d594c10";//getIntent().getExtras().getString(MERCHANT_ID);
    mIsFirstTransaction = false;//getIntent().getExtras().getBoolean(FIRST_TRANSACTION);
    mParams = new ArrayList<>();//getIntent().getParcelableArrayListExtra(PARAMS);
    mParams.add(SimplParam.create("transaction_amount_in_paise", "2500"));
    showTransactionWebView();
  }

  /**
   * To continue the transaction using web view
   */
  private void showTransactionWebView() {
    addFragment(R.id.__simpl_fragment_container,
        SimplTransactionWebViewFragment.newInstance(mMerchantId, mSimplTransaction, mParams,
            mIsFirstTransaction), SimplTransactionWebViewFragment.TAG);
  }

  /**
   * To add the fragment into the given Container ID
   *
   * @param containerId {@link android.view.ViewGroup} id
   * @param fragment    {@link Fragment} to be added
   * @param tag         TAG to be associated with the fragment
   */
  public void addFragment(final int containerId, final Fragment fragment, final String tag) {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    if (mFragment != null) fragmentTransaction.remove(fragment);
    mFragment = fragment;
    fragmentTransaction.replace(containerId, fragment, tag);
    fragmentTransaction.commitAllowingStateLoss();
  }
}
