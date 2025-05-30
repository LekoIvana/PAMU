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
    private lateinit var emptyMessage: TextView
    private lateinit var adapter: GroupedReservationAdapter
    private val groupedItems = mutableListOf<ReservationListItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_approved_reservations, container, false)

        recyclerView = view.findViewById(R.id.approvedRecyclerView)
        emptyMessage = view.findViewById(R.id.emptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GroupedReservationAdapter(groupedItems)
        recyclerView.adapter = adapter

        loadApprovedReservations()
        return view
    }

    fun loadApprovedReservations() {
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val displayDateFormatter = SimpleDateFormat("EEEE, dd. MMMM yyyy.", Locale("hr"))

        db.collection("appointments")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                val validReservations = mutableListOf<Reservation>()
                val toDelete = mutableListOf<String>()

                for (doc in snapshot) {
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
                                    validReservations.add(
                                        Reservation(id, date, time, user, note, category)
                                    )
                                } else {
                                    toDelete.add(id)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Obriši stare rezervacije iz Firestore
                toDelete.forEach { id ->
                    db.collection("appointments").document(id).delete()
                }

                groupedItems.clear()

                val groupedMap = validReservations.groupBy { it.date }

                val sortedKeys = groupedMap.keys.sorted()
                for (dateKey in sortedKeys) {
                    val label = try {
                        displayDateFormatter.format(SimpleDateFormat("yyyy-MM-dd").parse(dateKey)!!)
                    } catch (e: Exception) {
                        dateKey
                    }

                    groupedItems.add(ReservationListItem.DateHeader(label))
                    groupedMap[dateKey]?.sortedBy { it.time }?.forEach { res ->
                        groupedItems.add(ReservationListItem.ReservationItem(res))
                    }
                }

                adapter.notifyDataSetChanged()
                emptyMessage.visibility = if (groupedItems.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju rezervacija", Toast.LENGTH_SHORT).show()
            }
    }
}
