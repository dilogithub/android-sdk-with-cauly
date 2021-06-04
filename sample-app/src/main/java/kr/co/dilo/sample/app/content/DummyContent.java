package kr.co.dilo.sample.app.content;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 컨텐츠 리스트 아이템 (Model)
 */
public class DummyContent {

  public static class DummyItem implements Parcelable {
    public final int image;
    public final String title;
    public final String desc;

    public DummyItem(int image, String title, String desc) {
      this.image = image;
      this.title = title;
      this.desc = desc;
    }

    protected DummyItem(Parcel in) {
      image = in.readInt();
      title = in.readString();
      desc = in.readString();
    }

    public static final Creator<DummyItem> CREATOR = new Creator<DummyItem>() {
      @Override
      public DummyItem createFromParcel(Parcel in) {
        return new DummyItem(in);
      }

      @Override
      public DummyItem[] newArray(int size) {
        return new DummyItem[size];
      }
    };

    @Override
    public String toString() {
      return title;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(image);
      dest.writeString(title);
      dest.writeString(desc);
    }
  }
}
