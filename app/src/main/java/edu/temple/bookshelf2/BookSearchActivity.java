package edu.temple.bookshelf2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class BookSearchActivity extends AppCompatActivity {

    volatile BookList books;

    Handler downloadHandler =  new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                JSONArray bookListArray = new JSONArray((String) msg.obj);
                BookList bl = new BookList();
                for(int i = 0; i < bookListArray.length(); i++) {

                    JSONObject bookObject = bookListArray.getJSONObject(i);
                    Book b = new Book(
                            bookObject.getString("id"),
                            bookObject.getString("title"),
                            bookObject.getString("author"),
                            bookObject.getString("cover_url"));
                    bl.add(b);
                }
                books = new BookList(bl);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        Button goButton = (Button) findViewById(R.id.searchGoButton);
        EditText editTextSearch = (EditText) findViewById(R.id.editTextSearch);


        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //books ;
                Thread t = new Thread() {
                    @Override

                    public void run() {
                        super.run();
                        try {
                            URL url = new URL("https://kamorris.com/lab/cis3515/search.php?term=" + editTextSearch.getText().toString());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                            Message msg = Message.obtain();
                            StringBuilder builder = new StringBuilder();
                            String tmpString;
                            while ((tmpString = reader.readLine()) != null) {
                                builder.append(tmpString);
                            }
                            msg.obj = builder.toString();
                            downloadHandler.sendMessage(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                Toast.makeText(BookSearchActivity.this, books.get(0).getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putParcelable("books", books);
                intent.putExtra("bundle", bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }
}