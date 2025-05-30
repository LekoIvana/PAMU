package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupedUserReservationAdapter(
    private val items: List<ReservationListItem>,
    private val onCancelClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_RESERVATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReservationListItem.DateHeader -> TYPE_HEADER
            is ReservationListItem.ReservationItem -> TYPE_RESERVATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_reservation, parent, false)
            ReservationViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReservationListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is ReservationListItem.ReservationItem -> (holder as ReservationViewHolder).bind(item.reservation)
        }
    }

    inner class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateHeaderText: TextView = view.findViewById(R.id.dateHeaderTextView)
        fun bind(header: ReservationListItem.DateHeader) {
            dateHeaderText.text = header.dateLabel
        }
    }

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTime: TextView = view.findViewById(R.id.userReservationDateTime)
        private val category: TextView = view.findViewById(R.id.userReservationCategory)
        private val note: TextView = view.findViewById(R.id.userReservationNote)
        private val cancelButton: ImageButton = view.findViewById(R.id.cancelReservationButton)

        fun bind(reservation: Reservation) {
            dateTime.text = "${reservation.date} u ${reservation.time}"
            category.text = "Kategorija: ${reservation.category}"
            note.text = "Napomena: ${reservation.note}"
            cancelButton.setOnClickListener {
                onCancelClick(reservation.id)
            }
        }
    }
}
