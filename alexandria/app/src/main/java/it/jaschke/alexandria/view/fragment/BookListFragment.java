package it.jaschke.alexandria.view.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookSelectionEvent;
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

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;

    private final int BOOK_LIST_LOADER_ID = 10;

    public BookListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bookListAdapter = new BookListAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BookListFragment.this.restartLoader();
                    }
                }
        );

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO: Use view model, fix this mess
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Book selectedBook = new Book();
                    // TODO: Remove usage of getColumnIndex
                    selectedBook.setId(cursor.getLong(
                            cursor.getColumnIndex(BookContract.BookEntry._ID)));
                    EventBus.getDefault().post(new BookSelectionEvent(selectedBook));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(BOOK_LIST_LOADER_ID, null, this);
    }


    // TODO: This does not look right. Verify.
    private void restartLoader(){
        getLoaderManager().restartLoader(BOOK_LIST_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = BookContract.BookEntry.COLUMN_TITLE +" LIKE ? OR " + BookContract.BookEntry.COLUMN_SUBTITLE + " LIKE ? ";
        String searchString =searchText.getText().toString();

        // TODO: This does not look right. Verify.
        if(searchString.length() > 0){
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
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

}
