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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.adapters.TextViewBindingAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcel;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.BR;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookSelectionEvent;

/**
 * View model for the book list's view. Provides data and behaviour.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class BookListViewModel {

    /**
     * Identifies messages written to the log by this class.
     */
    private static final String LOG_TAG = BookListViewModel.class.getSimpleName();

    /**
     * The position of the currently selected book, possibly
     * {@link AdapterView#INVALID_POSITION}.
     */
    private int mSelectedPosition = AdapterView.INVALID_POSITION;

    /**
     * The text entered by the user to limit the books listed to those
     * containing this value.
     */
    private String mSearchString;

    /**
     * Publishes a {@link BookSelectionEvent} on the {@link EventBus}, with a
     * new instance of {@link Book} with only its id (the ISBN-13) assigned.
     */
    private final AdapterView.OnItemClickListener mBookClickListener =
            (parent, view, position, id) -> {
                mSelectedPosition = position;
                Book selectedBook = new Book();
                selectedBook.setId(id);
                EventBus.getDefault().post(new BookSelectionEvent(selectedBook));
            };

    /**
     * Updates the value of the search string with the changes entered by
     * the user.
     */
    private final TextWatcher mSearchStringWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignored
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Ignored
        }

        @Override
        public void afterTextChanged(Editable s) {
            setSearchString(s != null ? s.toString() : null);
        }
    };


    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    /**
     * Sets the value of the selected position to
     * {@link AdapterView#INVALID_POSITION}.
     */
    public void clearSelectedPosition() {
        mSelectedPosition = AdapterView.INVALID_POSITION;
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String searchString) {
        if (StringUtils.equals(mSearchString, searchString)) {
            return;
        }
        mSearchString = searchString;
        Log.v(LOG_TAG, "Search string changed: " + mSearchString);

    }

    public AdapterView.OnItemClickListener getBookClickListener() {
        return mBookClickListener;
    }

    public TextWatcher getSearchStringWatcher() {
        return mSearchStringWatcher;
    }

}
