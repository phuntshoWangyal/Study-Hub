package ca.unb.mobiledev.studyhub


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException

class AddCourseFragment : DialogFragment() {

    interface AddCourseDialogListener {
        fun onCourseAdded(course: Course)
    }

    private lateinit var listener: AddCourseDialogListener

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        try {
            listener = context as AddCourseDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement AddCourseDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.popup_course, null)

            val courseCodeInput = view.findViewById<EditText>(R.id.edit_course_code)
            val courseNameInput = view.findViewById<EditText>(R.id.edit_course_name)
            val addButton = view.findViewById<Button>(R.id.btn_add_course)
            val cancelButton = view.findViewById<Button>(R.id.btn_cancel)

            // Inflate and set the layout for the dialog
            builder.setView(view)

            // Setup button listeners
            addButton.setOnClickListener {
                val courseCode = courseCodeInput.text.toString().trim()
                val courseName = courseNameInput.text.toString().trim()

                if (courseCode.isNotEmpty() && courseName.isNotEmpty()) {
                    val newCourse = Course(courseCode, courseName)
                    val uid = FirebaseService.auth.currentUser?.uid
                    if (uid != null) {
                        // 1️⃣ Save locally first
                        val existing = CourseStorage.loadCourses(requireContext(), uid)
                        CourseStorage.saveCourses(requireContext(), uid, existing.toMutableList().apply { add(newCourse) })

                        // 2️⃣ Update UI
                        listener.onCourseAdded(newCourse)
                        dialog?.dismiss()

                        // 3️⃣ Try Firebase in background
                        FirebaseService.addCourse(newCourse, {
                            Log.i("AddCourseFragment", "Course synced to Firebase")
                        }, { error ->
                            Log.e("AddCourseFragment", "Failed to sync to Firebase: ${error.message}")
                        })

                    } else {
                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Course code and name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }




            cancelButton.setOnClickListener {
                dialog?.cancel()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "AddCourseDialogFragment"
    }
}