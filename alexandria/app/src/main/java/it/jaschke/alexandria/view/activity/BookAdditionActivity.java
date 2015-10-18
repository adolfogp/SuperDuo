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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookAdditionEvent;
import it.jaschke.alexandria.model.event.BookDeletionEvent;
import it.jaschke.alexandria.receiver.NotificationBroadcastReceiver;
import it.jaschke.alexandria.view.fragment.BookDetailFragment;

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
     * Identifies the {@code Fragment} used to present the details of a book.
     */
    private static final String TAG_BOOK_DETAIL_FRAGMENT =
            BookDetailFragment.class.getCanonicalName();

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
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
    }

    /**
     * Displays the detail view of the added book.
     *
     * @param event the book selection event.
     */
    public void onEvent(BookAdditionEvent event) {
        showBookDetail(event.getAddedBook());
    }

    /**
     * Removes the detail view of the added book.
     *
     * @param event the book selection event.
     */
    public void onEvent(BookDeletionEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment detailFragment =
                fragmentManager.findFragmentByTag(TAG_BOOK_DETAIL_FRAGMENT);
        if (detailFragment == null) {
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(detailFragment);
        transaction.commit();
    }

    /**
     * Shows the book's details on a new {@code Activity} or the same one,
     * depending on the screen size of the device being used.
     */
    private void showBookDetail(Book book) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.book_detail_container
                , BookDetailFragment.newInstance(book)
                , TAG_BOOK_DETAIL_FRAGMENT);
        transaction.commit();
    }

}
