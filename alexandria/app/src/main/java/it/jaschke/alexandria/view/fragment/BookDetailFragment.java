package it.jaschke.alexandria.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import it.jaschke.alexandria.databinding.BookDetailFragmentBinding;
import it.jaschke.alexandria.model.view.BookDetailViewModel;
import it.jaschke.alexandria.view.activity.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.service.BookService;

import static it.jaschke.alexandria.data.BookContract.BookEntry;
import static it.jaschke.alexandria.model.view.BookDetailViewModel.BookDetailQuery;
import static it.jaschke.alexandria.model.view.BookDetailViewModel.BookAuthorQuery;
import static it.jaschke.alexandria.model.view.BookDetailViewModel.BookCategoryQuery;


/**
 * Displays detailed information for a given {@link Book}. New instances of
 * this class must be created with the factory method
 * {@link #newInstance(Book)}.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookDetailFragment extends Fragment {

    // TODO: Put delete button in menu item

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = BookDetailFragment.class.getSimpleName();

    /**
     * The MIME type for plain text.
     */
    private static final String PLAIN_TEXT_MEDIA_TYPE = "text/plain";

    /**
     * Identifies the {@code Loader} that retrieves the book details from
     * the local database.
     */
    private static final int BOOK_DETAIL_LOADER_ID = 330364;

    /**
     * Identifies the {@code Loader} that retrieves the book authors' information
     * from the local database.
     */
    private static final int BOOK_AUTHOR_LOADER_ID = 410920;

    /**
     * Identifies the {@code Loader} that retrieves the book categoies'
     * information from the local database.
     */
    private static final int BOOK_CATEGORY_LOADER_ID = 924922;

    /**
     * Key used to access the {@link Book} specified as argument at creation
     * time.
     * @see #newInstance(Book)
     */
    private static final String ARG_BOOK = "EXTRA_BOOK";

    /**
     * Key used to save and retrieve the serialized {@link #mViewModel}.
     */
    private static final String STATE_VIEW_MODEL = "state_view_model";

    /**
     * Provides data and behaviour to the {@link BookDetailFragment}.
     */
    private BookDetailViewModel mViewModel;

    /**
     * Binds the view to the view model.
     * @see BookDetailViewModel
     */
    private BookDetailFragmentBinding mBinding = null;

    /**
     * Lets the user share the book's title.
     */
    private ShareActionProvider mShareActionProvider = null;

    /**
     * Creates a new instance of {@link BookDetailFragment} for the specified
     * book. You must use this factory method to create new instances.
     *
     * @param book the {@link Book} for which the details will be displayed.
     * @return A new instance of {@link BookDetailFragment}.
     */
    public static BookDetailFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOOK, Parcels.wrap(book));
        BookDetailFragment fragment = new BookDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() == null) {
            throw new IllegalStateException("No book specified as Fragment argument.");
        }
        this.restoreState(savedInstanceState);
    }

    /**
     * Loads the previous state, stored in the {@link Bundle} passed as argument,
     * into the {@link BookDetailFragment}. {@link #mViewModel} in particular.
     * If the argument is {@code null}, nothing is done.
     *
     * @param savedInstanceState the {@link BookDetailFragment}'s previous state.
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mViewModel = Parcels.unwrap(savedInstanceState.getParcelable(STATE_VIEW_MODEL));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_VIEW_MODEL, Parcels.wrap(mViewModel));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(BOOK_DETAIL_LOADER_ID, null
                , new BookLoaderCallbacks());
        getLoaderManager().initLoader(BOOK_AUTHOR_LOADER_ID, null
                , new AuthorLoaderCallbacks());
        getLoaderManager().initLoader(BOOK_CATEGORY_LOADER_ID, null
                , new CategoryLoaderCallbacks());
    }

    //TODO: Add delete action to viewModel and invoke from menu item

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater
                , R.layout.fragment_book_detail
                , container
                , false);
        if (mViewModel == null) {
            mViewModel = newViewModel();
        }
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    /**
     * Returns a new {@link BookDetailViewModel} based on the book data passed
     * in the {@link Fragment}'s arguments.
     *
     * @return a new {@link BookDetailViewModel} based on the movie data passed
     *     in the {@link Fragment}'s arguments.
     */
    private BookDetailViewModel newViewModel() {
        Book book = Parcels.unwrap(getArguments().getParcelable(ARG_BOOK));
        BookDetailViewModel viewModel = new BookDetailViewModel();
        viewModel.setBook(book);
        return viewModel;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(getShareBookTitleIntent());
    }

    /**
     * Returns an {@link Intent} that can be used to share the book's title
     * or {@code null} if there are no trailers (or there is no app on
     * the device that may be used to share).
     *
     * @return an {@link Intent} that can be used to share the first movie
     *     trailer or {@code null} if one is not available.
     */
    private Intent getShareBookTitleIntent() {
        if (mViewModel == null || mViewModel.getTitle() == null) {
            return null;
        }
        Context context = getActivity();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mViewModel.getTitle());
        intent.setType(PLAIN_TEXT_MEDIA_TYPE);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Log.w(LOG_TAG, "Unable to create share intent. No application available to share.");
            intent = null;
        }
        return intent;
    }

    /**
     * Handles the callbacks for the {@link Loader} that retrieves the book's
     * details from the {@code ContentProvider}. When loading is finished,
     * sets them on the {@link BookDetailFragment#mViewModel} of the associated
     * {@link BookDetailFragment}.
     */
    private class BookLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(BookDetailFragment.this.getActivity()
                    , BookEntry.buildBookUri(mViewModel.getBook().getId())
                    , BookDetailQuery.PROJECTION
                    , null
                    , null
                    , null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mViewModel.setBookData(data);
            // Set the share intent, if the provider has already been loaded
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(getShareBookTitleIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mViewModel.setBookData(null);
        }
    }

    /**
     * Handles the callbacks for the {@link Loader} that retrieves the book
     * authors' details from the {@code ContentProvider}. When loading is finished,
     * sets them on the {@link BookDetailFragment#mViewModel} of the associated
     * {@link BookDetailFragment}.
     */
    private class AuthorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(BookDetailFragment.this.getActivity()
                    , BookEntry.buildBookAuthorsUri(mViewModel.getBook().getId())
                    , BookAuthorQuery.PROJECTION
                    , null
                    , null
                    , BookAuthorQuery.SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mViewModel.setBookAuthorData(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mViewModel.setBookAuthorData(null);
        }
    }

    /**
     * Handles the callbacks for the {@link Loader} that retrieves the book
     * categories' details from the {@code ContentProvider}. When loading is
     * finished, sets them on the {@link BookDetailFragment#mViewModel} of the
     * associated {@link BookDetailFragment}.
     */
    private class CategoryLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(BookDetailFragment.this.getActivity()
                    , BookEntry.buildBookCategoriesUri(mViewModel.getBook().getId())
                    , BookCategoryQuery.PROJECTION
                    , null
                    , null
                    , BookCategoryQuery.SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mViewModel.setBookCategoryData(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mViewModel.setBookCategoryData(null);
        }
    }

}