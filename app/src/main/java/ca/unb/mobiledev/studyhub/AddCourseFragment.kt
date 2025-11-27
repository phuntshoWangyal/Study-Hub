package ca.unb.mobiledev.studyhub


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
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
                    FirebaseService.createCourse(courseCode, courseName)
                    // Send the course back to the activity
                    listener.onCourseAdded(newCourse)
                    dialog?.dismiss()
                } else {
                    //Add a error message or smth
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