package ca.unb.mobiledev.studyhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.unb.mobiledev.studyhub.R

class rank_fragment : Fragment() {

    private lateinit var rankBadge: ImageView
    private lateinit var timeStudyToday: TextView
    private lateinit var expToday: TextView
    private lateinit var rankProgress: ProgressBar

    // Example variable: you can later replace this with real data from Firebase or app logic
    private var totalPoints: Int = 0
    private var expTodayPoints: Int = 60
    private var studyHoursToday: Int = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rankBadge = view.findViewById(R.id.rankBadge)
        timeStudyToday = view.findViewById(R.id.timeStudyToday)
        expToday = view.findViewById(R.id.expToday)
        rankProgress = view.findViewById(R.id.rankProgress)

        updateRankUI()
    }

    private fun updateRankUI() {
        // Update text values
        timeStudyToday.text = "Time Study Today: ${studyHoursToday}h"
        expToday.text = "Exp Today: $expTodayPoints"

        // Update total points (for now simulate adding exp)
        totalPoints += expTodayPoints
        if (totalPoints > 225) totalPoints = 225

        // Update badge drawable based on points
        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankBadge.setImageLevel(totalPoints)

        // Progress bar: show percentage toward next level
        val nextLevelMax = when {
            totalPoints < 30 -> 30
            totalPoints < 75 -> 75
            totalPoints < 135 -> 135
            totalPoints < 225 -> 225
            else -> 225
        }

        val prevLevelMin = when {
            totalPoints < 30 -> 0
            totalPoints < 75 -> 30
            totalPoints < 135 -> 75
            totalPoints < 225 -> 135
            else -> 225
        }

        val levelProgress = ((totalPoints - prevLevelMin).toFloat() / (nextLevelMax - prevLevelMin) * 100).toInt()
        rankProgress.progress = levelProgress
    }
}
