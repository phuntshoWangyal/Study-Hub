package ca.unb.mobiledev.studyhub

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TestScoresFragment : Fragment() {

    private var courseCode: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestScoresAdapter
    private lateinit var addTestButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString(ARG_COURSE_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.testRecyclerView)
        addTestButton = view.findViewById(R.id.addTestButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TestScoresAdapter(
            mutableListOf(),
            onTestClick = { test -> openTestDetails(test) },
            onEditClick = { test -> showEditTestDialog(test) },
            onDeleteClick = { test -> confirmDeleteTest(test) }
        )
        recyclerView.adapter = adapter

        addTestButton.setOnClickListener {
            showAddTestDialog()
        }

        loadTests()
    }
    private fun showAddTestDialog() {
        val code = courseCode
        if (code.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Course code missing", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_test_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_add_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel_test)

        val dialog = builder.setView(dialogView).create()

        confirmButton.setOnClickListener {
            val testName = nameInput.text.toString().trim()

            if (testName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter test name", Toast.LENGTH_SHORT).show()
            } else {
                val code = courseCode ?: return@setOnClickListener
                FirebaseService.createTest(code, testName)
                Toast.makeText(requireContext(), "Test created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadTests()     // refresh list
            }
        }


        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadTests() {
        val code = courseCode ?: return

        FirebaseService.getTests(code) { testNames ->
            if (testNames.isEmpty()) {
                adapter.setData(emptyList())
                return@getTests
            }

            val result = mutableListOf<TestItem>()
            var remaining = testNames.size

            for (name in testNames) {
                FirebaseService.getGrade(code, name) { grade ->
                    result.add(TestItem(name = name, grade = grade))

                    remaining--
                    if (remaining == 0) {
                        result.sortBy { it.name }
                        adapter.setData(result)
                    }
                }
            }
        }
    }

    private fun openTestDetails(test: TestItem) {
        val code = courseCode ?: return
        (activity as? MainPage)?.openTestDetails(code, test.name)
    }

    private fun showEditTestDialog(test: TestItem) {
        // hook into your existing edit-test dialog logic
    }

    private fun confirmDeleteTest(test: TestItem) {
        // show AlertDialog and call FirebaseService.deleteTest(...)
    }

    companion object {
        private const val ARG_COURSE_CODE = "course_code"

        fun newInstance(courseCode: String): TestScoresFragment {
            val f = TestScoresFragment()
            f.arguments = Bundle().apply {
                putString(ARG_COURSE_CODE, courseCode)
            }
            return f
        }
    }
}
