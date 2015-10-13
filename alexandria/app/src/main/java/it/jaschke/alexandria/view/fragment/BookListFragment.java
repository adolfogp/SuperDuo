package it.jaschke.alexandria.view.fragment;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.databinding.BookListFragmentBinding;
import it.jaschke.alexandria.model.view.BookListViewModel;
import it.jaschke.alexandria.view.adapter.BookListAdapter;
import it.jaschke.alexandria.data.BookContract;

/**
 * Displays a list of the books in the {@code ContentProvider}, including
 * their title and cover image.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel
 */
public class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = BookListFragment.class.getSimpleName();

    /**
     * Identifies the {@link Loader} that retrieves the book data.
     */
    private static final int BOOK_LIST_LOADER_ID = 527669;

    /**
     * Key used to save and retrieve the serialized {@link #mViewModel}.
     */
    private static final String STATE_VIEW_MODEL = "state_view_model";

    /**
     * Binds the view to the view model.
     * @see BookListViewModel
     */
    private BookListFragmentBinding mBinding = null;

    /**
     * View model that provides data and behaviour to the
     * {@link BookListFragment}.
     */
    private BookListViewModel mViewModel;

    /**
     * Adapter that provides the {@link View}s for presenting each book.
     */
    private BookListAdapter mBookListAdapter;

    private final View.OnClickListener searchClickLister = (view) -> {
        Log.wtf(LOG_TAG, "searchString: " + mViewModel.getSearchString());
        BookListFragment.this.restartLoader();
        mViewModel.clearSelectedPosition();
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        if (mViewModel == null) {
            mViewModel = new BookListViewModel();
        }
    }

    /**
     * Loads the previous state, stored in the {@link Bundle} passed as argument,
     * into to {@link BookListFragment}. {@link #mViewModel} in particular.
     * If the argument is {@code null}, nothing is done.
     *
     * @param savedInstanceState the {@link BookListFragment}'s previous state.
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
        getLoaderManager().initLoader(BOOK_LIST_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater
                , R.layout.fragment_book_list
                , container
                , false);
        mBinding.setViewModel(mViewModel);
        mBookListAdapter = new BookListAdapter(getActivity(), null, 0);
        mBinding.bookListView.setAdapter(mBookListAdapter);
        mBinding.searchEditText.addTextChangedListener(
                mViewModel.getSearchStringWatcher());
        mBinding.searchImageButton.setOnClickListener(searchClickLister);
        return mBinding.getRoot();
    }

    // TODO: This does not look right. Verify.
    private void restartLoader(){
        getLoaderManager().restartLoader(BOOK_LIST_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: fix all of this.

        final String selection = BookContract.BookEntry.COLUMN_TITLE +" LIKE ? OR " + BookContract.BookEntry.COLUMN_SUBTITLE + " LIKE ? ";
        String searchString = mViewModel.getSearchString();

        // TODO: This does not look right. Verify.
        if(StringUtils.trimToNull(searchString) != null) {
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    BookContract.BookEntry.CONTENT_URI,
                    BookListAdapter.PROJECTION_BOOK_LIST,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }
        return new CursorLoader(
                getActivity(),
                BookContract.BookEntry.CONTENT_URI,
                BookListAdapter.PROJECTION_BOOK_LIST,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor oldCursor = mBookListAdapter.swapCursor(data);
        if (oldCursor != null) {
            oldCursor.close();
        }

        // Scroll to the last selected item if reloading after an event
        // that causes the first item to be shown (e.g. configuration change).
        if (mBinding.bookListView.getFirstVisiblePosition() == 0
                && mViewModel.getSelectedPosition() != AdapterView.INVALID_POSITION) {
            mBinding.bookListView.smoothScrollToPosition(
                    mViewModel.getSelectedPosition());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor oldCursor = mBookListAdapter.swapCursor(null);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

}
