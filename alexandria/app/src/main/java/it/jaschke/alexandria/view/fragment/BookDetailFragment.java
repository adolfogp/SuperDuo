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

import it.jaschke.alexandria.view.activity.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.service.BookService;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Key used to access the {@link Book} specified as argument at creation
     * time.
     * @see #newInstance(Book)
     */
    private static final String ARG_BOOK = "EXTRA_BOOK";

    private final int LOADER_ID = 10;
    private View rootView;
    private String ean; // TODO: Use view and domain model instead of these variables.
    private String bookTitle;
    private ShareActionProvider shareActionProvider;

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

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Book book = new Book();
                book.setId(Long.parseLong(ean));
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EXTRA_BOOK
                        , Parcels.wrap(book));
                bookIntent.setAction(BookService.DELETE_BOOK);
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

    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}