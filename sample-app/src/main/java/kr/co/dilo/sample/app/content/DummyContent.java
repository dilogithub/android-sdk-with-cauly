package kr.co.dilo.sample.app.content;

import java.io.Serializable;

/**
 * 컨텐츠 리스트 아이템 (Model)
 */
public class DummyContent {

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
