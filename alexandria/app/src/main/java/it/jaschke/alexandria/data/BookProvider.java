package it.jaschke.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static it.jaschke.alexandria.data.BookContract.BookEntry;
import static it.jaschke.alexandria.data.BookContract.AuthorEntry;
import static it.jaschke.alexandria.data.BookContract.CategoryEntry;

/**
 * Provides access to the book data used by the application.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel
 */
public class BookProvider extends ContentProvider {

    /**
     * Identifies a query for a single book's basic information, by its id.
     */
    private static final int BOOK_ID = 100;

    /**
     * Identifies a query for basic book information.
     */
    private static final int BOOK = 101;

    /**
     * Identifies a query for an author by its id.
     */
    private static final int AUTHOR_ID = 200;

    /**
     * Identifies a query for author information.
     */
    private static final int AUTHOR = 201;

    /**
     * Identifies a query for the information of a single category of books,
     * by its id.
     */
    private static final int CATEGORY_ID = 300;

    /**
     * Identifies a query for book category information.
     */
    private static final int CATEGORY = 301;

    /**
     * Identifies a query for full book data that does not include some details
     * like the subtitle.
     */
    private static final int BOOK_FULL = 500;

    /**
     * Identifies a query for all the data available for a single book.
     */
    private static final int BOOK_FULLDETAIL = 501;

    /**
     * Used to match URIs to queries and their result type.
     */
    private static final UriMatcher uriMatcher = buildUriMatcher();

    /**
     * Used to get access the database holding the data.
     */
    private BookDbHelper dbHelper;

    /**
     * Used to query for detailed book data.
     */
    private static final SQLiteQueryBuilder bookFull;

    static{
        bookFull = new SQLiteQueryBuilder();
        bookFull.setTables(BookEntry.TABLE_NAME + " LEFT OUTER JOIN "
                + AuthorEntry.TABLE_NAME + " USING (" + BookEntry._ID + ")"
                + " LEFT OUTER JOIN " +  CategoryEntry.TABLE_NAME
                + " USING (" + BookEntry._ID + ")");
    }


    /**
     * Returns a new instance of {@link UriMatcher} that maps URIs to the
     * equivalent constants used by the provider.
     *
     * @return a new instance of {@link UriMatcher}.
     * @see #BOOK
     * @see #BOOK_ID
     * @see #AUTHOR
     * @see #AUTHOR_ID
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK +"/#", BOOK_ID);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK_AUTHOR +"/#", AUTHOR_ID);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK_CATEGORY +"/#", CATEGORY_ID);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK, BOOK);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK_AUTHOR, AUTHOR);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK_CATEGORY, CATEGORY);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_FULLBOOK +"/#", BOOK_FULLDETAIL);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_FULLBOOK, BOOK_FULL);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BookDbHelper(getContext());
        return true;

    }

    @Override
    public Cursor query(Uri uri
            , String[] projection
            , String selection
            , String[] selectionArgs
            , String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                retCursor=dbHelper.getReadableDatabase().query(
                        BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selection==null? null : selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR:
                retCursor=dbHelper.getReadableDatabase().query(
                        AuthorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY:
                retCursor=dbHelper.getReadableDatabase().query(
                        CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_ID:
                retCursor=dbHelper.getReadableDatabase().query(
                        BookEntry.TABLE_NAME,
                        projection,
                        BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR_ID:
                retCursor=dbHelper.getReadableDatabase().query(
                        AuthorEntry.TABLE_NAME,
                        projection,
                        AuthorEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY_ID:
                retCursor=dbHelper.getReadableDatabase().query(
                        CategoryEntry.TABLE_NAME,
                        projection,
                        CategoryEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_FULLDETAIL:
                String[] bfd_projection = {
                        BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_TITLE
                        , BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_SUBTITLE
                        , BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_COVER_IMAGE_URL
                        , BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_DESCRIPTION
                        , "group_concat(DISTINCT " + AuthorEntry.TABLE_NAME + "."
                                + AuthorEntry.COLUMN_NAME +") as " + AuthorEntry.COLUMN_NAME
                        , "group_concat(DISTINCT " + CategoryEntry.TABLE_NAME + "."
                                + CategoryEntry.COLUMN_NAME +") as " + CategoryEntry.COLUMN_NAME
                };
                retCursor = bookFull.query(dbHelper.getReadableDatabase()
                        , bfd_projection
                        , BookEntry.TABLE_NAME + "." + BookEntry._ID
                                + " = '" + ContentUris.parseId(uri) + "'"
                        , selectionArgs
                        , BookEntry.TABLE_NAME + "." + BookEntry._ID
                        , null
                        , sortOrder);
                break;
            case BOOK_FULL:
                String[] bf_projection = {
                        BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_TITLE
                        , BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_COVER_IMAGE_URL
                        , "group_concat(DISTINCT " + AuthorEntry.TABLE_NAME + "."
                                + AuthorEntry.COLUMN_NAME + ") as " + AuthorEntry.COLUMN_NAME
                        , "group_concat(DISTINCT " + CategoryEntry.TABLE_NAME + "."
                                + CategoryEntry.COLUMN_NAME +") as " + CategoryEntry.COLUMN_NAME
                };
                retCursor = bookFull.query(dbHelper.getReadableDatabase()
                        , bf_projection
                        , null
                        , selectionArgs
                        , BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry._ID
                        , null
                        , sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }



    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOK_FULLDETAIL:
                return BookEntry.CONTENT_ITEM_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR_ID:
                return AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK:
                return BookEntry.CONTENT_TYPE;
            case AUTHOR:
                return AuthorEntry.CONTENT_TYPE;
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        switch (uriMatcher.match(uri)) {
            case BOOK: {
                long id = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = BookContract.BookEntry.buildBookUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver().notifyChange(
                        BookContract.BookEntry.buildFullBookUri(id), null);
                break;
            }
            case AUTHOR:{
                long id = db.insert(BookContract.AuthorEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = BookContract.AuthorEntry.buildAuthorUri(values.getAsLong("_id"));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case CATEGORY: {
                long id = db.insert(BookContract.CategoryEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = BookContract.CategoryEntry.buildCategoryUri(values.getAsLong("_id"));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                rowsDeleted = db.delete(
                        BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsDeleted = db.delete(
                        BookContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(
                        BookContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                rowsDeleted = db.delete(
                        BookContract.BookEntry.TABLE_NAME
                        , BookContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'"
                        , selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                rowsUpdated = db.update(BookContract.BookEntry.TABLE_NAME
                        , values
                        , selection
                        , selectionArgs);
                break;
            case AUTHOR:
                rowsUpdated = db.update(BookContract.AuthorEntry.TABLE_NAME
                        , values
                        , selection
                        , selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(BookContract.CategoryEntry.TABLE_NAME
                        , values
                        , selection
                        , selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}