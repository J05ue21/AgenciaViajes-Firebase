package com.dsm.agenciaviajes.datos

data class Destino(
    var id: String? = null,
    var nombre: String? = null,
    var pais: String? = null,
    var precio: Double? = null,
    var descripcion: String? = null,
    var imagenUrl: String? = null
)
