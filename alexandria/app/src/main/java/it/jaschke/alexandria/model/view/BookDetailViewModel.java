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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.BR;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.databinding.AuthorListItemBinding;
import it.jaschke.alexandria.databinding.CategoryListItemBinding;
import it.jaschke.alexandria.model.domain.Author;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.domain.Category;
import it.jaschke.alexandria.model.event.BookDeletionEvent;
import it.jaschke.alexandria.service.BookService;

import static it.jaschke.alexandria.data.BookContract.BookEntry;
import static it.jaschke.alexandria.data.BookContract.AuthorEntry;
import static it.jaschke.alexandria.data.BookContract.CategoryEntry;

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
     * Requests {@link BookService} to delete the book for which the detail data
     * is shown and publishes a {@link BookDeletionEvent} on the {@link EventBus}.
     * If no book is set, does nothing.
     *
     * @param context the {@link Context} used to comunicate with the
     *     {@link BookService} to request the book's deletion.
     */
    public void deleteBook(Context context) {
        if (mBook == null) {
            Log.i(LOG_TAG, "Ignoring deletion request. No book set.");
            return;
        }
        Intent bookIntent = new Intent(context, BookService.class);
        bookIntent.putExtra(BookService.EXTRA_BOOK, Parcels.wrap(mBook));
        bookIntent.setAction(BookService.ACTION_DELETE_BOOK);
        context.startService(bookIntent);
        EventBus.getDefault().post(new BookDeletionEvent(mBook));
    }

    /**
     * Returns the title for the currently set {@link Book}, possibly
     * {@code null}.
     *
     * @return the title for the currently set {@link Book}, possibly
     *     {@code null}.
     */
    @Bindable
    public String getTitle() {
        return mBook != null
                ? mBook.getTitle()
                : null;
    }

    /**
     * Returns the subtitle for the currently set {@link Book}, possibly
     * {@code null}.
     *
     * @return the title for the currently set {@link Book}, possibly
     *     {@code null}.
     */
    @Bindable
    public String getSubtitle() {
        return mBook != null
                ? mBook.getSubtitle()
                : null;
    }

    /**
     * Returns the subtitle for the currently set {@link Book}, possibly
     * {@code null}.
     *
     * @return the title for the currently set {@link Book}, possibly
     *     {@code null}.
     */
    @Bindable
    public String getDescription() {
        return mBook != null
                ? mBook.getDescription()
                : null;
    }

    /**
     * Returns the URI of the movie's poster image.
     *
     * @return the URI of the movie's poster image.
     */
    @Bindable
    public String getCoverUri() {
        if (mBook == null) {
            return null;
        }
        return mBook.getCoverUri() != null
                ? mBook.getCoverUri().toString()
                : null;
    }

    /**
     * Returns the book's {@link Author}s.
     *
     * @return the book's {@link Author}s.
     */
    @Bindable
    public List<Author> getAuthors() {
        if (mBook == null) {
            return Collections.emptyList();
        }
        return mBook.getAuthors() != null
                ? mBook.getAuthors()
                : Collections.emptyList();
    }

    /**
     * Returns the book's {@link Category} list.
     *
     * @return the book's {@link Category} list.
     */
    @Bindable
    public List<Category> getCategories() {
        if (mBook == null) {
            return Collections.emptyList();
        }
        return mBook.getCategories() != null
                ? mBook.getCategories()
                : Collections.emptyList();
    }

    /**
     * Loads the book's cover image from the specified URI into the
     * {@link ImageView}. This method is used by the Data Binding Library.
     *
     * @param view {@link ImageView} to place the image into.
     * @param coverUri where the image should be retrieved from.
     */
    @BindingAdapter({"bind:coverUri"})
    public static void loadBookCoverImage(ImageView view, String coverUri) {
        Context context = view.getContext();
        Picasso.with(context)
                .load(coverUri) // TODO: Add placeholder and error images
                .into(view);
    }

    /**
     * Removes all views from the {@link LinearLayout} and adds new ones for
     * the specified {@link Author}s.
     *
     * @param container {@link LinearLayout} that will contain the authors.
     * @param authors the {@link Author}s to be placed in the container.
     */
    @BindingAdapter({"bind:authors"})
    public static void loadAuthorViews(LinearLayout container, List<Author> authors) {
        container.removeAllViews();
        if (authors == null || authors.isEmpty()) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) container.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Author author : authors) {
            AuthorListItemBinding binding = DataBindingUtil.inflate(inflater
                    , R.layout.list_item_author
                    , container
                    , false);
            AuthorListItemViewModel itemViewModel = new AuthorListItemViewModel();
            itemViewModel.setAuthor(author);
            binding.setViewModel(itemViewModel);
            container.addView(binding.getRoot());
        }
    }

    /**
     * Removes all views from the {@link LinearLayout} and adds new ones for
     * the specified instances of {@link Category}.
     *
     * @param container {@link LinearLayout} that will contain the categories.
     * @param categories the instances of {@link Category} to be placed in the
     *                   container.
     */
    @BindingAdapter({"bind:categories"})
    public static void loadCategoryViews(LinearLayout container, List<Category> categories) {
        container.removeAllViews();
        if (categories == null || categories.isEmpty()) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) container.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Category category : categories) {
            CategoryListItemBinding binding = DataBindingUtil.inflate(inflater
                    , R.layout.list_item_category
                    , container
                    , false);
            CategoryListItemViewModel itemViewModel = new CategoryListItemViewModel();
            itemViewModel.setCategory(category);
            binding.setViewModel(itemViewModel);
            container.addView(binding.getRoot());
        }
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
     * Retrieves the data from the cursor passed as argument and sets it onto the
     * {@link BookDetailViewModel}'s current {@link Book}. The projection used
     * must be {@link BookAuthorQuery#PROJECTION}. This method
     * also notifies the data binding of the change, so the visual elements can
     * be updated.
     *
     * @param cursor the {@link Cursor} containing the data  to load.
     * @throws IllegalStateException if there is no {@link Book} currently set
     *     in the {@link BookDetailViewModel}.
     */
    public void setBookAuthorData(Cursor cursor) {
        if (mBook == null) {
            throw new IllegalStateException("No book currently set in BookDetailViewModel.");
        }
        if (cursor == null || !cursor.moveToFirst()) {
            Log.d(LOG_TAG, "The cursor contains no authors.");
            mBook.setAuthors(Collections.emptyList());
            notifyPropertyChanged(BR.authors);
            return;
        }
        List<Author> authors = new ArrayList<>();
        do {
            authors.add(newAuthor(cursor));
        } while (cursor.moveToNext());
        mBook.setAuthors(authors);
        notifyPropertyChanged(BR.authors);
    }

    /**
     * Returns a new instance of {@link Author} with the data of the touple
     * currently pointed at by the {@link Cursor} passed as argument. The
     * data of the cursor is expected to appear as in {@link BookAuthorQuery}.
     *
     * @param cursor the {@link Cursor} from which the data of the
     *     {@link Author} will be retrieved.
     * @return  a new instance of {@link Author} with the data of the touple
     *     currently pointed at by the {@link Cursor} passed as argument.
     */
    private Author newAuthor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Author author = new Author();
        author.setId(cursor.getLong(BookAuthorQuery.COL_ID));
        author.setName(cursor.getString(BookAuthorQuery.COL_NAME));
        return author;
    }

    /**
     * Retrieves the data from the cursor passed as argument and sets it onto the
     * {@link BookDetailViewModel}'s current {@link Book}. The projection used
     * must be {@link BookCategoryQuery#PROJECTION}. This method
     * also notifies the data binding of the change, so the visual elements can
     * be updated.
     *
     * @param cursor the {@link Cursor} containing the data  to load.
     * @throws IllegalStateException if there is no {@link Book} currently set
     *     in the {@link BookDetailViewModel}.
     */
    public void setBookCategoryData(Cursor cursor) {
        if (mBook == null) {
            throw new IllegalStateException("No book currently set in BookDetailViewModel.");
        }
        if (cursor == null || !cursor.moveToFirst()) {
            Log.d(LOG_TAG, "The cursor contains no categories.");
            mBook.setCategories(Collections.emptyList());
            notifyPropertyChanged(BR.authors);
            return;
        }
        List<Category> categories = new ArrayList<>();
        do {
            categories.add(newCategory(cursor));
        } while (cursor.moveToNext());
        mBook.setCategories(categories);
        notifyPropertyChanged(BR.categories);
    }

    /**
     * Returns a new instance of {@link Category} with the data of the touple
     * currently pointed at by the {@link Cursor} passed as argument. The
     * data of the cursor is expected to appear as in {@link BookCategoryQuery}.
     *
     * @param cursor the {@link Cursor} from which the data of the
     *     {@link Category} will be retrieved.
     * @return  a new instance of {@link Category} with the data of the touple
     *     currently pointed at by the {@link Cursor} passed as argument.
     */
    private Category newCategory(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Category author = new Category();
        author.setId(cursor.getLong(BookCategoryQuery.COL_ID));
        author.setName(cursor.getString(BookCategoryQuery.COL_NAME));
        return author;
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

    /**
     * Provides information about projection and column indices expected by
     * {@link BookDetailViewModel} when setting book authors from a
     * {@link Cursor}.
     */
    public static final class BookAuthorQuery {

        /**
         * Projection that includes the details of the book authors.
         * Used to query {@link it.jaschke.alexandria.data.BookProvider}.
         */
        public static final String[] PROJECTION = {
                AuthorEntry._ID,
                AuthorEntry.COLUMN_BOOK_ID,
                AuthorEntry.COLUMN_NAME,
        };

        /**
         * Sort order to be used in the query. Considers the local id of
         * the authors, in ascending order.
         */
        public static final String SORT_ORDER = AuthorEntry._ID + " ASC";

        /**
         * Index of {@link AuthorEntry#_ID} in {@link #PROJECTION}.
         */
        public static final int COL_ID = 0;

        /**
         * Index of {@link AuthorEntry#COLUMN_BOOK_ID} in
         * {@link #PROJECTION}.
         */
        public static final int COL_BOOK_ID = 1;

        /**
         * Index of {@link AuthorEntry#COLUMN_NAME} in
         * {@link #PROJECTION}.
         */
        public static final int COL_NAME = 2;

        /**
         * The class only provides constants and utility methods.
         */
        private BookAuthorQuery() {
            // Empty constructor
        }

    }

    /**
     * Provides information about projection and column indices expected by
     * {@link BookDetailViewModel} when setting book categories from a
     * {@link Cursor}.
     */
    public static final class BookCategoryQuery {

        /**
         * Projection that includes the details of the book categories.
         * Used to query {@link it.jaschke.alexandria.data.BookProvider}.
         */
        public static final String[] PROJECTION = {
                CategoryEntry._ID,
                CategoryEntry.COLUMN_BOOK_ID,
                CategoryEntry.COLUMN_NAME,
        };

        /**
         * Sort order to be used in the query. Considers the local id of
         * the authors, in ascending order.
         */
        public static final String SORT_ORDER = AuthorEntry._ID + " ASC";

        /**
         * Index of {@link AuthorEntry#_ID} in {@link #PROJECTION}.
         */
        public static final int COL_ID = 0;

        /**
         * Index of {@link AuthorEntry#COLUMN_BOOK_ID} in
         * {@link #PROJECTION}.
         */
        public static final int COL_BOOK_ID = 1;

        /**
         * Index of {@link AuthorEntry#COLUMN_NAME} in
         * {@link #PROJECTION}.
         */
        public static final int COL_NAME = 2;

        /**
         * The class only provides constants and utility methods.
         */
        private BookCategoryQuery() {
            // Empty constructor
        }

    }

}
