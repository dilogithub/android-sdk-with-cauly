package kr.co.dilo.sample.app.ui.content

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * 컨텐츠 리스트 아이템 (Model)
 */
class DummyContent {
    class DummyItem : Parcelable {
        val image: Int
        val title: String
        val desc: String?

        constructor(image: Int, title: String, desc: String?) {
            this.image = image
            this.title = title
            this.desc = desc
        }

        internal constructor(`in`: Parcel) {
            image = `in`.readInt()
            title = `in`.readString()!!
            desc = `in`.readString()
        }

        override fun toString(): String {
            return title
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(image)
            dest.writeString(title)
            dest.writeString(desc)
        }

        companion object CREATOR : Creator<DummyItem> {
            override fun createFromParcel(parcel: Parcel): DummyItem {
                return DummyItem(parcel)
            }

            override fun newArray(size: Int): Array<DummyItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
