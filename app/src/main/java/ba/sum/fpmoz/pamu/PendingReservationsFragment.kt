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
    private lateinit var adapter: GroupedPendingReservationAdapter
    private lateinit var emptyMessage: TextView
    private val items = mutableListOf<ReservationListItem>()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateLabelFormat = SimpleDateFormat("EEEE, dd. MMMM", Locale("hr"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pending_reservations, container, false)

        recyclerView = view.findViewById(R.id.pendingRecyclerView)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GroupedPendingReservationAdapter(items,
            onApprove = { id -> showApproveDialog(id) },
            onReject = { id -> showRejectDialog(id) }
        )
        recyclerView.adapter = adapter

        loadReservations()

        return view
    }

    private fun showApproveDialog(id: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_confirm_reservation_admin, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<TextView>(R.id.dialogMessage).text = "Želite li potvrditi ovu rezervaciju?"
        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            db.collection("appointments").document(id)
                .update("status", "approved")
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rezervacija prihvaćena", Toast.LENGTH_SHORT).show()
                    loadReservations()
                    (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
                        if (fragment is ApprovedReservationsFragment) {
                            fragment.loadApprovedReservations()
                        }
                    }

                }
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRejectDialog(id: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reject_reservation_admin, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<TextView>(R.id.dialogMessage).text = "Jeste li sigurni da želite odbiti ovu rezervaciju?"
        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            db.collection("appointments").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rezervacija odbijena", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReservations() {
        val currentDateTime = Calendar.getInstance().time

        db.collection("appointments")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val reservations = mutableListOf<Reservation>()
                val toDelete = mutableListOf<String>()

                for (doc in snapshot.documents) {
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
                                if (endTime.before(currentDateTime)) {
                                    toDelete.add(id)
                                } else {
                                    val user = doc.getString("userEmail") ?: ""
                                    val note = doc.getString("note") ?: ""
                                    val category = doc.getString("category") ?: ""
                                    reservations.add(Reservation(id, date, time, user, note, category))
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }

                toDelete.forEach { db.collection("appointments").document(it).delete() }

                val grouped = reservations
                    .sortedWith(compareBy({ it.date }, { it.time }))
                    .groupBy { it.date }

                items.clear()
                for ((date, resList) in grouped) {
                    val dateLabel = try {
                        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                        dateLabelFormat.format(parsedDate ?: Date())
                    } catch (_: Exception) {
                        date
                    }

                    items.add(ReservationListItem.DateHeader(dateLabel))
                    items.addAll(resList.map { ReservationListItem.ReservationItem(it) })
                }

                adapter.notifyDataSetChanged()
                emptyMessage.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
