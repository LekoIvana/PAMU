package ba.sum.fpmoz.pamu

sealed class ReservationListItem {
    data class DateHeader(val dateLabel: String) : ReservationListItem()
    data class ReservationItem(val reservation: Reservation) : ReservationListItem()
}
