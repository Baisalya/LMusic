package com.lala.lmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button rewind, markbutton, forward, playbtn, btnnext, btnprev;
    TextView txtsname, txtstart, txtsstop;
    SeekBar seekmusic;
    CircleLineVisualizer visualizer;
    ImageView imageview;

    String sname;
    public static final String EXTRA_NAME = "Song_name";
    static MediaPlayer mediaPlayer;
    int postion;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getSupportActionBar().setTitle("Now Playing");
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_player);
        rewind=findViewById(R.id.rewind);
        markbutton=findViewById(R.id.markbtn);
        forward=findViewById(R.id.forward);
        playbtn=findViewById(R.id.playbtn);
        btnnext=findViewById(R.id.btnnext);
        btnprev=findViewById(R.id.btnprev);
        seekmusic=findViewById(R.id.seekbar);
        txtsname=findViewById(R.id.txtsn);
        txtstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);
         imageview=findViewById(R.id.imageview);
        // visualizer=findViewById(R.id.blast);
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();

        }
        Intent i=getIntent();
        Bundle bundle=i.getExtras();
        mySongs=(ArrayList)bundle.getParcelableArrayList("songs");
        String songName=i.getStringExtra("songname");
        postion=bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri=Uri.parse(mySongs.get(postion).toString());
        sname=mySongs.get(postion).getName();
        txtsname.setText(sname);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        updateseekbar= new Thread()
        {
            @Override
            public void run(){
                int totalDuration=mediaPlayer.getDuration();
                int currentpostion=0;
                while (currentpostion<totalDuration){
                    try {
                        sleep(500);
                        currentpostion=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentpostion);
                    }
                    catch (InterruptedException|IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.purple_200),PorterDuff.Mode.SRC_IN);
        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        String endTime=creatTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);
        final Handler handler=new Handler();
        final int delay=1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime=creatTime(mediaPlayer.getCurrentPosition());
                txtstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        }, delay);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    playbtn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else
                    {
                    playbtn.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

                }
            }
        });
        //visualizer after adding paste next prev
       int audiosessionId=mediaPlayer.getAudioSessionId();
        if (audiosessionId!=-1){
            visualizer.setAudioSessionId(audiosessionId);
        }
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                postion=((postion+1))%mySongs.size();
                Uri u=Uri.parse(mySongs.get(postion).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname=mySongs.get(postion).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageview);
            }
        });
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                postion=((postion-1)<0)?(mySongs.size()):(postion-1);
                Uri u=Uri.parse(mySongs.get(postion).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname=mySongs.get(postion).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageview);
                int audiosessionId=mediaPlayer.getAudioSessionId();
                if (audiosessionId!=-1){
                    visualizer.setAudioSessionId(audiosessionId);
                }


            }
        });

        //next song
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
              btnnext.performClick();
            }
        });
        //rewind&forward
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-50000);

                }

            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+50000);
                }
            }
        });
        markbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PlayerActivity.this,"under construction",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startAnimation(View view){
        ObjectAnimator animator=ObjectAnimator.ofFloat(imageview,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }
    public String creatTime(int duration){
        String time ="";
        int min=duration/1000/60;
        int sec=duration/1000%60;
        time+=min+":";
        if (sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }
}