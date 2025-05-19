package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReservationAdapter(
    private val reservations: List<Reservation>,
    private val onApprove: (String) -> Unit,
    private val onReject: (String) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTime: TextView = view.findViewById(R.id.reservationDateTime)
        val category: TextView = view.findViewById(R.id.reservationCategory)
        val user: TextView = view.findViewById(R.id.reservationUser)
        val note: TextView = view.findViewById(R.id.reservationNote)
        val approveButton: ImageButton = view.findViewById(R.id.approveButton)
        val rejectButton: ImageButton = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_card, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.dateTime.text = "${reservation.date} u ${reservation.time}"
        holder.category.text = "Kategorija: ${reservation.category}"
        holder.user.text = reservation.user
        holder.note.text = "Napomena: ${reservation.note}"

        holder.approveButton.setOnClickListener {
            onApprove(reservation.id)
        }

        holder.rejectButton.setOnClickListener {
            onReject(reservation.id)
        }
    }

    override fun getItemCount(): Int = reservations.size
}
