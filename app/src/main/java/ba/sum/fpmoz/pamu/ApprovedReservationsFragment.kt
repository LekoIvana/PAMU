package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ApprovedReservationsFragment : Fragment() {

    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApprovedReservationAdapter
    private lateinit var emptyMessage: TextView
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_approved_reservations, container, false)

        recyclerView = view.findViewById(R.id.approvedRecyclerView)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ApprovedReservationAdapter(reservations)
        recyclerView.adapter = adapter

        loadApprovedReservations()
        return view
    }

    private fun loadApprovedReservations() {
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        db.collection("appointments")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations.clear()

                val filteredList = snapshot.documents.sortedWith(compareBy(
                    { it.getString("date") },
                    { it.getString("time") }
                ))

                for (doc in filteredList) {
                    val id = doc.id
                    val date = doc.getString("date")
                    val time = doc.getString("time")

                    if (date != null && time != null) {
                        try {
                            val fullDateTime = formatter.parse("$date $time")
                            if (fullDateTime != null) {
                                val cal = Calendar.getInstance()
                                cal.time = fullDateTime
                                cal.add(Calendar.HOUR_OF_DAY, 1)
                                val endTime = cal.time

                                if (endTime.after(currentDateTime)) {
                                    val user = doc.getString("userEmail") ?: ""
                                    val note = doc.getString("note") ?: ""
                                    val category = doc.getString("category") ?: ""
                                    reservations.add(
                                        Reservation(id, date, time, user, note, category)
                                    )
                                } else {
                                    db.collection("appointments").document(id).delete()
                                }
                            }
                        } catch (e: Exception) {
                            // skip if date/time parse fails
                        }
                    }
                }

                adapter.notifyDataSetChanged()
                emptyMessage.visibility = if (reservations.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju rezervacija", Toast.LENGTH_SHORT).show()
            }
    }
}
