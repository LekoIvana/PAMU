package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var newPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        val uid = user?.uid

        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        toolbar = findViewById(R.id.settingsToolbar)


        setSupportActionBar(toolbar)
        supportActionBar?.title = "Postavke"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        userEmailTextView.text = user?.email ?: "Nepoznat email"

        // Dohvati ime iz Firestore-a
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("userName")
                    userNameTextView.text = userName ?: "Nepoznato ime"
                }
                .addOnFailureListener {
                    userNameTextView.text = "Greška pri dohvaćanju imena"
                }
        }


        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()

            if (newPassword.length < 6) {
                Toast.makeText(this, "Lozinka mora imati barem 6 znakova.", Toast.LENGTH_SHORT).show()
            } else {
                user?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Lozinka promijenjena.", Toast.LENGTH_SHORT).show()
                            newPasswordEditText.text.clear()
                        } else {
                            Toast.makeText(this, "Greška: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }


        deleteAccountButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)

            val alertDialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val confirmButton = dialogView.findViewById<Button>(R.id.confirmDeleteButton)

            cancelButton.setOnClickListener { alertDialog.dismiss() }

            confirmButton.setOnClickListener {
                val user = auth.currentUser
                val uid = user?.uid

                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("users").document(uid ?: "").delete()
                        Toast.makeText(this, "Račun je obrisan.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Greška: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
                alertDialog.dismiss()
            }

            alertDialog.show()
        }


    }
}
