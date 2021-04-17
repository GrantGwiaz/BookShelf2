package edu.temple.bookshelf2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface, ControlFragment.MediaActionInterface {

    FragmentManager fm;

    boolean twoPane;
    BookDetailsFragment bookDetailsFragment;
    ControlFragment controlFragment;
    Book selectedBook;

    private final String TAG_BOOKLIST = "booklist", TAG_BOOKDETAILS = "bookdetails", TAG_CONTROL = "control";
    private final String KEY_SELECTED_BOOK = "selectedBook";
    private final String KEY_BOOKLIST = "searchedook";
    private final int BOOK_SEARCH_REQUEST_CODE = 123;

    BookList bookList;

    AudiobookService.MediaControlBinder mediaControlBinder;
    boolean isConnected;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mediaControlBinder = (AudiobookService.MediaControlBinder) binder;
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(MainActivity.this, AudiobookService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        findViewById(R.id.searchDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, BookSearchActivity.class), BOOK_SEARCH_REQUEST_CODE);
            }
        });

        if (savedInstanceState != null) {
            // Fetch selected book if there was one
            selectedBook = savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

            // Fetch previously searched books if one was previously retrieved
            bookList = savedInstanceState.getParcelable(KEY_BOOKLIST);
        } else {
            // Create empty booklist if
            bookList = new BookList();
        }

        twoPane = findViewById(R.id.container2) != null;

        fm = getSupportFragmentManager();

        Fragment fragment3;
        fragment3 = fm.findFragmentById(R.id.container_3);

        // add the control fragment
        fm.beginTransaction()
                .add(R.id.container_3, ControlFragment.newInstance(null, -1, -1), TAG_CONTROL)
                .commit();

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container_1);


        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(fragment1 instanceof BookListFragment))
            fm.beginTransaction()
                    .add(R.id.container_1, BookListFragment.newInstance(bookList), TAG_BOOKLIST)
                    .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
        bookDetailsFragment = (selectedBook == null) ? new BookDetailsFragment() : BookDetailsFragment.newInstance(selectedBook);
        if (twoPane) {
            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment, TAG_BOOKDETAILS)
                    .commit();
        } else if (selectedBook != null) {
            /*
            If a book was selected, and we now have a single container, replace
            BookListFragment with BookDetailsFragment, making the transaction reversible
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, bookDetailsFragment, TAG_BOOKDETAILS)
                    .addToBackStack(null)
                    .commit();
        }

    }


    @Override
    public void bookSelected(int index) {
        // Store the selected book to use later if activity restarts
        selectedBook = bookList.get(index);

        if (twoPane)
            /*
            Display selected book using previously attached fragment
             */
            bookDetailsFragment.displayBook(selectedBook);
        else {
            /*
            Display book using new fragment
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, BookDetailsFragment.newInstance(selectedBook), TAG_BOOKDETAILS)
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }

        fm.beginTransaction()
                .replace(R.id.container_3, ControlFragment.newInstance(selectedBook.getTitle(), 0, selectedBook.getDuration()), TAG_CONTROL)
                .commit();
    }

    /**
     * Display new books when retrieved from a search
     */
    private void showNewBooks() {
        if ((fm.findFragmentByTag(TAG_BOOKDETAILS) instanceof BookDetailsFragment)) {
            fm.popBackStack();
        }
        ((BookListFragment) fm.findFragmentByTag(TAG_BOOKLIST)).showNewBooks();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_BOOK, selectedBook);
        outState.putParcelable(KEY_BOOKLIST, bookList);
    }

    @Override
    public void onBackPressed() {
        // If the user hits the back button, clear the selected book
        selectedBook = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            bookList.clear();
            bookList.addAll((BookList) data.getParcelableExtra(BookSearchActivity.BOOKLIST_KEY));
            if (bookList.size() == 0) {
                Toast.makeText(this, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
            }
            showNewBooks();
        }
    }

    @Override
    public void play() {
        if (isConnected) {
            mediaControlBinder.play(selectedBook.getId());
        }
    }

    @Override
    public void pause() {
        if (isConnected) {
            mediaControlBinder.pause();
        }
    }

    @Override
    public void stop() {
        if (isConnected) {
            mediaControlBinder.stop();
            ((SeekBar) findViewById(R.id.ProgressSeekBar)).setProgress(0);
        }
    }

    @Override
    public int seekChange(int newProgress) {
        if (isConnected) {
            mediaControlBinder.seekTo(newProgress);
            ((SeekBar) findViewById(R.id.ProgressSeekBar)).setProgress(newProgress);
            return newProgress;
        } else {
            return -1;
        }
    }

    @Override
    public boolean isPlaying() {
        if (isConnected) {
            return mediaControlBinder.isPlaying();
        }
        return false;
    }

}
