package kr.co.dilo.sample.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import kr.co.dilo.sample.app.databinding.FragmentLogBinding;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;

/**
 * 로그 보여주는 화면
 */
public class LogFragment extends Fragment {

  /**
   * 로그 초기화 메시지
   */
  public static final String CLEAR_LOG = "jmi;oaFwe1cft($*#&E1FLS3D2OIV=+'vm,.x!@8907";

  TextView log;
  ScrollView scrollView;
  private FragmentLogBinding viewBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    viewBinding = FragmentLogBinding.inflate(inflater, container, false);
    return viewBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    log = viewBinding.logArea;
    scrollView = viewBinding.scrollView;

    log.setOnLongClickListener(v -> {
      new AlertDialog.Builder(getActivity())
              .setTitle("")
              .setMessage("로그를 삭제하시겠습니까?")
              .setPositiveButton("삭제", (dialog, which) -> log.setText(""))
              .setNegativeButton("취소", null)
              .show();
      return false;
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    viewBinding = null;
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  public void log(String message) {
    if (log != null) {
      if (message.equals(CLEAR_LOG)) {
        log.setText("");
        Log.d(DiloSampleAppUtil.LOG_TAG, "로그 초기화");
        return;
      }

      log.append("# " + message + "\n");
      scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
      Log.d(DiloSampleAppUtil.LOG_TAG, message);
    }
  }
}
