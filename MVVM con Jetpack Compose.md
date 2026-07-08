MVVM (Model-View-ViewModel) es un patrón de arquitectura de software que separa la lógica de negocio de la interfaz de usuario. Su objetivo principal es desacoplar la vista (UI) del modelo (datos) a través de un intermediario llamado ViewModel , lo que facilita el mantenimiento, las pruebas y la escalabilidad de la aplicación. 

 Ideaclave: La Vista solo se preocupa por mostrar información y capturar eventos del usuario. El ViewModel contiene toda la lógica de presentación y expone el estado al que la Vista reacciona. El Modelo es la capa de datos (base de datos, API, preferencias, etc.). 

En el ecosistema Android, MVVM es el patrón recomendado por Google, ya que se integra de forma natural con Jetpack Compose y componentes como ViewModel, LiveData y StateFlow. 



**----- Start of picture text -----**<br>
Eventos del usuario Solicita datos<br>(Jetpack Compose) (State holder) (Data layer)<br>• Muestra el estado • Expone estado (StateFlow) • API / Base de datos<br>Estado (StateFlow) Respuesta / Datos<br>• Captura eventos • Contiene lógica de negocio • Repositorio<br>• Composable functions • viewModelScope * Retrofit/Room<br>• collectAsState() • MutableStateFlow • Repository pattern<br>**----- End of picture text -----**<br>


Flujo unidireccional: La  View  envía  eventos → el  ViewModel  procesa  y  actualiza  el  estado → la  View  se  recompone automáticamente. 

Ventajas de usar MVVM con Jetpack Compose:

- Separacion de responsabilidades: La UI (Compose) se enfoca en la presentación, el ViewModel en la lógica y el Modelo en los datos. 

- Persistencia ante cambios de configuracion: El ViewModel retiene el estado automáticamente al rotar la pantalla o cambiar de configuracion.

- Estado reactivo: Al usar StateFlow o LiveData, la UI se actualiza de forma automática y eficiente.

- Testabilidad: El ViewModel se puede probar de forma aislada sin depender de la UI o del contexto de Android. 

- Escalabilidad: Facilita la incorporación de nuevas funcionalidades sin afectar otras capas. 

- Integracion nativa: Jetpack Compose está diseñado para trabajar con ViewModel, ofreciendo funciones como 

viewModel() y collectAsState(). 

Ejemplo Práctico: App de lista de usuarios:

Construiremos una app que simula la carga de una lista de usuarios desde una API. El ejemplo sigue la arquitectura MVVM y utiliza Jetpack Compose para la UI y Kotlin Coroutines para operaciones asíncronas. 

1. Modelo de datos.

Crea la clase User en model/User.kt: 

package com.example.app.model 

data class User( val id: Int, val name: String, val email: String ) 

2. Repositorio (Modelo).

Simula una fuente de datos en repository/UserRepository.kt: 

package com.example.app.repository 
import com.example.app.model.User 
import kotlinx.coroutines.delay 

class UserRepository { 
   suspend fun getUsers(): List<User> { 
      // Simula una llamada a API con retraso 
      delay(2000) 
      return listOf( 
         User(1, "Ana García", "ana@email.com"), 
         User(2, "Luis Pérez", "luis@email.com"), 
         User(3, "Marta Díaz", "marta@email.com") ) } } 

3. ViewModel con estado.

Define el estado de la UI y el ViewModel en viewmodel/UserViewModel.kt: 

package com.example.app.viewmodel 

import androidx.lifecycle.ViewModel 
import androidx.lifecycle.viewModelScope 
import com.example.app.model.User 
import com.example.app.repository.UserRepository 
import kotlinx.coroutines.flow.MutableStateFlow 
import kotlinx.coroutines.flow.StateFlow 
import kotlinx.coroutines.launch 

// Estado sellado para representar los distintos estados de la UI 

sealed class UiState { 

object Loading : UiState() 
data class Success(val users: List<User>) : UiState() 

data class Error(val message: String) : UiState() 

} 

class UserViewModel( 

private val repository: UserRepository = UserRepository()
) : ViewModel() { 
private val _uiState = MutableStateFlow<UiState>(UiState.Loading) 
val uiState: StateFlow<UiState> = _uiState 



init { 
   loadUsers() 

} 

fun loadUsers() { 

viewModelScope.launch { 

_uiState.value = UiState.Loading 

try { 

val users = repository.getUsers() 
_uiState.value = UiState.Success(users) 

} catch (e: Exception) { 

_uiState.value = UiState.Error(e.message ?: "Error desconocido") 

} 

} 

} 
} 

# Nota: El UiState es una sealed class que representa de forma exhaustiva todos los posibles estados de la pantalla: Cargando, Éxito y Error. 

4. UI con Jetpack Compose

En MainActivity.kt, crea la pantalla que reacciona al estado del ViewModel: 

package com.example.app 

import android.os.Bundle 
import androidx.activity.ComponentActivity 
import androidx.activity.compose.setContent 
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn 
import androidx.compose.foundation.lazy.items 
import androidx.compose.material3.* 
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment 
import androidx.compose.ui.Modifier 
import androidx.compose.ui.unit.dp 
import androidx.lifecycle.viewmodel.compose.viewModel 
import com.example.app.ui.theme.AppTheme 
import com.example.app.viewmodel.UiState 
import com.example.app.viewmodel.UserViewModel 

class MainActivity : ComponentActivity() { 
   override fun onCreate(savedInstanceState: Bundle?) { 
      super.onCreate(savedInstanceState) 
      setContent { 
         AppTheme { 
            Surface(modifier = Modifier.fillMaxSize()) { 
               UserListScreen() 
               } 
               } 
               } 
               } 
               } 
               
               @Composable 
               fun UserListScreen( 
                  viewModel: UserViewModel = viewModel()
               ) { 
                  val uiState by viewModel.uiState.collectAsState() 

                  when (uiState) { 
                     is UiState.Loading -> { 
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() 
                        } 
                        } 
                        is UiState.Success -> { 
                           LazyColumn { 
                              items(uiState.users) { user -> 

                              UserItem(user = user) 

} 

} 

} 
is UiState.Error -> { 
   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 

Column(horizontalAlignment = Alignment.CenterHorizontally) { 

Text(text = " Error: ${uiState.message}", color = MaterialTheme.colorScheme.error) 
Spacer(modifier = Modifier.height(8.dp)) 
Button(onClick = { viewModel.loadUsers() }) { 
   Text("Reintentar") 

} 
} 
} 
} 
} 
} 

@Composable fun UserItem(user: com.example.app.model.User) { 
   Card( 
      modifier = Modifier .fillMaxWidth() .padding(8.dp), 
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) 
      ) { 
         Column(modifier = Modifier.padding(16.dp)) { 



**----- Start of picture text -----**<br>
                                Text(text = user.name, style =<br>MaterialTheme.typography.titleMedium)
                                Text(text = user.email, style =<br>MaterialTheme.typography.bodyMedium)
                                }
                                }
                                }

5. Agregar dependencias
<br>En build.gradle.kts (módulo app), asegúrate de incluir:
// ViewModel y StateFlow<br>                    
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")<br>                    
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")<br>
// Coroutines<br>                    
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")<br>

**----- End of picture text -----**<br>

ESTRUCTURA DEL PROYECTO EN ANDROID STUDIO

Una vez creados todos los archivos del ejemplo anterior, la estructura de carpetas y archivos dentro de app/src/main/java/com/example/app/ debe verse así. Esta organización refleja fielmente la separación de capas propuesta por MVVM:

**app** 
└──  **src**
      └──  **main** 
            ├──  **java**    
            |      └──  **com**        
            |            └──  **example**            
            |                  └──  **app** // Paquete raíz de la app │               
            |                        ├──  MainActivity.kt // (VIEW) Pantalla principal con Compose                 │               
            |                        ├──  **model** // Capa de MODELO (datos)                   
            |                              └──  User.kt // Data class de usuario                
            |                        │ 
            |                        │                 
            |                        ├──  **repository** // Capa de MODELO (fuentes de datos) 
            |                        │    └──  UserRepository.kt // Simula la obtención de usuarios                 
            |                        │     
            |                        ├──  **viewmodel** // Capa de VIEWMODEL (lógica de UI) 
            |                        │   └──  UserViewModel.kt // StateFlow, UiState y loadUsers() 
            |                        │               
            |                        └──  **ui** // (Opcional) Temas y estilos de Compose 
            |                        │    └──  **theme**                        
            |                                 ├──  Color.kt 
            |                                 ├──  Theme.kt 
            |                                 └──  Type.kt  
            |
            |
            ├── **res** // Recursos (drawables, valores, etc.) 
            │   ├── o **drawable** 
            │   ├── o **values** 
            │   └── ... 
            │ 
            └──  AndroidManifest.xml // Permisos y configuración de la app 

Explicacion de cada carpeta:

- **model/** : Contiene las clases de datos (data classes) que representan las entidades de la aplicación (ej. User, Post, Product). Son objetos simples (POJOs) sin lógica de negocio. 

- **repository/** : Actúa como la fuente única de verdad para los datos. Aquí se decide si los datos vienen de una API (Retrofit), de una base de datos local (Room) o de memoria caché. Es el punto de acceso al Modelo.

- **viewmodel/** : Contiene las clases ViewModel que exponen el estado de la Ul (StateFlow) y manejan la lógica de presentación. Nunca deben tener referencias a la Vista (Activities/Fragments/Composables). 

- **ui/theme/** : (Generado automáticamente al crear el proyecto) Define los colores, tipografías y formas del tema de Jetpack Compose. 

- **MainActivity.kt** : Es el punto de entrada de la UI. En Compose, actúa como el contenedor o View que configura el tema y la pantalla inicial, y observa los estados del ViewModel. 

- **AndroidManifest.xml** : Archivo de configuracion donde se declaran los permisos (ej. INTERNET) y la actividad principal. 

# Buenas practicas: Mantén esta estructura organizada desde el inicio. A medida que la app crezca, podrías agregar carpetas como data/ (para API y DB), di/ (para inyección de dependencias) o utils/ (para extensiones y utilidades), pero la base de model , repository , viewmodel y UI siempre se mantiene. 

Flujo de trabajo MVVM con Compose:

1. El usuario interactua con la UI (ej. presiona un botón). 

2. La View (Composable) invoca una función del ViewModel.

3. El ViewModel actualiza el estado (ej. _uiState.value = UiState.Loading). 

4. La View observa el estado mediante collectAsState() y se recompone automáticamente. 

5. El ViewModel realiza la operación asíncrona (llamada a API, BD, etc.) usando viewModelScope. 

6. Al finalizar, el ViewModel actualiza el estado con el resultado (éxito o error). 

7. La View refleja el nuevo estado de forma inmediata.

# Ventaja clave: Este flujo es unidireccional y reactivo. La Ul nunca modifica los datos directamente; solo envia eventos y reacciona a los cambios de estado. Esto hace que el código sea más predecible y fácil de depurar.  

Actividades de Aplicación.

1. Amplía el ejemplo: Agrega un campo de búsqueda que filtre los usuarios por nombre en tiempo real.

2. Persistencia: Integra Room para almacenar los usuarios localmente y mostrar datos cacheados cuando no haya conexión. 

3. Navegacion: Implementa una segunda pantalla que muestre el detalle de un usuario al hacer clic en un elemento de la lista, usando NavController de Compose. 

4. Pruebas: Escribe una prueba unitaria para el ViewModel utilizando MockK , simulando el repositorio. 

5. Manejo de errores avanzado: Diferencia entre errores de red, timeout y errores del servidor, mostrando mensajes específicos.