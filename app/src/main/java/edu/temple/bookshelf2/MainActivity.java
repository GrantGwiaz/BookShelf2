package edu.temple.bookshelf2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface {

    FragmentManager fm;

    boolean twoPane;
    BookDetailsFragment bookDetailsFragment;
    Book selectedBook;
    private final String KEY_SELECTED_BOOK = "selectedBook";

    BookList bookList = new BookList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fetch selected book if there was one
        if (savedInstanceState != null)
            selectedBook = savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

        twoPane = findViewById(R.id.container2) != null;

        fm = getSupportFragmentManager();

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container1);


        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(fragment1 instanceof BookListFragment))
            fm.beginTransaction()
                    .add(R.id.container1, BookListFragment.newInstance(bookList))
            .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
        bookDetailsFragment = (selectedBook == null) ? new BookDetailsFragment() : BookDetailsFragment.newInstance(selectedBook);
        if (twoPane) {
            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment)
                    .commit();
        } else if (selectedBook != null) {
            /*
            If a book was selected, and we now have a single container, replace
            BookListFragment with BookDetailsFragment, making the transaction reversible
             */
            fm.beginTransaction()
                    .replace(R.id.container1, bookDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }

        Button search = (Button) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BookSearchActivity.class);
                intent.setFlags(0);
                startActivityForResult(intent, 1);
            }
        });

    }


    @Override
    public void bookSelected(int index) {
        //Store the selected book to use later if activity restarts
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
                    .replace(R.id.container1, BookDetailsFragment.newInstance(selectedBook))
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_BOOK, selectedBook);
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
        if (requestCode ==1) {

            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bundle bundle = extras.getBundle("bundle");
                BookList b = bundle.getParcelable("books");
                //BookList b = extras.getParcelable("books");
                //if(b.size() > 0) {
                  //  s = b.get(1).getTitle();
                //}
            }
        }
    }
}
