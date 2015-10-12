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

package it.jaschke.alexandria.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.receiver.NotificationBroadcastReceiver;

/**
 * Displays a {@code Fragment} that lets the user search for books by their
 * ISBN-13 number and add them to the {@code ContentProvider}.
 *
 * @see it.jaschke.alexandria.view.fragment.BookAdditionFragment
 * @see it.jaschke.alexandria.service.BookService
 * @see it.jaschke.alexandria.data.BookProvider
 */
public class BookAdditionActivity extends AppCompatActivity {

    /**
     * Receives notifications broadcasted by
     * {@link it.jaschke.alexandria.service.BookService}.
     */
    private NotificationBroadcastReceiver mBookNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_addition);
    }

    @Override
    public void onResume() {
        mBookNotificationReceiver =
                NotificationBroadcastReceiver.registerLocalReceiver(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationBroadcastReceiver.unregisterLocalReceiver(this
                , mBookNotificationReceiver);
        mBookNotificationReceiver = null;
    }

}
