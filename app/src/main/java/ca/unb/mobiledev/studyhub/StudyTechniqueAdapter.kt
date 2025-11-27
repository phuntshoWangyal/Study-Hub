package ca.unb.mobiledev.studyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudyTechniqueAdapter(
    private val techniques: List<StudyTechnique>,
    private val onItemClick: (StudyTechnique) -> Unit

) : RecyclerView.Adapter<StudyTechniqueAdapter.TechniqueViewHolder>() {

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.technique_title)
        val subtitle: TextView = itemView.findViewById(R.id.technique_subtitle)
        val image: ImageView = itemView.findViewById(R.id.technique_image)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.study_technique_item, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        val technique = techniques[position]

        holder.title.text = technique.title
        holder.subtitle.text = technique.subtitle
        holder.image.setImageResource(technique.imageRes) // 👈 set image

        holder.itemView.setOnClickListener {
            onItemClick(technique)
        }
    }


    override fun getItemCount(): Int = techniques.size
}
