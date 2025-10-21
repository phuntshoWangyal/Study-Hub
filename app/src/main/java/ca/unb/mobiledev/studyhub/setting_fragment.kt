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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [setting.newInstance] factory method to
 * create an instance of this fragment.
 */
class setting_fragment : Fragment() {
    // TODO: Rename and chGITange types of parameters
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Code to get the date from database, you will need to change it to make it work with fragments, everything else is done including functions
        /*
        */

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

        val cardView = view.findViewById<MaterialCardView>(R.id.signOutCardView)
        cardView.setOnClickListener {
            FirebaseService.signOut()
            val intent = Intent(activity, LoginPage::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
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