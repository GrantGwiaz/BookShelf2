package edu.temple.bookshelf2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface, ControlFragment.MediaActionInterface {

    FragmentManager fm;

    boolean twoPane;
    private BookDetailsFragment bookDetailsFragment;
    private ControlFragment controlFragment;
    Book selectedBook, playingBook;

    private final String TAG_BOOKLIST = "booklist", TAG_BOOKDETAILS = "bookdetails", TAG_CONTROL = "control";
    private final String KEY_SELECTED_BOOK = "selectedBook", KEY_PLAYING_BOOK ="playingBook";
    private final String KEY_BOOKLIST = "searchedook";
    private final int BOOK_SEARCH_REQUEST_CODE = 123;


    private AudiobookService.MediaControlBinder mediaControlBinder;
    private boolean sericeConnected;

    Intent serviceIntent;

    BookList bookList;


    String savedStateFilename = "savedStatefile";
    File savedStateFile;

    boolean dialog = false;


    Handler progressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(mediaControlBinder.isPlaying() && playingBook != null) {
                controlFragment.updateProgress((int) (((float) ((AudiobookService.BookProgress) msg.obj).getProgress() / playingBook.getDuration()) * 100));
                controlFragment.setNowPlaying("Now Playing: " + playingBook.getTitle());
            }
            return true;
        }
    });

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            mediaControlBinder = (AudiobookService.MediaControlBinder) iBinder;
            mediaControlBinder.setProgressHandler(progressHandler);
            sericeConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sericeConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedStateFile = new File(getFilesDir(), savedStateFilename);

        if(!dialog) {
            if (savedStateFile.exists()) {
                try {
                    SavedState saved;
                    FileInputStream fin = openFileInput(savedStateFilename);
                    ObjectInputStream oin = new ObjectInputStream(fin);
                    saved = (SavedState) oin.readObject();
                    oin.close();

                    selectedBook =saved.getSelectedBook();
                    playingBook =saved.getPlayingBook();
                    bookList = saved.getCurrentList();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            dialog =false;
        }
        else{
            Toast.makeText(this,"Hello FFFFart",Toast.LENGTH_SHORT).show();
        }




        serviceIntent = new Intent(this, AudiobookService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        fm = getSupportFragmentManager();

        findViewById(R.id.searchDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = true;
                startActivityForResult(new Intent(MainActivity.this, BookSearchActivity.class), BOOK_SEARCH_REQUEST_CODE);
            }
        });

        if (savedInstanceState != null) {
            // Fetch selected book if there was one
            selectedBook =  savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

            // Fetch playing book if there is one
            playingBook =   savedInstanceState.getParcelable(KEY_PLAYING_BOOK);

            // Fetch previously searched books if one was previously retrieved
            bookList =  savedInstanceState.getParcelable(KEY_BOOKLIST);
        } else {
            // Create empty booklist if
            if( bookList == null) {
                bookList = new BookList();
            }
        }

        twoPane = findViewById(R.id.container2) != null;

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container_1);

        if((controlFragment = (ControlFragment) fm.findFragmentById(R.id.container_3)) == null) {
            controlFragment =  new ControlFragment();
            fm.beginTransaction()
                    .add(R.id.container_3, controlFragment)
                    .commit();
        }


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
        outState.putParcelable(KEY_PLAYING_BOOK, playingBook);
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
            bookList.addAll( data.getParcelableExtra(BookSearchActivity.BOOKLIST_KEY));
            if (bookList.size() == 0) {
                Toast.makeText(this, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
            }
            showNewBooks();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // here we are going to restore the selectedBook, playingBook, and bookList from internal memory

        // might have to put file = new File(getFilesDir(), internalFilename); maybe not ttho
        //restore from file

        //fromPause =false;

        // TODO update fragments


    }

    @Override
    protected void onPause() {
        super.onPause();
        SavedState saved = new SavedState(selectedBook, playingBook, bookList);
        try{
            FileOutputStream fos = openFileOutput(savedStateFilename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(saved);
            oos.close();
        }
        catch( Exception e) {
            e.printStackTrace();
        }
        //fromPause =true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void play() {
        if (selectedBook !=null) {
            playingBook =selectedBook;
            controlFragment.setNowPlaying("Now Playing: " + playingBook.getTitle());
            if(sericeConnected) {
                mediaControlBinder.play(selectedBook.getId());
            }
            // ensures that the service doesn't stop
            // if the activity is destroyed while the book is playing
            startService(serviceIntent);

        }
    }

    @Override
    public void pause() {
        if (sericeConnected) {
            mediaControlBinder.pause();
        }

    }

    @Override
    public void stop() {
        if (sericeConnected) {
            mediaControlBinder.stop();
            controlFragment.updateProgress(0);
            controlFragment.setNowPlaying("");

            //If no book id playing, then its fine to let
            // the service stop once the activity is destroyed
            stopService(serviceIntent);
        }
    }

    @Override
    public void seekChange(int newProgress) {
        if (sericeConnected && selectedBook !=null) {
            mediaControlBinder.seekTo((int) ((newProgress/100f) * playingBook.getDuration()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
