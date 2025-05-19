package ba.sum.fpmoz.pamu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UserReservationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reservations)

        // Pokretanje fragmenta unutar kontejnera
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, UserReservationsFragment())
            .commit()

        val toolbar = findViewById<Toolbar>(R.id.userReservationsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Moje rezervacije"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

    }
}

