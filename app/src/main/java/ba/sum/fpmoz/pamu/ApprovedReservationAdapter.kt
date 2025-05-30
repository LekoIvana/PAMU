package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ApprovedReservationAdapter(
    private val reservations: List<Reservation>
) : RecyclerView.Adapter<ApprovedReservationAdapter.ApprovedViewHolder>() {

    inner class ApprovedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTime: TextView = view.findViewById(R.id.reservationDateTime)
        val category: TextView = view.findViewById(R.id.reservationCategory)
        val user: TextView = view.findViewById(R.id.reservationUser)
        val note: TextView = view.findViewById(R.id.reservationNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_approved_reservation, parent, false)
        return ApprovedViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovedViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.dateTime.text = "${reservation.date} u ${reservation.time}"
        holder.category.text = "Kategorija: ${reservation.category}"
        holder.user.text = reservation.user
        holder.note.text = "Napomena: ${reservation.note}"

    }

    override fun getItemCount(): Int = reservations.size
}
