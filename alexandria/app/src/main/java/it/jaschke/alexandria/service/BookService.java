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
import java.net.URL;

import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.data.BookContract;

import static it.jaschke.alexandria.data.BookContract.BookEntry;


/**
 * An {@link IntentService} subclass that handles book data download, storage
 * and deletion asynchronously. To request either action, use an {@link Intent}
 * specifying {@link #ACTION_FETCH_BOOK} or {@link #ACTION_DELETE_BOOK} and
 * the {@link Book} in {@link #EXTRA_BOOK}. This service notifies about
 * empty results, download and processing errors by publishing {@link Intent}s
 * with {@link #ACTION_NOTIFY} on the {@link LocalBroadcastManager}. The
 * notifications have different categories.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel
 */
public class BookService extends IntentService {

    /**
     * Identifies messages written to the log by this class.
     */
    private static final String LOG_TAG = BookService.class.getSimpleName();

    /**
     * Action specified to the service in {@link Intent}s that request that data
     * for a book is retrieved from the RESTful service and stored in the
     * {@code ContentProvider}. The intent must also provide {@link #EXTRA_BOOK}.
     */
    public static final String ACTION_FETCH_BOOK =
            "it.jaschke.alexandria.services.action.ACTION_FETCH_BOOK";

    /**
     * Action specified to the service in {@link Intent}s that request that data
     * for a book is deleted from the {@code ContentProvider}. The intent must
     * also provide {@link #EXTRA_BOOK}.
     */
    public static final String ACTION_DELETE_BOOK =
            "it.jaschke.alexandria.services.action.ACTION_DELETE_BOOK";

    /**
     * Extra included in the {@link Intent} to specify the {@link Book} to
     * operate on (e.g. fetch or delete). The only required attribute is
     * {@link Book#getId()}, the book's ISBN-13.
     */
    public static final String EXTRA_BOOK =
            "it.jaschke.alexandria.service.extra.Book";

    /**
     * Action specified by the service when broadcasting an {@link Intent} to
     * notify about an event.
     */
    public static final String ACTION_NOTIFY =
            "it.jaschke.alexandria.service.action.ACTION_NOTIFY";

    /**
     * Category used to notify that no results were found on the RESTful API for
     * the requested book.
     */
    public static final String CATEGORY_NO_RESULT =
            "it.jaschke.alexandria.service.category.CATEGORY_NO_RESULT";

    /**
     * Category used to notify that an error occurred while downloading
     * results from the RESTful API for the requested book.
     */
    public static final String CATEGORY_DOWNLOAD_ERROR =
            "it.jaschke.alexandria.service.category.CATEGORY_DOWNLOAD_ERROR";

    /**
     * Category used to notify that an error occurred while processing the
     * retuls from the RESTful API for the requested book.
     */
    public static final String CATEGORY_RESULT_PROCESSING_ERROR =
            "it.jaschke.alexandria.service.category.CATEGORY_RESULT_PROCESSING_ERROR";

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
        if (ACTION_FETCH_BOOK.equals(action)) {
            fetchBook(book);
        } else if (ACTION_DELETE_BOOK.equals(action)) {
            deleteBook(book);
        }
    }

    /**
     * Deletes the book with the specified id from the {@code ContentProvider}.
     *
     * @param book an instance of {@link Book} with its id set to the book's
     *             ISBN-13 number.
     */
    private void deleteBook(Book book) {
        int count = getContentResolver().delete(
                BookEntry.CONTENT_URI
                , BookEntry._ID + "= ?"
                , new String[]{Long.toString(book.getId())});
        Log.i(LOG_TAG, "Deleted book: " + book);
        if (count != 1) {
            Log.w(LOG_TAG, "Expected 1 book to be deleted, but got " + count);
        }
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
     * available already. Posts notifications on the {@link LocalBroadcastManager}
     * if no results are returned by the service, an error occurred while
     * downloading the book's data or processing the result.
     *
     * @param book an instance of {@link Book} with its id set to the book's
     *             ISBN-13 number.
     */
    private void fetchBook(Book book) {
        final long isbn = book.getId();
        if(Long.toString(isbn).length() != 13) {
            Log.w(LOG_TAG, "Not an ISBN-13. Ignoring " + isbn);
            return;
        }
        // Do not fetch books already in the database.
        if (isBookFetched(isbn)) {
            return;
        }

        String bookJsonString = null;

        try {
            bookJsonString = downloadBookData(isbn);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Unable to download book data.", ioe);
            postNotification(CATEGORY_DOWNLOAD_ERROR, book);
            return;
        }
        if (StringUtils.trimToNull(bookJsonString) == null) {
            postNotification(CATEGORY_NO_RESULT, book);
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
                postNotification(CATEGORY_NO_RESULT, book);
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
            postNotification(CATEGORY_RESULT_PROCESSING_ERROR, book);
        }
    }

    /**
     * Posts an {@link Intent} with {@link #ACTION_NOTIFY} and the specified
     * category to the {@link LocalBroadcastManager}. The {@link Book} is
     * added as an extra, associated to the key {@link #EXTRA_BOOK}.
     *
     * @param category the notification's category.
     * @param book the book to add as an extra. May be {@code null}.
     */
    private void postNotification(String category, Book book) {
        Intent notificationIntent = new Intent(ACTION_NOTIFY);
        notificationIntent.addCategory(category);
        if (book != null) {
            notificationIntent.putExtra(EXTRA_BOOK, Parcels.wrap(book));
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
    }

    /**
     * Downloads information about the book with the specified ISBN-13,
     * from a Google API, in JSON format.
     *
     * @param isbn the book's ISBN-13.
     * @return the data for the specified book, downloaded from the Google API,
     *     in JSON format. May be null.
     * @throws IOException if an error occurs while downloading the book's data.
     */
    private String downloadBookData(long isbn) throws IOException {
        StringBuilder buffer = new StringBuilder();
        final String FORECAST_BASE_URL =
                "https://www.googleapis.com/books/v1/volumes?";
        final String QUERY_PARAM = "q";
        final String ISBN_PARAM = "isbn:" + isbn;
        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                .build();
        URL url = new URL(builtUri.toString());
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try  {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
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
            values.put(BookContract.AuthorEntry.COLUMN_BOOK_ID, id);
            values.put(BookContract.AuthorEntry.COLUMN_NAME, jsonArray.getString(i));
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
            values.put(BookContract.CategoryEntry.COLUMN_BOOK_ID, id);
            values.put(BookContract.CategoryEntry.COLUMN_NAME, jsonArray.getString(i));
            getContentResolver().insert(BookContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }