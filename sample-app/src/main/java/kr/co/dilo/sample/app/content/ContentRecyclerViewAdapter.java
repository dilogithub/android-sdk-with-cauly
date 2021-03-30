package kr.co.dilo.sample.app.content;

import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.co.dilo.sample.app.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyContent.DummyItem}.
 */
public class ContentRecyclerViewAdapter extends RecyclerView.Adapter<ContentRecyclerViewAdapter.ViewHolder> {

  private final List<DummyContent.DummyItem> mValues;

  public ContentRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
    mValues = items;
  }

  private OnContentSelectedListener listener;

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_home, parent, false);

    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mImageView.setImageResource(mValues.get(position).image);
    holder.mTitleView.setText(mValues.get(position).title);
    holder.mDescView.setText(mValues.get(position).desc);
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final ImageView mImageView;
    public final TextView mTitleView;
    public final TextView mDescView;
    public DummyContent.DummyItem mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int pos = getAdapterPosition();
          if (pos != RecyclerView.NO_POSITION && listener != null) {
            listener.onContentSelected(v, pos, mValues.get(pos));
          }
        }
      });
      mImageView = (ImageView) view.findViewById(R.id.content_image);
      mTitleView = (TextView) view.findViewById(R.id.content_title);
      mDescView = (TextView) view.findViewById(R.id.content_desc);
    }
  }

  public void setOnContentClickListener(OnContentSelectedListener listener) {
    this.listener = listener;
  }

  public interface OnContentSelectedListener {
    void onContentSelected(View view, int pos, DummyContent.DummyItem item);
  }
}
