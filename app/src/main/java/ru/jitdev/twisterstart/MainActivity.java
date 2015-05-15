package ru.jitdev.twisterstart;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


public class MainActivity extends ActionBarActivity implements
        RecognitionListener, View.OnClickListener, SoundPool.OnLoadCompleteListener {
    final Random random = new Random();
    private ArrayList<String> alResult = new ArrayList<>();
    private ArrayList<Integer> alColors = new ArrayList<>();
    private LinearLayout llColor;
    private ImageView ivLimb;
    private TextView tvResult;
    private TextView tvPreload;
    private SoundPool spSound;
    Button btnStart, btnRepeat;
    private int SoundLeft;
    private int SoundRight;
    private int SoundHand;
    private int SoundLeg;
    private int SoundOn;
    private int SoundRed;
    private int SoundGreen;
    private int SoundYellow;
    private int SoundBlue;
    private int Sound1;
    private int Sound2;
    private int Sound3;
    private int SoundClick;
    private SpeechRecognizer recognizer;

    // Поисковые фразы
    private static final String KEYWORD = "twister";
    private static final String MENU_SEARCH = "menu";
    private static final String COMMAND_START = "start";
    private static final String COMMAND_HELLO = "hello";

    // Заголовки
    TextView tvCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPreload = (TextView)findViewById(R.id.tvPreload);
        tvCaption = (TextView)findViewById(R.id.tvCaption);
        tvPreload.setText(R.string.preload);


        btnStart = (Button) findViewById(R.id.btnStart);
        btnRepeat = (Button) findViewById(R.id.btnRepeat);
        btnStart.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);

        ivLimb = (ImageView) findViewById(R.id.ivLimb);
        llColor = (LinearLayout) findViewById(R.id.llColor);
        tvResult = (TextView) findViewById(R.id.tvResult);

        Resources res = getResources();
        alResult.add(res.getString(R.string.res_LeftHandRed));
        alResult.add(res.getString(R.string.res_LeftHandGreen));
        alResult.add(res.getString(R.string.res_LeftHandYellow));
        alResult.add(res.getString(R.string.res_LeftHandBlue));
        alResult.add(res.getString(R.string.res_RightHandRed));
        alResult.add(res.getString(R.string.res_RightHandGreen));
        alResult.add(res.getString(R.string.res_RightHandYellow));
        alResult.add(res.getString(R.string.res_RightHandBlue));
        alResult.add(res.getString(R.string.res_RightLegRed));
        alResult.add(res.getString(R.string.res_RightLegGreen));
        alResult.add(res.getString(R.string.res_RightLegYellow));
        alResult.add(res.getString(R.string.res_RightLegBlue));
        alResult.add(res.getString(R.string.res_LeftLegRed));
        alResult.add(res.getString(R.string.res_LeftLegGreen));
        alResult.add(res.getString(R.string.res_LeftLegYellow));
        alResult.add(res.getString(R.string.res_LeftLegBlue));

        alColors.add(res.getColor(R.color.clr_Red));
        alColors.add(res.getColor(R.color.clr_Green));
        alColors.add(res.getColor(R.color.clr_Yellow));
        alColors.add(res.getColor(R.color.clr_Blue));

        spSound = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        spSound.setOnLoadCompleteListener(this);
        SoundLeft = spSound.load(this, R.raw.left, 1);
        SoundRight = spSound.load(this, R.raw.right, 1);
        SoundHand = spSound.load(this, R.raw.hand, 1);
        SoundLeg = spSound.load(this, R.raw.leg, 1);
        SoundOn = spSound.load(this, R.raw.on, 1);
        SoundRed = spSound.load(this, R.raw.red, 1);
        SoundGreen = spSound.load(this, R.raw.green, 1);
        SoundYellow = spSound.load(this, R.raw.yellow, 1);
        SoundBlue = spSound.load(this, R.raw.blue, 1);
        SoundClick = spSound.load(this,R.raw.click,1);

        // Подготавливаем разпознаватор. Это займет некоторое время, поэому делается асинхронно
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    tvCaption.setText("Failed to init recognizer " + result);
                } else {
                    tvPreload.setText(R.string.title);
                    switchSearch(KEYWORD);
                }
            }
        }.execute();
    }

    // Control And View
    @Override
    public void onClick(View v) {
        if(!"".equals(tvPreload.getText())){tvPreload.setText("");}

        switch (v.getId()) {
            case R.id.btnStart: // кнопка Start
                int randResult = random.nextInt(16);
                String resText = alResult.get(randResult);
                int resColor = FindColor(randResult);
                Drawable resLimb = FindLimb(randResult);
                Sound1 = FindSideSound(randResult);
                Sound2 = FindLimbSound(randResult);
                Sound3 = FindColorSound(resColor);
                SetColorAndLimb(resText, resColor, resLimb);
                new Thread(new Runnable() {
                    public void run() {
                        PlaySounds(Sound1, Sound2, Sound3);
                    }
                }).start();
                break;
            case R.id.btnRepeat: // кнопка Repeat
                PlaySounds(Sound1, Sound2, Sound3);
                break;
        }

    }
    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

    }

    private void SetColorAndLimb(String resText, int resColor, Drawable resLimb){
        // Устанавливаем текст
        tvResult.setText(resText);
        // Установть цвет
        llColor.setBackgroundColor(alColors.get(resColor));
        // Установить конечность
        ivLimb.setImageDrawable(resLimb);
    }
    private void PlaySounds(int sound1, int sound2, int sound3){
        spSound.play(sound1, 1, 1, 0, 0, 1.1f);
        try {
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spSound.play(sound2, 1, 1, 0, 0, 1.1f);
        try {
            TimeUnit.MILLISECONDS.sleep(450);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spSound.play(SoundOn, 1, 1, 0, 0, 1.1f);
        try {
            TimeUnit.MILLISECONDS.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spSound.play(sound3, 1, 1, 0, 0, 1.1f);
    }

    private int FindColor(int result){
        if (result < 4){
            return result;
        }else {
            while (result > 3) {
                result = result - 4;
            }
            return result;
        }
    }
    private Drawable FindLimb(int result){
        switch (result/4) {
            case 0:
                return getResources().getDrawable(R.drawable.lefthand);
            case 1:
                return getResources().getDrawable(R.drawable.righthand);
            case 2:
                return getResources().getDrawable(R.drawable.rightleg);
            case 3:
                return getResources().getDrawable(R.drawable.leftleg);
            default:
                return null;
        }
    }

    private int FindSideSound(int result) {
        if(result < 4 || result > 11){
            return SoundLeft;
        }else{
            return SoundRight;
        }
    }
    private int FindLimbSound(int result) {
        if(result < 8){
            return SoundHand;
        }else{
            return SoundLeg;
        }
    }
    private int FindColorSound(int result) {
        switch (result) {
            case 0:
                return SoundRed;
            case 1:
                return SoundGreen;
            case 2:
                return SoundYellow;
            case 3:
                return SoundBlue;
            default:
                return -1;
        }
    }

    // Recognition
    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Чувствительно распознования
                .setKeywordThreshold(1e-43f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(KEYWORD, KEYWORD);
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }
    private void switchSearch(String searchName) {
        recognizer.stop();
        if (searchName.equals(KEYWORD)){
            recognizer.startListening(searchName);
            tvCaption.setText("Скажите \"Твистер!\"");}
        else if (searchName.equals(MENU_SEARCH)){
             recognizer.startListening(searchName, 5 * 1000);
             spSound.play(SoundClick, 1, 1, 0, 0, 1);
             tvCaption.setText("Скажите команду");}
        else if (searchName.equals(COMMAND_START)){
            recognizer.startListening(KEYWORD);
            tvCaption.setText("");
            onClick(btnStart);}
        else if (searchName.equals(COMMAND_HELLO)) {
            recognizer.startListening(KEYWORD);
            tvCaption.setText("");
            onClick(btnRepeat);}

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();

    }
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(KEYWORD))
            switchSearch(MENU_SEARCH);
        if (text.equals(COMMAND_START))
            switchSearch(COMMAND_START);
        if (text.equals(COMMAND_HELLO))
            switchSearch(COMMAND_HELLO);
    }
    @Override
    public void onResult(Hypothesis hypothesis) {

    }
    @Override
    public void onBeginningOfSpeech() {

    }
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KEYWORD))
            switchSearch(KEYWORD);
        tvCaption.setText("Скажите \"Твистер!\"");
    }
    @Override
    public void onError(Exception e) {
        tvCaption.setText(e.getMessage());
    }
    @Override
    public void onTimeout() {
        switchSearch(KEYWORD);
    }



}
