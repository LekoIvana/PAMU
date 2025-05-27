package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ba.sum.fpmoz.pamu.R
import ba.sum.fpmoz.pamu.model.Usluga
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.Toolbar


class AddSubserviceActivity : AppCompatActivity() {

    private lateinit var etKategorija: EditText
    private lateinit var etNaziv: EditText
    private lateinit var etCijena: EditText
    private lateinit var btnSpremi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subservice)

        etKategorija = findViewById(R.id.etKategorija)
        etNaziv = findViewById(R.id.etNaziv)
        etCijena = findViewById(R.id.etCijena)
        btnSpremi = findViewById(R.id.btnSpremi)

        val toolbar = findViewById<Toolbar>(R.id.addSubserviceToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dodavanje podusluge"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish() // ili startActivity(Intent(...)) ako želiš vratiti na određeni ekran
        }

        btnSpremi.setOnClickListener {
            val kategorija = etKategorija.text.toString().trim().lowercase()
            val naziv = etNaziv.text.toString().trim()
            val cijena = etCijena.text.toString().toIntOrNull()

            if (kategorija.isEmpty() || naziv.isEmpty() || cijena == null) {
                Toast.makeText(this, "Popuni sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novaUsluga = Usluga(naziv, cijena)
            dodajUsluguFirestore(kategorija, novaUsluga)
        }
    }

    private fun dodajUsluguFirestore(kategorija: String, usluga: Usluga) {
        val db = FirebaseFirestore.getInstance()
        db.collection("services")
            .document(kategorija)
            .collection("usluge")
            .document(usluga.naziv.replace(" ", "_").lowercase())
            .set(usluga)
            .addOnSuccessListener {
                Toast.makeText(this, "Uspješno dodano u $kategorija!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}