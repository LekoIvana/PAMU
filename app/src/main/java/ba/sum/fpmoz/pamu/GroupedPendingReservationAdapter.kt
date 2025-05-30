package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class GroupedPendingReservationAdapter(
    private val items: List<ReservationListItem>,
    private val onApprove: (String) -> Unit,
    private val onReject: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_RESERVATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReservationListItem.DateHeader -> VIEW_TYPE_HEADER
            is ReservationListItem.ReservationItem -> VIEW_TYPE_RESERVATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reservation_card, parent, false)
                ReservationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReservationListItem.DateHeader -> {
                (holder as HeaderViewHolder).dateHeaderText.text = item.dateLabel
            }
            is ReservationListItem.ReservationItem -> {
                val reservation = item.reservation
                val reservationHolder = holder as ReservationViewHolder
                reservationHolder.dateTime.text = "${reservation.date} u ${reservation.time}"
                reservationHolder.category.text = "Kategorija: ${reservation.category}"
                reservationHolder.user.text = reservation.user
                reservationHolder.note.text = "Napomena: ${reservation.note}"

                reservationHolder.approveButton.setOnClickListener {
                    onApprove(reservation.id)
                }

                reservationHolder.rejectButton.setOnClickListener {
                    onReject(reservation.id)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateHeaderText: TextView = view.findViewById(R.id.dateHeaderTextView)
    }

    class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTime: TextView = view.findViewById(R.id.reservationDateTime)
        val category: TextView = view.findViewById(R.id.reservationCategory)
        val user: TextView = view.findViewById(R.id.reservationUser)
        val note: TextView = view.findViewById(R.id.reservationNote)
        val approveButton: ImageButton = view.findViewById(R.id.approveButton)
        val rejectButton: ImageButton = view.findViewById(R.id.rejectButton)
    }
}
