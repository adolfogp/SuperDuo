package it.jaschke.alexandria.view.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;

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


    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String bookTitle = cursor.getString(COL_TITLE);
        viewHolder.bookTitle.setText(bookTitle);
        String bookSubTitle = cursor.getString(COL_SUBTITLE);
        viewHolder.bookSubTitle.setText(bookSubTitle);
        String imgUrl = cursor.getString(COL_COVER_IMAGE_URL);
        Picasso.with(context)
                .load(imgUrl)
                .into(viewHolder.bookCover);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
