package br.usp.ime.mac0447.datasetcollector.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.usp.ime.mac0447.datasetcollector.dto.ObjType
import br.usp.ime.mac0447.datasetcollector.service.ObjTypeService

class MainViewModel : ViewModel() {
    var objTypes: MutableLiveData<ArrayList<ObjType>> = MutableLiveData<ArrayList<ObjType>>()
    var objTypeService: ObjTypeService = ObjTypeService()

    init {
        fetchObjTypes("e")
    }

    fun fetchObjTypes(typeName: String) {
        objTypes = objTypeService.fetchObjTypes(typeName)
    }
}