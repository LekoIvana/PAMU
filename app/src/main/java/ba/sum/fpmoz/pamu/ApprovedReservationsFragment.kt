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

class ApprovedReservationsFragment : Fragment() {

    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApprovedReservationAdapter
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_approved_reservations, container, false)

        recyclerView = view.findViewById(R.id.approvedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ApprovedReservationAdapter(reservations) // koristi adapter bez dugmadi
        recyclerView.adapter = adapter

        loadApprovedReservations()

        return view
    }

    private fun loadApprovedReservations() {
        db.collection("appointments")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations.clear()
                val list = snapshot.documents
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
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju rezervacija", Toast.LENGTH_SHORT).show()
            }
    }
}
