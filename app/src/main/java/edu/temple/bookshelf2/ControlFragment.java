package edu.temple.bookshelf2;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlFragment extends Fragment {

    private static final String PLAYING_BOOK_KEY = "playing book";
    private static final String PROGRESS_KEY = "progress";
    private static final String DURATION_KEY = "duration";
    private String playing;
    private int curProgress;
    private int duration;
    private boolean started;

    private TextView nowPlayingTextView;
    private SeekBar seekBar;

    MediaActionInterface parentActivity;



    public ControlFragment() {}

    public static ControlFragment newInstance(String playing, int progress, int duration) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();

        args.putString(PLAYING_BOOK_KEY, playing);
        args.putInt(PROGRESS_KEY, progress);
        args.putInt(DURATION_KEY, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MediaActionInterface) {
            parentActivity = (MediaActionInterface) context;
        } else {
            throw new RuntimeException("Please implement the required interface(s)");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playing = getArguments().getString(PLAYING_BOOK_KEY);
            curProgress = getArguments().getInt(PROGRESS_KEY);
            duration = getArguments().getInt(DURATION_KEY);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control, container, false);

        nowPlayingTextView = v.findViewById(R.id.NowPlayingtextView);
        seekBar = v.findViewById(R.id.ProgressSeekBar);
        seekBar.setMax(100);

        v.findViewById(R.id.PauseImageButton).setOnClickListener((view) -> {
            parentActivity.pause();
        });
        v.findViewById(R.id.PlayImageButton).setOnClickListener((view) -> {
            parentActivity.play();
        });
        v.findViewById(R.id.StopImageButton).setOnClickListener((view) -> {
            parentActivity.stop();
        });

        //If the user is dragging the seekbar, update the book postion
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int changedProg;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    parentActivity.seekChange(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return v;
    }

    public void setNowPlaying(String title) {
        nowPlayingTextView.setText(title);
    }

    public void updateProgress(int progress) {
        seekBar.setProgress(progress);
    }

    interface MediaActionInterface {
        void play();
        void pause();
        void stop();
        void seekChange(int newProgress);
    }

}