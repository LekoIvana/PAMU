package ba.sum.fpmoz.pamu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        // Toolbar s navigacijom
        val toolbar = findViewById<Toolbar>(R.id.contactToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Klik na broj telefona
        val phoneText = findViewById<TextView>(R.id.contactPhone)
        phoneText.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+38763000111")
            startActivity(intent)
        }

        // Klik na email adresu
        val emailText = findViewById<TextView>(R.id.contactEmail)
        emailText.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:glambooksalon@gmail.com")
            }
            startActivity(intent)
        }
    }
}
