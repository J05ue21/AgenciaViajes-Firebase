package com.dsm.agenciaviajes

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dsm.agenciaviajes.datos.Destino
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddDestinoActivity : AppCompatActivity() {
    private lateinit var imgPreview: ImageView
    private lateinit var etNombre: EditText
    private lateinit var spPais: Spinner
    private lateinit var etPrecio: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var tvTitulo: TextView

    // los ? luego del nombre de la variable permite incluso valores "null"
    private var imagenUri: Uri? = null
    private var imagenUrlExistente: String? = null
    private var idDestinoEditar: String? = null

    // esta variable almacena la referencia a la base de datos de firebase
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("destinos")

    // Selector de imagen de la galería
    private val seleccionarImagenLanguage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null)    // impide ejecutar el codigo si la variable uri es null
        {
            imagenUri = uri
            imgPreview.setImageURI(uri) //muestra la imagen en el layout
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_destino)
        imgPreview = findViewById(R.id.imgPreview)
        etNombre = findViewById(R.id.etNombreDestino)
        spPais = findViewById(R.id.spPais)
        etPrecio = findViewById(R.id.etPrecioDestino)
        etDescripcion = findViewById(R.id.etDescripcionDestino)
        btnGuardar = findViewById(R.id.btnGuardarDestino)
        tvTitulo = findViewById(R.id.tvTituloFormulario)

        val btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)

        // Cargar adaptador de Recycler View y mostrar los paises
        ArrayAdapter.createFromResource(
            this,
            R.array.paises_array,
            android.R.layout.simple_spinner_item
        )
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPais.adapter = adapter
            }

        // boton para elegir imagen de la galeria
        btnSeleccionarImagen.setOnClickListener {
            seleccionarImagenLanguage.launch("image/*")
        }

        // verifica si se esta editando o agregando
        if (intent.hasExtra("id")) {
            idDestinoEditar = intent.getStringExtra("id")
            etNombre.setText(intent.getStringExtra("nombre"))
            etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
            etDescripcion.setText(intent.getStringExtra("descripcion"))
            imagenUrlExistente = intent.getStringExtra("imagenUrl")

            // selecciona el pais en el spinner (Recycler View)
            val paisIntent = intent.getStringExtra("pais")
            val adapter = spPais.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(paisIntent)
            if (position >= 0) spPais.setSelection(position)

            // mostrar imagen exixtente
            Glide.with(this).load(imagenUrlExistente).into(imgPreview)

            tvTitulo.text = getString(R.string.editar_destino)
            btnGuardar.text = getString(R.string.actualizar_destino_activity_kt)
        }
        btnGuardar.setOnClickListener { validarYGuardar() }
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString().trim()
        val pais = spPais.selectedItem.toString()
        val precioStr = etPrecio.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        // Validaciones de campos de entrada ---------------------------------------

        // es necesario ingresar nombre Destino
        if (nombre.isEmpty()) {
            etNombre.error = getString(R.string.nombre_destino_activity_kt)
            return
        }

        // es necesario ingresar precio
        if (precioStr.isEmpty()) {
            etPrecio.error = getString(R.string.ingrese_precio_destinoactivity_kt)
            return
        }

        // el precio debe ser mayor a 0
        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            etPrecio.error = getString(R.string.validar_precio_mayor_0)
            return
        }

        // se pide agregar una descripcion
        if (descripcion.length < 20) {
            etDescripcion.error =
                "La descripción debe tener al menos 20 caracteres (${descripcion.length}/20)"
            return
        }

        //debe agregar una imagen para el registro
        if (imagenUri == null && imagenUrlExistente.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.debe_seleccionar_imagen), Toast.LENGTH_SHORT)
                .show()
            return
        }
        // Fin validaciones ----------------------------------------------------------------------

        btnGuardar.isEnabled = false // deshabilitar boton para evitar multiples clicks

        // Guardar ruta local si se seleccionó nueva imagen, o conservar la existente
        val rutaImagenFinal = if (imagenUri != null) {
            guardarImagenLocalmente(imagenUri!!)
        } else {
            imagenUrlExistente
        }

        if (rutaImagenFinal != null) {
            guardarEnRealtimeDB(nombre, pais, precio, descripcion, rutaImagenFinal)
        } else {
            btnGuardar.isEnabled = true
            Toast.makeText(this, getString(R.string.error_al_guardar_imagen), Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarImagenLocalmente(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val archivoLocal = java.io.File(filesDir, "${UUID.randomUUID()}.jpg")
            val outputStream = java.io.FileOutputStream(archivoLocal)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            archivoLocal.absolutePath // devuelve la ruta interna /data/user/0/...
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun guardarEnRealtimeDB(
        nombre: String,
        pais: String,
        precio: Double,
        descripcion: String,
        imagenUrl: String
    ) {
        val key = idDestinoEditar ?: database.push().key

        if (key != null) {
            val destino = Destino(key, nombre, pais, precio, descripcion, imagenUrl)
            database.child(key).setValue(destino)
                .addOnSuccessListener {
                    Toast.makeText(this,
                        getString(R.string.destino_guardado_exito), Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnGuardar.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.error_al_guardar, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}