package it.jaschke.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import it.jaschke.alexandria.model.domain.Author;
import it.jaschke.alexandria.model.domain.Category;

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
     * Identifies a query for basic book information.
     */
    private static final int BOOK = 100;

    /**
     * Identifies a query for a single book's basic information, by its id.
     */
    private static final int BOOK_ID = 110;

    /**
     * Identifies a query for author information.
     */
    private static final int AUTHOR = 200;

    /**
     * Identifies a query for an author by its id.
     */
    private static final int AUTHOR_ID = 210;

    /**
     * Identifies a query for the authors of a specific book, by the
     * book's identifier (ISBN-13).
     */
    private static final int BOOK_AUTHOR = 220;

    /**
     * Identifies a query for book category information.
     */
    private static final int CATEGORY = 300;

    /**
     * Identifies a query for the information of a single category of books,
     * by its id.
     */
    private static final int CATEGORY_ID = 310;

    /**
     * Identifies a query for the categories of a specific book, by the
     * book's identifier (ISBN-13).
     */
    private static final int BOOK_CATEGORY = 320;

    /**
     * Selection for a book queried by id.
     */
    private static final String SELECTION_BOOK_ID =
            BookEntry.TABLE_NAME + "." + BookEntry._ID + " = ? ";

    /**
     * Selection for an author queried by id.
     */
    private static final String SELECTION_AUTHOR_ID =
            AuthorEntry.TABLE_NAME + "." + AuthorEntry._ID + " = ? ";

    /**
     * Selection for a category queried by id.
     */
    private static final String SELECTION_CATEGORY_ID =
            CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID + " = ? ";

    /**
     * Selection for the authors of a specific book, queried by the
     * book's identifier.
     */
    private static final String SELECTION_BOOK_AUTHORS =
            AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID + " = ? ";

    /**
     * Selection for the authors of a specific book, queried by the
     * book's identifier.
     */
    private static final String SELECTION_BOOK_CATEGORIES =
            CategoryEntry.TABLE_NAME + "." + CategoryEntry.COLUMN_BOOK_ID + " = ? ";

    /**
     * Used to match URIs to queries and their result type.
     */
    private static UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Used to get access the database holding the data.
     */
    private BookDbHelper mOpenHelper;

    /**
     * Used to query book data.
     */
    private static SQLiteQueryBuilder sBookQueryBuilder;

    static {
        sBookQueryBuilder = new SQLiteQueryBuilder();
        sBookQueryBuilder.setTables(BookEntry.TABLE_NAME);
    }

    /**
     * Used to query author data.
     */
    private static SQLiteQueryBuilder sAuthorQueryBuilder;

    static {
        sAuthorQueryBuilder = new SQLiteQueryBuilder();
        sAuthorQueryBuilder.setTables(AuthorEntry.TABLE_NAME);
    }

    /**
     * Used to query category data.
     */
    private static SQLiteQueryBuilder sCategoryQueryBuilder;

    static {
        sCategoryQueryBuilder = new SQLiteQueryBuilder();
        sCategoryQueryBuilder.setTables(CategoryEntry.TABLE_NAME);
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
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
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
                , BookContract.PATH_BOOK + "/#/" + BookContract.PATH_BOOK_AUTHOR
                , BookProvider.BOOK_AUTHOR);
        matcher.addURI(BookContract.CONTENT_AUTHORITY
                , BookContract.PATH_BOOK + "/#/" + BookContract.PATH_BOOK_CATEGORY
                , BookProvider.BOOK_CATEGORY);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BookDbHelper(getContext());
        return true;

    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                return BookEntry.CONTENT_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR:
                return AuthorEntry.CONTENT_TYPE;
            case AUTHOR_ID:
                return AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            case CATEGORY_ID:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK_AUTHOR:
                return AuthorEntry.CONTENT_TYPE;
            case BOOK_CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri
            , String[] projection
            , String selection
            , String[] selectionArgs
            , String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                retCursor = getAllBooks(projection, selection, selectionArgs, sortOrder);
                break;
            case BOOK_ID:
                retCursor = getBookById(uri, projection);
                break;
            case AUTHOR:
                retCursor = getAllAuthors(projection, selection, selectionArgs, sortOrder);
                break;
            case AUTHOR_ID:
                retCursor = getAuthorById(uri, projection);
                break;
            case CATEGORY:
                retCursor = getAllCategories(projection, selection, selectionArgs, sortOrder);
                break;
            case CATEGORY_ID:
                retCursor = getCategoryById(uri, projection);
                break;
            case BOOK_AUTHOR:
                retCursor = getBookAuthors(uri, projection);
                break;
            case BOOK_CATEGORY:
                retCursor = getBookCategories(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Queries the database for all registered books.
     *
     * @param projection the columns to return.
     * @param selection the <i>WHERE</i> clause.
     * @param selectionArgs the values for the arguments used in {@code selection}.
     * @param sortOrder how the rows sould be ordered.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getAllBooks(String[] projection
            , String selection
            , String[] selectionArgs
            , String sortOrder) {
        return sBookQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , selection
                , selectionArgs
                , null // groupBy
                , null // having
                , sortOrder);
    }

    /**
     * Queries the database for the book with the id contained in the URI.
     *
     * @param uri the URI used to query, containing the id of the book.
     * @param projection the columns to return.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getBookById(Uri uri, String[] projection) {
        String id = Long.toString(ContentUris.parseId(uri));
        return sBookQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , SELECTION_BOOK_ID // selection
                , new String[] {id}  // selectionArgs
                , null // groupBy
                , null // having
                , null  // sortOrder
        );
    }

    /**
     * Queries the database for all registered authors.
     *
     * @param projection the columns to return.
     * @param selection the <i>WHERE</i> clause.
     * @param selectionArgs the values for the arguments used in {@code selection}.
     * @param sortOrder how the rows sould be ordered.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getAllAuthors(String[] projection
            , String selection
            , String[] selectionArgs
            , String sortOrder) {
        return sAuthorQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , selection
                , selectionArgs
                , null // groupBy
                , null // having
                , sortOrder);
    }

    /**
     * Queries the database for the author with the id contained in the URI.
     *
     * @param uri the URI used to query, containing the id of the author.
     * @param projection the columns to return.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getAuthorById(Uri uri, String[] projection) {
        String id = Long.toString(ContentUris.parseId(uri));
        return sAuthorQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , SELECTION_AUTHOR_ID // selection
                , new String[] {id}  // selectionArgs
                , null // groupBy
                , null // having
                , null  // sortOrder
        );
    }

    /**
     * Queries the database for all registered book categories.
     *
     * @param projection the columns to return.
     * @param selection the <i>WHERE</i> clause.
     * @param selectionArgs the values for the arguments used in {@code selection}.
     * @param sortOrder how the rows sould be ordered.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getAllCategories(String[] projection
            , String selection
            , String[] selectionArgs
            , String sortOrder) {
        return sCategoryQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , selection
                , selectionArgs
                , null // groupBy
                , null // having
                , sortOrder);
    }

    /**
     * Queries the database for the category with the id contained in the URI.
     *
     * @param uri the URI used to query, containing the id of the category.
     * @param projection the columns to return.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getCategoryById(Uri uri, String[] projection) {
        String id = Long.toString(ContentUris.parseId(uri));
        return sCategoryQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , SELECTION_CATEGORY_ID // selection
                , new String[] {id}  // selectionArgs
                , null // groupBy
                , null // having
                , null  // sortOrder
        );
    }

    /**
     * Queries the database for all the authors related to a book with the
     * book's id contained in the URI.
     *
     * @param uri the URI used to query, containing the id of the book.
     * @param projection the columns to return.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getBookAuthors(Uri uri, String[] projection) {
        String id = Long.toString(BookEntry.getBookIdFromUri(uri));
        return sAuthorQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , SELECTION_BOOK_AUTHORS
                , new String[] {id}
                , null // groupBy
                , null // having
                , null  // sortOrder
        );
    }

    /**
     * Queries the database for all the categories related to a book with the
     * book's id contained in the URI.
     *
     * @param uri the URI used to query, containing the id of the book.
     * @param projection the columns to return.
     * @return a {@link Cursor} for the result.
     */
    private Cursor getBookCategories(Uri uri, String[] projection) {
        String id = Long.toString(BookEntry.getBookIdFromUri(uri));
        return sCategoryQueryBuilder.query(
                mOpenHelper.getReadableDatabase()
                , projection
                , SELECTION_BOOK_CATEGORIES
                , new String[] {id}
                , null // groupBy
                , null // having
                , null  // sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri resultUri;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                long id = db.insert(BookEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    resultUri = BookEntry.buildBookUri(id);
                } else {
                    throw new android.database.SQLException("Insertion failed. " + uri);
                }
                break;
            case AUTHOR:
                id = db.insert(AuthorEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    resultUri = AuthorEntry.buildAuthorUri(id);
                } else {
                    throw new android.database.SQLException("Insertion failed. " + uri);
                }
                break;
            case CATEGORY:
                id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    resultUri = CategoryEntry.buildCategoryUri(id);
                } else {
                    throw new android.database.SQLException("Insertion failed. " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int update(Uri uri
            , ContentValues values
            , String selection
            , String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsAffected;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                rowsAffected =
                        db.update(BookEntry.TABLE_NAME
                                , values, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsAffected =
                        db.update(AuthorEntry.TABLE_NAME
                                , values, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsAffected =
                        db.update(CategoryEntry.TABLE_NAME
                                , values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown: " + uri);
        }
        // notify listeners
        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsAffected;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                rowsAffected =
                        db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsAffected =
                        db.delete(AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsAffected =
                        db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // notify listeners
        if (rowsAffected > 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}