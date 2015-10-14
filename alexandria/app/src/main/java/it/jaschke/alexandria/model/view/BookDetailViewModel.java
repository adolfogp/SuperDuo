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

package it.jaschke.alexandria.model.view;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcel;

import it.jaschke.alexandria.BR;
import it.jaschke.alexandria.model.domain.Book;

import static it.jaschke.alexandria.data.BookContract.BookEntry;

/**
 * View model for the book detail view. Provides data and behaviour.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class BookDetailViewModel extends BaseObservable {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = BookDetailViewModel.class.getSimpleName();

    /**
     * The book for which the detail data is being shown.
     */
    private Book mBook;

    /**
     * Creates a new instance of {@link BookDetailViewModel} with the
     * default values for all its attributes.
     */
    public BookDetailViewModel() {
        // Empty bean constructor.
    }

    public Book getBook() {
        return mBook;
    }

    /**
     * Sets the book for which the detail data is shown. If the
     * {@link Book}'s title has been set (is not {@code null}) it
     * also notifies the data binding of the change, otherwise the binding is
     * not notified.
     *
     * @param book the book for which the detail data should be shown.
     * @see #setBookData(Cursor)
     */
    public void setBook(Book book) {
        mBook = book;
    }

    /**
     * Retrieves the data from the cursor passed as argument and sets it onto the
     * {@link BookDetailViewModel}'s current {@link Book}. The projection used
     * must be {@link BookDetailQuery#PROJECTION}. This method also notifies the
     * data binding of the change, so the visual elements can be updated.
     *
     * @param cursor the {@link Cursor} containing the data  to load.
     * @throws IllegalStateException if there is no {@link Book} currently set
     *     in the {@link BookDetailViewModel}.
     * @throws IllegalArgumentException if the data passed does not belong to
     *     the {@link Book} currently set (i.e. does not have the same id).
     */
    public void setBookData(Cursor cursor) {
        if (mBook == null) {
            throw new IllegalStateException("No book currently set in BookDetailViewModel.");
        }
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "The cursor contains no data. Ignoring book details.");
            return;
        }
        if (mBook.getId() != cursor.getLong(BookDetailQuery.COL_ID)) {
            throw new IllegalArgumentException(
                    "The data passed does not belong to the book");
        }
        mBook.setTitle(cursor.getString(BookDetailQuery.COL_TITLE));
        mBook.setSubtitle(cursor.getString(BookDetailQuery.COL_SUBTITLE));
        mBook.setDescription(cursor.getString(BookDetailQuery.COL_DESCRIPTION));
        String coverUrl = cursor.getString(BookDetailQuery.COL_COVER_IMAGE_URL);
        mBook.setCoverUri(Uri.parse(StringUtils.trimToEmpty(coverUrl)));
        notifyPropertyChanged(BR._all);
    }

    /**
     * Provides information about projection and column indices expected by
     * {@link BookDetailViewModel} when setting book details from a {@link Cursor}.
     */
    public static final class BookDetailQuery {

        /**
         * Projection that includes the movie details to be presented. Used to
         * query {@link it.jaschke.alexandria.data.BookProvider}.
         */
        public static final String[] PROJECTION = {
                BookEntry._ID,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_SUBTITLE,
                BookEntry.COLUMN_DESCRIPTION,
                BookEntry.COLUMN_COVER_IMAGE_URL
        };

        /**
         * Index of {@link BookEntry#_ID} in {@link #PROJECTION}.
         */
        public static final int COL_ID = 0;

        /**
         * Index of {@link BookEntry#COLUMN_TITLE} in {@link #PROJECTION}.
         */
        public static final int COL_TITLE = 1;

        /**
         * Index of {@link BookEntry#COLUMN_SUBTITLE} in {@link #PROJECTION}.
         */
        public static final int COL_SUBTITLE = 2;

        /**
         * Index of {@link BookEntry#COLUMN_DESCRIPTION} in {@link #PROJECTION}.
         */
        public static final int COL_DESCRIPTION = 3;

        /**
         * Index of {@link BookEntry#COLUMN_COVER_IMAGE_URL} in
         * {@link #PROJECTION}.
         */
        public static final int COL_COVER_IMAGE_URL = 4;

        /**
         * The class only provides constants and utility methods.
         */
        private BookDetailQuery() {
            // Empty constructor
        }
    }

}
