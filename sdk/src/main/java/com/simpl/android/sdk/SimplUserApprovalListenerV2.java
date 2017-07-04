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
 * User approval listener V2
 *
 * @author : Akshay Deo
 * @date : 16/10/15 : 8:12 PM
 * @email : akshay@betacraft.co
 */
public interface SimplUserApprovalListenerV2 {
  /**
   * Called when operation is successful
   *
   * @param status                status of approval : true if user is approved and false if he is
   *                              not.
   * @param buttonText            Text that should appear on Simpl payment option
   * @param showSimplIntroduction Boolean to indicate that User should be shown an introduction
   *                              modal related to Simpl
   */
  void onSuccess(final boolean status, final String buttonText,
      final boolean showSimplIntroduction);

  /**
   * Called when opration is unsuccessful
   *
   * @param throwable reason of the exception. Use throwable.getMessage() to show user readable
   *                  error
   */
  void onError(final Throwable throwable);
}
