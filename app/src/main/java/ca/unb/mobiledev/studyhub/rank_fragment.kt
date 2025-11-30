package ca.unb.mobiledev.studyhub

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ca.unb.mobiledev.studyhub.FirebaseService.getCourseList
import ca.unb.mobiledev.studyhub.FirebaseService.getWeeklyTime
import ca.unb.mobiledev.studyhub.FirebaseService.getTests
import ca.unb.mobiledev.studyhub.FirebaseService.getTestTopics
import ca.unb.mobiledev.studyhub.FirebaseService.getCourseTimeByTechnique
import ca.unb.mobiledev.studyhub.FirebaseService.getTotalTime
import ca.unb.mobiledev.studyhub.FirebaseService.firestore

class rank_fragment : Fragment() {

    // ------------------ UI ------------------
    private lateinit var rankBadge: ImageView
    private lateinit var expTotal: TextView
    private lateinit var rankProgress: ProgressBar

    // Charts
    private lateinit var weeklyBarChart: BarChart
    private lateinit var testChart: CombinedChart

    // Data
    private var totalPoints: Int = 0
    private val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ----- Bind UI -----
        rankBadge = view.findViewById(R.id.rankBadge)
        expTotal = view.findViewById(R.id.expTotal)
        rankProgress = view.findViewById(R.id.rankProgress)
        weeklyBarChart = view.findViewById(R.id.weeklyBarChart)
        testChart = view.findViewById(R.id.courseBarChart)

        // Show placeholder UI until Firebase loads
        expTotal.text = "Exp: 0"
        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankProgress.progress = 0

        // Load EXP
        loadExperience()

        // Load charts
        loadWeeklyChart()
        loadTestChart()
    }

    // -------------------------------------------------------------
    //                  EXPERIENCE + RANKING LOGIC
    // -------------------------------------------------------------
    private fun loadExperience() {
        getTotalTime { totalStudyDouble ->

            // Convert to hours if needed — assuming already hours
            val totalStudyHours = totalStudyDouble

            // Scale EXP = 3x total study time
            val exp = (totalStudyHours * 3).toInt()

            activity?.runOnUiThread {
                expTotal.text = "Exp: $exp"

                totalPoints = exp.coerceAtMost(225)
                applyRankProgress(totalPoints)
            }
        }
    }

    private fun applyRankProgress(points: Int) {

        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankBadge.setImageLevel(points)

        val nextLevelMax = when {
            points < 30 -> 30
            points < 75 -> 75
            points < 135 -> 135
            points < 225 -> 225
            else -> 225
        }

        val prevLevelMin = when {
            points < 30 -> 0
            points < 75 -> 30
            points < 135 -> 75
            points < 225 -> 135
            else -> 225
        }

        val levelProgress =
            if (nextLevelMax - prevLevelMin > 0)
                ((points - prevLevelMin).toFloat() /
                        (nextLevelMax - prevLevelMin) * 100).toInt()
            else 0

        rankProgress.progress = levelProgress.coerceIn(0, 100)
    }

    // -------------------------------------------------------------
    //                  WEEKLY BAR CHART
    // -------------------------------------------------------------
    private fun loadWeeklyChart() {
        getCourseList { courseList ->

            if (courseList.isEmpty()) {
                drawWeeklyChart(emptyList(), emptyList())
                return@getCourseList
            }

            val year = FirebaseService.getCurrentYear()
            val week = FirebaseService.getCurrentWeek()

            val courseNames = courseList.toMutableList()
            val weeklyData = Array(courseList.size) { MutableList(7) { 0.0 } }

            var loadedCourses = 0

            courseList.forEachIndexed { courseIndex, courseName ->

                getWeeklyTime(courseName, year, week) { weekList ->

                    for (i in 0 until 7) {
                        weeklyData[courseIndex][i] = weekList.getOrNull(i) ?: 0.0
                    }

                    loadedCourses++
                    if (loadedCourses == courseList.size) {
                        drawWeeklyChart(courseNames, weeklyData.toList())
                    }
                }
            }
        }
    }


    private fun drawWeeklyChart(courseNames: List<String>, weeklyData: List<List<Double>>) {

        if (courseNames.isEmpty()) {
            weeklyBarChart.clear()
            return
        }

        val barEntries = ArrayList<BarEntry>()

        // Build stack values for each day
        for (day in 0 until 7) {
            val stackValues = FloatArray(courseNames.size)

            for (courseIndex in courseNames.indices) {
                stackValues[courseIndex] = weeklyData[courseIndex][day].toFloat()
            }

            barEntries.add(BarEntry(day.toFloat(), stackValues))
        }

        // DataSet
        val barSet = BarDataSet(barEntries, "Weekly Study by Course")
        barSet.setDrawValues(false)

        // Create a color per course
        val colors = listOf(
            Color.parseColor("#D9534F"), // red
            Color.parseColor("#5BC0DE"), // cyan
            Color.parseColor("#5CB85C"), // green
            Color.parseColor("#F0AD4E"), // orange
            Color.parseColor("#4285F4"), // blue
            Color.parseColor("#9B59B6"), // purple
            Color.parseColor("#FF66CC"), // pink
        )

        val appliedColors = mutableListOf<Int>()
        for (i in courseNames.indices) {
            appliedColors.add(colors[i % colors.size])
        }
        barSet.colors = appliedColors

        barSet.stackLabels = courseNames.toTypedArray()

        val barData = BarData(barSet)
        barData.barWidth = 0.6f

        weeklyBarChart.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = true

            axisRight.isEnabled = false

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(days)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            setFitBars(true)
            invalidate()
        }
    }


    // -------------------------------------------------------------
    //                  TEST CHART (Bar = Study, Line = Grade)
    // -------------------------------------------------------------
    private fun loadTestChart() {

        getCourseList { courses ->
            if (courses.isEmpty()) {
                drawTestChart(emptyList(), emptyList(), emptyList())
                return@getCourseList
            }

            val firstCourse = courses[0]

            getTests(firstCourse) { tests ->
                if (tests.isEmpty()) {
                    drawTestChart(emptyList(), emptyList(), emptyList())
                    return@getTests
                }

                val testNames = ArrayList<String>()
                val studyTotals = ArrayList<Float>()
                val grades = ArrayList<Float>()

                var testsLoaded = 0   // Count how many tests finished loading

                for (testName in tests) {

                    testNames.add(testName)

                    getTestTopics(firstCourse, testName) { topics ->

                        if (topics.isEmpty()) {
                            // No topics → still must push default values
                            studyTotals.add(0f)
                            grades.add(0f)
                            testsLoaded++

                            if (testsLoaded == tests.size)
                                drawTestChart(testNames, studyTotals, grades)

                            return@getTestTopics
                        }

                        var totalStudy = 0.0
                        var topicsLoaded = 0

                        for (topic in topics) {

                            getCourseTimeByTechnique(firstCourse, topic, 1) { time ->

                                totalStudy += time
                                topicsLoaded++

                                if (topicsLoaded == topics.size) {

                                    // All topics loaded: now fetch grade
                                    firestore.collection("Grades")
                                        .document(testName)
                                        .get()
                                        .addOnSuccessListener { doc ->

                                            val grade = (doc.getDouble("grade") ?: 0.0)

                                            studyTotals.add(totalStudy.toFloat())
                                            grades.add(grade.toFloat())
                                            testsLoaded++

                                            if (testsLoaded == tests.size)
                                                drawTestChart(testNames, studyTotals, grades)
                                        }
                                        .addOnFailureListener {

                                            studyTotals.add(totalStudy.toFloat())
                                            grades.add(0f)
                                            testsLoaded++

                                            if (testsLoaded == tests.size)
                                                drawTestChart(testNames, studyTotals, grades)
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun drawTestChart(
        testNames: List<String>,
        studyTotals: List<Float>,
        grades: List<Float>
    ) {

        val barEntries = ArrayList<BarEntry>()
        val lineEntries = ArrayList<Entry>()

        for (i in studyTotals.indices)
            barEntries.add(BarEntry(i.toFloat(), studyTotals[i]))

        for (i in grades.indices)
            lineEntries.add(Entry(i.toFloat(), grades[i]))

        val barSet = BarDataSet(barEntries, "Study (hrs)").apply {
            color = Color.parseColor("#D9534F")
            valueTextColor = Color.DKGRAY
            valueTextSize = 10f
        }

        val lineSet = LineDataSet(lineEntries, "Grade").apply {
            color = Color.BLUE
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            valueTextSize = 10f
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT
        }

        val combined = CombinedData().apply {
            setData(BarData(barSet))
            setData(LineData(lineSet))
        }

        testChart.apply {
            data = combined
            description.isEnabled = false
            axisRight.isEnabled = false

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(testNames)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            invalidate()
        }
    }
}


