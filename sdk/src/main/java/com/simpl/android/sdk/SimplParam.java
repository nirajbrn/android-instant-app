package com.simpl.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Key val params for custom requests
 *
 * @author : Akshay Deo
 * @date : 08/01/16 : 2:41 PM
 * @email : akshay@betacraft.co
 */
public final class SimplParam implements Parcelable {
  /**
   * Key of the param
   */
  private String mKey;
  /**
   * Value of params
   */
  private String mValue;

  protected SimplParam(Parcel in) {
    mKey = in.readString();
    mValue = in.readString();
  }

  public static final Creator<SimplParam> CREATOR = new Creator<SimplParam>() {
    @Override public SimplParam createFromParcel(Parcel in) {
      return new SimplParam(in);
    }

    @Override public SimplParam[] newArray(int size) {
      return new SimplParam[size];
    }
  };

  public String getKey() {
    return mKey;
  }

  public String getValue() {
    return mValue;
  }

  private SimplParam(final String key, final String value) {
    mKey = key;
    mValue = value;
  }

  /**
   * Factory for {@link SimplParam}
   *
   * @param key   Key of the param
   * @param value Value of the param
   * @return New instance of {@link SimplParam}
   */
  public static SimplParam create(final String key, final String value) {
    return new SimplParam(key, value);
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(mKey);
    parcel.writeString(mValue);
  }
}
