package ba.sum.fpmoz.pamu

data class Reservation(
    val id: String,
    val date: String,
    val time: String,
    val user: String,
    val note: String,
    val category: String
)
