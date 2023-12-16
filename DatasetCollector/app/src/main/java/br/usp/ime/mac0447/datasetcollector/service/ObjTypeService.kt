package br.usp.ime.mac0447.datasetcollector.service

import androidx.lifecycle.MutableLiveData
import br.usp.ime.mac0447.datasetcollector.dto.ObjType

class ObjTypeService {
    fun fetchObjTypes(typeName: String): MutableLiveData<ArrayList<ObjType>> {
        var _objTypes = MutableLiveData<ArrayList<ObjType>>()

        _objTypes.value = arrayListOf(
            ObjType("Banana"),
            ObjType("Camiseta"),
            ObjType("Poste")
        )

        return _objTypes
    }
}