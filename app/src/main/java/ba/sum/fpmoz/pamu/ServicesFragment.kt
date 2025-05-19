package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.servicesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServiceAdapter(serviceList) { service ->
            // Klik na uslugu (nije obavezno)
            Toast.makeText(requireContext(), "Klik: ${service.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = serviceAdapter

        val fab = view.findViewById<FloatingActionButton>(R.id.addServiceFab)
        fab.setOnClickListener {
            val intent = Intent(requireContext(), AddServiceActivity::class.java)
            startActivity(intent)
        }

        fetchServices()

        return view
    }

    private fun fetchServices() {
        db.collection("services").get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: "N/A"
                    val description = document.getString("description") ?: ""
                    val imageRes = R.drawable.balayage // možeš ovo kasnije zamijeniti pravom slikom
                    serviceList.add(Service(name, description, imageRes))
                }
                serviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri učitavanju usluga", Toast.LENGTH_SHORT).show()
            }
    }
}
