package br.usp.ime.mac0447.datasetcollector.dto

data class ObjType(var typeName: String, var typeId: Int = 0) {
    override fun toString(): String {
        return typeName
    }
}