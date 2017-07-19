package com.dpvr.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dpvr.droidplaycontroller.R;


public class IncomingCallActivity extends BaseActivity {
    public static final String INCOMING_CALL_NAME = "incoming_call_name";


    Button btn_hangon;
    Button btn_hangoff;

    TextView name;

    private TimeHandler timeHandler;


    public static class TimeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        btn_hangon = (Button) findViewById(R.id.btn_hangon);
        btn_hangoff = (Button) findViewById(R.id.btn_hangoff);
        name = (TextView) findViewById(R.id.name);

        Intent intent = getIntent();
        String phone = intent.getStringExtra("phonenum");
        System.out.println("liweiwei...phone = " + phone.toString());

        btn_hangon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("liweiwei...btn_hangon");
                connectManager.callin(true);
            }
        });

        btn_hangoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("liweiwei...btn_hangoff");
                connectManager.callin(false);
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}