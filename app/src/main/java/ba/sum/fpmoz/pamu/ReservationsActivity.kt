package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ReservationsActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var timeListView: ListView
    private lateinit var noteEditText: EditText
    private lateinit var submitReservationButton: Button

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val allTimes = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00")
    private var selectedCategory = ""
    private var selectedDate = ""
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reservations)

        calendarView = findViewById(R.id.calendarView)
        selectedDateText = findViewById(R.id.selectedDateText)
        timeListView = findViewById(R.id.timeListView)
        noteEditText = findViewById(R.id.noteEditText)
        submitReservationButton = findViewById(R.id.submitReservationButton)

        noteEditText.visibility = View.GONE
        submitReservationButton.visibility = View.GONE
        timeListView.visibility = View.GONE

        selectedCategory = intent.getStringExtra("selectedCategory") ?: "Nepoznata"

        val toolbar = findViewById<Toolbar>(R.id.reservationsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = " $selectedCategory"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            if (selectedCalendar.before(today)) {
                Toast.makeText(this, "Ne možete rezervirati termin za prošli datum.", Toast.LENGTH_SHORT).show()
                selectedDateText.visibility = View.GONE
                timeListView.visibility = View.GONE
                noteEditText.visibility = View.GONE
                submitReservationButton.visibility = View.GONE
                return@setOnDateChangeListener
            }

            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDateText.text = "Odabrani datum: $selectedDate"
            selectedDateText.visibility = View.VISIBLE
            loadAvailableTimes()
        }

        timeListView.setOnItemClickListener { _, _, position, _ ->
            selectedTime = timeListView.adapter.getItem(position).toString()
            noteEditText.visibility = View.VISIBLE
            submitReservationButton.visibility = View.VISIBLE
        }

        submitReservationButton.setOnClickListener {
            val note = noteEditText.text.toString().trim()
            if (note.isEmpty()) {
                Toast.makeText(this, "Unesite napomenu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTime.isNullOrEmpty()) {
                Toast.makeText(this, "Odaberite termin iz liste", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_reservation, null)
            val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelButton)
            val confirmBtn = dialogView.findViewById<Button>(R.id.confirmButton)

            dialogMessage.text = "Želite li rezervirati $selectedTime za $selectedDate?"

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            confirmBtn.setOnClickListener {
                reserveAppointment(selectedTime!!, note)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun loadAvailableTimes() {
        db.collection("appointments")
            .whereEqualTo("category", selectedCategory)
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { snapshot ->
                val takenTimes = snapshot.documents.mapNotNull { it.getString("time") }
                val availableTimes = allTimes.filterNot { takenTimes.contains(it) }

                if (availableTimes.isEmpty()) {
                    Toast.makeText(this, "Nema dostupnih termina za taj dan", Toast.LENGTH_SHORT).show()
                    timeListView.visibility = View.GONE
                    return@addOnSuccessListener
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availableTimes)
                timeListView.adapter = adapter
                timeListView.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dohvaćanju termina", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reserveAppointment(time: String, note: String) {
        val email = auth.currentUser?.email ?: return

        db.collection("appointments")
            .whereEqualTo("category", selectedCategory)
            .whereEqualTo("date", selectedDate)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val appointment = hashMapOf(
                        "category" to selectedCategory,
                        "date" to selectedDate,
                        "time" to time,
                        "userEmail" to email,
                        "note" to note,
                        "status" to "pending"
                    )
                    db.collection("appointments")
                        .add(appointment)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Rezervacija je poslana. Čeka se odobrenje.", Toast.LENGTH_SHORT).show()
                            timeListView.visibility = View.GONE
                            noteEditText.text.clear()
                            noteEditText.visibility = View.GONE
                            submitReservationButton.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Greška pri spremanju rezervacije", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Taj termin je već zauzet", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
