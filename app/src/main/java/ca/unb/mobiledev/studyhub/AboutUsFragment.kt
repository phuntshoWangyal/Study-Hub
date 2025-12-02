package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class AboutUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val link1: LinearLayout = view.findViewById(R.id.linkedinLink1)
        val link2: LinearLayout = view.findViewById(R.id.linkedinLink2)
        val link3: LinearLayout = view.findViewById(R.id.linkedinLink3)

        val url1 = "https://www.linkedin.com/in/phuntsho-wangyal"
        val url2 = "https://www.linkedin.com/in/tonypham06/"
        val url3 = "https://www.instagram.com/serhiikhrapchun"

        link1.setOnClickListener { openUrl(url1) }
        link2.setOnClickListener { openUrl(url2) }
        link3.setOnClickListener { openUrl(url3) }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
