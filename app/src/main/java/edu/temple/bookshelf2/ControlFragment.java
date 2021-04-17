package edu.temple.bookshelf2;

import android.content.Context;
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
    private static final String DURATION_KEY = "duration";
    private String playing;
    private int curProgress;
    private int duration;

    ImageButton pause, play, stop;
    TextView nowPlaying;
    SeekBar progressBar;

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

        /*
         This fragment needs to communicate with its parent activity
         so we verify that the activity implemented our defined interface
         */
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

        pause = v.findViewById(R.id.PauseImageButton);
        play = v.findViewById(R.id.PlayImageButton);
        stop = v.findViewById(R.id.StopImageButton);
        nowPlaying = v.findViewById(R.id.NowPlayingtextView);
        progressBar = v.findViewById(R.id.ProgressSeekBar);

        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        if(playing != null && duration != -1) {
            nowPlaying.setText(getString(R.string.playing) + playing);
            if(curProgress != -1) {
                double d = curProgress;
                d = 100*d/duration;
                progressBar.setProgress((int) Math.round(d));
            }
        }

        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!parentActivity.isPlaying()) {
                    parentActivity.pause();
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!parentActivity.isPlaying()) {
                    if(curProgress == 0) {
                        parentActivity.play();
                    } else {
                        parentActivity.pause();
                    }
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.stop();
            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    double p = progress;
                    p = p*(curProgress)/100;
                    parentActivity.seekChange((int) Math.round(p));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        return v;
    }


    interface MediaActionInterface {
        void play();
        void pause();
        void stop();
        int seekChange(int newProgress);
        public boolean isPlaying();
    }

}