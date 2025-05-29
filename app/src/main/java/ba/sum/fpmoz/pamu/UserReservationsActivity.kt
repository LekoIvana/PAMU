package ba.sum.fpmoz.pamu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UserReservationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reservations)

        // Postavljanje fragmenta u FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, UserReservationsFragment())
            .commit()

        // Toolbar s bijelim naslovom i strelicom natrag
        val toolbar = findViewById<Toolbar>(R.id.userReservationsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Moje rezervacije"

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}

