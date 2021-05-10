package kr.co.dilo.sample.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kr.co.dilo.sample.app.ContentActivity;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sample.app.content.ContentRecyclerViewAdapter;
import kr.co.dilo.sample.app.content.DummyContent;

/**
 * 홈 화면 (컨텐츠 리스트)
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_list, container, false);

        // 어뎁터 설정
        if (v instanceof RecyclerView) {
            Context context = v.getContext();
            RecyclerView recyclerView = (RecyclerView) v;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            ContentRecyclerViewAdapter adapter = new ContentRecyclerViewAdapter(DummyContent.ITEMS);
            adapter.setOnContentClickListener((view, pos, item) -> {
                Intent intent = new Intent(getActivity(), ContentActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .putExtra("item", item)
                        .putExtra("index", pos);
                startActivityForResult(intent, pos);
            });
            recyclerView.setAdapter(adapter);
        }
        return v;
    }

}
