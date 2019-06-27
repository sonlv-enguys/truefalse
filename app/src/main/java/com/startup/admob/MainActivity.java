package com.startup.admob;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int diem = 0;
    View btnTrue, btnFalse;
    ImageView imgStatus;
    ProgressBar progressBar;
    TextView tvPhepTinh, tvDiem, tvTime;
    boolean isFalse;
    AudioManager audio;
    CountDownTimer countDownTimer;
    int totalTime = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress);
        imgStatus = findViewById(R.id.imgStatus);
        tvDiem = findViewById(R.id.tvDiem);
        tvPhepTinh = findViewById(R.id.tvPhepTinh);
        btnFalse = findViewById(R.id.btnFalse);
        btnTrue = findViewById(R.id.btnTrue);
        tvTime = findViewById(R.id.tvTime);
        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFalse) {
                    imgStatus.setImageResource(R.drawable.clear);
                    if (diem > 0) diem--;
                    playMedia(R.raw.error);
                } else {
                    imgStatus.setImageResource(R.drawable.checked);
                    diem++;
                    playMedia(R.raw.next);
                }
                showStatus();
            }
        });
        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFalse) {
                    imgStatus.setImageResource(R.drawable.checked);
                    diem++;
                    playMedia(R.raw.next);
                } else {
                    imgStatus.setImageResource(R.drawable.clear);
                    if (diem > 0) diem--;
                    playMedia(R.raw.error);
                }
                showStatus();
            }
        });
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateVolume();
        loadGame();
        startTime();

    }

    public void playMedia(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    public void updateVolume() {
        int volume = audio.getStreamVolume(AudioManager.STREAM_RING);
        if (volume > 5) volume = 5;
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_VIBRATE);
    }

    public void startTime() {
        progressBar.setProgress(100);
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(totalTime, 1) {
            public void onTick(long millisUntilFinished) {
                int time = Math.round(100.0f
                        * millisUntilFinished / totalTime);
                progressBar.setProgress(time);
                tvTime.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                gameOver();
            }
        }.start();
    }

    public void showStatus() {
        btnFalse.setEnabled(false);
        btnTrue.setEnabled(false);

        imgStatus.setVisibility(View.VISIBLE);
        imgStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                imgStatus.setVisibility(View.INVISIBLE);
                loadGame();
                btnFalse.setEnabled(true);
                btnTrue.setEnabled(true);
            }
        }, 1000);
        tvDiem.setText("Turn: " + diem);

    }

    public void loadGame() {
        String phepTinh = "";
        int a = new Random().nextInt(100);
        int b = new Random().nextInt(100);
        isFalse = new Random().nextBoolean();
        if (new Random().nextBoolean()) {
            int c = a + b;
            if (isFalse) c += new Random().nextInt(10);
            phepTinh = a + " + " + b + " = " + c;
        } else {
            a = new Random().nextInt(50);
            b = new Random().nextInt(50);
            int c = a * b;
            if (isFalse) c += new Random().nextInt(10);
            phepTinh = a + " x " + b + " = " + c;
        }
        tvPhepTinh.setText(phepTinh);
    }


    public void gameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        progressBar.setProgress(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hết giờ");
        builder.setMessage("Bạn đạt được " + diem + " điểm");
        builder.setCancelable(false);
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                diem = 0;
                loadGame();
                startTime();
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
