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

        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.nav_menu)

        val db = FirebaseFirestore.getInstance()
        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    val menu = navigationView.menu

                    // Uvijek sakrij "Dodaj uslugu" i "Upravljanje korisnicima"
                    menu.findItem(R.id.nav_add_service)?.isVisible = false
                    menu.findItem(R.id.nav_manage_users)?.isVisible = false

                    if (role == "admin") {
                        menu.findItem(R.id.nav_appointments)?.isVisible = false
                        menu.findItem(R.id.nav_admin_reservations)?.isVisible = true
                        menu.findItem(R.id.nav_admin_panel)?.isVisible = true
                    } else {
                        menu.findItem(R.id.nav_admin_reservations)?.isVisible = false
                        menu.findItem(R.id.nav_admin_panel)?.isVisible = false
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška pri dohvaćanju uloge", Toast.LENGTH_SHORT).show()
                }
        }

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.navUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.navUserEmail)

        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
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
                R.id.nav_admin_reservations -> {
                    startActivity(Intent(this, ReservationsAdminActivity::class.java))
                    true
                }
                R.id.nav_appointments -> {
                    startActivity(Intent(this, UserReservationsActivity::class.java))
                    true
                }
                R.id.nav_admin_panel -> {
                    startActivity(Intent(this, AdminPanelActivity::class.java))
                    true
                }
                else -> false
            }
        }

        recyclerView = findViewById(R.id.serviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db.collection("services").get()
            .addOnSuccessListener { result ->
                val services = mutableListOf<Service>()
                for (document in result) {
                    val name = document.getString("name") ?: "N/A"
                    val description = document.getString("description") ?: ""
                    val imageRes = R.drawable.balayage
                    services.add(Service(name, description, imageRes))
                }

                recyclerView.adapter = ServiceAdapter(services) { service ->
                    val intent = Intent(this, ReservationsActivity::class.java)
                    intent.putExtra("selectedCategory", service.name)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri učitavanju usluga", Toast.LENGTH_SHORT).show()
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
