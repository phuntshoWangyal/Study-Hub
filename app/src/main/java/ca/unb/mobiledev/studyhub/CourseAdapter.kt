package ca.unb.mobiledev.studyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(private val courseList: List<Course>, private val onItemClick: (Course) -> Unit   ) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseCode: TextView = itemView.findViewById(R.id.courseCode)
        val courseName: TextView = itemView.findViewById(R.id.courseName)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_item, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.courseCode.text = course.courseCode
        holder.courseName.text = course.courseName

        holder.itemView.setOnClickListener {
            onItemClick(course)
        }
    }


    override fun getItemCount(): Int = courseList.size
}
