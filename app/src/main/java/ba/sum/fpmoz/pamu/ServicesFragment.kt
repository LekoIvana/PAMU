package ba.sum.fpmoz.pamu

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ServicesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = mutableListOf<Service>()
    private val db = FirebaseFirestore.getInstance()

    // ✅ ActivityResultLauncher za osvježavanje nakon uređivanja
    private val editServiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            fetchServices()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.servicesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        serviceAdapter = ServiceAdapter(
            services = serviceList,
            onEditClick = { service ->
                val intent = Intent(requireContext(), EditServiceActivity::class.java)
                intent.putExtra("SERVICE_ID", service.id)
                intent.putExtra("SERVICE_NAME", service.name)
                intent.putExtra("SERVICE_DESC", service.description)
                intent.putExtra("SERVICE_IMAGE", service.imageResId)
                editServiceLauncher.launch(intent)
            },
            onDeleteClick = { service ->
                db.collection("services").document(service.id)
                    .delete()
                    .addOnSuccessListener {
                        serviceList.remove(service)
                        serviceAdapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Usluga obrisana", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Greška pri brisanju usluge", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        recyclerView.adapter = serviceAdapter

        val fab = view.findViewById<FloatingActionButton>(R.id.addServiceFab)
        fab.setOnClickListener {
            startActivity(Intent(requireContext(), AddServiceActivity::class.java))
        }

        fetchServices()

        return view
    }

    private fun fetchServices() {
        db.collection("services").get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (document in result) {
                    val id = document.id
                    val name = document.getString("name") ?: "N/A"
                    val description = document.getString("description") ?: ""
                    val imageRes = R.drawable.balayage
                    serviceList.add(Service(id = id, name = name, description = description, imageResId = imageRes))
                }
                serviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri učitavanju usluga", Toast.LENGTH_SHORT).show()
            }
    }
}