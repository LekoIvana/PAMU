package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditServiceActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var serviceId: String

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)

        nameEditText = findViewById(R.id.editTextName)
        descriptionEditText = findViewById(R.id.editTextDescription)
        saveButton = findViewById(R.id.buttonSave)

        // Primanje podataka
        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        val serviceDesc = intent.getStringExtra("SERVICE_DESC") ?: ""

        nameEditText.setText(serviceName)
        descriptionEditText.setText(serviceDesc)

        saveButton.setOnClickListener {
            val updatedData = mapOf(
                "name" to nameEditText.text.toString(),
                "description" to descriptionEditText.text.toString()
            )

            db.collection("services").document(serviceId)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usluga ažurirana", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // 🔄 ključna linija za osvježavanje u fragmentu
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška pri spremanju", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
