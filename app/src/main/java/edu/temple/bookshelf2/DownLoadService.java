package edu.temple.bookshelf2;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;

public class DownLoadService extends IntentService {


    // This class doesnt do anything, I thought have to make a thread in Intent Service
    // but it didnt work so I just did a thread in  main activity for downloading

    public DownLoadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getAudioBook(intent.getIntExtra("book_ID", 1));
    }

    public void getAudioBook(int audioBookId) {
        String savedAudioBookFilename = "AudioBook" + audioBookId;
        String url =  "https://kamorris.com/lab/audlib/download.php?id=" + audioBookId;
        Toast.makeText(getApplicationContext(), "stupidsexy hhhhhhhh", Toast.LENGTH_SHORT).show();

        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());


            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            File audioBook = new File(getExternalFilesDir(null), savedAudioBookFilename);
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(audioBook));
            fos.write(buffer);
            fos.flush();
            fos.close();
            Log.d("DOWNLOADED AUDIOBOOK", "Downloaded this god dang book");
            Toast.makeText(getApplicationContext(), "stupidsexy flanders", Toast.LENGTH_SHORT).show();

        } catch(Exception e) {
            e.printStackTrace();
        }


    }


}
