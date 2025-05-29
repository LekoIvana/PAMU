package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class UserReservationsFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessageText: TextView
    private lateinit var adapter: UserReservationAdapter
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_reservations, container, false)

        recyclerView = view.findViewById(R.id.userReservationsRecyclerView)
        emptyMessageText = view.findViewById(R.id.emptyMessageText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserReservationAdapter(reservations) { reservationId ->
            showConfirmationDialog(reservationId)
        }

        recyclerView.adapter = adapter
        loadReservations()

        return view
    }

    private fun showConfirmationDialog(reservationId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_confirm_delete, null)

        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmBtn = dialogView.findViewById<Button>(R.id.confirmButton)

        dialogMessage.text = "Jeste li sigurni da želite otkazati ovaj termin?"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        confirmBtn.setOnClickListener {
            db.collection("appointments").document(reservationId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Termin je otkazan", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri otkazivanju termina", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReservations() {
        val userEmail = auth.currentUser?.email ?: return

        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

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

                    val dateTimeStr = "$date $time"
                    val appointmentTime = formatter.parse(dateTimeStr)?.time ?: 0L

                    if (appointmentTime >= now) {
                        val user = doc.getString("userEmail") ?: ""
                        val note = doc.getString("note") ?: ""
                        val category = doc.getString("category") ?: ""
                        reservations.add(Reservation(id, date, time, user, note, category))
                    }
                }
                adapter.notifyDataSetChanged()
                emptyMessageText.visibility = if (reservations.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
