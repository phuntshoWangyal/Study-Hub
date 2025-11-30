package ca.unb.mobiledev.studyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestScoresAdapter(
    private val items: MutableList<TestItem>,
    private val onTestClick: (TestItem) -> Unit,
    private val onEditClick: (TestItem) -> Unit,
    private val onDeleteClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestScoresAdapter.TestViewHolder>() {

    inner class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.testNameText)
        val gradeText: TextView = itemView.findViewById(R.id.testGradeText)
        val moreButton: ImageView = itemView.findViewById(R.id.testMoreButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test, parent, false)
        return TestViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val item = items[position]

        holder.nameText.text = item.name

        val gradeDisplay = if (item.grade == null) "--" else item.grade.toString()
        holder.gradeText.text = gradeDisplay

        holder.itemView.setOnClickListener {
            onTestClick(item)
        }

        // Menu
        holder.moreButton.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v)
            popup.menuInflater.inflate(R.menu.menu_test_item, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_test -> { onEditClick(item); true }
                    R.id.action_delete_test -> { onDeleteClick(item); true }
                    else -> false
                }
            }
            popup.show()
        }
    }



    fun setData(newItems: List<TestItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
