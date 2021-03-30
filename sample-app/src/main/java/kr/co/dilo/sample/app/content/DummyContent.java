package kr.co.dilo.sample.app.content;

import kr.co.dilo.sample.app.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

  /**
   * An array of sample (dummy) items.
   */
  public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

  private static final int COUNT = 15;

  static {
    // Add some sample items.
    for (int i = 1; i <= COUNT; i++) {
      addItem(createDummyItem(i));
    }
  }

  private static void addItem(DummyItem item) {
    ITEMS.add(item);
  }

  private static DummyItem createDummyItem(int position) {
    return new DummyItem(R.drawable.content_image, "딜로", "오디오 광고는 딜로! 에피소드 "+ position);
  }

  /**
   * A dummy item representing a piece of content.
   */
  public static class DummyItem implements Serializable {
    public final int image;
    public final String title;
    public final String desc;

    public DummyItem(int image, String title, String desc) {
      this.image = image;
      this.title = title;
      this.desc = desc;
    }

    @Override
    public String toString() {
      return title;
    }
  }
}
