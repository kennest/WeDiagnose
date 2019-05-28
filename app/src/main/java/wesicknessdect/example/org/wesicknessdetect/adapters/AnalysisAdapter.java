package wesicknessdect.example.org.wesicknessdetect.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import wesicknessdect.example.org.wesicknessdetect.R;
import wesicknessdect.example.org.wesicknessdetect.activities.AnalysisDetailsActivity;
import wesicknessdect.example.org.wesicknessdetect.models.Diagnostic;
import wesicknessdect.example.org.wesicknessdetect.models.DiagnosticPictures;
import wesicknessdect.example.org.wesicknessdetect.models.Picture;

/**
 * Created by Jordan Adopo on 03/02/2019.
 */

public class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.StatusHolder> {

    Activity context;
    List<Diagnostic> diagnostics;
    RelativeLayout container;

    public AnalysisAdapter(Activity context, List<Diagnostic> diagnostics) {
        this.context = context;
        this.diagnostics = diagnostics;
    }

    @NonNull
    @Override
    public StatusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.analysis_list_item,
                parent,
                false);
        return new StatusHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull StatusHolder holder, int position) {
        Log.e("XXXX 0 " + position, diagnostics.get(position).getPictures().size() + "");
        if (diagnostics.get(position).getPictures().size() > 0) {

            holder.counter.setText(Integer.toString(diagnostics.get(position).getPictures().size()));
            for (Picture s : diagnostics.get(position).getPictures()) {
                Log.e("XXXX N " + position, s.getImage());
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(new File(diagnostics.get(position).getPictures().get(0).getImage())), options);
            Glide.with(context)
                    .asBitmap()
                    .load(bm)
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().error(R.drawable.information))
                    .apply(new RequestOptions().placeholder(R.drawable.restart))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.image);
            //holder.image.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(new File(diagnosticPictures.get(position).pictures.get(0).getImage()))));
        }
        holder.userName.setText(diagnostics.get(position).getDisease());
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String result = formatter.format(now);
        holder.now.setText(result);
        holder.itemView.setTag(diagnostics.get(position).getX());
        //holder.analyseTime.setText(diagnosticPictures.get(position).diagnostic.getAdvancedAnalysis()+" Ago");
        holder.analyseTime.setText("1 min Ago");
        //holder.slideview.addOnPageChangeListener(this);

        holder.image.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));
    }

    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
    }

    @Override
    public int getItemCount() {
        return diagnostics.size();
    }

    class StatusHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        CircularImageView image;

        @BindView(R.id.now)
        TextView now;

        @BindView(R.id.user_name)
        TextView userName;

        @BindView(R.id.container)
        RelativeLayout container;

        @BindView(R.id.analyse_time)
        TextView analyseTime;

        @BindView(R.id.counter)
        TextView counter;

        public StatusHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("History item", "CLICKED");
                    Intent i = new Intent(context, AnalysisDetailsActivity.class);
                    i.putExtra("id", (Integer) v.getTag());
                    context.startActivity(i);
                }
            });
        }
    }
}
