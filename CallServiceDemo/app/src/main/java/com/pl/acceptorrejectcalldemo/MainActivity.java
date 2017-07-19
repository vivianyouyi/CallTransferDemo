package com.pl.acceptorrejectcalldemo;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity {

    Button btn_start_test;
    Button btn_end_test;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start_test = (Button) findViewById(R.id.btn_start_test);
        btn_end_test = (Button) findViewById(R.id.btn_end_test);

        btn_start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.startRecord();
            }
        });
        btn_end_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                audioManager.stopRecord();
            }
        });
        //  playMusic();
    }

    private void playMusic() {
        String musicURL = "http://www.roboming.com/music.mp3";
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(musicURL);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
