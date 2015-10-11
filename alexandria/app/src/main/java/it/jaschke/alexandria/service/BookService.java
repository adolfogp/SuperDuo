package it.jaschke.alexandria.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.view.activity.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.BookContract;

import static it.jaschke.alexandria.data.BookContract.BookEntry;


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
     * Returns {@code true} if the book with the specified ISBN-13 number is
     * already in the {@code ContentProvider}, {@code false} otherwise.
     *
     * @param isbn the ISBN-13 number of the book.
     * @return {@code true} if the book with the specified ISBN-13 number is
     *     already in the {@code ContentProvider}, {@code false} otherwise.
     */
    private boolean isBookFetched(long isbn) {
        Cursor bookEntry = getContentResolver().query(
                BookEntry.buildBookUri(isbn),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        boolean result = bookEntry.getCount() > 0;
        bookEntry.close();
        return result;
    }

    /**
     * Downloads the information for the book with the specified ISBN-13 from
     * a Google API and inserts it into the {@code ContentProvider}, if not
     * available already.
     *
     * @param isbn the book's ISBN-13 number.
     */
    private void fetchBook(long isbn) {
        if(Long.toString(isbn).length() != 13) {
            Log.w(LOG_TAG, "Not an ISBN-13. Ignoring " + isbn);
            return;
        }
        // Do not fetch books already in the database.
        if (isBookFetched(isbn)) {
            return;
        }

        String bookJsonString = downloadBookData(isbn);

        if (StringUtils.EMPTY.equals(bookJsonString)) {
            return;
        }


        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESCRIPTION = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";
        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                // TODO: Use EventBus instead of broadcast
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            JSONObject bookInfo =
                    ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = StringUtils.EMPTY;
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc = StringUtils.EMPTY;
            if(bookInfo.has(DESCRIPTION)){
                desc = bookInfo.getString(DESCRIPTION);
            }

            String imgUrl = StringUtils.EMPTY;
            if(bookInfo.has(IMG_URL_PATH)
                    && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            insertBook(isbn, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                insertAuthors(isbn, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                insertCategories(isbn, bookInfo.getJSONArray(CATEGORIES));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON", e);
        }
    }

    /**
     * Downloads information about the book with the specified ISBN-13,
     * from a Google API, in JSON format.
     *
     * @param isbn the book's ISBN-13.
     * @return the data for the specified book, downloaded from the Google API,
     *     in JSON format.
     */
    private String downloadBookData(long isbn) {
        StringBuilder buffer = new StringBuilder();
        final String FORECAST_BASE_URL =
                "https://www.googleapis.com/books/v1/volumes?";
        final String QUERY_PARAM = "q";
        final String ISBN_PARAM = "isbn:" + isbn;
        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.wtf(LOG_TAG, "The URI is not valid. " + builtUri.toString(), e);
            return buffer.toString();
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try  {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return buffer.toString();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error downloading book data.", e);
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
        return buffer.toString();
    }

    /**
     * Inserts a book's data into the {@code ContentProvider}.
     *
     * @param id the book's ISBN-13.
     * @param title the book's title.
     * @param subtitle the book's subtitle.
     * @param description the book's description.
     * @param coverImageUrl URL of the cover's image.
     */
    private void insertBook(long id
            , String title
            , String subtitle
            , String description
            , String coverImageUrl) {
        ContentValues values= new ContentValues();
        values.put(BookContract.BookEntry._ID, id);
        values.put(BookContract.BookEntry.COLUMN_TITLE, title);
        values.put(BookContract.BookEntry.COLUMN_COVER_IMAGE_URL, coverImageUrl);
        values.put(BookContract.BookEntry.COLUMN_SUBTITLE, subtitle);
        values.put(BookContract.BookEntry.COLUMN_DESCRIPTION, description);
        getContentResolver().insert(BookContract.BookEntry.CONTENT_URI,values);
    }

    /**
     * Inserts a book authors' data into the {@code ContentProvider}.
     *
     * @param id the book's ISBN-13.
     * @param jsonArray {@link JSONArray} with the authors' data.
     * @throws JSONException if an error occurs while accessing the data.
     */
    private void insertAuthors(long id, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(BookContract.AuthorEntry._ID, id);
            values.put(BookContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(BookContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    /**
     * Inserts a book's category data into the {@code ContentProvider}.
     *
     * @param id the book's ISBN-13.
     * @param jsonArray {@link JSONArray} with the category data.
     * @throws JSONException if an error occurs while accessing the data.
     */
    private void insertCategories(long id, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(BookContract.CategoryEntry._ID, id);
            values.put(BookContract.CategoryEntry.COLUMN_NAME, jsonArray.getString(i));
            getContentResolver().insert(BookContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }