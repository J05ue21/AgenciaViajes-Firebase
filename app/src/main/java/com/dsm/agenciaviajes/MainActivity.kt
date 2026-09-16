package com.dsm.agenciaviajes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsm.agenciaviajes.adapters.DestinoAdapter
import com.dsm.agenciaviajes.datos.Destino
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class MainActivity : AppCompatActivity()
{
    private lateinit var rvDestinos: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var adapter: DestinoAdapter

    private val listaDestinos = mutableListOf<Destino>()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("destinos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // configurar toolbar para habilitar el menú
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        rvDestinos = findViewById(R.id.rvDestinos)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAgregar = findViewById(R.id.fabAgregar)

        rvDestinos.layoutManager = LinearLayoutManager(this)
        adapter = DestinoAdapter(
            listaDestinos,
            onEditarClick = { destino -> abrirEditarDestino(destino) },
            onEliminarClick = { destino -> confirmarEliminacion(destino) }
        )
        rvDestinos.adapter = adapter

        fabAgregar.setOnClickListener {
            startActivity(Intent(this, AddDestinoActivity::class.java))
        }

        cargarDestinosDesdeFirebase()
    }

    // Mostar listado de destinos registrados en firebase
    private fun cargarDestinosDesdeFirebase()
    {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaDestinos.clear()
                for (data in snapshot.children) {
                    val destino = data.getValue(Destino::class.java)
                    if (destino != null) {
                        listaDestinos.add(destino)
                    }
                }
                adapter.notifyDataSetChanged()

                if (listaDestinos.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvDestinos.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvDestinos.visibility = View.VISIBLE
                }
            }

            // si hay un error al cargar los datos, se muestra un mensaje
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Editar un registro
    private fun abrirEditarDestino(destino: Destino) {
        val intent = Intent(this, AddDestinoActivity::class.java).apply {
            putExtra("id", destino.id)
            putExtra("nombre", destino.nombre)
            putExtra("pais", destino.pais)
            putExtra("precio", destino.precio)
            putExtra("descripcion", destino.descripcion)
            putExtra("imagenUrl", destino.imagenUrl)
        }
        startActivity(intent)
    }

    // Eliminar un registro después de confirmar
    private fun confirmarEliminacion(destino: Destino) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.titulo_confirmar_eliminacion))
            .setMessage(getString(R.string.seguro_eliminar, destino.nombre))
            .setPositiveButton(getString(R.string.button_eliminar_main_kt)) { _, _ ->
                destino.id?.let { key ->
                    database.child(key).removeValue().addOnSuccessListener {
                        Toast.makeText(this,
                            getString(R.string.mensaje_destino_eliminado), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.boton_para_cancelar), null) // No hace nada al hacer clic en "Cancelar"
            .show()
    }

    // cerrar Sesión
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            confirmarCierreSesion()
            true
        }
        else
        {
            super.onOptionsItemSelected(item)
        }
    }

    private fun confirmarCierreSesion()
    {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.button_cerrar_sesion))
            .setMessage(getString(R.string.confirmar_cerrar_sesion))
            .setPositiveButton(getString(R.string.si_confirma_salir)) { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.button_cancelar_cierre_sesion), null)
            .show()
    }

}