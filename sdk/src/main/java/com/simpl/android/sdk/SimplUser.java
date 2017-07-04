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
 * Model representing a User in Simpl system
 *
 * @author : Akshay Deo
 * @date : 15/10/15 : 12:58 PM
 * @email : akshay@betacraft.co
 */
public final class SimplUser implements Parcelable {
  private String mEmailAddress;
  private String mPhoneNumber;

  protected SimplUser(Parcel in) {
    mEmailAddress = in.readString();
    mPhoneNumber = in.readString();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mEmailAddress);
    dest.writeString(mPhoneNumber);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<SimplUser> CREATOR = new Creator<SimplUser>() {
    @Override public SimplUser createFromParcel(Parcel in) {
      return new SimplUser(in);
    }

    @Override public SimplUser[] newArray(int size) {
      return new SimplUser[size];
    }
  };

  public String getEmailAddress() {
    return mEmailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    mEmailAddress = emailAddress;
  }

  public String getPhoneNumber() {
    return mPhoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    mPhoneNumber = phoneNumber;
  }

  /**
   * Constructor
   *
   * @param emailAddress Email address of the user
   * @param phoneNumber  Phone number of the user
   */
  public SimplUser(final String emailAddress, final String phoneNumber) {
    mEmailAddress = emailAddress;
    mPhoneNumber = phoneNumber;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimplUser simplUser = (SimplUser) o;

    return mEmailAddress.equals(simplUser.mEmailAddress) && mPhoneNumber.equals(
        simplUser.mPhoneNumber);
  }

  @Override public int hashCode() {
    int result = mEmailAddress.hashCode();
    result = 31 * result + mPhoneNumber.hashCode();
    return result;
  }

  @Override public String toString() {
    return "SimplUser{" +
        "mPhoneNumber='" + mPhoneNumber + '\'' +
        ", mEmailAddress='" + mEmailAddress + '\'' +
        '}';
  }
}
