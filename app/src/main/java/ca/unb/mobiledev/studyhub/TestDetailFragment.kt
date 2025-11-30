package ca.unb.mobiledev.studyhub

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class TestDetailFragment : Fragment() {

    private lateinit var courseCode: String
    private lateinit var testName: String

    private lateinit var courseTitleText: TextView
    private lateinit var testNameText: TextView
    private lateinit var gradeText: TextView
    private lateinit var topicsContainer: LinearLayout
    private lateinit var editTestButton: LinearLayout
    private lateinit var deleteTestButton: ImageButton
    private var editDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ use the SAME keys as in companion object
        val args = requireArguments()
        courseCode = args.getString(ARG_COURSE_CODE)
            ?: error("Missing ARG_COURSE_CODE in TestDetailFragment arguments")
        testName = args.getString(ARG_TEST_NAME)
            ?: error("Missing ARG_TEST_NAME in TestDetailFragment arguments")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseTitleText = view.findViewById(R.id.courseTitleText)
        testNameText    = view.findViewById(R.id.testNameText)
        gradeText       = view.findViewById(R.id.testGradeText)
        topicsContainer = view.findViewById(R.id.topicsContainer)
        editTestButton  = view.findViewById(R.id.btnEditTest)
        deleteTestButton = view.findViewById(R.id.btnDeleteTest)

        // Show test name inside the card
        testNameText.text = testName

        loadCourseName()
        loadGrade()
        loadTopics()

        editTestButton.setOnClickListener {
            showEditTestDialog()
        }

        deleteTestButton.setOnClickListener {
            confirmDeleteTest()
        }
    }

    // ✅ no Firebase call, no crash
    private fun loadCourseName() {
        courseTitleText.text = courseCode
    }

    private fun loadGrade() {
        FirebaseService.getGrade(courseCode, testName) { grade ->
            val text = if (grade == 0.0) {
                "Grade: not entered yet"
            } else {
                "Grade: $grade"
            }
            gradeText.text = text
        }
    }

    private fun loadTopics() {
        val uid = FirebaseService.auth.currentUser?.uid ?: return

        val ref = FirebaseService.realtimeDb.getReference(
            "users/$uid/Courses/$courseCode/Tests/$testName/Topics"
        )

        ref.get().addOnSuccessListener { snapshot ->
            topicsContainer.removeAllViews()

            val topics = snapshot.children.mapNotNull { it.key }

            if (topics.isEmpty()) {
                val tv = TextView(requireContext()).apply {
                    text = "No topics added yet"
                    setTextColor(Color.DKGRAY)
                }
                topicsContainer.addView(tv)
                return@addOnSuccessListener
            }

            for (topic in topics) {
                val chip = TextView(requireContext()).apply {
                    text = topic
                    setPadding(24, 12, 24, 12)
                    setTextColor(Color.BLACK)
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD

                    background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.topic_chip_background
                    )

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 12)
                    layoutParams = params
                }
                topicsContainer.addView(chip)
            }
        }
    }


    companion object {
        private const val ARG_COURSE_CODE = "course_code"
        private const val ARG_TEST_NAME = "test_name"

        fun newInstance(courseCode: String, testName: String): TestDetailFragment {
            val f = TestDetailFragment()
            f.arguments = Bundle().apply {
                putString(ARG_COURSE_CODE, courseCode)
                putString(ARG_TEST_NAME, testName)
            }
            return f
        }
    }

    private fun showEditTestDialog() {
        FirebaseService.getTopics(courseCode) { allTopics ->

            val uid = FirebaseService.auth.currentUser?.uid ?: return@getTopics
            val topicsRef = FirebaseService.realtimeDb.getReference(
                "users/$uid/Courses/$courseCode/Tests/$testName/Topics"
            )

            topicsRef.get().addOnSuccessListener { snapshot ->
                val selectedTopics = snapshot.children.mapNotNull { it.key }.toSet()

                FirebaseService.getGrade(courseCode, testName) { currentGrade ->

                    if (!isAdded) return@getGrade

                    val dialogView = layoutInflater.inflate(R.layout.dialog_edit_test, null)
                    val nameEdit = dialogView.findViewById<EditText>(R.id.editTestNameEditText)
                    val gradeEdit = dialogView.findViewById<EditText>(R.id.editGradeEditText)
                    val topicsCheckboxContainer =
                        dialogView.findViewById<LinearLayout>(R.id.topicsCheckboxContainer)

                    nameEdit.setText(testName)
                    if (currentGrade != 0.0) {
                        gradeEdit.setText(currentGrade.toString())
                    }

                    val checkBoxes = mutableListOf<CheckBox>()
                    for (topic in allTopics) {
                        val cb = CheckBox(requireContext()).apply {
                            text = topic
                            isChecked = selectedTopics.contains(topic)
                        }
                        topicsCheckboxContainer.addView(cb)
                        checkBoxes.add(cb)
                    }

                    val activity = activity
                    if (!isAdded || activity == null || activity.isFinishing || activity.isDestroyed) {
                        return@getGrade
                    }

                    val dialog = AlertDialog.Builder(activity)
                        .setTitle("Edit Test")
                        .setView(dialogView)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Save", null)
                        .create()

                    dialog.setOnShowListener {
                        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        saveButton.setOnClickListener {
                            val newName = nameEdit.text.toString().trim()
                            val gradeStr = gradeEdit.text.toString().trim()
                            val newGrade = gradeStr.toDoubleOrNull() ?: 0.0

                            if (newName.isEmpty()) {
                                nameEdit.error = "Name required"
                                return@setOnClickListener
                            }

                            // collect checked topics
                            val chosenTopics = checkBoxes
                                .filter { it.isChecked }
                                .map { it.text.toString() }

                            var finalTestName = testName

                            // rename test if needed
                            if (newName != testName) {
                                FirebaseService.updateTest(courseCode, newName, testName)
                                finalTestName = newName
                                testName = newName
                                testNameText.text = newName
                            }

                            // update grade
                            FirebaseService.setGrade(courseCode, finalTestName, newGrade)
                            gradeText.text =
                                if (newGrade == 0.0) "Grade: not entered yet"
                                else "Grade: $newGrade"

                            // 5) REPLACE topics: clear node then re-add only checked ones
                            val topicsRefForSave = FirebaseService.realtimeDb.getReference(
                                "users/$uid/Courses/$courseCode/Tests/$finalTestName/Topics"
                            )

                            topicsRefForSave.removeValue().addOnSuccessListener {
                                chosenTopics.forEach { topic ->
                                    FirebaseService.addTopic(courseCode, finalTestName, topic)
                                }
                                // refresh chips on main screen
                                loadTopics()
                            }

                            dialog.dismiss()
                        }
                    }
                    editDialog = dialog
                    dialog.show()
                }
            }
        }
    }


    private fun confirmDeleteTest() {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete test")
            .setMessage("Are you sure you want to delete \"$testName\"? This can’t be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                FirebaseService.deleteTest(courseCode, testName)
                Toast.makeText(requireContext(), "Test deleted", Toast.LENGTH_SHORT).show()

                (activity as? MainPage)?.openTestScores(courseCode)
            }
            .show()
    }

}
