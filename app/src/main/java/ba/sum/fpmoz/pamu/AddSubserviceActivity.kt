package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class AddSubserviceActivity : AppCompatActivity() {

    private lateinit var autoKategorija: AutoCompleteTextView
    private lateinit var etNaziv: EditText
    private lateinit var etCijena: EditText
    private lateinit var btnSpremi: Button


    private val kategorijeMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subservice)

        autoKategorija = findViewById(R.id.autoKategorija)
        etNaziv = findViewById(R.id.etNaziv)
        etCijena = findViewById(R.id.etCijena)
        btnSpremi = findViewById(R.id.btnSpremi)

        val toolbar = findViewById<Toolbar>(R.id.addSubserviceToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dodaj poduslugu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val db = FirebaseFirestore.getInstance()

        db.collection("services")
            .get()
            .addOnSuccessListener { result ->
                val kategorije = mutableListOf<String>()
                for (doc in result) {
                    val name = doc.getString("name") ?: ""
                    val id = doc.id
                    kategorije.add(name)
                    kategorijeMap[name] = id
                }

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

        btnSpremi.setOnClickListener {
            val kategorijaNaziv = autoKategorija.text.toString().trim()
            val naziv = etNaziv.text.toString().trim()
            val cijena = etCijena.text.toString().toIntOrNull()

            if (kategorijaNaziv.isEmpty() || naziv.isEmpty() || cijena == null) {
                Toast.makeText(this, "Popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kategorijaId = kategorijeMap[kategorijaNaziv]
            if (kategorijaId == null) {
                Toast.makeText(this, "Odaberite validnu kategoriju!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dodajPoduslugu(kategorijaId, naziv, cijena)
        }
    }

    private fun dodajPoduslugu(serviceId: String, naziv: String, cijena: Int) {
        val db = FirebaseFirestore.getInstance()
        val subserviceId = naziv.replace(" ", "_").lowercase()

        val podusluga = hashMapOf(
            "name" to naziv,
            "price" to cijena
        )

        db.collection("services")
            .document(serviceId)
            .collection("subservices")
            .document(subserviceId)
            .set(podusluga)
            .addOnSuccessListener {
                Toast.makeText(this, "Podusluga uspješno dodana", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
