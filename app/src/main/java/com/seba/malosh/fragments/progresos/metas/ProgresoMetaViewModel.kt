import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProgresoMetaViewModel : ViewModel() {


    val estadoDias = mutableMapOf<String, String>()


    private val _fechaInicio = MutableLiveData<Long>()
    val fechaInicio: LiveData<Long> get() = _fechaInicio

    private val _fechaFin = MutableLiveData<Long>()
    val fechaFin: LiveData<Long> get() = _fechaFin


    private val _habitos = MutableLiveData<List<String>>()
    val habitos: LiveData<List<String>> get() = _habitos


    fun cargarDatosIniciales(fechaInicio: Long, fechaFin: Long, habitos: List<String>) {
        _fechaInicio.value = fechaInicio
        _fechaFin.value = fechaFin
        _habitos.value = habitos
    }


    fun actualizarEstadoDia(fecha: String, estado: String) {
        estadoDias[fecha] = estado
    }
}
