/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model that represents a transaction in Simpl system
 *
 * @author : Akshay Deo
 * @date : 15/10/15 : 12:59 PM
 * @email : akshay@betacraft.co
 */
public final class SimplTransaction implements Parcelable {
  /**
   * User who is performing the transaction
   */
  private SimplUser mUser;
  /**
   * Transaction amount in paise
   */
  private long mAmountInPaise;

  protected SimplTransaction(Parcel in) {
    mUser = in.readParcelable(SimplUser.class.getClassLoader());
    mAmountInPaise = in.readLong();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(mUser, flags);
    dest.writeLong(mAmountInPaise);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<SimplTransaction> CREATOR = new Creator<SimplTransaction>() {
    @Override public SimplTransaction createFromParcel(Parcel in) {
      return new SimplTransaction(in);
    }

    @Override public SimplTransaction[] newArray(int size) {
      return new SimplTransaction[size];
    }
  };

  public SimplUser getUser() {
    return mUser;
  }

  public void setUser(SimplUser user) {
    mUser = user;
  }

  public long getAmountInPaise() {
    return mAmountInPaise;
  }

  public void setAmountInPaise(long amountInPaise) {
    mAmountInPaise = amountInPaise;
  }

  /**
   * Constructor
   *
   * @param user          {@link SimplUser} who is performing the transaction
   * @param amountInPaise Transaction amount in Paise
   */
  public SimplTransaction(final SimplUser user, final long amountInPaise) {
    mUser = user;
    mAmountInPaise = amountInPaise;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimplTransaction that = (SimplTransaction) o;

    if (mAmountInPaise != that.mAmountInPaise) return false;
    return mUser.equals(that.mUser);
  }

  @Override public int hashCode() {
    int result = mUser.hashCode();
    result = 31 * result + (int) (mAmountInPaise ^ (mAmountInPaise >>> 32));
    return result;
  }

  @Override public String toString() {
    return "SimplTransaction{" +
        "mUser=" + mUser +
        ", mAmountInPaise=" + mAmountInPaise +
        '}';
  }
}
