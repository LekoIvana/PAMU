package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserReservationAdapter(
    private val reservations: List<Reservation>,
    private val onCancel: (String) -> Unit
) : RecyclerView.Adapter<UserReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTime: TextView = view.findViewById(R.id.userReservationDateTime)
        val category: TextView = view.findViewById(R.id.userReservationCategory)
        val note: TextView = view.findViewById(R.id.userReservationNote)
        val cancelButton: ImageButton = view.findViewById(R.id.cancelReservationButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.dateTime.text = "${reservation.date} u ${reservation.time}"
        holder.category.text = "Kategorija: ${reservation.category}"
        holder.note.text = "Napomena: ${reservation.note}"

        holder.cancelButton.setOnClickListener {
            onCancel(reservation.id)
        }
    }

    override fun getItemCount(): Int = reservations.size
}
