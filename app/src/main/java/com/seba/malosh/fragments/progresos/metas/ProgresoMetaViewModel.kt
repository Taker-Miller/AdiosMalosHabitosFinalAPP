import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProgresoMetaViewModel : ViewModel() {

    // Estado de los días (Completado/Fallido) usando un mapa
    val estadoDias = mutableMapOf<String, String>()

    // Fechas de inicio y fin de la meta
    private val _fechaInicio = MutableLiveData<Long>()
    val fechaInicio: LiveData<Long> get() = _fechaInicio

    private val _fechaFin = MutableLiveData<Long>()
    val fechaFin: LiveData<Long> get() = _fechaFin

    // Lista de hábitos
    private val _habitos = MutableLiveData<List<String>>()
    val habitos: LiveData<List<String>> get() = _habitos

    // Método para cargar los datos iniciales en el ViewModel
    fun cargarDatosIniciales(fechaInicio: Long, fechaFin: Long, habitos: List<String>) {
        _fechaInicio.value = fechaInicio
        _fechaFin.value = fechaFin
        _habitos.value = habitos
    }

    // Método para actualizar el estado de un día
    fun actualizarEstadoDia(fecha: String, estado: String) {
        estadoDias[fecha] = estado
    }
}
