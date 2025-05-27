package ba.sum.fpmoz.pamu

import ba.sum.fpmoz.pamu.Service
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class ServiceAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val isAdmin: Boolean = true
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {


    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.serviceNameTextView)
        val descText: TextView = itemView.findViewById(R.id.serviceDescriptionTextView)
        val imageView: ImageView = itemView.findViewById(R.id.serviceImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.nameText.text = service.name
        holder.descText.text = service.description
        holder.imageView.setImageResource(service.imageResId)

        holder.itemView.setOnClickListener {
            if (isAdmin) {
                val context = holder.itemView.context
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Opcije za '${service.name}'")
                    .setItems(arrayOf("Uredi", "Obriši")) { _, which ->
                        when (which) {
                            0 -> onEditClick(service)
                            1 -> onDeleteClick(service)
                        }
                    }
                    .show()
            }
        }
    }


    override fun getItemCount() = services.size
}
