package it.jaschke.alexandria.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.view.activity.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BookService extends IntentService {

    /**
     * Identifies messages written to the log by this class.
     */
    private final String LOG_TAG = BookService.class.getSimpleName();

    /**
     * Identifies a request to fetch a book.
     */
    public static final String FETCH_BOOK =
            "it.jaschke.alexandria.services.action.FETCH_BOOK";

    /**
     * Identifies a request to delete a book.
     */
    public static final String DELETE_BOOK =
            "it.jaschke.alexandria.services.action.DELETE_BOOK";

    /**
     * Extra included in the {@link Intent} to specify the {@link Book} to
     * operate on (e.g. fetch or delete). The only required attribute is
     * {@link Book#getId()}.
     */
    public static final String EXTRA_BOOK = "it.jaschke.alexandria.services.extra.Book";

    /**
     * Creates a new instance of {@link BookService}.
     */
    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Book book = Parcels.unwrap(intent.getParcelableExtra(EXTRA_BOOK));
        final String action = intent.getAction();
        if (FETCH_BOOK.equals(action)) {
            fetchBook(book.getId());
        } else if (DELETE_BOOK.equals(action)) {
            deleteBook(book.getId());
        }
    }

    /**
     * Deletes the book with the specified id from the {@code ContentProvider}.
     *
     * @param id the book's ISBN-13 number.
     */
    private void deleteBook(long id) {
        getContentResolver().delete(BookContract.BookEntry.buildBookUri(id), null, null);
    }

    /**
     * Downloads the information for the book with the specified ISBN-13 from
     * a Google API.
     *
     * @param isbn the book's ISBN-13 number.
     */
    private void fetchBook(long isbn) {
        if(Long.toString(isbn).length() != 13) {
            Log.w(LOG_TAG, "Not an ISBN-13. Ignoring " + isbn);
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                BookContract.BookEntry.buildBookUri(isbn),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // Do not fetch books already in the database.
        if (bookEntry.getCount() > 0) {
            bookEntry.close();
            return;
        }

        bookEntry.close();

        // TODO: Use Retrofit
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + isbn;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        final String ITEMS = "items";

        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(isbn, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(isbn, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(isbn,bookInfo.getJSONArray(CATEGORIES) );
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    private void writeBackBook(long id, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(BookContract.BookEntry._ID, id);
        values.put(BookContract.BookEntry.COLUMN_TITLE, title);
        values.put(BookContract.BookEntry.COLUMN_COVER_IMAGE_URL, imgUrl);
        values.put(BookContract.BookEntry.COLUMN_SUBTITLE, subtitle);
        values.put(BookContract.BookEntry.COLUMN_DESCRIPTION, desc);
        getContentResolver().insert(BookContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(long id, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(BookContract.AuthorEntry._ID, id);
            values.put(BookContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(BookContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(long id, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(BookContract.CategoryEntry._ID, id);
            values.put(BookContract.CategoryEntry.COLUMN_NAME, jsonArray.getString(i));
            getContentResolver().insert(BookContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }