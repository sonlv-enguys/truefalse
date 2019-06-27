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
    //Khai báo biến thể hiện điểm người chơi đạt được
    int diem = 0;
    //Khai báo nút TRUE và FALSE
    View btnTrue, btnFalse;
    //Khai báo ImageView thể hiện kết quả khi người dùng chọn TRUE hoặc FALSE
    ImageView imgStatus;
    //Khai báo thanh trạng thái đếm lùi thời gian chơi game
    ProgressBar progressBar;
    //Khai báo TextView thể hiện phép tính, điểm và thời gian
    TextView tvPhepTinh, tvDiem, tvTime;
    //Khai báo kết quả phép tính
    boolean isFalse;
    //Khai báo AudioManager để cập nhật lại âm lượng âm thanh phát ra
    AudioManager audio;
    //Khai báo CountDownTimer dùng để chạy thời gian đếm lùi
    CountDownTimer countDownTimer;
    //Khai báo tổng thời gian chơi 1 lượt là 60 giây
    int totalTime = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set view xml cho activity
        setContentView(R.layout.activity_main);

        //Khởi tạo các view theo id đã khai báo trong file xml
        progressBar = findViewById(R.id.progress);
        imgStatus = findViewById(R.id.imgStatus);
        tvDiem = findViewById(R.id.tvDiem);
        tvPhepTinh = findViewById(R.id.tvPhepTinh);
        btnFalse = findViewById(R.id.btnFalse);
        btnTrue = findViewById(R.id.btnTrue);
        tvTime = findViewById(R.id.tvTime);

        //Set sự kiện click cho nút TRUE
        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFalse) {// Nếu đáp án là sai mà chọn đúng thì bị trừ 1 điểm, đồng thời hiện thị ảnh là sai và phát ra âm thanh sai
                    imgStatus.setImageResource(R.drawable.clear);
                    if (diem > 0) diem--;
                    playMedia(R.raw.error);
                } else {
                    // Nếu đáp án là đúng mà chọn đúng thì cộng 1 điểm, đồng thời hiện thị ảnh là đúng và phát ra âm thanh đúng
                    imgStatus.setImageResource(R.drawable.checked);
                    diem++;
                    playMedia(R.raw.next);
                }
                //Sau khi hiện thị đúng sai thì tiếp tục xử lý phép tính tiếp theo
                showStatus();
            }
        });
        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFalse) {// Nếu đáp án là sai mà chọn sai thì được cộng 1 điểm, đồng thời hiện thị ảnh là đúng và phát ra âm thanh đúng
                    imgStatus.setImageResource(R.drawable.checked);
                    diem++;
                    playMedia(R.raw.next);
                } else {
                    // Nếu đáp án là đúng mà chọn sai thì bị trừ 1 điểm, đồng thời hiện thị ảnh là sai và phát ra âm thanh sai
                    imgStatus.setImageResource(R.drawable.clear);
                    if (diem > 0) diem--;
                    playMedia(R.raw.error);
                }
                //Sau khi hiện thị đúng sai thì tiếp tục xử lý phép tính tiếp theo
                showStatus();
            }
        });
        //Khởi tạo audio để phát âm thanh
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //Cập nhật âm lượng
        updateVolume();
        //Bắt đầu chơi game
        loadGame();
        //Bắt đầu tính ngược thời gian
        startTime();

    }

    /*
    Phát ra một âm thanh
     */
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

    /*
    Cập nhật lại âm lượng cho audio
     */
    public void updateVolume() {
        int volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume > 5) volume = 5;
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_VIBRATE);
    }

    /*
    Bắt đầu tính thời gian chạy cho một lượt chơi
    - Set giá trị Progress của progressBar về 100
     */
    public void startTime() {
        progressBar.setProgress(100);
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(totalTime, 1) {
            public void onTick(long millisUntilFinished) {
                //Cập nhật lại progressBar và thời gian đếm lùi
                int time = Math.round(100.0f
                        * millisUntilFinished / totalTime);
                progressBar.setProgress(time);
                tvTime.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // Khi hết thời gian thì hiện thị thông báo kết thúc và thông báo số điểm người chơi đạt được
                gameOver();
            }
        }.start();
    }

    /*
    Mỗi lần người chơi chọn đáp án đúng sai thì cập nhật lại số điểm và hiện thị hình ảnh đúng sai đồng thời disable hai nút TRUE và FALSE.
    Sau 1 giây thì ẩn hình ảnh đúng sai đồng thời enable hai nút TRUE và FALSE xong thì load lượt chơi tiếp theo
     */
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

    /*
    Load lượt chơi tiếp theo:
    1. Khởi tạo ngẫu nhiên hai số a và b trong khoảng 0 đến 100
    2. Khởi tạo ngẫu nhiên một kết quả đúng hoặc sai
    3. Khởi tạo 1 giá trị ngẫu nhiên là phép nhân hay phép cộng
    4. tính toán biến c là giá trị của a + b hoặc a x c
    5. Nếu kết quả ở bước 2 là đúng thì giữ nguyên giá trị c ở bước 4. Còn sai thì kết quả c sẽ được cộng ngẫu nhiên một số từ 0 đến 10
     */
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

    /*
    Hiện thị thông báo hết giờ và kết quả đạt được
    1. nếu chọn Restart thì reset điểm về 0 và bắt đầu chơi game cũng như chạy lại bộ đếm lùi thời gian
    2. Nếu chọn Close thì đóng thông báo và thoát game
     */
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
