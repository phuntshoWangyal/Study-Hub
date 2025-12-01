package ca.unb.mobiledev.studyhub

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ca.unb.mobiledev.studyhub.FirebaseService.getCourseList
import ca.unb.mobiledev.studyhub.FirebaseService.getWeeklyTime
import ca.unb.mobiledev.studyhub.FirebaseService.getTests
import ca.unb.mobiledev.studyhub.FirebaseService.getTestTopics
import ca.unb.mobiledev.studyhub.FirebaseService.getCourseTimeByTechnique
import ca.unb.mobiledev.studyhub.FirebaseService.getTotalTime

class rank_fragment : Fragment() {

    private lateinit var rankBadge: ImageView
    private lateinit var expTotal: TextView
    private lateinit var rankProgress: ProgressBar

    // Charts
    private lateinit var weeklyBarChart: BarChart
    private lateinit var testChart: ScatterChart
    private var displayedWeek = FirebaseService.getCurrentWeek().toInt()
    private var displayedYear = FirebaseService.getCurrentYear().toInt()

    private lateinit var weekRangeText: TextView
    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView

    private lateinit var courseTitle: TextView
    private lateinit var techniqueTitle: TextView
    private lateinit var dropCourse: ImageView
    private lateinit var dropTechnique: ImageView
    private var courseListMemory = listOf<String>()
    private var techniqueListMemory = listOf(
        "Freestyle",
        "Pomodoro",
        "90-minute blocks"
    )
    private var selectedCourse: String? = null
    private var selectedTechnique: Int = 0




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
        weekRangeText = view.findViewById(R.id.weeklyDateRange)
        arrowLeft = view.findViewById(R.id.arrowLeft)
        arrowRight = view.findViewById(R.id.arrowRight)
        courseTitle = view.findViewById(R.id.courseStatTitle)
        techniqueTitle = view.findViewById(R.id.techniqueSelector)
        dropCourse = view.findViewById(R.id.dropDownArrow)
        dropTechnique = view.findViewById(R.id.dropDownTechnique)


        // Show placeholder UI until Firebase loads
        expTotal.text = "Exp: 0"
        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankProgress.progress = 0

        // Load EXP
        loadExperience()

        // Load charts
        loadWeeklyChart()

        // Load Test Chart ONLY after courses load
        getCourseList { list ->
            courseListMemory = list
            if (list.isNotEmpty()) {

                selectedCourse = list[0]
                selectedTechnique = 0

                courseTitle.text = selectedCourse
                techniqueTitle.text = techniqueListMemory[selectedTechnique]

                loadTestChart(selectedCourse, selectedTechnique)
            }
        }


        arrowLeft.setOnClickListener {
            displayedWeek--

            // Handle when week < 1 (previous year)
            if (displayedWeek < 1) {
                displayedYear--
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.YEAR, displayedYear)
                displayedWeek = cal.getActualMaximum(java.util.Calendar.WEEK_OF_YEAR)
            }

            refreshWeeklyChart()
        }

        arrowRight.setOnClickListener {
            displayedWeek++

            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.YEAR, displayedYear)

            if (displayedWeek > cal.getActualMaximum(java.util.Calendar.WEEK_OF_YEAR)) {
                displayedWeek = 1
                displayedYear++
            }

            refreshWeeklyChart()
        }
        dropCourse.setOnClickListener {
            showDropDown(dropCourse, courseListMemory) { selected ->
                selectedCourse = selected
                courseTitle.text = selected

                loadTestChart(selectedCourse, selectedTechnique)
            }
        }



        dropTechnique.setOnClickListener {

            showDropDown(dropTechnique, techniqueListMemory) { selected ->
                selectedTechnique = techniqueListMemory.indexOf(selected)
                techniqueTitle.text = selected

                loadTestChart(selectedCourse, selectedTechnique)
            }
        }




    }

    private fun showDropDown(
        anchor: View,
        items: List<String>,
        onSelect: (String) -> Unit
    ) {
        val context = requireContext()

        // Build the list view
        val listView = ListView(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1,
                items
            )
            dividerHeight = 1
        }

        // PopupWindow with WRAP_CONTENT
        val popupWindow = PopupWindow(
            listView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        listView.setOnItemClickListener { _, _, position, _ ->
            onSelect(items[position])
            popupWindow.dismiss()
        }

        popupWindow.elevation = 20f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.isOutsideTouchable = true

        // Show dropdown
        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    private fun refreshWeeklyChart() {
        weekRangeText.text = getWeekRangeString(displayedWeek, displayedYear)
        loadWeeklyChart()
    }

    private fun loadExperience() {
        getTotalTime { totalStudyDouble ->
            val totalStudyHours = totalStudyDouble
            val exp = (totalStudyHours * 100).toInt()
            activity?.runOnUiThread {
                expTotal.text = "Exp: $exp"
                totalPoints = exp.coerceAtMost(3000)
                applyRankProgress(totalPoints)
            }
        }
    }

    private fun applyRankProgress(points: Int) {
        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankBadge.setImageLevel(points)
        val (prevLevelMin, nextLevelMax) = when {
            points < 500 -> 0 to 500
            points < 1000 -> 500 to 1000
            points < 1750 -> 1000 to 1750
            points < 3000 -> 1750 to 3000
            else -> 3000 to 3000
        }

        val levelProgress =
            if (nextLevelMax > prevLevelMin)
                ((points - prevLevelMin).toFloat() /
                        (nextLevelMax - prevLevelMin) * 100).toInt()
            else 100

        rankProgress.progress = levelProgress.coerceIn(0, 100)
    }



    private fun getWeekRangeString(week: Int, year: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.clear()
        cal.set(java.util.Calendar.WEEK_OF_YEAR, week)
        cal.set(java.util.Calendar.YEAR, year)

        // Start = Sunday
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = cal.time

        // End = Saturday
        cal.add(java.util.Calendar.DAY_OF_WEEK, 6)
        val end = cal.time

        val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
        return "${sdf.format(start)} - ${sdf.format(end)}"
    }

    private fun loadWeeklyChart() {
        getCourseList { courseList ->
            courseListMemory = courseList
            if (courseList.isEmpty()) {
                drawWeeklyChart(emptyList(), emptyList())
                return@getCourseList
            }

            val year = displayedYear.toString()
            val week = displayedWeek.toString()

            weekRangeText.text = getWeekRangeString(displayedWeek, displayedYear)


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

        for (day in 0 until 7) {
            val stackValues = FloatArray(courseNames.size)

            for (courseIndex in courseNames.indices) {
                stackValues[courseIndex] = weeklyData[courseIndex][day].toFloat()
            }

            barEntries.add(BarEntry(day.toFloat(), stackValues))
        }

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



    private fun loadTestChart(selectedCourse: String?, technique: Int) {
        val course = selectedCourse ?: return

        // Clear old data
        drawTestScatter(emptyList(), emptyList(), emptyList())

        getTests(course) { tests ->
            if (tests.isEmpty()) {
                drawTestScatter(emptyList(), emptyList(), emptyList())
                return@getTests
            }

            // testNames is the label list in fixed order
            val testNames = tests.toList()

            // Pre-allocate arrays so each index = one test
            val studyTotalsHours = MutableList(tests.size) { 0f } // X values (hours)
            val grades = MutableList(tests.size) { 0f }           // Y values (0–100)

            var testsCompleted = 0

            tests.forEachIndexed { testIndex, testName ->

                getTestTopics(course, testName) { topics ->

                    // If this test has no topics → 0 hours, but still show grade
                    if (topics.isEmpty()) {
                        FirebaseService.getGrade(course, testName) { grade ->
                            grades[testIndex] = grade.toFloat()
                            testsCompleted++

                            if (testsCompleted == tests.size) {
                                drawTestScatter(testNames, studyTotalsHours, grades)
                            }
                        }
                        return@getTestTopics
                    }

                    var testStudyTotalHours = 0.0
                    var topicsLoaded = 0

                    topics.forEach { topicName ->
                        getCourseTimeByTechnique(course, topicName, technique) { timeHours ->
                            // timeHours is already "hours" for this topic+technique
                            testStudyTotalHours += timeHours
                            topicsLoaded++

                            // Once we've got all topics for this test, fetch the grade
                            if (topicsLoaded == topics.size) {
                                FirebaseService.getGrade(course, testName) { grade ->
                                    studyTotalsHours[testIndex] = testStudyTotalHours.toFloat()
                                    grades[testIndex] = grade.toFloat()
                                    testsCompleted++

                                    android.util.Log.d(
                                        "RankFragment",
                                        "FINISHED test=$testName → totalHours=${testStudyTotalHours.toFloat()}, grade=${grade.toFloat()}"
                                    )
                                    // When ALL tests are done, draw the scatter once
                                    if (testsCompleted == tests.size) {
                                        android.util.Log.d(
                                            "RankFragment",
                                            "Final studyTotalsHours=$studyTotalsHours, grades=$grades"
                                        )
                                        drawTestScatter(testNames, studyTotalsHours, grades)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun drawTestScatter(
        testNames: List<String>,
        studyHoursRaw: List<Float>,
        grades: List<Float>
    ) {
        if (testNames.isEmpty()) {
            testChart.clear()
            testChart.invalidate()
            return
        }

        // --- Convert hours → seconds so tiny values appear ---
        val xValues = studyHoursRaw.map { it * 3600f }  // convert to seconds
        val maxX = (xValues.maxOrNull() ?: 0f).coerceAtLeast(1f)

        val entries = ArrayList<Entry>()
        for (i in testNames.indices) {
            val e = Entry(xValues[i], grades[i])
            e.data = testNames[i]
            entries.add(e)
        }

        val scatterSet = ScatterDataSet(entries, "Study time vs grade").apply {
            color = Color.parseColor("#4285F4")
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 12f

            setDrawValues(true)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(e: Entry?): String {
                    return (e?.data as? String) ?: ""
                }
            }
        }

        testChart.apply {
            data = ScatterData(scatterSet)
            description.isEnabled = false
            legend.isEnabled = false   // Cleaner look

            // --- Y Axis (Grade) ---
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false

            // --- X Axis (Seconds studied) ---
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                axisMinimum = 0f
                axisMaximum = maxX * 1.3f   // prevent squishing
                granularity = maxX / 5f
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}s"
                    }
                }
            }

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)

            invalidate()
        }
    }


}


