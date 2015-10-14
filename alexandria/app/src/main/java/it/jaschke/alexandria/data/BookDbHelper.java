package it.jaschke.alexandria.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static it.jaschke.alexandria.data.BookContract.AuthorEntry;
import static it.jaschke.alexandria.data.BookContract.BookEntry;
import static it.jaschke.alexandria.data.BookContract.CategoryEntry;

/**
 * Manages the creation and maintenance of the local book database.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel.
 */
public class BookDbHelper extends SQLiteOpenHelper {

    /**
     * Version number of the dabase.
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * Name of the SQLite database file.
     */
    public static final String DATABASE_NAME = "alexandria.db";

    /**
     * Statement used to create the table that holds the book data.
     */
    private static final String SQL_CREATE_BOOK_TABLE =
            "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
            + BookEntry._ID + " INTEGER PRIMARY KEY,"
            + BookEntry.COLUMN_TITLE + " TEXT NOT NULL,"
            + BookEntry.COLUMN_SUBTITLE + " TEXT ,"
            + BookEntry.COLUMN_DESCRIPTION + " TEXT ,"
            + BookEntry.COLUMN_COVER_IMAGE_URL + " TEXT, "
            + "UNIQUE ("+ BookEntry._ID +") ON CONFLICT IGNORE"
            + ");";

    /**
     * Statement used to create the table that holds the author data.
     */
    private static final String SQL_CREATE_AUTHOR_TABLE =
            "CREATE TABLE " + AuthorEntry.TABLE_NAME + " ("
            + AuthorEntry._ID + "  INTEGER PRIMARY KEY, "
            + AuthorEntry.COLUMN_BOOK_ID + "  INTEGER NOT NULL, "
            + AuthorEntry.COLUMN_NAME + " TEXT,"
            + " FOREIGN KEY (" + AuthorEntry.COLUMN_BOOK_ID + ") REFERENCES "
            + BookEntry.TABLE_NAME + " (" + BookEntry._ID + ")"
            + "UNIQUE (" + AuthorEntry.COLUMN_BOOK_ID + ", "
            + AuthorEntry.COLUMN_NAME + ") ON CONFLICT REPLACE"
            + ");";

    /**
     * Statement used to create the table that holds the book category data.
     */
    private static final String SQL_CREATE_CATEGORY_TABLE =
            "CREATE TABLE " + CategoryEntry.TABLE_NAME + " ("
            + CategoryEntry._ID + "  INTEGER PRIMARY KEY, "
            + CategoryEntry.COLUMN_BOOK_ID + "  INTEGER NOT NULL, "
            + CategoryEntry.COLUMN_NAME + " TEXT,"
            + " FOREIGN KEY (" + CategoryEntry.COLUMN_BOOK_ID + ") REFERENCES "
            + BookEntry.TABLE_NAME + " (" + BookEntry._ID + ")"
            + "UNIQUE (" + CategoryEntry.COLUMN_BOOK_ID + ", "
            + CategoryEntry.COLUMN_NAME + ") ON CONFLICT REPLACE"
            + ");";

    /**
     * Creates a new instance of {@link BookDbHelper}.
     *
     * @param context {@link Context} to create or open the database with.
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_AUTHOR_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AuthorEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        onCreate(db);
    }

}
