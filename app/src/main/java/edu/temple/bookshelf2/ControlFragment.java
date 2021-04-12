package edu.temple.bookshelf2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlFragment extends Fragment {

    private static final String PLAYING_BOOK_KEY = "playing book";
    private static final String PROGRESS_KEY = "progress";
    private String playing;
    private int progress;

    ImageButton pause, play, stop;
    TextView nowPlaying;
    SeekBar progressBar;


    public ControlFragment() {}

    public static ControlFragment newInstance(String playing, int progress) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();

        args.putString(PLAYING_BOOK_KEY, playing);
        args.putInt(PROGRESS_KEY, progress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playing = getArguments().getString(PLAYING_BOOK_KEY);
            progress = getArguments().getInt(PROGRESS_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control, container, false);

        pause = v.findViewById(R.id.PauseImageButton);
        play = v.findViewById(R.id.PlayImageButton);
        stop = v.findViewById(R.id.StopImageButton);
        nowPlaying = v.findViewById(R.id.NowPlayingtextView);
        progressBar = v.findViewById(R.id.ProgressSeekBar);


        if(playing != null) {
            nowPlaying.setText("Now Playing: " + playing);
            if(progressBar != null) {
                progressBar.setProgress(progress);
            }
        }
        return v;
    }
}