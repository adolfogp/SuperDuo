package it.jaschke.alexandria.view.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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


/**
 * Displays detailed information for a given {@link Book}. New instances of
 * this class must be created with the factory method
 * {@link #newInstance(Book)}.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
     * Identifies the {@code Loader} that retrieves the book details cached in
     * the local database.
     */
    private static final int BOOK_DETAIL_LOADER_ID = 330364;

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

    // FIXME: Continue here.......... Register Loaders

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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            // TODO: Use view and domain models
            Book book = Parcels.unwrap(getArguments().getParcelable(ARG_BOOK));
            ean = Long.toString(book.getId());
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Book book = new Book();
                book.setId(Long.parseLong(ean));
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EXTRA_BOOK
                        , Parcels.wrap(book));
                bookIntent.setAction(BookService.ACTION_DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                BookContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)+bookTitle);
        shareActionProvider.setShareIntent(shareIntent);

        String bookSubTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_DESCRIPTION));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(BookContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_COVER_IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            ImageView fullBookCoverImageView =
                    (ImageView) rootView.findViewById(R.id.fullBookCover);
            Picasso.with(fullBookCoverImageView.getContext())
                    .load(imgUrl)
                    .into(fullBookCoverImageView);
            fullBookCoverImageView.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(BookContract.CategoryEntry.COLUMN_NAME));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        if(rootView.findViewById(R.id.right_container)!=null){
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

}