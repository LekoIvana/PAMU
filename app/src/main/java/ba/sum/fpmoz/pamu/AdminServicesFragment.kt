package ba.sum.fpmoz.pamu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminServicesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = mutableListOf<Service>()
    private val db = FirebaseFirestore.getInstance()

    // ✅ Launcher za dobivanje rezultata iz EditServiceActivity
    private val editServiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            loadServicesFromFirestore()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_services, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewServices)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        serviceAdapter = ServiceAdapter(
            services = serviceList,
            onEditClick = { service -> openEditActivity(service) },
            onDeleteClick = { service -> confirmDelete(service) }
        )
        recyclerView.adapter = serviceAdapter

        loadServicesFromFirestore()

        return view
    }

    private fun loadServicesFromFirestore() {
        db.collection("services")
            .get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val id = document.id

                    val imageRes = when (name.lowercase()) {
                        "šišanje" -> R.drawable.balayage
                        "balayage" -> R.drawable.balayage
                        else -> R.drawable.balayage
                    }

                    val service = Service(
                        id = id,
                        name = name,
                        description = description,
                        imageResId = imageRes
                    )
                    serviceList.add(service)
                }
                serviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju usluga", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openEditActivity(service: Service) {
        val intent = Intent(requireContext(), EditServiceActivity::class.java)
        intent.putExtra("SERVICE_ID", service.id)
        intent.putExtra("SERVICE_NAME", service.name)
        intent.putExtra("SERVICE_DESC", service.description)
        intent.putExtra("SERVICE_IMAGE", service.imageResId)
        editServiceLauncher.launch(intent) // ✅ zamijenjeno startActivity()
    }

    private fun confirmDelete(service: Service) {
        AlertDialog.Builder(requireContext())
            .setTitle("Brisanje usluge")
            .setMessage("Želite li obrisati '${service.name}'?")
            .setPositiveButton("Da") { _, _ -> deleteService(service) }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun deleteService(service: Service) {
        db.collection("services").document(service.id)
            .delete()
            .addOnSuccessListener {
                serviceList.remove(service)
                serviceAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Usluga obrisana", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri brisanju", Toast.LENGTH_SHORT).show()
            }
    }
}
