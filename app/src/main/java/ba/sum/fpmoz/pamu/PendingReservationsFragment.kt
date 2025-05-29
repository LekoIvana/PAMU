package ba.sum.fpmoz.pamu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
    private lateinit var emptyMessage: TextView
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pending_reservations, container, false)

        recyclerView = view.findViewById(R.id.pendingRecyclerView)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReservationAdapter(reservations,
            onApprove = { id ->
                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_confirm_reservation_admin, null)

                val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
                val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
                val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

                dialogMessage.text = "Želite li potvrditi ovu rezervaciju?"

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                confirmButton.setOnClickListener {
                    db.collection("appointments").document(id)
                        .update("status", "approved")
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Rezervacija prihvaćena", Toast.LENGTH_SHORT).show()
                            loadReservations()
                        }
                    dialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            },
            onReject = { id ->
                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_reject_reservation_admin, null)

                val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
                val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
                val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

                dialogMessage.text = "Jeste li sigurni da želite odbiti ovu rezervaciju?"

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                confirmButton.setOnClickListener {
                    db.collection("appointments").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Rezervacija odbijena", Toast.LENGTH_SHORT).show()
                            loadReservations()
                        }
                    dialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        )

        recyclerView.adapter = adapter
        loadReservations()
        return view
    }

    private fun loadReservations() {
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        db.collection("appointments")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations.clear()

                val toDelete = mutableListOf<String>()

                val filteredList = snapshot.documents.filter { doc ->
                    val date = doc.getString("date")
                    val time = doc.getString("time")
                    if (date != null && time != null) {
                        try {
                            val fullDateTime = formatter.parse("$date $time")
                            fullDateTime?.let {
                                val cal = Calendar.getInstance()
                                cal.time = it
                                cal.add(Calendar.HOUR_OF_DAY, 1)
                                val endTime = cal.time
                                if (endTime.before(currentDateTime)) {
                                    toDelete.add(doc.id)
                                    false
                                } else {
                                    true
                                }
                            } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                }.sortedWith(compareBy({ it.getString("date") }, { it.getString("time") }))

                // Obriši zastarjele rezervacije
                for (docId in toDelete) {
                    db.collection("appointments").document(docId).delete()
                }

                for (doc in filteredList) {
                    val id = doc.id
                    val date = doc.getString("date") ?: ""
                    val time = doc.getString("time") ?: ""
                    val user = doc.getString("userEmail") ?: ""
                    val note = doc.getString("note") ?: ""
                    val category = doc.getString("category") ?: ""
                    reservations.add(Reservation(id, date, time, user, note, category))
                }

                adapter.notifyDataSetChanged()
                emptyMessage.visibility = if (reservations.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}

data class Reservation(
    val id: String,
    val date: String,
    val time: String,
    val user: String,
    val note: String,
    val category: String
)


