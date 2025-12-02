package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.google.firebase.Firebase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class setting_fragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView: TextView = view.findViewById(R.id.dateJonedProfile)
        FirebaseService.getDate { date ->
            if (date != null) {
                textView.text = date
            } else {
                textView.text = "N/A"
            }
        }

        val usernameText: TextView = view.findViewById(R.id.userName)
        val usernameField: TextView = view.findViewById(R.id.userNameProfile)
        FirebaseService.getName { name ->
            if (name != null) {
                usernameText.text = name
                usernameField.text = name
            } else {
                usernameText.text = "N/A"
                usernameField.text = "N/A"
            }
        }

        val emailText: TextView = view.findViewById(R.id.userEmailProfile)
        FirebaseService.getEmail { email ->
            if (email != null) {
                emailText.text = email
            } else {
                emailText.text = "N/A"
            }
        }

        val timeText: TextView = view.findViewById(R.id.totalHourStudyProfile)
        FirebaseService.getTotalTime { time ->
            val hours = time.toInt()
            val minutes = ((time - hours) * 60).toInt()
            val seconds = (((time - hours) * 60 - minutes) * 60).toInt()
            val hoursStr = String.format("%02d", hours)
            val minStr = String.format("%02d", minutes)
            val secStr = String.format("%02d", seconds)
            timeText.text = "$hoursStr:$minStr:$secStr"
        }

        val cardView = view.findViewById<MaterialCardView>(R.id.signOutCardView)
        cardView.setOnClickListener {
            FirebaseService.signOut()

            val act = requireActivity()
            val intent = Intent(act, LoginPage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            act.startActivity(intent)
            act.finish()
        }

        val emailCardView = view.findViewById<MaterialCardView>(R.id.editUserNameCardView)
        emailCardView.setOnClickListener {
            showChangeUsernameDialog(view.context)
        }

        val passwordCardView = view.findViewById<MaterialCardView>(R.id.editPasswordCardView)
        passwordCardView.setOnClickListener {
            showChangePasswordDialog(view.context)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    fun showChangeUsernameDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.name_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val newName = nameInput.text.toString()
            FirebaseService.usernameChange(newName)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showChangePasswordDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.password_dialog, null)

        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val passwordRepeatInput = dialogView.findViewById<EditText>(R.id.passwordCheckInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val errorText = dialogView.findViewById<TextView>(R.id.errorText)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val newPass = passwordInput.text.toString()
            val newPass2 = passwordRepeatInput.text.toString()
            errorText.visibility = View.VISIBLE
            if(newPass == newPass2 && newPass != "" && newPass.length >= 6){
                FirebaseService.passwordChange(newPass)
                dialog.dismiss()
            }
            else{
                if(newPass == ""){
                    errorText.text = "Entered password was empty"
                }
                else if(newPass.length < 6){
                    errorText.text = "Password should be at least 6 symbols long"
                }
                else{
                    errorText.text = "Passwords do not match"
                }
                errorText.visibility = View.VISIBLE
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment setting.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            rank_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}