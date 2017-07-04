/*
 * Copyright © 2015, Get Simpl Technologies Private Limited
 * All rights reserved.
 *
 * This software is proprietary, commercial software. All use must be licensed. The licensee is given the right to use the software under certain conditions, but is restricted from other uses of the software, such as modification, further distribution, or reverse engineering. Unauthorized use, duplication, reverse engineering, any form of redistribution, or use in part or in whole other than by prior, express, written and signed license for use is subject to civil and criminal prosecution. If you have received this file in error, please notify the copyright holder and destroy this and any other copies as instructed.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ON AN "AS IS" BASIS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED, AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simpl.android.sdk.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.simpl.android.sdk.SimplAirbrakeNotifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sms broadcast receiver for automatically reading the OTP. Supposed to be used in place with
 * the listener associated with it.
 *
 * @author : Akshay Deo
 * @date : 19/10/15 : 5:26 PM
 * @email : akshay@betacraft.co
 */
public final class SmsBroadcastReceiver extends BroadcastReceiver {
    /**
     * TAG for logging
     */
    private static final String TAG = "##SmsBroadcastReceiver##";
    /**
     * ACTION hardcoded in the Android system
     */
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    /**
     * Regular Expression for parsing the SMS
     */
    private static final Pattern smsRegExPattern1 = Pattern.compile("[A-Z]\\w+ OTP is ([\\d]+)");
    private static final Pattern smsRegExPattern2 = Pattern.compile("[A-Z]\\w+ OTP: ([\\d]+)");
    /**
     * Current listener
     */
    private OtpReceivedListener mOtpReceivedListener;

    /**
     * Callback listener used for getting otp from this receiver
     */
    public interface OtpReceivedListener {
        /**
         * Called when OTP is parsed successfully
         *
         * @param otp Current OTP
         */
        void onReceive(final String otp);
    }

    @SuppressLint("LongLogTag")
    public SmsBroadcastReceiver() {
        super();
        Log.d(TAG, "Receiver created");
    }

    /**
     * Setter
     */
    public void setOtpReceivedListener(OtpReceivedListener otpReceivedListener) {
        mOtpReceivedListener = otpReceivedListener;
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * Context.registerReceiver(BroadcastReceiver,IntentFilter, String, Handler)}.
     * When it runs on the main thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p/>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b>  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use
     * {@link Context#startService(Intent)} instead of
     * Context.bindService(Intent, ServiceConnection, int).  If you wish
     * to interact with a service that is already running, you can use
     * {@link #peekService}.
     * <p/>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(final Context context,
                          final Intent intent) {
        Log.d(TAG, "Received SMS broadcast intent");
        if (!intent.getAction().equals(SMS_RECEIVED)) {
            return;
        }
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            final SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = readMessageFromPdu(pdus[i]);
                // This is purposefully not broken as there could be a case where user
                // might be receiving multiple OTP messages due to resend and all
                detectSimplOtp(messages[i]);
            }
        }
    }

    /**
     * To detect if the current {@link SmsMessage} is from Simpl or not
     *
     * @param message Received {@link SmsMessage}
     * @return boolean flag to indicate if its associated with US or not
     */
    @SuppressLint("LongLogTag")
    private boolean detectSimplOtp(final SmsMessage message) {
        try {
            Log.d(TAG, "Detecting simpl message");
            Log.d(TAG, "Message is " + message.toString());
            if (!message.getOriginatingAddress().contains("SIMPLX") && !message.getOriginatingAddress()
                    .contains("GTSMPL")) {
                Log.d(TAG, "Not from Simpl : " + message.getOriginatingAddress());
                return false;
            }
            Log.d(TAG, "Message body is =>" + message.getMessageBody());
            final Matcher matcher1 = smsRegExPattern1.matcher(message.getMessageBody());
            if (matcher1.find()) {
                Log.d(TAG, "OTP is =>" + matcher1.group(1));
                if (mOtpReceivedListener == null) {
                    return true;
                }
                mOtpReceivedListener.onReceive(matcher1.group(1));
            }

            final Matcher matcher2 = smsRegExPattern2.matcher(message.getMessageBody());
            if (matcher2.find()) {
                Log.d(TAG, "OTP is =>" + matcher2.group(1));
                if (mOtpReceivedListener == null) {
                    return true;
                }
                mOtpReceivedListener.onReceive(matcher2.group(1));
            }

            return true;
        } catch (final Exception exception) {
            // Again this is the SAD thing, I don't wanna crash this in any case
            // so fuck it and do this shit !!
            SimplAirbrakeNotifier.notify(exception);
        }
        return false;
    }

    /**
     * Helper to read message from PDU
     *
     * @param pdu Pdu
     * @return Instance of {@link SmsMessage}
     */
    private SmsMessage readMessageFromPdu(final Object pdu) {
        return SmsMessage.createFromPdu((byte[]) pdu);
    }
}
