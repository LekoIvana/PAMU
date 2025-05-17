package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val users: List<User>,
    private val onDeleteClick: (User) -> Unit,
    private val onRoleChangeClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val userEmailText: TextView = view.findViewById(R.id.userEmailText)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteUserBtn)
        val roleBtn: ImageButton = view.findViewById(R.id.editRoleBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.userNameText.text = user.userName
        holder.userEmailText.text = user.email

        holder.deleteBtn.setOnClickListener { onDeleteClick(user) }
        holder.roleBtn.setOnClickListener { onRoleChangeClick(user) }
    }
}
