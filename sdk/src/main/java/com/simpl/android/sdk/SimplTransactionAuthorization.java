/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk;

/**
 * This represents a transaction authorization
 *
 * @author : Akshay Deo
 * @date : 16/10/15 : 7:09 PM
 * @email : akshay@betacraft.co
 */
public class SimplTransactionAuthorization {
  /**
   * Transaction authorization code
   */
  private String mTransactionToken;

  public String getTransactionToken() {
    return mTransactionToken;
  }

  public void setTransactionToken(String transactionToken) {
    mTransactionToken = transactionToken;
  }

  /**
   * Constructor
   *
   * @param transactionToken transaction token
   */
  public SimplTransactionAuthorization(final String transactionToken) {
    mTransactionToken = transactionToken;
  }

  @Override public String toString() {
    return "SimplTransactionAuthorization{" +
        "mTransactionToken='" + mTransactionToken + '\'' +
        '}';
  }
}
