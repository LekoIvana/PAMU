package ba.sum.fpmoz.pamu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val options = arrayOf("user", "admin")
        val currentRoleIndex = if (user.role == "admin") 1 else 0

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Odaberi novu ulogu za ${user.userName}")
        builder.setSingleChoiceItems(options, currentRoleIndex) { dialog, which ->
            val selectedRole = options[which]
            db.collection("users").document(user.uid)
                .update("role", selectedRole)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Uloga promijenjena", Toast.LENGTH_SHORT).show()
                    fetchUsers()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri izmjeni uloge", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        builder.setNegativeButton("Odustani") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

}
