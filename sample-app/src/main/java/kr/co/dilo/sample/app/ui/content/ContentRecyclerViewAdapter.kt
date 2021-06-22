package kr.co.dilo.sample.app.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.dilo.sample.app.databinding.FragmentHomeBinding

/**
 * 컨텐츠 View Adapter
 */
class ContentRecyclerViewAdapter(private val items: List<DummyContent.DummyItem>) :
    RecyclerView.Adapter<ContentRecyclerViewAdapter.ViewHolder>() {
    private var listener: OnContentSelectedListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: DummyContent.DummyItem = items[position]
        holder.item = item
        holder.image.setImageResource(item.image)
        holder.title.text = item.title
        holder.desc.text = item.desc
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * 컨텐츠 리스트 아이템 (View)
     */
    inner class ViewHolder(view: FragmentHomeBinding) : RecyclerView.ViewHolder(view.root) {
        val image: ImageView
        val title: TextView
        val desc: TextView
        var item: DummyContent.DummyItem? = null

        init {
            // 뷰홀더 자체 클릭 시
            view.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener!!.onContentSelected(pos, items[pos])
                }
            }

            image = view.contentImage
            title = view.contentTitle
            desc = view.contentDesc
        }
    }

    fun setOnContentClickListener(listener: (pos: Int, item: DummyContent.DummyItem?) -> Unit) {
        this.listener = object: OnContentSelectedListener {
            override fun onContentSelected(pos: Int, item: DummyContent.DummyItem?) {
                listener(pos, item)
            }
        }
    }

    interface OnContentSelectedListener {
        fun onContentSelected(pos: Int, item: DummyContent.DummyItem?)
    }
}
