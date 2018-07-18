package goods.cap.app.candynote;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import goods.cap.app.candynote.Activity.SearchActivity;
import goods.cap.app.candynote.Activity.SettingActivity;
import goods.cap.app.candynote.Helper.SearchDBHelper;
import goods.cap.app.candynote.Helper.WordHeleper;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btnTTS)ImageButton btnTTS; //Action TTS
    @BindView(R.id.mainTitle)TextView mainTitle; //Search view and help message
    @BindView(R.id.btnPen)ImageButton btnPen; //pen image
    @BindView(R.id.btnSpeak)ImageButton btnSpeak; //voice image
    @BindView(R.id.btnCam)ImageButton btnCam; //camera image
    @BindView(R.id.textPen)TextView textPen; //help pen
    @BindView(R.id.textSpeak)TextView textSpeak; //help speak
    @BindView(R.id.textCam)TextView textCam; //help cam
    @BindView(R.id.textTTS)TextView textTTS; //help tts
    @BindView(R.id.toolbar)Toolbar toolbar;
    private boolean isLoading = false; //Searching or finished
    private boolean isSearch = false; //Search ok or fail
    private TextToSpeech tts;
    private DatabaseReference ref;
    private SearchDBHelper searchDBHelper;
    private WordHeleper wordHeleper;
    private final static int REQ_CODE_SPEECH_INPUT = 100;
    private final static int REQ_CODE_CAM_INPUT = 101;
    private final static int REQ_CODE_PEN_INPUT = 102;
    private final static int MY_TTS_CHECK_CODE = 200;
    private final static int PERMISSION_CEHCK_CODE = 300;
    private static final String logger = MainActivity.class.getSimpleName();
    private String search; //search data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        try{
            initLayout();
            initDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //Init Layout Setting
    private void initLayout(){
        btnTTS.setVisibility(View.GONE);
        textTTS.setVisibility(View.GONE);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    //Check TTS and Init TTS
    private void initTTS(){
        try {
            Intent checkTTS = new Intent();
            checkTTS.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTS, MY_TTS_CHECK_CODE);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.tts_not_setup), Toast.LENGTH_SHORT).show();
        }
    }

    private void initDatabase(){
        //Firebase Init
        ref = FirebaseDatabase.getInstance().getReference();
        //SQLite Init
    }
    //TTS(Korean)
    private void setBtnTTS(){
        //Some data acquired
        if(isSearch){
            if(textTTS.getVisibility() == View.GONE){
                textTTS.setVisibility(View.VISIBLE);
            }
            Animation mAnimation = new AlphaAnimation(1, 0);
            mAnimation.setDuration(200);
            mAnimation.setInterpolator(new LinearInterpolator());
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setRepeatMode(Animation.REVERSE);
            textTTS.startAnimation(mAnimation);
        }else{
            //reset data
            btnTTS.clearAnimation();
            btnTTS.setVisibility(View.GONE);
            textTTS.setVisibility(View.GONE);
        }
    }
    //Main Title (Searching, Search data, Default)
    private void setMainTitle(){
        if(isLoading) {
            mainTitle.setText(getResources().getString(R.string.searching));
            Animation mAnimation = new AlphaAnimation(1, 0);
            mAnimation.setDuration(200);
            mAnimation.setInterpolator(new LinearInterpolator());
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setRepeatMode(Animation.REVERSE);
            mainTitle.startAnimation(mAnimation);
        }else{
            mainTitle.clearAnimation();
            //Some data acquired
            if(isSearch){
                mainTitle.setText(search);
            }else{
                mainTitle.setText(getResources().getString(R.string.not_found));
            }
        }
    }
    @OnClick(R.id.btnPen)
    private void clickPen(){
        if(!isLoading){
            if(getSupportActionBar() != null) {getSupportActionBar().hide();}

        }else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.searching), Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.btnSpeak)
    private void clickSpeak(){
        if(!isLoading){
            if(getSupportActionBar() != null) {getSupportActionBar().hide();}
            //Voice recode
            promptSpeechInput();
        }else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.searching), Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.btnCam)
    private void clickCam(){
        if(!isLoading){
            if(getSupportActionBar() != null) {getSupportActionBar().hide();}

        }else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.searching), Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.btnTTS)
    private void clickTTS(){

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }else{
           switch(item.getItemId()) {
                case R.id.action_settings:
                    startActivity(new Intent(MainActivity.this, SettingActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
                case R.id.action_search_history:
                    startActivity(new Intent(MainActivity.this, SearchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.i(logger, "Voice Result: " + result.get(0));

                }
                break;
            }
            case REQ_CODE_CAM_INPUT: {

                break;
            }
            case REQ_CODE_PEN_INPUT: {

                break;
            }
            case MY_TTS_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                if (tts.isLanguageAvailable(Locale.KOREA) == TextToSpeech.LANG_AVAILABLE) {
                                    tts.setLanguage(Locale.KOREA);
                                }
                            }else if (status == TextToSpeech.ERROR) {
                                Toast.makeText(getApplicationContext(), getString(R.string.tts_not_setup), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else{
                    Intent installTTS = new Intent();
                    installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTS);
                }
            }
        }
    }
    private void speakKorean(String s){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ttsGreater21(s);
        }else{
            ttsUnder20(s);
        }
    }
    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CEHCK_CODE: {
                //Permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_ok),Toast.LENGTH_SHORT).show();
                } else{
                    //Permission denied
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },PERMISSION_CEHCK_CODE);
                }
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    @Override
    protected void onStop(){
        super.onStop();
        if (tts!= null) {
            tts.stop();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
