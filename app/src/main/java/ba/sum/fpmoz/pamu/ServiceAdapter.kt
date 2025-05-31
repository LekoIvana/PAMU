package ba.sum.fpmoz.pamu

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.pamu.R
import kotlin.concurrent.thread
import java.net.URL

class ServiceAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val isAdmin: Boolean = true,
    private val onItemClick: ((Service) -> Unit)? = null
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

        val imageUrl = service.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            // Učitavanje slike iz URL-a u pozadinskoj niti
            thread {
                try {
                    val inputStream = URL(imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    Handler(Looper.getMainLooper()).post {
                        holder.imageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post {
                        // Ako ne uspije učitati, prikaži balayage sliku iz drawable
                        holder.imageView.setImageResource(R.drawable.balayage)
                    }
                }
            }
        } else {
            // Ako nema URL slike, prikaži balayage
            holder.imageView.setImageResource(R.drawable.balayage)
        }

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
