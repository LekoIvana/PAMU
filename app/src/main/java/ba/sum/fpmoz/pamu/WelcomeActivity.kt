package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog

class WelcomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var navigationView: NavigationView
    private lateinit var auth: FirebaseAuth
    private var isAdmin: Boolean = false
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Početna"

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        recyclerView = findViewById(R.id.serviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.nav_menu)

        setupUserRoleAndMenu()
        setupNavigationHeader()
        setupNavigationListener(toolbar)


        loadServices(db)
    }

    override fun onResume() {
        super.onResume()

        loadServices(db)
    }

    private fun setupUserRoleAndMenu() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    isAdmin = role == "admin"

                    val menu = navigationView.menu
                    menu.findItem(R.id.nav_add_service)?.isVisible = false
                    menu.findItem(R.id.nav_manage_users)?.isVisible = false

                    if (isAdmin) {
                        menu.findItem(R.id.nav_appointments)?.isVisible = false
                        menu.findItem(R.id.nav_admin_reservations)?.isVisible = true
                        menu.findItem(R.id.nav_admin_panel)?.isVisible = true
                        menu.findItem(R.id.nav_dodaj_subuslugu)?.isVisible = true
                    } else {
                        menu.findItem(R.id.nav_admin_reservations)?.isVisible = false
                        menu.findItem(R.id.nav_admin_panel)?.isVisible = false
                        menu.findItem(R.id.nav_dodaj_subuslugu)?.isVisible = false
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška pri dohvaćanju uloge", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.navUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.navUserEmail)

        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("userName")
                    val email = document.getString("email") ?: user.email
                    navUserName.text = name ?: "Korisnik"
                    navUserEmail.text = email ?: "email@primjer.com"
                }
                .addOnFailureListener {
                    navUserName.text = "Greška"
                    navUserEmail.text = user.email ?: "email@primjer.com"
                }
        }
    }

    private fun setupNavigationListener(toolbar: Toolbar) {
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_logout -> {
                    showLogoutDialog()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_dodaj_subuslugu -> {
                    startActivity(Intent(this, AddSubserviceActivity::class.java))
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
                R.id.nav_location -> {
                    startActivity(Intent(this, LocationActivity::class.java))
                    true
                }
                R.id.nav_contact -> {
                    startActivity(Intent(this, ContactActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadServices(db: FirebaseFirestore) {
        db.collection("services").get()
            .addOnSuccessListener { result ->
                val services = mutableListOf<Service>()
                for (document in result) {
                    val id = document.id
                    val name = document.getString("name") ?: "N/A"
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl")

                    val service = if (!imageUrl.isNullOrEmpty()) {

                        Service(id = id, name = name, description = description, imageUrl = imageUrl)
                    } else {

                        Service(id = id, name = name, description = description, imageResId = R.drawable.balayage)
                    }
                    services.add(service)
                }

                recyclerView.adapter = ServiceAdapter(
                    services = services,
                    onEditClick = { service ->
                        if (isAdmin) {
                            val intent = Intent(this, EditServiceActivity::class.java)
                            intent.putExtra("SERVICE_ID", service.id)
                            intent.putExtra("SERVICE_NAME", service.name)
                            intent.putExtra("SERVICE_DESC", service.description)
                            intent.putExtra("SERVICE_IMAGE", service.imageResId)
                            startActivity(intent)
                        }
                    },
                    onDeleteClick = { service ->
                        if (isAdmin) {
                            db.collection("services").document(service.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Usluga obrisana", Toast.LENGTH_SHORT).show()
                                    loadServices(db)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Greška pri brisanju", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    isAdmin = isAdmin,
                    onItemClick = { service ->
                        val intent = Intent(this, ReservationsActivity::class.java)
                        intent.putExtra("selectedCategory", service.name)
                        startActivity(intent)
                    }
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri učitavanju usluga", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmLogoutButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
