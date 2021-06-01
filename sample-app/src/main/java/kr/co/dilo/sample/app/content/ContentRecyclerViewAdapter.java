package kr.co.dilo.sample.app.content;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import kr.co.dilo.sample.app.databinding.FragmentHomeBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyContent.DummyItem}.
 */
public class ContentRecyclerViewAdapter extends RecyclerView.Adapter<ContentRecyclerViewAdapter.ViewHolder> {

  private final List<DummyContent.DummyItem> items;

  public ContentRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
    this.items = items;
  }

  private OnContentSelectedListener listener;

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(FragmentHomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    DummyContent.DummyItem item = items.get(position);

    holder.item = item;
    holder.image.setImageResource(item.image);
    holder.title.setText(item.title);
    holder.desc.setText(item.desc);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  /**
   * 컨텐츠 리스트 아이템 (View)
   */
  public class ViewHolder extends RecyclerView.ViewHolder {
    public final ImageView image;
    public final TextView title;
    public final TextView desc;
    public DummyContent.DummyItem item;

    public ViewHolder(FragmentHomeBinding view) {
      super(view.getRoot());

      // 뷰홀더 자체 클릭 시
      View view1 = view.getRoot();
      view1.setOnClickListener(v -> {
        int pos = getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && listener != null) {
          listener.onContentSelected(pos, items.get(pos));
        }
      });

      image = view.contentImage;
      title = view.contentTitle;
      desc  = view.contentDesc;
    }
  }

  public void setOnContentClickListener(OnContentSelectedListener listener) {
    this.listener = listener;
  }

  public interface OnContentSelectedListener {
    void onContentSelected(int pos, DummyContent.DummyItem item);
  }
}
