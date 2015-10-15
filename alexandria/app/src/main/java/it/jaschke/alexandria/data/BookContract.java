package it.jaschke.alexandria.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The tables and columns for the book database.
 *
 * @author Sascha Jaschke
 * @author Jesús Adolfo García Pasquel
 */
public class BookContract {

    /**
     * Identifies the content provider.
     */
    public static final String CONTENT_AUTHORITY = "it.jaschke.alexandria";

    /**
     * Base for all of this app's content provider URIs.
     */
    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path for book basic data.
     */
    public static final String PATH_BOOK = "book";

    /**
     * Path for author data.
     */
    public static final String PATH_BOOK_AUTHOR = "author";

    /**
     * Path for the book categories.
     */
    public static final String PATH_BOOK_CATEGORY = "category";

    /**
     * Defines the contents of the table holding book data.
     *
     * @author Sascha Jaschke
     * @author Jesús Adolfo García Pasquel
     */
    public static final class BookEntry implements BaseColumns {

        /**
         * Base URI for book data.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();

        /**
         * Type for {@code content:} URIs with directories of books.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        /**
         * Type for {@code content:} URIs with a single book.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        /**
         * Name of the table containing cached book data.
         */
        public static final String TABLE_NAME = "book";

        /**
         * The book's title.
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * The book's subtitle.
         */
        public static final String COLUMN_SUBTITLE = "subtitle";

        /**
         * A description of the book's contents.
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /**
         * The URL of the image of the book's cover.
         */
        public static final String COLUMN_COVER_IMAGE_URL = "cover_image_url";

        /**
         * Returns the URI for a particular book's basic data given its id.
         *
         * @param id the book's identifier.
         * @return the URI for the basic data of the book with the specified id.
         */
        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Returns the URI for all the authors of a particular book,
         * given the book's id.
         *
         * @param id the book's identifier (ISBN-13).
         * @return the URI for all the authors of a particular book.
         */
        public static Uri buildBookAuthorsUri(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .appendPath(PATH_BOOK_AUTHOR).build();
        }
        /**
         * Returns the URI for all the categories of a particular book,
         * given the book's id.
         *
         * @param id the book's identifier (ISBN-13).
         * @return the URI for all the categories of a particular book.
         */
        public static Uri buildBookCategoriesUri(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .appendPath(PATH_BOOK_CATEGORY).build();
        }

        /**
         * Extracts the book's id from a book authors URI or book categories URI.
         *
         * @param uri the URI from which the movie's id will be extracted.
         * @return the movie's id.
         * @see #buildBookAuthorsUri(long)
         * @see #buildBookCategoriesUri(long)
         */
        public static long getBookIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

    }

    /**
     * Defines the contents of the table holding author data.
     *
     * @author Sascha Jaschke
     * @author Jesús Adolfo García Pasquel
     */
    public static final class AuthorEntry implements BaseColumns {

        /**
         * Base URI for author data.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK_AUTHOR).build();

        /**
         * Type for {@code content:} URIs with directories of authors' data.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK_AUTHOR;

        /**
         * Type for {@code content:} URIs with a single author's data.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK_AUTHOR;

        /**
         * Name of the table containing author data.
         */
        public static final String TABLE_NAME = "author";

        /**
         * Identifier of the book related to this author.
         */
        public static final String COLUMN_BOOK_ID = "book_id";

        /**
         * The author's name.
         */
        public static final String COLUMN_NAME = "name";

        /**
         * Returns the URI for a particular author review given its id.
         *
         * @param id the author's identifier.
         * @return the URI for the author with the specified id.
         */
        public static Uri buildAuthorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Defines the contents of the table holding book categories.
     *
     * @author Sascha Jaschke
     * @author Jesús Adolfo García Pasquel
     */
    public static final class CategoryEntry implements BaseColumns {

        /**
         * Base URI for book category data.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK_CATEGORY).build();

        /**
         * Type for {@code content:} URIs with directories of book categories
         * data.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK_CATEGORY;

        /**
         * Type for {@code content:} URIs with a single book category.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK_CATEGORY;

        /**
         * Name of the table containing book category data.
         */
        public static final String TABLE_NAME = "categories";

        /**
         * Identifier of the book related to this category.
         */
        public static final String COLUMN_BOOK_ID = "book_id";

        /**
         * The category's name.
         */
        public static final String COLUMN_NAME = "name";

        /**
         * Returns the URI for a particular book category given its id.
         *
         * @param id the category's identifier.
         * @return the URI for the book category with the specified id.
         */
        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}