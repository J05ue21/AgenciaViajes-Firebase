package com.dsm.agenciaviajes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsm.agenciaviajes.R
import com.dsm.agenciaviajes.datos.Destino  // paquete --> data

class DestinoAdapter(private val listaDestinos: List<Destino>,
                     private val onEditarClick: (Destino) -> Unit,
                     private val onEliminarClick: (Destino) -> Unit
                    ) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>()

{
    class DestinoViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        val imgDestino : ImageView = itemView.findViewById(R.id.imgDestino)
        val tvNombre : TextView = itemView.findViewById(R.id.tvNombreDestino)
        val tvPais : TextView = itemView.findViewById(R.id.tvPaisDestino)
        val tvPrecio : TextView = itemView.findViewById(R.id.tvPrecioDestino)
        val tvDescripcion : TextView = itemView.findViewById(R.id.tvDescripcionDestino)
        val btnEditar : Button = itemView.findViewById(R.id.btnEditar)
        val btnEliminar : Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder
    {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaDestinos[position]

        holder.tvNombre.text = destino.nombre
        holder.tvPais.text = destino.pais
        holder.tvPrecio.text = "$${String.format("%.2f", destino.precio ?: 0.0)}"   // formato de dos decimales
        holder.tvDescripcion.text = destino.descripcion

        // Cargar la imagen desde Firebase Storage con glide
        Glide.with(holder.itemView.context)
            .load(destino.imagenUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.imgDestino)
        holder.btnEditar.setOnClickListener { onEditarClick(destino) }
        holder.btnEliminar.setOnClickListener { onEliminarClick(destino) }
    }

    override fun getItemCount(): Int = listaDestinos.size
}