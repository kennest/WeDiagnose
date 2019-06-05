package wesicknessdect.example.org.wesicknessdetect.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;

import java.lang.reflect.Type;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wesicknessdect.example.org.wesicknessdetect.R;
import wesicknessdect.example.org.wesicknessdetect.activities.tensorflow.Classifier;
import wesicknessdect.example.org.wesicknessdetect.adapters.PartialResultImageAdapter;
import wesicknessdect.example.org.wesicknessdetect.tasks.RemoteTasks;
import wesicknessdect.example.org.wesicknessdetect.tasks.SystemTasks;
import wesicknessdect.example.org.wesicknessdetect.models.Diagnostic;
import wesicknessdect.example.org.wesicknessdetect.models.Disease;
import wesicknessdect.example.org.wesicknessdetect.models.DiseaseSymptom;
import wesicknessdect.example.org.wesicknessdetect.models.Profile;
import wesicknessdect.example.org.wesicknessdetect.models.Symptom;
import wesicknessdect.example.org.wesicknessdetect.utils.AppController;

public class PartialResultActivity extends BaseActivity implements CardStackListener {

    @BindView(R.id.analysed_img)
    CardStackView images_analysed_lv;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.btn_save_diagnostic)
    FloatingActionButton save_diagnostic;

    @BindView(R.id.btn_next_diagnostic)
    FloatingActionButton go_to_quiz_diagnostic;


    @BindView(R.id.disease_txt)
    TextView disease;
    String TAG = "PartialResultActivity";

    Map<Integer, List<Classifier.Recognition>> recognitions_by_part = new HashMap<>();
    Map<Integer, String> images_by_parts = new HashMap<>();
    HashMap<Long, Integer> disease_score = new HashMap<>();
    List<Integer> img_symptoms_id = new ArrayList<>();
    Set<String> symptoms_set = new HashSet<>();
    List<Classifier.Recognition> recognitions = new ArrayList<>();
    private Map<Integer, Map<Integer, String>> images_by_part_adapter = new HashMap<>();
    PartialResultImageAdapter partialResultImageAdapter;
    CardStackLayoutManager manager;
    Diagnostic diagnostic = new Diagnostic();
    int index = 0;
    Map.Entry<Long, Integer> maxEntry = null;
    String symptoms_ids = "";


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partial_results);
        ButterKnife.bind(this);
        InitCardSwipe();

        //Get Location
        SystemTasks.getInstance(PartialResultActivity.this).ensureLocationSettings();

        //Init Diagnostic infos
        InitDiagnosticData();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InitScoreData();
            }
        });


    }

    private void InitDiagnosticData() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        String location = FastSave.getInstance().getString("location", "0.0:0.0");
        String[] locpart = location.split(":");

        diagnostic.setLocalisation("SRID=4326;POINT (" + locpart[0] + " " + locpart[1] + ")");


        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD HH:mm");
        String date = df.format(c.getTime());
        Log.e("Time:", date);

        diagnostic.setProbability(95f);
        long user_id = Long.parseLong(FastSave.getInstance().getString("user_id", "1"));
        diagnostic.setUser_id(user_id);
        diagnostic.setIs_share(0);
        diagnostic.setCulture_id(1);
        diagnostic.setFinish(true);
        diagnostic.setAdvancedAnalysis("none");
        String uuid = UUID.randomUUID().toString();
        diagnostic.setUuid(uuid);
        diagnostic.setCreation_date(date);
        diagnostic.setImages_by_parts(images_by_parts);

        DB.profileDao().getAll().observe(this, new Observer<List<Profile>>() {
            @Override
            public void onChanged(List<Profile> profiles) {
                for (Profile p : profiles) {
                    diagnostic.setCountry_id(p.getCountry_id());
                }
            }
        });
    }

    private void InitScoreData() {

        //Get All recognition in one list
        for (Map.Entry<Integer, List<Classifier.Recognition>> recognition_entry : recognitions_by_part.entrySet()) {
            recognitions.addAll(recognition_entry.getValue().subList(0, 4));
        }

        //Log.e("All img Recognitions", recognitions.size() + "");

        //Add distinct label in a list
        for (Classifier.Recognition r : recognitions) {
            symptoms_set.add(r.getTitle().toUpperCase());
        }

        //Log.e("All Recognitions label", symptoms_set.size() + "");

        //Check Symptoms Table to get the id of the given label
        DB.symptomDao().getAll().observe(PartialResultActivity.this, new Observer<List<Symptom>>() {
            @Override
            public void onChanged(List<Symptom> symptoms) {
                for (Symptom s : symptoms) {
                    //Remove accent from String
                    String item = Normalizer.normalize(s.getName(), Normalizer.Form.NFD);
                    item = item.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

                    for (String n : symptoms_set) {
                        //Log.e("All Symptoms checked:", item.toUpperCase() + "//" + n);
                        if (item.toUpperCase().equals(n)) {
                            img_symptoms_id.add(s.getId());
                            if (symptoms_ids == "") {
                                symptoms_ids = +s.getId() + "";
                            } else {
                                symptoms_ids = symptoms_ids + ":" + s.getId();
                            }
                        }
                    }
                    diagnostic.setSymptoms(symptoms_ids);
                }
                // Log.e("All img symptom id", img_symptoms_id.size() + "");
            }
        });

        DB.diseaseDao().getAll().observe(PartialResultActivity.this, new Observer<List<Disease>>() {
            @Override
            public void onChanged(List<Disease> diseases) {
                for (Disease d : diseases) {
                    disease_score.put((long) d.getId(), 0);
                }
                DB.diseaseSymptomsDao().getAll().observe(PartialResultActivity.this, new Observer<List<DiseaseSymptom>>() {
                    @Override
                    public void onChanged(List<DiseaseSymptom> diseaseSymptoms) {
                        int score = 0;
                        for (DiseaseSymptom ds : diseaseSymptoms) {
                            for (Integer i : img_symptoms_id) {
                                //Log.e("Score index", (long)i+ "//"+ds.getSymptom_id() );
                                Long l = Long.valueOf(i);
                                if (l.equals(ds.getSymptom_id())) {
                                    //Log.e("Score index equal", (long) i + "//" + ds.getSymptom_id() + "//" + ds.getDisease_id());
                                    score = score + 1;
                                    disease_score.put(ds.getDisease_id(), score);
                                }
                            }
                        }


                        //Get the max value of the score map
                        //Log.e("Score", disease_score.size() + "");
                        for (Map.Entry<Long, Integer> score_entry : disease_score.entrySet()) {
                            //Log.e("Score " + score_entry.getKey(), score_entry.getValue() + "");
                            if (maxEntry == null || score_entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                                maxEntry = score_entry;
                                DB.diseaseDao().getAll().observe(PartialResultActivity.this, new Observer<List<Disease>>() {
                                    @Override
                                    public void onChanged(List<Disease> diseases) {
                                        for (Disease d : diseases) {
                                            if (d.getId() == maxEntry.getKey()) {
                                                disease.setText(d.getName().toUpperCase());
                                                diagnostic.setDisease(d.getName());
                                            }
                                        }
                                    }
                                });
                            }
                        }

                    }
                });
            }
        });

    }

    @OnClick(R.id.btn_save_diagnostic)
    public void SendDiagnostic() {
            diagnostic.setPictures(AppController.getInstance().getPictures());
            RemoteTasks.getInstance(this).sendDiagnostic(diagnostic, false);
        AppController.getInstance().setRecognitions_by_part(recognitions_by_part);
    }

    @OnClick(R.id.btn_next_diagnostic)
    public void goToQuiz(){
        Intent quiz=new Intent(this,QuizActivity.class);

        Gson gson_score=new Gson();
        String disease_score_gson=gson_score.toJson(disease_score);
        String diagnostic_gson=gson_score.toJson(diagnostic);
        String recognition_by_part_gson=gson_score.toJson(recognitions_by_part);

        quiz.putExtra("disease_score_gson",disease_score_gson);
        quiz.putExtra("diagnostic_gson",diagnostic_gson);
        quiz.putExtra("recognition_by_part_gson",recognition_by_part_gson);
        startActivity(quiz);
    }

    private void InitCardSwipe() {
        Gson gson = new Gson();
        Type typeOfHashMap = new TypeToken<Map<Integer, List<Classifier.Recognition>>>() {
        }.getType();
        Type typeOfHashMap2 = new TypeToken<Map<Integer, String>>() {
        }.getType();

        String recognitions_json = getIntent().getStringExtra("recognitions_by_part");
        String images_json = getIntent().getStringExtra("images_by_part");

        //Log.e(getLocalClassName() + " InitCard:", images_json);

        recognitions_by_part = gson.fromJson(recognitions_json, typeOfHashMap);
        images_by_parts = gson.fromJson(images_json, typeOfHashMap2);

        //Log.e(TAG + " map size 0", images_by_parts.size() + "");

        for (Map.Entry<Integer, String> entry : images_by_parts.entrySet()) {
            Map<Integer, String> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            images_by_part_adapter.put(index, map);
            index = index + 1;
            //Log.e(getLocalClassName() + " Index:", index + "");
        }

        //Log.e(TAG + " map size", images_by_part_adapter.size() + "");
        manager = new CardStackLayoutManager(this, this);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(false);
        manager.setSwipeThreshold(0.3f);
        manager.setStackFrom(StackFrom.Top);
        manager.setTranslationInterval(8f);
        partialResultImageAdapter = new PartialResultImageAdapter(this, recognitions_by_part, images_by_part_adapter);
        manager.setVisibleCount(partialResultImageAdapter.getItemCount());
        images_analysed_lv.setLayoutManager(manager);
        images_analysed_lv.setAdapter(partialResultImageAdapter);
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        //Log.e("Card Swiped", manager.getTopPosition() + "/****/" + partialResultImageAdapter.getItemCount());
        if (manager.getTopPosition() == partialResultImageAdapter.getItemCount()) {
            this.Reload();
        }
    }


    @Override
    public void onCardRewound() {

        //Log.e("Card Swiped", "Rewind");
    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {
        //Log.e("Card Appeared ", position + "");
        if (position == 0) {
            //Log.e("Card Appeared ", position + "");
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        //Log.e("Card Disappeared ", position + "//" + partialResultImageAdapter.getItemCount());
        if (position == (partialResultImageAdapter.getItemCount() - 1)) {
            // Log.e("Card Disappeared ", position + "//" + (partialResultImageAdapter.getItemCount() - 1));
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}


