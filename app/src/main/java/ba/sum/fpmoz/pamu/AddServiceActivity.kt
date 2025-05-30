package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AddServiceActivity : AppCompatActivity() {

    private lateinit var serviceNameInput: EditText
    private lateinit var serviceDescriptionInput: EditText
    private lateinit var saveServiceBtn: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        // Toolbar sa strelicom
        toolbar = findViewById(R.id.addServiceToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dodaj uslugu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Polja za unos
        serviceNameInput = findViewById(R.id.serviceNameInput)
        serviceDescriptionInput = findViewById(R.id.serviceDescriptionInput)
        saveServiceBtn = findViewById(R.id.saveServiceBtn)

        val db = FirebaseFirestore.getInstance()

        saveServiceBtn.setOnClickListener {
            val name = serviceNameInput.text.toString().trim()
            val description = serviceDescriptionInput.text.toString().trim()

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Ispunite sva polja", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val service = hashMapOf(
                "name" to name,
                "description" to description,
                "timestamp" to Timestamp.now()
            )

            db.collection("services").add(service)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usluga je spremljena", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška pri spremanju", Toast.LENGTH_SHORT).show()
                }
        }
    }
}