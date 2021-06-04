package kr.co.dilo.sample.app.banner;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kr.co.dilo.sample.app.databinding.RollingBannerBinding;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private final List<Integer> items;

    public BannerAdapter(List<Integer> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RollingBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(items.get(position % items.size()));
//        holder.imageView.setOnClickListener(v -> DiloSampleAppUtil.openBrowser(v.getContext(), "https://www.dilo.co.kr"));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;

        public ViewHolder(@NonNull RollingBannerBinding view) {
            super(view.getRoot());

            this.imageView = view.banner;
        }
    }
}

