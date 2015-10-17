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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.parceler.Parcels;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookDeletionEvent;
import it.jaschke.alexandria.view.fragment.BookDetailFragment;

/**
 * Shows the details of a {@link Book} passed as an extra in the {@code Intent},
 * using the key {@link #EXTRA_BOOK}. The only attribute that the {@link Book}
 * must have assigned is {@link Book#getId()}.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookDetailActivity extends AppCompatActivity {

    /**
     * Key used to access the {@link Book} to show, from the {@code Intent}'s
     * extras.
     */
    public static final String EXTRA_BOOK = "extra_book";

    /**
     * Identifies the {@code Fragment} used to present the details of a movie.
     */
    private static final String BOOK_DETAIL_FRAGMENT_TAG =
            BookDetailFragment.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Book book = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_BOOK));
        if (book == null) {
            throw new IllegalArgumentException(
                    "The book specified in the Intent may not be null.");
        }
        setContentView(R.layout.activity_book_detail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(BOOK_DETAIL_FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.book_detail_container
                    , BookDetailFragment.newInstance(book)
                    , BOOK_DETAIL_FRAGMENT_TAG);
            transaction.commit();
        }
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Finishes the {@code Activity} upon receiving notification of a book
     * deletion event.
     *
     * @param event the book deletion event.
     * @see #finish()
     */
    public void onEvent(BookDeletionEvent event) {
        finish();
    }

}
