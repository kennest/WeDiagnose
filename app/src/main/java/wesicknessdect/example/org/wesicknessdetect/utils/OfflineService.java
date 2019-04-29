package wesicknessdect.example.org.wesicknessdetect.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import wesicknessdect.example.org.wesicknessdetect.database.AppDatabase;
import wesicknessdect.example.org.wesicknessdetect.futuretasks.RemoteTasks;
import wesicknessdect.example.org.wesicknessdetect.models.Diagnostic;
import wesicknessdect.example.org.wesicknessdetect.models.Picture;
import wesicknessdect.example.org.wesicknessdetect.models.SymptomRect;

public class OfflineService extends Service {
    private Timer mTimer = null;
    public AppDatabase DB;
    public static String str_receiver = "scanleaf.offline.service";
    Intent intent;
    List<SymptomRect> symptomRects;
    List<Diagnostic> diagnostics;
    List<Picture> pictures;

    public OfflineService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        DB = AppDatabase.getInstance(this);
        Toast.makeText(getApplicationContext(), "Offline service Started", Toast.LENGTH_LONG).show();
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskOffline(), 1, 60000);
        intent = new Intent(str_receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @SuppressLint("StaticFieldLeak")
    private void SendData() {

        if (diagnostics != null) {
            for (Diagnostic d : diagnostics) {
                Log.e("Diag::Size", diagnostics.size() + "");
                if (d.getSended() == 0) {
                    Log.e("Offline diag::", d.getX() + "//" + d.getSended());
                    try {
                        RemoteTasks.getInstance(getApplicationContext()).sendDiagnostic(d);
                        for (Picture p : pictures) {
                            Log.e("Offline pic::", p.getX() + "//" + p.getSended() + "//" + p.getCulture_part_id());
                            if (p.getDiagnostic_id() == d.getX()) {
                                if (p.getSended() == 0) {
                                    Log.e("Offline pic::", p.getX() + "//" + p.getSended());
                                    try {
                                        RemoteTasks.getInstance(getApplicationContext()).SendDiagnosticPicture(p);
                                        for (SymptomRect rect : symptomRects) {
                                            if (rect.getPicture_id() == p.getX()) {
                                                //Log.e("Offline RectF::", rect.picture_id+"/ SENDED-> "+rect.getSended()+"/ID-> "+rect.getX());
                                                if (rect.getSended() == 0) {
                                                    Log.e("Offline RectF::", rect.picture_id + "/ SENDED-> " + rect.getSended() + "/ID-> " + rect.getX());
                                                    RemoteTasks.getInstance(getApplicationContext()).sendSymptomRect(rect);
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void SendDataOffline() throws IOException {
        if (diagnostics != null) {
            for (Diagnostic d : diagnostics) {
                Log.e("Diag::Size", diagnostics.size() + "");
                if (d.getSended() == 0) {
                    RemoteTasks.getInstance(getApplicationContext()).SendOfflineDiagnostic(d);
                }
            }
        }
    }

    private class TimerTaskOffline extends TimerTask {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void run() {

            String location= FastSave.getInstance().getString("location","0.0:0.0");
            String[] split=location.split(":");

            Double lat= Double.valueOf(split[0]);
            Double longi= Double.valueOf(split[1]);

            Log.d("Mes coordonnées", "Lat: "+lat+", Longi: "+longi);
            //Toast.makeText(getApplicationContext(), "Offline Really Started", Toast.LENGTH_LONG).show();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }


                @Override
                protected Void doInBackground(Void... voids) {
                    Log.e("Pre Task", "Started");
                    diagnostics = DB.diagnosticDao().getAllSync();
                    pictures = DB.pictureDao().getAllSync();
                    symptomRects = DB.symptomRectDao().getAllSync();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        SendDataOffline();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("Pre Task", "Finished");
                }
            }.execute();
        }

    }
}


