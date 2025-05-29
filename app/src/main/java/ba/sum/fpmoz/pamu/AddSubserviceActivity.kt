package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ba.sum.fpmoz.pamu.model.Usluga
import com.google.firebase.firestore.FirebaseFirestore

class AddSubserviceActivity : AppCompatActivity() {

    private lateinit var autoKategorija: AutoCompleteTextView
    private lateinit var etNaziv: EditText
    private lateinit var etCijena: EditText
    private lateinit var btnSpremi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subservice)

        // Inicijalizacija UI elemenata
        autoKategorija = findViewById(R.id.autoKategorija)
        etNaziv = findViewById(R.id.etNaziv)
        etCijena = findViewById(R.id.etCijena)
        btnSpremi = findViewById(R.id.btnSpremi)

        // Toolbar s bijelim naslovom i strelicom
        val toolbar = findViewById<Toolbar>(R.id.addSubserviceToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dodaj poduslugu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Učitaj kategorije iz Firestore (polje "name")
        val db = FirebaseFirestore.getInstance()
        db.collection("services")
            .get()
            .addOnSuccessListener { result ->
                val kategorije = result.documents.mapNotNull { it.getString("name") }

                if (kategorije.isEmpty()) {
                    Toast.makeText(this, "Nema pronađenih kategorija", Toast.LENGTH_SHORT).show()
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategorije)
                autoKategorija.setAdapter(adapter)

                autoKategorija.setOnClickListener {
                    autoKategorija.showDropDown()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dohvaćanju kategorija", Toast.LENGTH_SHORT).show()
            }

        // Spremi novu poduslugu
        btnSpremi.setOnClickListener {
            val kategorija = autoKategorija.text.toString().trim().lowercase()
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

