package it.jaschke.alexandria.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by saj on 23/12/14.
 */
public class BookProviderTest extends AndroidTestCase {
    public static final String LOG_TAG = BookProviderTest.class.getSimpleName();

    public void setUp() {
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                BookContract.BookEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                BookContract.CategoryEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                BookContract.AuthorEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                BookContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                BookContract.AuthorEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                BookContract.CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(BookContract.BookEntry.CONTENT_URI);
        assertEquals(BookContract.BookEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(BookContract.AuthorEntry.CONTENT_URI);
        assertEquals(BookContract.AuthorEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(BookContract.CategoryEntry.CONTENT_URI);
        assertEquals(BookContract.CategoryEntry.CONTENT_TYPE, type);

        long id = 9780137903955L;
        type = mContext.getContentResolver().getType(BookContract.BookEntry.buildBookUri(id));
        assertEquals(BookContract.BookEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(BookContract.AuthorEntry.buildAuthorUri(id));
        assertEquals(BookContract.AuthorEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(BookContract.CategoryEntry.buildCategoryUri(id));
        assertEquals(BookContract.CategoryEntry.CONTENT_ITEM_TYPE, type);

    }

    public void testInsertRead(){

        insertReadBook();
        insertReadAuthor();
        insertReadCategory();
    }

    public void insertReadBook(){
        ContentValues bookValues = BookDbTest.getBookValues();

        Uri bookUri = mContext.getContentResolver().insert(BookContract.BookEntry.CONTENT_URI, bookValues);
        long bookRowId = ContentUris.parseId(bookUri);
        assertTrue(bookRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                BookContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, bookValues);

        cursor = mContext.getContentResolver().query(
                BookContract.BookEntry.buildBookUri(bookRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, bookValues);

    }

    public void insertReadAuthor(){
        ContentValues authorValues = BookDbTest.getAuthorValues();

        Uri authorUri = mContext.getContentResolver().insert(BookContract.AuthorEntry.CONTENT_URI, authorValues);
        long authorRowId = ContentUris.parseId(authorUri);
        assertTrue(authorRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                BookContract.AuthorEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, authorValues);

        cursor = mContext.getContentResolver().query(
                BookContract.AuthorEntry.buildAuthorUri(authorRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, authorValues);

    }

    public void insertReadCategory(){
        ContentValues categoryValues = BookDbTest.getCategoryValues();

        Uri categoryUri = mContext.getContentResolver().insert(BookContract.CategoryEntry.CONTENT_URI, categoryValues);
        long categoryRowId = ContentUris.parseId(categoryUri);
        assertTrue(categoryRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                BookContract.CategoryEntry.CONTENT_URI,
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, categoryValues);

        cursor = mContext.getContentResolver().query(
                BookContract.CategoryEntry.buildCategoryUri(categoryRowId),
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        BookDbTest.validateCursor(cursor, categoryValues);

    }

}