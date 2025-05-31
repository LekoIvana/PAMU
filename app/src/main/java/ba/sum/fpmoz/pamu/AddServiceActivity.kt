package ba.sum.fpmoz.pamu

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager
import io.github.jan.supabase.storage.storage

class AddServiceActivity : AppCompatActivity() {

    private lateinit var selectImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var saveServiceBtn: Button
    private lateinit var serviceNameInput: EditText
    private lateinit var serviceDescriptionInput: EditText

    private var selectedImageUri: Uri? = null
    private var isUploading = false

    private val supabaseClient = createSupabaseClient(
        supabaseUrl = "https://ikskiauzrtefhfivnmre.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imlrc2tpYXV6cnRlZmhmaXZubXJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg2MzQ3ODcsImV4cCI6MjA2NDIxMDc4N30.YxJypKKFZZUktxTsBG7Y-Dh9v6U6akP-F9R2K_ci98w"
    ) {
        install(Storage)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageView.setImageURI(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openGallery()
        else Toast.makeText(this, "Potrebna je dozvola za pristup slikama", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        selectImageButton = findViewById(R.id.selectImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        saveServiceBtn = findViewById(R.id.saveServiceBtn)
        serviceNameInput = findViewById(R.id.serviceNameInput)
        serviceDescriptionInput = findViewById(R.id.serviceDescriptionInput)

        selectImageButton.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        saveServiceBtn.setOnClickListener {
            if (isUploading) {
                Toast.makeText(this, "Upload je u tijeku, molimo pričekajte...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = serviceNameInput.text.toString().trim()
            val description = serviceDescriptionInput.text.toString().trim()

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Molimo ispunite naziv i opis usluge", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Molimo odaberite sliku", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImageAndSaveService(name, description, selectedImageUri!!)
        }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                Toast.makeText(this, "Potrebna je dozvola za pristup slikama", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadImageAndSaveService(name: String, description: String, imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            isUploading = true
            saveServiceBtn.isEnabled = false
            selectImageButton.isEnabled = false

            val bucket = "usluge"
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val filePath = "services/$userId/${System.currentTimeMillis()}.jpg"

            val inputStream = contentResolver.openInputStream(imageUri)
            val fileBytes = withContext(Dispatchers.IO) { inputStream?.readBytes() }

            if (fileBytes == null) {
                Toast.makeText(this@AddServiceActivity, "Ne mogu učitati sliku", Toast.LENGTH_SHORT).show()
                resetUi()
                return@launch
            }

            val uploadResult = withContext(Dispatchers.IO) {
                supabaseClient.storage.from(bucket).upload(filePath, fileBytes)
            }

            val imageUrl = supabaseClient.storage.from(bucket).publicUrl(filePath)

            val service = hashMapOf(
                "name" to name,
                "description" to description,
                "imageUrl" to imageUrl,
                "timestamp" to Timestamp.now()
            )

            val db = FirebaseFirestore.getInstance()
            val serviceId = name.lowercase().replace(" ", "_") // Čitljiv i jedinstven ID

            db.collection("services").document(serviceId).set(service)
                .addOnSuccessListener {
                    Toast.makeText(this@AddServiceActivity, "Usluga i slika su spremljeni", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@AddServiceActivity, "Greška pri spremanju usluge", Toast.LENGTH_SHORT).show()
                    resetUi()
                }
        }
    }

    private fun resetUi() {
        isUploading = false
        saveServiceBtn.isEnabled = true
        selectImageButton.isEnabled = true
    }
}
