package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class PendingReservationsFragment : Fragment() {

    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pending_reservations, container, false)
        recyclerView = view.findViewById(R.id.pendingRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReservationAdapter(reservations, onApprove = { id ->
            db.collection("appointments").document(id)
                .update("status", "approved")
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rezervacija prihvaćena", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
        }, onReject = { id ->
            db.collection("appointments").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rezervacija odbijena", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
        })

        recyclerView.adapter = adapter

        loadReservations()
        return view
    }

    private fun loadReservations() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        db.collection("appointments")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations.clear()
                val list = snapshot.documents
                    .filter { it.getString("date") ?: "" >= today }
                    .sortedWith(compareBy({ it.getString("date") }, { it.getString("time") }))

                for (doc in list) {
                    val id = doc.id
                    val date = doc.getString("date") ?: ""
                    val time = doc.getString("time") ?: ""
                    val user = doc.getString("userEmail") ?: ""
                    val note = doc.getString("note") ?: ""
                    val category = doc.getString("category") ?: ""
                    reservations.add(Reservation(id, date, time, user, note, category))
                }

                adapter.notifyDataSetChanged()
            }
    }
}

// Reservation data class
data class Reservation(
    val id: String,
    val date: String,
    val time: String,
    val user: String,
    val note: String,
    val category: String
)
