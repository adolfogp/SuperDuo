package it.jaschke.alexandria.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.parceler.Parcels;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookAdditionEvent;
import it.jaschke.alexandria.model.event.BookDeletionEvent;
import it.jaschke.alexandria.model.event.BookSelectionEvent;
import it.jaschke.alexandria.receiver.NotificationBroadcastReceiver;
import it.jaschke.alexandria.service.BookService;
import it.jaschke.alexandria.view.fragment.BookAdditionFragment;
import it.jaschke.alexandria.view.fragment.BookDetailFragment;

/**
 * Presents a collection of books to select from, the details of the selected
 * book and lets the user add more books by entering their ISBN-13 number.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Identifies the {@code Fragment} used to present the details of a book.
     */
    private static final String TAG_BOOK_DETAIL_FRAGMENT =
            BookDetailFragment.class.getCanonicalName();

    /**
     * Identifies the {@code Fragment} used to add books.
     */
    private static final String TAG_BOOK_ADDITION_FRAGMENT =
            BookAdditionFragment.class.getCanonicalName();

    /**
     * Indicates if the activity contains two panes (master-detail) or just
     * one.
     */
    private boolean mTwoPane;

    /**
     * Receives notifications broadcasted by {@link BookService}.
     */
    private NotificationBroadcastReceiver mBookNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.book_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                // TODO: Put a placeholder fragment in the detail area.
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        mBookNotificationReceiver =
                NotificationBroadcastReceiver.registerLocalReceiver(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationBroadcastReceiver.unregisterLocalReceiver(this
                , mBookNotificationReceiver);
        mBookNotificationReceiver = null;
        EventBus.getDefault().unregister(this);
    }

    /**
     * Displays the detail view of the selected book.
     *
     * @param event the book selection event.
     */
    public void onEvent(BookSelectionEvent event) {
        showSelectedBookDetail(event.getSelectedBook());
    }

    /**
     * Displays the detail view of the selected book.
     *
     * @param event the book selection event.
     */
    public void onEvent(BookAdditionEvent event) {
        showAddedBookDetail(event.getAddedBook());
    }

    /**
     * Removes the detail view of the added book.
     *
     * @param event the book selection event.
     */
    public void onEvent(BookDeletionEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment detailFragment =
                fragmentManager.findFragmentByTag(TAG_BOOK_DETAIL_FRAGMENT);
        if (detailFragment == null) {
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(detailFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.menu_item_add_book:
                showAddBook();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the book addition pane on a new {@code Activity} or the same one,
     * depending on the screen size of the device being used.
     */
    private void showAddBook() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.book_addition_container
                    , new BookAdditionFragment()
                    , TAG_BOOK_ADDITION_FRAGMENT);
            transaction.commit();
        } else {
            Intent intent = new Intent(this, BookAdditionActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Shows the book's details on a new {@code Activity} or the same one,
     * depending on the screen size of the device being used.
     */
    private void showSelectedBookDetail(Book book) {
        if (mTwoPane) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment bookAdditionFragment =
                    fragmentManager.findFragmentByTag(TAG_BOOK_ADDITION_FRAGMENT);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (bookAdditionFragment != null) {
                transaction.remove(bookAdditionFragment);
            }
            transaction.replace(R.id.book_detail_container
                    , BookDetailFragment.newInstance(book)
                    , TAG_BOOK_DETAIL_FRAGMENT);
            transaction.commit();
        } else {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, Parcels.wrap(book));
            startActivity(intent);
        }
    }

    /**
     * Shows the added book's details on its container if two panes are
     * avialable. Otherwise ignores the request.
     */
    private void showAddedBookDetail(Book book) {
        if (!mTwoPane) {
            Log.w(LOG_TAG, "Ignoring request to show details for the added book.");
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.book_detail_container
                , BookDetailFragment.newInstance(book)
                , TAG_BOOK_DETAIL_FRAGMENT);
        transaction.commit();
    }

}