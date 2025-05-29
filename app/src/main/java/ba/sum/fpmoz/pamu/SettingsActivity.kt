package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var newPasswordEditText: EditText
    private lateinit var passwordToggleSettings: ImageButton
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var toolbar: Toolbar

    private var isPasswordVisible = false

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
        passwordToggleSettings = findViewById(R.id.passwordToggleSettings)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        toolbar = findViewById(R.id.settingsToolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Postavke"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        userEmailTextView.text = user?.email ?: "Nepoznat email"

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    userNameTextView.text = it.getString("userName") ?: "Nepoznato ime"
                }
                .addOnFailureListener {
                    userNameTextView.text = "Greška pri dohvaćanju imena"
                }
        }

        // Prikaz/sakrivanje lozinke
        passwordToggleSettings.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                newPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggleSettings.setImageResource(R.drawable.ic_visibility)
            } else {
                newPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggleSettings.setImageResource(R.drawable.ic_visibility_off)
            }
            newPasswordEditText.setSelection(newPasswordEditText.text?.length ?: 0)
        }

        // Promjena lozinke
        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            if (newPassword.length < 6) {
                Toast.makeText(this, "Lozinka mora imati barem 6 znakova.", Toast.LENGTH_SHORT).show()
            } else {
                user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Lozinka promijenjena.", Toast.LENGTH_SHORT).show()
                        newPasswordEditText.text?.clear()
                    } else {
                        Toast.makeText(this, "Greška: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Dijalog za potvrdu brisanja računa
        deleteAccountButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
            val alertDialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val confirmButton = dialogView.findViewById<Button>(R.id.confirmDeleteButton)
            val passwordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)
            val passwordToggle = dialogView.findViewById<ImageButton>(R.id.passwordToggleDialog)

            var isDialogPasswordVisible = false

            passwordToggle.setOnClickListener {
                isDialogPasswordVisible = !isDialogPasswordVisible
                if (isDialogPasswordVisible) {
                    passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    passwordToggle.setImageResource(R.drawable.ic_visibility)
                } else {
                    passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    passwordToggle.setImageResource(R.drawable.ic_visibility_off)
                }
                passwordInput.setSelection(passwordInput.text?.length ?: 0)
            }

            cancelButton.setOnClickListener { alertDialog.dismiss() }

            confirmButton.setOnClickListener {
                val enteredPassword = passwordInput.text.toString()
                if (enteredPassword.length < 6) {
                    Toast.makeText(this, "Unesite ispravnu lozinku (min 6 znakova).", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val userEmail = auth.currentUser?.email
                if (userEmail != null && uid != null) {
                    val credential = EmailAuthProvider.getCredential(userEmail, enteredPassword)
                    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    db.collection("users").document(uid).delete()
                                    Toast.makeText(this, "Račun je obrisan.", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Greška: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Neispravna lozinka.", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }
}
