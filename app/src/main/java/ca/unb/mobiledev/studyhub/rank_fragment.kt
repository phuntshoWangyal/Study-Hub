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
    private lateinit var testChart: CombinedChart

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

        expTotal.text = "Exp: 0"
        rankBadge.setImageResource(R.drawable.ic_rank_badge)
        rankProgress.progress = 0

        loadExperience()

        loadWeeklyChart()
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

        val listView = ListView(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1,
                items
            )
            dividerHeight = 1
        }

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

        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = cal.time
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

        val colors = listOf(
            Color.parseColor("#D9534F"),
            Color.parseColor("#5BC0DE"),
            Color.parseColor("#5CB85C"),
            Color.parseColor("#F0AD4E"),
            Color.parseColor("#4285F4"),
            Color.parseColor("#9B59B6"),
            Color.parseColor("#FF66CC"),
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
        drawTestScatter(emptyList(), emptyList(), emptyList())

        getTests(course) { tests ->
            if (tests.isEmpty()) {
                drawTestScatter(emptyList(), emptyList(), emptyList())
                return@getTests
            }
            val testNames = tests.toList()
            val studyTotalsHours = MutableList(tests.size) { 0f }
            val grades = MutableList(tests.size) { 0f }

            var testsCompleted = 0

            tests.forEachIndexed { testIndex, testName ->

                getTestTopics(course, testName) { topics ->
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
                            testStudyTotalHours += timeHours
                            topicsLoaded++
                            if (topicsLoaded == topics.size) {
                                FirebaseService.getGrade(course, testName) { grade ->
                                    studyTotalsHours[testIndex] = testStudyTotalHours.toFloat()
                                    grades[testIndex] = grade.toFloat()
                                    testsCompleted++

                                    android.util.Log.d(
                                        "RankFragment",
                                        "FINISHED test=$testName → totalHours=${testStudyTotalHours.toFloat()}, grade=${grade.toFloat()}"
                                    )
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

    private fun calculateLinearRegression(x: List<Float>, y: List<Float>): Pair<List<Entry>, Double> {
        val n = x.size
        if (n == 0) return Pair(emptyList(), 0.0)

        val sumX = x.sumOf { it.toDouble() }
        val sumY = y.sumOf { it.toDouble() }
        val sumXY = x.zip(y).sumOf { (xx, yy) -> (xx * yy).toDouble() }
        val sumX2 = x.sumOf { (it * it).toDouble() }

        val slope = ((n * sumXY) - (sumX * sumY)) / ((n * sumX2) - (sumX * sumX))
        val intercept = (sumY - slope * sumX) / n

        val meanY = y.average().toFloat()
        val ssTot = y.sumOf { ((it - meanY) * (it - meanY)).toDouble() }
        val ssRes = x.zip(y).sumOf { (xx, yy) ->
            val pred = slope * xx + intercept
            ((yy - pred) * (yy - pred))
        }

        val r2 = if (ssTot == 0.0) 1.0 else 1 - (ssRes / ssTot)

        val minX = x.minOrNull() ?: 0f
        val maxX = x.maxOrNull() ?: 1f

        val trendEntries = listOf(
            Entry(minX, (slope * minX + intercept).toFloat()),
            Entry(maxX, (slope * maxX + intercept).toFloat())
        )

        return Pair(trendEntries, r2)
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

        val xValues = studyHoursRaw.map { it * 3600f }
        val maxX = (xValues.maxOrNull() ?: 1f)

        val scatterEntries = testNames.indices.map { i ->
            Entry(xValues[i], grades[i]).apply { data = testNames[i] }
        }

        val (trendEntries, r2) = calculateLinearRegression(xValues, grades)

        val scatterSet = ScatterDataSet(scatterEntries, "Study vs Grade").apply {
            color = Color.parseColor("#4285F4")
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 12f
            setDrawValues(true)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(e: Entry?): String =
                    e?.data?.toString() ?: ""
            }
        }

        val trendSet = LineDataSet(trendEntries, "Trend line").apply {
            color = Color.RED
            lineWidth = 2.5f
            setDrawCircles(false)
            setDrawValues(false)
        }

        val combined = CombinedData()
        combined.setData(ScatterData(scatterSet))
        combined.setData(LineData(trendSet))

        testChart.data = combined

        testChart.apply {
            description.isEnabled = true
            description.text = "R² = ${"%.3f".format(r2)}"
            description.textColor = Color.BLACK

            legend.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                axisMinimum = 0f
                axisMaximum = maxX * 1.3f
                granularity = maxX / 5f
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String =
                        "${value.toInt()}s"
                }
            }

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            invalidate()
        }
    }
}