/*
 * Copyright 2015 Jesús Adolfo García Pasquel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.jaschke.alexandria.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.service.BookService;

/**
 * Handles notifications broadcasted by {@link BookService}. Receives
 * {@link Intent}s with action  {@link BookService#ACTION_NOTIFY} and
 * displays messages using a {@link Toast} based on the {@link Intent}s
 * category (e.g. {@link BookService#CATEGORY_DOWNLOAD_ERROR}).
 *
 * @author Jesús Adolfo García Pasquel
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG =
            NotificationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getCategories() == null
                || intent.getCategories().size() != 1) {
            Log.e(LOG_TAG, "Expected one category.");
            return;
        }
        String message = null;
        final String category = intent.getCategories().iterator().next();
        if (BookService.CATEGORY_NO_RESULT.equals(category)) {
            message = context.getString(R.string.no_result);
        } else if (BookService.CATEGORY_DOWNLOAD_ERROR.equals(category)) {
            message = context.getString(R.string.download_error);
        } else if (BookService.CATEGORY_RESULT_PROCESSING_ERROR.equals(category)) {
            message = context.getString(R.string.response_processing_error);
        } else {
            Log.e(LOG_TAG, "Unexpected notification category "+ category);
        }
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates a new instance of {@link NotificationBroadcastReceiver},
     * assigns and registers it on the {@link LocalBroadcastManager}.
     *
     * @param context the {@link Context} used to get the
     *     {@link LocalBroadcastManager}.
     * @return the registered {@link NotificationBroadcastReceiver}.
     */
    public static NotificationBroadcastReceiver registerLocalReceiver(
            Context context) {
        NotificationBroadcastReceiver broadcastReceiver =
                new NotificationBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BookService.ACTION_NOTIFY);
        filter.addCategory(BookService.CATEGORY_NO_RESULT);
        filter.addCategory(BookService.CATEGORY_DOWNLOAD_ERROR);
        filter.addCategory(BookService.CATEGORY_RESULT_PROCESSING_ERROR);
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

    /**
     * Unregisters the specified {@link NotificationBroadcastReceiver} from
     * the {@link LocalBroadcastManager}.
     *
     * @param context {@link Context} used to get the {@link LocalBroadcastManager}.
     * @param broadcastReceiver the {@link NotificationBroadcastReceiver} to
     *                          unregister.
     */
    public static void unregisterLocalReceiver(Context context
            , NotificationBroadcastReceiver broadcastReceiver) {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(broadcastReceiver);
    }

}
