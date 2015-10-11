package it.jaschke.alexandria.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.service.BookService;
import it.jaschke.alexandria.view.fragment.NavigationDrawerFragment;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.model.event.BookSelectionEvent;
import it.jaschke.alexandria.view.fragment.AboutFragment;
import it.jaschke.alexandria.view.fragment.AddBookFragment;
import it.jaschke.alexandria.view.fragment.BookDetailFragment;
import it.jaschke.alexandria.view.fragment.ListOfBooksFragment;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;

    /**
     * Receives notifications broadcasted by {@link BookService}.
     */
    private BroadcastReceiver mBookNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        registerNotificationReceiver();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterNotificationReceiver();
        EventBus.getDefault().unregister(this);
    }

    private void registerNotificationReceiver() {
        mBookNotificationReceiver = new NotificationBroadcastReciever();
        IntentFilter filter = new IntentFilter(BookService.ACTION_NOTIFY);
        filter.addCategory(BookService.CATEGORY_NO_RESULT);
        filter.addCategory(BookService.CATEGORY_DOWNLOAD_ERROR);
        filter.addCategory(BookService.CATEGORY_RESULT_PROCESSING_ERROR);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBookNotificationReceiver, filter);
    }

    private void unregisterNotificationReceiver() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBookNotificationReceiver);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooksFragment();
                break;
            case 1:
                nextFragment = new AddBookFragment();
                break;
            case 2:
                nextFragment = new AboutFragment();
                break;

        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays the detail view of the selected movie.
     *
     * @param event the movie selection event.
     */
    public void onEvent(BookSelectionEvent event) {
        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        // TODO: Stop adding these operations to the backstack
        getSupportFragmentManager().beginTransaction()
                .replace(id, BookDetailFragment.newInstance(event.getSelectedBook()))
                .addToBackStack("Book Detail")
                .commit();
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        super.onBackPressed();
    }

    /**
     * Handles notifications broadcasted by {@link BookService}.
     */
    private class NotificationBroadcastReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getCategories() == null
                    || intent.getCategories().size() != 1) {
                Log.e(LOG_TAG, "Expected one category.");
                return;
            }
            String message = null;
            final String category = intent.getCategories().iterator().next();
            if (BookService.CATEGORY_NO_RESULT.equals(category)) {
                message = getString(R.string.no_result);
            } else if (BookService.CATEGORY_DOWNLOAD_ERROR.equals(category)) {
                message = getString(R.string.download_error);
            } else if (BookService.CATEGORY_RESULT_PROCESSING_ERROR.equals(category)) {
                message = getString(R.string.response_processing_error);
            } else {
                Log.e(LOG_TAG, "Unexpected notification category "+ category);
            }
            if (message != null) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


}