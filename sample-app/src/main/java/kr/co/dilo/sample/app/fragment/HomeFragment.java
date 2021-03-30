package kr.co.dilo.sample.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kr.co.dilo.sample.app.ContentActivity;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sample.app.content.ContentRecyclerViewAdapter;
import kr.co.dilo.sample.app.content.DummyContent;

/**
 * A fragment representing a list of Items.
 */
public class HomeFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HomeFragment newInstance(int columnCount) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_list, container, false);

        // Set the adapter
        if (v instanceof RecyclerView) {
            Context context = v.getContext();
            RecyclerView recyclerView = (RecyclerView) v;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            ContentRecyclerViewAdapter adapter = new ContentRecyclerViewAdapter(DummyContent.ITEMS);
            adapter.setOnContentClickListener(new ContentRecyclerViewAdapter.OnContentSelectedListener() {
                @Override
                public void onContentSelected(View view, int pos, DummyContent.DummyItem item) {
                    Intent intent = new Intent(getActivity(), ContentActivity.class)
                        .putExtra("item", item)
                        .putExtra("index", pos);
                    startActivityForResult(intent, pos);
                }
            });
            recyclerView.setAdapter(adapter);
        }
        return v;
    }

}
