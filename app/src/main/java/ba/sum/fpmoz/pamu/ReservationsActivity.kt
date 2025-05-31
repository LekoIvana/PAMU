package ba.sum.fpmoz.pamu

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ReservationsActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var timeListView: ListView
    private lateinit var subserviceSpinner: Spinner
    private lateinit var submitReservationButton: Button

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val allTimes = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00")
    private var selectedCategoryName = ""
    private var selectedCategoryId = ""
    private var selectedDate = ""
    private var selectedTime: String? = null
    private var selectedSubservice: String? = null
    private var timeAdapter: TimeAdapter? = null

    data class Subservice(val name: String, val price: Long) {
        override fun toString(): String = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reservations)

        calendarView = findViewById(R.id.calendarView)
        selectedDateText = findViewById(R.id.selectedDateText)
        timeListView = findViewById(R.id.timeListView)
        subserviceSpinner = findViewById(R.id.subserviceSpinner)
        submitReservationButton = findViewById(R.id.submitReservationButton)

        submitReservationButton.visibility = View.GONE
        timeListView.visibility = View.GONE
        subserviceSpinner.visibility = View.GONE

        selectedCategoryName = intent.getStringExtra("selectedCategory") ?: "Nepoznata"

        val toolbar = findViewById<Toolbar>(R.id.reservationsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = selectedCategoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Prvo dohvatiti ID kategorije iz naziva
        fetchCategoryIdByName(selectedCategoryName) { categoryId ->
            if (categoryId != null) {
                selectedCategoryId = categoryId
                // Sada može nastaviti s postavljanjem listenera i omogućiti odabir datuma
                setupCalendar()
            } else {
                Toast.makeText(this, "Kategorija nije pronađena", Toast.LENGTH_LONG).show()
                finish() // ili drugačije postupanje
            }
        }

        // Listener na spinneru za podusluge
        subserviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val subservice = parent.getItemAtPosition(position) as Subservice
                selectedSubservice = subservice.name
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSubservice = null
            }
        }

        submitReservationButton.setOnClickListener {
            if (selectedTime.isNullOrEmpty()) {
                Toast.makeText(this, "Odaberite termin iz liste", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedSubservice.isNullOrEmpty()) {
                Toast.makeText(this, "Odaberite uslugu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_reservation, null)
            val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelButton)
            val confirmBtn = dialogView.findViewById<Button>(R.id.confirmButton)

            dialogMessage.text = "Želite li rezervirati $selectedTime za $selectedDate?\nUsluga: $selectedSubservice"

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            confirmBtn.setOnClickListener {
                reserveAppointment(selectedTime!!, selectedSubservice!!)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupCalendar() {
        val today = Calendar.getInstance()
        calendarView.setDate(today.timeInMillis, false, true)

        calendarView.setDateTextAppearance(R.style.TodayDateTextAppearance)
        calendarView.setSelectedWeekBackgroundColor(Color.parseColor("#ff9ddb"))
        calendarView.setFocusedMonthDateColor(Color.parseColor("#89135b"))
        calendarView.setUnfocusedMonthDateColor(Color.GRAY)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            val now = Calendar.getInstance()
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)

            if (selectedCalendar.before(now)) {
                Toast.makeText(this, "Ne možete rezervirati termin za prošli datum.", Toast.LENGTH_SHORT).show()
                selectedDateText.visibility = View.GONE
                timeListView.visibility = View.GONE
                subserviceSpinner.visibility = View.GONE
                submitReservationButton.visibility = View.GONE
                return@setOnDateChangeListener
            }

            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDateText.text = "Odabrani datum: $selectedDate"
            selectedDateText.visibility = View.VISIBLE

            selectedTime = null
            selectedSubservice = null
            timeAdapter?.selectedPosition = -1
            timeAdapter?.notifyDataSetChanged()
            subserviceSpinner.visibility = View.GONE
            submitReservationButton.visibility = View.GONE

            loadAvailableTimes()
        }

        timeListView.setOnItemClickListener { _, _, position, _ ->
            selectedTime = timeAdapter?.getItem(position)
            timeAdapter?.selectedPosition = position
            timeAdapter?.notifyDataSetChanged()

            selectedSubservice = null
            loadSubservices()
            subserviceSpinner.visibility = View.VISIBLE
            submitReservationButton.visibility = View.VISIBLE
        }
    }

    private fun fetchCategoryIdByName(categoryName: String, callback: (String?) -> Unit) {
        db.collection("services")
            .whereEqualTo("name", categoryName)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    Log.d("ReservationsActivity", "Category ID found: ${result.documents[0].id}")
                    callback(result.documents[0].id)
                } else {
                    Log.d("ReservationsActivity", "Category not found for name: $categoryName")
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e("ReservationsActivity", "Error fetching category ID", it)
                callback(null)
            }
    }

    private fun loadAvailableTimes() {
        db.collection("appointments")
            .whereEqualTo("category", selectedCategoryId)
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

                timeAdapter = TimeAdapter(this, availableTimes)
                timeListView.adapter = timeAdapter
                timeListView.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dohvaćanju termina", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSubservices() {
        db.collection("services")
            .document(selectedCategoryId)
            .collection("subservices")
            .get()
            .addOnSuccessListener { result ->
                val subservices = result.mapNotNull {
                    val name = it.getString("name") ?: return@mapNotNull null
                    val price = it.getLong("price") ?: return@mapNotNull null
                    Subservice(name, price)
                }
                if (subservices.isNotEmpty()) {
                    val adapter = object : ArrayAdapter<Subservice>(this, android.R.layout.simple_spinner_item, subservices) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent)
                            (view as TextView).text = "${subservices[position].name} - ${subservices[position].price} KM"
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            (view as TextView).text = "${subservices[position].name} - ${subservices[position].price} KM"
                            return view
                        }
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subserviceSpinner.adapter = adapter
                    subserviceSpinner.visibility = View.VISIBLE
                } else {
                    subserviceSpinner.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dohvaćanju usluga", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reserveAppointment(time: String, subservice: String) {
        val email = auth.currentUser?.email ?: return

        db.collection("appointments")
            .whereEqualTo("category", selectedCategoryId)
            .whereEqualTo("date", selectedDate)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val appointment = hashMapOf(
                        "category" to selectedCategoryId,
                        "date" to selectedDate,
                        "time" to time,
                        "userEmail" to email,
                        "note" to subservice,
                        "status" to "pending"
                    )
                    db.collection("appointments")
                        .add(appointment)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Rezervacija je poslana. Čeka se odobrenje.", Toast.LENGTH_SHORT).show()
                            timeListView.visibility = View.GONE
                            subserviceSpinner.visibility = View.GONE
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

class TimeAdapter(
    context: Context,
    private val times: List<String>
) : ArrayAdapter<String>(context, 0, times) {

    var selectedPosition: Int = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_time, parent, false)
        val timeTextView = view.findViewById<TextView>(R.id.timeTextView)

        timeTextView.text = times[position]

        if (position == selectedPosition) {
            timeTextView.setBackgroundColor(Color.parseColor("#f3c4ff"))
        } else {
            timeTextView.setBackgroundColor(Color.TRANSPARENT)
        }

        return view
    }
}
