package ba.sum.fpmoz.pamu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity



class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.locationToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Otvori lokaciju
        val openMapBtn = findViewById<Button>(R.id.openMapBtn)
        openMapBtn.setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=Fra+Grge+Martića+40,+88240+Posušje")
            val intent = Intent(Intent.ACTION_VIEW, uri)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Rezervna opcija: otvori preko weba
                val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=Fra+Grge+Martića+40,+88240+Posušje")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                startActivity(fallbackIntent)
            }
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}
