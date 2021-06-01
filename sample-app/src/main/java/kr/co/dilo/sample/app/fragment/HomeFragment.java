package kr.co.dilo.sample.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import kr.co.dilo.sample.app.ContentActivity;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sample.app.banner.BannerAdapter;
import kr.co.dilo.sample.app.content.ContentRecyclerViewAdapter;
import kr.co.dilo.sample.app.content.DummyContent;

import java.util.ArrayList;
import java.util.List;

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
        Context context = v.getContext();
        RecyclerView listView = v.findViewById(R.id.list);
        RecyclerView bannerView = v.findViewById(R.id.rolling_banner);

        SwipeRefreshLayout swipeRefreshLayout = v.findViewById(R.id.home_list_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            listView.setY(-5000);
            listView.animate().y(0).setDuration(1000).start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        listView.setLayoutManager(new LinearLayoutManager(context));

        // 어댑터 설정
        List<DummyContent.DummyItem> items = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            items.add(
                    new DummyContent.DummyItem(R.drawable.content_image, "딜로", "오디오 광고는 딜로! 에피소드 " + i)
            );
        }

        ContentRecyclerViewAdapter adapter = new ContentRecyclerViewAdapter(items);
        adapter.setOnContentClickListener((pos, item) -> {
            Intent intent = new Intent(getActivity(), ContentActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .putExtra("item", item)
                    .putExtra("index", pos);
            startActivityForResult(intent, pos);
        });
        listView.setAdapter(adapter);

        bannerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        List<Integer> res = new ArrayList<>();
        res.add(R.drawable.dilo_banner1);
        res.add(R.drawable.content_image);
        res.add(R.drawable.dilo_banner2);
        res.add(R.drawable.dilo_banner3);
        res.add(R.drawable.dilo_banner4);
        res.add(R.drawable.dilo_banner5);
        BannerAdapter bannerAdapter = new BannerAdapter(res);
        bannerView.setAdapter(bannerAdapter);

        return v;
    }

}
