package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var navigationView: NavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Početna"


        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.navUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.navUserEmail)

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(uid)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("userName")
                    val email = document.getString("email") ?: user.email

                    navUserName.text = name ?: "Korisnik"
                    navUserEmail.text = email ?: "email@primjer.com"
                } else {
                    navUserName.text = "Nepoznato"
                    navUserEmail.text = user?.email ?: "email@primjer.com"
                }
            }.addOnFailureListener {
                navUserName.text = "Greška"
                navUserEmail.text = user?.email ?: "email@primjer.com"
            }
        }


        // Klikovi u meniju
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_appointments -> {
                    Toast.makeText(this, "Prikaz termina (uskoro dostupno)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }


        // Prikaz usluga
        recyclerView = findViewById(R.id.serviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val services = listOf(
            Service("Balayage", "Tehnika bojenja s prirodnim prijelazima.", R.drawable.balayage),
            Service("Svečana frizura", "Elegantne frizure za posebne prilike.", R.drawable.balayage),
            Service("Šišanje", "Moderna i klasična šišanja po želji.", R.drawable.balayage),
            Service("Nokti", "Ugradnja i njega umjetnih noktiju.", R.drawable.balayage),
            Service("Manikura", "Njega prirodnih noktiju i ruku.", R.drawable.balayage),
            Service("Pedikura", "Estetska i medicinska njega stopala.", R.drawable.balayage)
        )

        recyclerView.adapter = ServiceAdapter(services) { service ->
            Toast.makeText(this, "Odabrano: ${service.name}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
