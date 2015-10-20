package it.jaschke.alexandria.view.adapter;


import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.databinding.BookListItemBinding;

import static it.jaschke.alexandria.data.BookContract.BookEntry;

/**
 * {@link CursorAdapter} that shows book titles and covers for the entries in
 * {@link it.jaschke.alexandria.data.BookProvider}. The projection
 * must be {@link #PROJECTION_BOOK_LIST}.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel
 */
public class BookListAdapter extends CursorAdapter {

    /**
     * Projection that includes the book details to be presented. Used to
     * query {@link it.jaschke.alexandria.data.BookProvider}.
     */
    public static final String[] PROJECTION_BOOK_LIST = {
            BookEntry._ID,
            BookEntry.COLUMN_TITLE,
            BookContract.BookEntry.COLUMN_SUBTITLE,
            BookEntry.COLUMN_COVER_IMAGE_URL
    };

    /**
     * Index of {@link BookEntry#_ID} in {@link #PROJECTION_BOOK_LIST}.
     */
    public static final int COL_ID = 0;

    /**
     * Index of {@link BookEntry#COLUMN_TITLE} in {@link #PROJECTION_BOOK_LIST}.
     */
    public static final int COL_TITLE = 1;

    /**
     * Index of {@link BookEntry#COLUMN_SUBTITLE} in {@link #PROJECTION_BOOK_LIST}.
     */
    public static final int COL_SUBTITLE = 2;

    /**
     * Index of {@link BookEntry#COLUMN_COVER_IMAGE_URL} in
     * {@link #PROJECTION_BOOK_LIST}.
     */
    public static final int COL_COVER_IMAGE_URL = 3;

    /**
     * Creates a new instance of {@link BookListAdapter}.
     *
     * @param context the {@link Context}.
     * @param cursor the {@link Cursor} from which the data is retrieved.
     * @param flags flags that determine the behaviour of the adapter.
     */
    public BookListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        BookListItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.list_item_book, parent, false);
        binding.getRoot().setTag(binding);
        return binding.getRoot();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        BookListItemBinding binding = (BookListItemBinding) view.getTag();
        String title = cursor.getString(COL_TITLE);
        String subtitle = cursor.getString(COL_SUBTITLE);
        if (StringUtils.trimToNull(subtitle) != null) {
            title = context.getString(R.string.title_subtitle_pattern
                    , title, subtitle);
        }
        binding.bookTitleTextView.setText(title);
        Picasso.with(context)
                .load(cursor.getString(COL_COVER_IMAGE_URL))
                .into(binding.bookCoverImageView);
    }

}
