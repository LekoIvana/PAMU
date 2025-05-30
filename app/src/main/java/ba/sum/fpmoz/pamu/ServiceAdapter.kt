package ba.sum.fpmoz.pamu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val isAdmin: Boolean = true,
    private val onItemClick: ((Service) -> Unit)? = null // klik za obične korisnike
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
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_service_options, null)

                val titleText = dialogView.findViewById<TextView>(R.id.dialogTitle)
                val editOption = dialogView.findViewById<TextView>(R.id.optionEdit)
                val deleteOption = dialogView.findViewById<TextView>(R.id.optionDelete)

                titleText.text = "Opcije za '${service.name}'"

                val dialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create()

                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

                editOption.setOnClickListener {
                    dialog.dismiss()
                    onEditClick(service)
                }

                deleteOption.setOnClickListener {
                    dialog.dismiss()
                    onDeleteClick(service)
                }

                dialog.show()
            } else {
                onItemClick?.invoke(service)
            }
        }
    }

    override fun getItemCount() = services.size
}
