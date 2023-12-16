package br.usp.ime.mac0447.datasetcollector.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.usp.ime.mac0447.datasetcollector.dto.ObjType
import br.usp.ime.mac0447.datasetcollector.service.ObjTypeService

class MainViewModel : ViewModel() {
    private var _objTypes: MutableLiveData<ArrayList<ObjType>> = MutableLiveData<ArrayList<ObjType>>()
    var objTypeService: ObjTypeService = ObjTypeService()

    init {
        fetchObjTypes("e")
    }

    fun fetchObjTypes(typeName: String) {
        _objTypes = objTypeService.fetchObjTypes(typeName)
    }

    internal var objTypes: MutableLiveData<ArrayList<ObjType>>
        get() { return _objTypes }
        set(value) { _objTypes = value }
}