package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserReservationsFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserReservationAdapter
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_reservations, container, false)

        recyclerView = view.findViewById(R.id.userReservationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserReservationAdapter(reservations) { reservationId ->
            db.collection("appointments").document(reservationId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Termin je otkazan", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri otkazivanju termina", Toast.LENGTH_SHORT).show()
                }
        }

        recyclerView.adapter = adapter
        loadReservations()

        return view
    }

    private fun loadReservations() {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("appointments")
            .whereEqualTo("userEmail", userEmail)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations.clear()
                for (doc in snapshot) {
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
