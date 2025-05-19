package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        val approveButton: ImageButton = view.findViewById(R.id.approveButton)
        val rejectButton: ImageButton = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_card, parent, false)
        return ApprovedViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovedViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.dateTime.text = "${reservation.date} u ${reservation.time}"
        holder.category.text = "Kategorija: ${reservation.category}"
        holder.user.text = reservation.user
        holder.note.text = "Napomena: ${reservation.note}"

        // Sakrij dugmad za prihvati/odbij
        holder.approveButton.visibility = View.GONE
        holder.rejectButton.visibility = View.GONE
    }

    override fun getItemCount(): Int = reservations.size
}
