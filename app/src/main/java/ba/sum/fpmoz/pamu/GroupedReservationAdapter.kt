package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class GroupedReservationAdapter(
    private val items: List<ReservationListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_RESERVATION = 1
    }

    inner class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateHeaderTextView)
    }

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTime: TextView = view.findViewById(R.id.reservationDateTime)
        val category: TextView = view.findViewById(R.id.reservationCategory)
        val user: TextView = view.findViewById(R.id.reservationUser)
        val note: TextView = view.findViewById(R.id.reservationNote)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReservationListItem.DateHeader -> TYPE_DATE_HEADER
            is ReservationListItem.ReservationItem -> TYPE_RESERVATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DATE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reservation_card, parent, false)
            ReservationViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReservationListItem.DateHeader -> {
                (holder as DateHeaderViewHolder).dateText.text = item.dateLabel
            }
            is ReservationListItem.ReservationItem -> {
                val reservation = item.reservation
                val h = holder as ReservationViewHolder
                h.dateTime.text = "${reservation.date} u ${reservation.time}"
                h.category.text = "Kategorija: ${reservation.category}"
                h.user.text = reservation.user
                h.note.text = "Napomena: ${reservation.note}"

                // Ako su dugmad prisutna (koristimo istu XML karticu), sakrij ih
                h.itemView.findViewById<View>(R.id.approveButton)?.visibility = View.GONE
                h.itemView.findViewById<View>(R.id.rejectButton)?.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
