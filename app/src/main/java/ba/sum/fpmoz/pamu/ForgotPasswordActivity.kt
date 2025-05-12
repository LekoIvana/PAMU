package ba.sum.fpmoz.pamu

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var resetEmailEditText: EditText
    private lateinit var sendResetButton: Button
    private lateinit var backToLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        resetEmailEditText = findViewById(R.id.resetEmailEditText)
        sendResetButton = findViewById(R.id.sendResetButton)
        backToLogin = findViewById(R.id.backToLogin)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        sendResetButton.setOnClickListener {
            val email = resetEmailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Unesite email adresu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Provjera postoji li korisnik u Firestore kolekciji "users"
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Korisnik s tim emailom ne postoji
                        Toast.makeText(this, "Korisnik s tim emailom ne postoji", Toast.LENGTH_LONG).show()
                    } else {
                        // Ako korisnik postoji, šaljemo email za resetiranje lozinke
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Email za resetovanje lozinke je poslan", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Došlo je do pogreške, pokušajte ponovo", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Došlo je do pogreške, pokušajte ponovo", Toast.LENGTH_LONG).show()
                }
        }

        backToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}