package it.jaschke.alexandria.view.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.parceler.Parcels;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.databinding.BookAdditionFragmentBinding;
import it.jaschke.alexandria.model.domain.Book;
import it.jaschke.alexandria.model.event.BookAdditionEvent;
import it.jaschke.alexandria.model.view.BookAdditionViewModel;
import it.jaschke.alexandria.service.BookService;

/**
 * Lets the user add a new book by writing or scanning its ISBN-13. The addition
 * itself is performed by {@link it.jaschke.alexandria.service.BookService}.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookAdditionFragment extends Fragment {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = BookAdditionFragment.class.getSimpleName();

    /**
     * Key used to save and retrieve the serialized {@link #mViewModel}.
     */
    private static final String STATE_VIEW_MODEL = "state_view_model";

    /**
     * Binds the view to the view model.
     * @see BookAdditionViewModel
     */
    private BookAdditionFragmentBinding mBinding = null;


    /**
     * View model that provides data and behaviour to the
     * {@link BookAdditionFragment}.
     */
    private BookAdditionViewModel mViewModel;

    /**
     * Launches the {@code Activity} that scans ISBN-13 codes when the scan
     * button is clicked.
     *
     * @see #scanIsbnCode()
     */
    private final View.OnClickListener mScanClickLister = (view) -> scanIsbnCode();

    /**
     * Updates the value of the isbn in the view model with the changes entered
     * by the user.
     */
    private final TextWatcher mIsbnTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignored
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Ignored
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateIsbn(s);
        }
    };

    /**
     * Updates the value of {@link BookAdditionViewModel#mIsbn} with that
     * passed as argument. This method is called by the {@link TextWatcher}.
     * If the value is a valid ISBN-13, request the book's addition to the
     * {@link BookService}.
     *
     * @param s the updated value if the ISBN-13 field.
     * @see BookService
     */
    private void updateIsbn(Editable s) {
        mViewModel.setIsbn(s != null ? s.toString() : null);
        if (s != null && s.toString().length() ==
                getResources().getInteger(R.integer.isbn13_length)) {
            if (mViewModel.isValidIsbn(mViewModel.getIsbn())) {
                mBinding.isbnEditText.setError(null);
                requestBookAddition();
            } else {
                mBinding.isbnEditText.setError(getString(
                        R.string.msg_invalid_isbn, mViewModel.getIsbn()));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        if (mViewModel == null) {
            mViewModel = new BookAdditionViewModel();
        }
    }

    /**
     * Loads the previous state, stored in the {@link Bundle} passed as argument,
     * into to {@link BookAdditionFragment}. If the argument is {@code null},
     * nothing is done.
     *
     * @param savedInstanceState the {@link BookListFragment}'s previous state.
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mViewModel = Parcels.unwrap(savedInstanceState.getParcelable(STATE_VIEW_MODEL));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_VIEW_MODEL, Parcels.wrap(mViewModel));
    }

    @Override
    public View onCreateView(LayoutInflater inflater
            , ViewGroup container
            , Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater
                , R.layout.fragment_book_addition
                , container
                , false);
        mBinding.setViewModel(mViewModel);
        mBinding.isbnEditText.addTextChangedListener(mIsbnTextWatcher);
        mBinding.scanImageButton.setOnClickListener(mScanClickLister);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Clears the text input area when a book is successfully added.
     *
     * @param event the book addition event.
     */
    public void onEvent(BookAdditionEvent event) {
        mBinding.isbnEditText.setText(null);
    }

    /**
     * Launches the {@code Activity} that scans ISBN-13 codes and delivers
     * them to this {@code Fragment} as a result.
     *
     * @see #onActivityResult(int, int, Intent)
     */
    public void scanIsbnCode() {
        IntentIntegrator.forSupportFragment(this)
                .setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
                .initiateScan();
    }

    /**
     * Requests {@link BookService} to add the book with the ISBN entered by
     * the user (the current value of ).
     */
    private void requestBookAddition() {
        Book book = new Book();
        book.setId(Long.parseLong(mViewModel.getIsbn()));
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EXTRA_BOOK, Parcels.wrap(book));
        bookIntent.setAction(BookService.ACTION_FETCH_BOOK);
        getActivity().startService(bookIntent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult == null) {
            Log.w(LOG_TAG, "No scanned barcode resuts received.");
            return;
        }
        String scannedCode = scanResult.getContents();
        mBinding.isbnEditText.setText(scannedCode);
    }

}
