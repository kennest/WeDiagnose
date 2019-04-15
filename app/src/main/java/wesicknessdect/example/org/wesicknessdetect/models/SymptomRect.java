package wesicknessdect.example.org.wesicknessdetect.models;

import android.graphics.RectF;

import com.google.gson.annotations.SerializedName;

import androidx.room.Entity;

import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {@ForeignKey(entity = Symptom.class,
        parentColumns = "id",
        childColumns = "symptom_id",onUpdate = CASCADE)},indices = {@Index({"picture_id","symptom_id"})})
public class SymptomRect extends RectF {

    @SerializedName(value = "id")
    @PrimaryKey(autoGenerate = true)
    public int x;

    @SerializedName(value = "symptom")
    public int symptom_id;

    @SerializedName(value = "picture")
    public int picture_id;

    public int sended;

    public int getSymptom_id() {
        return symptom_id;
    }

    public void setSymptom_id(int symptom_id) {
        this.symptom_id = symptom_id;
    }

    public int getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(int picture_id) {
        this.picture_id = picture_id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getSended() {
        return sended;
    }

    public void setSended(int sended) {
        this.sended = sended;
    }
}
