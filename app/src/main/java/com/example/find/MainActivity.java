package com.example.find;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {
    private ImageButton startbtn;
    private ImageView stopbtn ,wait;
    private TextView texttop,textbot;
    private MediaRecorder mRecorder;

    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @SuppressLint({"WrongViewCast", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startbtn = findViewById(R.id.recbutton);
        stopbtn = findViewById(R.id.stoprec);
        wait = findViewById(R.id.wait);
        texttop  = findViewById(R.id.texttop);
        textbot  = findViewById(R.id.textbot);
        // cho nay tai sao dung External -> dung cache duoc khong
        // neu dung external phai request quyen write external storage
        mFileName = getCacheDir().getAbsolutePath();
        mFileName += "/input.3gp";

        if (!CheckPermissions()) {
            RequestPermissions();
        }

        stopbtn.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);

        startbtn.setOnClickListener(v -> {
            startbtn.setVisibility(View.GONE);
            stopbtn.setVisibility(View.VISIBLE);
            textbot.setVisibility(View.GONE);
            texttop.setText("Record music");
            startr();

            startbtn.postDelayed(()-> {
                stopbtn.setVisibility(View.GONE);
                wait.setVisibility(View.VISIBLE);
                texttop.setText("Waiting for result");
                stopr();
            },10000);
        });

    }

    public interface GitHubService {
        @Multipart
        @POST("upload")
        Call<ResponseBody> uploadFile(@Part MultipartBody.Part file);
    }

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .addNetworkInterceptor(new StethoInterceptor())
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("http://34.87.8.31/")
            .build();

    GitHubService service = retrofit.create(GitHubService.class);



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    private void startr() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
        Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
    }

    private void stopr() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();

        File file = new File(mFileName);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("audio/3gpp"),
                file);
        MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        Call<ResponseBody> responseBodyCall = service.uploadFile(filePart);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    assert response.body() != null;
                    JSONArray json = new JSONArray(response.body().string());
                    ArrayList<String> songs = new ArrayList<>();
                    for (int i = 0; i < json.length(); i++) {
                        String song = json.getString(i);
                        songs.add(song);
//                                JSONObject song = json.getJSONObject(i);
//                                songs.add(song.getString("name"));
                    }

                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    intent.putStringArrayListExtra(ListActivity.DATA_KEY, songs);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e("Call API", "Error", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error when uploading record", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
