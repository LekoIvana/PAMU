package ba.sum.fpmoz.pamu

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        recyclerView = view.findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        userAdapter = UserAdapter(userList,
            onDeleteClick = { user -> deleteUser(user) },
            onRoleChangeClick = { user -> toggleRole(user) }
        )
        recyclerView.adapter = userAdapter

        fetchUsers()

        return view
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    user.uid = document.id
                    userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(user: User) {
        db.collection("users").document(user.uid).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Korisnik obrisan", Toast.LENGTH_SHORT).show()
                fetchUsers()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri brisanju", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleRole(user: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_role, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.roleRadioGroup)
        val radioUser = dialogView.findViewById<RadioButton>(R.id.radioUser)
        val radioAdmin = dialogView.findViewById<RadioButton>(R.id.radioAdmin)

        // Postavi trenutno označenu ulogu
        if (user.role == "admin") {
            radioAdmin.isChecked = true
        } else {
            radioUser.isChecked = true
        }

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Odustani", null)
            .setPositiveButton("Spremi") { _, _ ->
                val selectedRole = if (radioAdmin.isChecked) "admin" else "user"
                db.collection("users").document(user.uid)
                    .update("role", selectedRole)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Uloga promijenjena", Toast.LENGTH_SHORT).show()
                        fetchUsers()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Greška pri izmjeni uloge", Toast.LENGTH_SHORT).show()
                    }
            }
            .create()

        alertDialog.show()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
    }


}
