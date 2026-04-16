package com.example.tfgprueba1.pantallas

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulBarraBusqueda
import com.example.tfgprueba1.ui.theme.AzulCalorias
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.GrisFuerte
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.util.concurrent.TimeUnit



data class OFFResponse(
    val products: List<OFFProduct> = emptyList()
)

data class OFFProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_es") val productNameEs: String?,
    val brands: String?,
    val quantity: String?,
    val nutriments: OFFNutriments?
)

data class OFFNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("proteins_100g") val proteins: Double?,
    @SerializedName("fat_100g") val fat: Double?
)



interface OpenFoodFactsApi {
    @GET(
        "cgi/search.pl?" +
                "json=1" +
                "&action=process" +
                "&sort_by=unique_scans_n" +
                "&countries_tags=en:spain" +
                "&fields=product_name,product_name_es,brands,quantity,nutriments"
    )
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 12,
        @Query("page") page: Int = 1
    ): OFFResponse
}


object OpenFoodFactsClient {

    fun create(context: Context): OpenFoodFactsApi {
        val cacheSize = 20L * 1024 * 1024 // 20 MB
        val cache = Cache(File(context.cacheDir, "off_cache_v2"), cacheSize)

        val client = OkHttpClient.Builder()
            // Timeouts más conservadores para móviles
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(cache)
            // DNS fallback si el del ISP falla
            .dns(CustomDns())
            // Logging (útil para debug, cambiar a NONE en producción)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            // User-Agent obligatorio para OpenFoodFacts
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "TFGNutricionApp/1.0 (android; contacto@tuapp.com)")
                    .build()
                chain.proceed(request)
            }

            // Retry automático con exponential backoff
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            // Cache offline: sirve datos antiguos si no hay red
            .addInterceptor(OfflineCacheInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    // Interceptor de reintentos con backoff exponencial
    class RetryInterceptor(private val maxRetries: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            var lastException: IOException? = null

            repeat(maxRetries) { attempt ->
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful || !shouldRetry(response.code)) {
                        return response
                    }
                    response.close()
                } catch (e: IOException) {
                    lastException = e
                    Log.w("OFFApi", "Intento ${attempt + 1} fallido: ${e.message}")
                    if (attempt < maxRetries - 1) {
                        Thread.sleep((10000L * (attempt + 1))) // 1s, 2s, 3s...
                    }
                }
            }
            throw lastException ?: IOException("Max retries reached")
        }

        fun shouldRetry(code: Int): Boolean {
            return code in 500..599 || code == 408 || code == 429
        }
    }

    // Interceptor para cache offline
    class OfflineCacheInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var request = chain.request()

            if (!isNetworkAvailable(context)) {
                // Sin red: usar solo cache (hasta 7 días)
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=604800")
                    .build()
            } else {
                // Con red: cache normal de 5 minutos
                request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .build()
            }

            return chain.proceed(request)
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    // DNS con fallback
    class CustomDns : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (e: UnknownHostException) {
                Log.w("OFFApi", "DNS del sistema falló, usando fallback para: $hostname")
                try {
                    listOf(InetAddress.getByName(hostname))
                } catch (e2: Exception) {
                    throw e
                }
            }
        }
    }
}



sealed class FoodResult {
    data class Success(val foods: List<DatosAlimento>) : FoodResult()
    data class Error(val message: String) : FoodResult()
    data object Empty : FoodResult()
}

class FoodRepository(private val api: OpenFoodFactsApi) {

    suspend fun searchFoods(
        query: String,
        page: Int = 1,
        momentoComida: String = "Sin clasificar" // Parámetro por defecto
    ): FoodResult {
        val searchTerm = query.trim().ifEmpty { "leche pan arroz pollo" }

        return try {
            val response = api.searchProducts(query = searchTerm, page = page)

            val alimentos = response.products
                .filter { isUsable(it) }
                .map { mapToDatosAlimento(it, momentoComida) } // <-- Pasa el momento
                .distinctBy { it.nombre }
                .sortedBy { it.nombre }

            if (alimentos.isEmpty()) FoodResult.Empty
            else FoodResult.Success(alimentos)

        } catch (e: UnknownHostException) {
            FoodResult.Error("Sin conexión a internet")
        } catch (e: SocketTimeoutException) {
            FoodResult.Error("Tiempo de espera agotado. Inténtalo de nuevo.")
        } catch (e: HttpException) {
            when (e.code()) {
                403 -> FoodResult.Error("Acceso denegado por el servidor (403)")
                429 -> FoodResult.Error("Demasiadas peticiones. Espera un momento.")
                else -> FoodResult.Error("Error del servidor (${e.code()})")
            }
        } catch (e: IOException) {
            FoodResult.Error("Error de red: ${e.message}")
        } catch (e: Exception) {
            FoodResult.Error("Error inesperado: ${e.javaClass.simpleName}")
        }
    }

    fun isUsable(prod: OFFProduct): Boolean {
        val nombre = prod.productNameEs?.takeIf { it.isNotBlank() }
            ?: prod.productName?.takeIf { it.isNotBlank() }
            ?: return false
        if (nombre.contains("sin nombre", ignoreCase = true)) return false
        val n = prod.nutriments
        return (n?.energyKcal ?: 0.0) > 0 || (n?.proteins ?: 0.0) > 0
    }

    // Ahora acepta momentoComida como parámetro
    private fun mapToDatosAlimento(
        prod: OFFProduct,
        momentoComida: String
    ): DatosAlimento {
        val n = prod.nutriments

        val nombre = (prod.productNameEs?.takeIf { it.isNotBlank() }
            ?: prod.productName?.takeIf { it.isNotBlank() }
            ?: "Producto sin nombre")
            .split(",").first().trim()
            .replaceFirstChar { it.uppercase() }

        val marca = prod.brands
            ?.split(",")?.firstOrNull()?.trim()
            ?.replaceFirstChar { it.uppercase() } ?: ""

        val esLiquido = prod.quantity?.let {
            it.contains("ml", ignoreCase = true) ||
                    it.contains("cl", ignoreCase = true) ||
                    it.contains("l", ignoreCase = true)
        } ?: false
        val medida = if (esLiquido) "ml" else "g"

        return DatosAlimento(
            nombre = nombre,
            cantidadMedida = 100.0,
            cantidadAlimento = 100.0,
            calorias = redondear2(n?.energyKcal ?: 0.0),
            carbohidratos = redondear2(n?.carbs ?: 0.0),
            proteinas = redondear2(n?.proteins ?: 0.0),
            grasas = redondear2(n?.fat ?: 0.0),
            fecha = LocalDate.now().toString(),
            momentoComida = momentoComida, // <-- Usa el parámetro recibido
            marca = marca,
            medida = medida
        )
    }

    fun redondear2(value: Double): Double =
        String.format("%.2f", value).replace(",", ".").toDoubleOrNull() ?: value
}



data class FoodSearchUiState(
    val searchQuery: String = "",
    val foods: List<DatosAlimento> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false
)

@OptIn(FlowPreview::class)
class FoodSearchViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FoodRepository(
        api = OpenFoodFactsClient.create(application)
    )

    private val _uiState = MutableStateFlow(FoodSearchUiState())
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    // Define aquí el momento de comida por defecto o hazlo configurable
    private val momentoComidaDefault = "Comida" // o "Desayuno", "Cena", etc.

    var searchJob: Job? = null
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        setupSearchFlow()
        loadInitialFoods()
    }

    fun setupSearchFlow() {
        _searchQuery
            .debounce(400L)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { query -> searchFoods(query) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query

        if (query.isBlank()) {
            searchJob?.cancel()
            loadInitialFoods()
        }
    }

    fun retry() {
        searchJob?.cancel()
        val q = _uiState.value.searchQuery
        if (q.isBlank()) loadInitialFoods() else searchFoods(q)
    }

    private fun loadInitialFoods() {
        executeSearch("")
    }

    fun searchFoods(query: String) {
        executeSearch(query)
    }

    private fun executeSearch(query: String) {
        searchJob?.cancel()

        val isOffline = !isNetworkAvailable(getApplication())
        _uiState.update { it.copy(isOffline = isOffline) }

        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, isEmpty = false)
            }

            try {
                // Pasa el momentoComida aquí
                val result = withTimeout(25000) {
                    repository.searchFoods(
                        query = query,
                        momentoComida = momentoComidaDefault // <-- Aquí pasas el valor
                    )
                }

                if (isActive) {
                    applyResult(result)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                if (isActive) {
                    applyResult(FoodResult.Error("La búsqueda tardó demasiado"))
                }
            } catch (e: CancellationException) {
                Log.d("FoodSearch", "Búsqueda cancelada intencionalmente")
            } catch (e: Exception) {
                if (isActive) {
                    Log.e("FoodSearch", "Error en búsqueda", e)
                    applyResult(FoodResult.Error("Error de conexión. Intenta de nuevo."))
                }
            }
        }
    }

    fun applyResult(result: FoodResult) {
        _uiState.update {
            when (result) {
                is FoodResult.Success -> it.copy(
                    foods = result.foods,
                    isLoading = false,
                    isEmpty = false,
                    errorMessage = null
                )
                is FoodResult.Error -> it.copy(
                    isLoading = false,
                    errorMessage = result.message,
                    isEmpty = false
                )
                is FoodResult.Empty -> it.copy(
                    isLoading = false,
                    isEmpty = true,
                    foods = emptyList(),
                    errorMessage = null
                )
            }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}


@Composable
fun ShimmerFoodList(itemCount: Int = 10) {
    LazyColumn {
        items(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(180, 14)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(110, 11)
                }
                Spacer(Modifier.width(16.dp))
                ShimmerBox(50, 14)
            }
            HorizontalDivider(color = GrisFuerte, thickness = 0.5.dp)
        }
    }
}

@Composable
fun ShimmerBox(width: Int, height: Int) {
    Box(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
    )
}



@Composable
fun FeedbackState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = GrisFuerte, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, color = NegroPuro, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = GrisFuerte, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}



object BibliotecaAlimentosScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: FoodSearchViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        val listState = rememberLazyListState()
        var selectedTabIndex by remember { mutableStateOf(1) }
        val context = LocalContext.current

        // Verificar estado de red actual
        var isNetworkAvailable by remember { mutableStateOf(checkNetwork(context)) }

        // Monitorear cambios de red
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000) // Verificar cada 3 segundos
                val currentState = checkNetwork(context)
                if (currentState != isNetworkAvailable) {
                    isNetworkAvailable = currentState
                }
            }
        }

        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text("Biblioteca Global", color = BlancoPuro, fontWeight = FontWeight.Bold)
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(AzulOscuroApp)
                            .padding(0.dp)
                    ) {
                        // ── Barra de búsqueda ─────────────────────────────────────
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 0.dp, top = 0.dp, start = 16.dp, end = 16.dp),
                            shape = RoundedCornerShape(15.dp),
                            placeholder = { Text("Buscar alimento...", color = GrisFuerte) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = NegroPuro) },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = uiState.searchQuery.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.SearchOff, "Limpiar", tint = GrisFuerte)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AzulBarraBusqueda,
                                unfocusedContainerColor = AzulBarraBusqueda,
                                focusedTextColor = NegroPuro,
                                unfocusedTextColor = NegroPuro,
                                cursorColor = AzulOscuroApp,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = AzulOscuroApp,
                        contentColor = BlancoPuro,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = BlancoPuro
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                selectedTabIndex = 0;
                                navigator.replace(MisAlimentosScreen)
                            },
                            text = { Text("Mis alimentos", color = BlancoPuro) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Biblioteca", color = BlancoPuro) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = {
                                selectedTabIndex = 2
                                navigator.replace(MisRecetasScreen)
                            },
                            text = { Text("Mis recetas", color = BlancoPuro) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.retry() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlancoPuro)
                        .padding(paddingValues)
                ) {
                    // Banner de offline si estamos sin red pero con datos
                    if (!isNetworkAvailable && uiState.foods.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFFA000),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Sin conexión. Mostrando datos guardados.",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // ── Contenido ─────────────────────────────────────────────
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.isLoading && uiState.foods.isEmpty() -> {
                                ShimmerFoodList()
                            }

                            uiState.errorMessage != null && uiState.foods.isEmpty() -> {
                                FeedbackState(
                                    icon = Icons.Default.WifiOff,
                                    title = "Oops",
                                    subtitle = uiState.errorMessage!!,
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Button(
                                        onClick = { viewModel.retry() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp)
                                    ) {
                                        Icon(Icons.Default.Refresh, null, tint = BlancoPuro)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Reintentar", color = BlancoPuro)
                                    }
                                }
                            }

                            uiState.isEmpty -> {
                                FeedbackState(
                                    icon = Icons.Default.SearchOff,
                                    title = "Sin resultados",
                                    subtitle = "Intenta con otra búsqueda",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            else -> {
                                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                    if (uiState.foods.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "${uiState.foods.size} resultados en España",
                                                color = GrisFuerte,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                    }

                                    items(
                                        items = uiState.foods,
                                        key = { it.nombre + it.marca + it.calorias }
                                    ) { datosAlimento ->
                                        AlimentoItemBiblioteca(
                                            alimento = datosAlimento,
                                            onClick = {
                                                navigator.push(AñadeOEditaScreen(datosAlimento,true))
                                            }
                                        )
                                        HorizontalDivider(color = GrisFuerte, thickness = 0.5.dp)
                                    }

                                    item { Spacer(Modifier.height(80.dp)) }
                                }

                                // Indicador de carga sutil cuando hay más datos
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 8.dp)
                                            .size(24.dp),
                                        color = AzulOscuroApp,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlimentoItemBiblioteca(alimento: DatosAlimento, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(alimento.nombre, color = NegroPuro, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${alimento.cantidadMedida} ${alimento.medida}", color = GrisFuerte, fontSize = 13.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${alimento.calorias.toInt()} kcal", color = AzulCalorias, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Row {
                MacroMiniTextBiblioteca("C", alimento.carbohidratos.toInt().toString(), RosaCarbos)
                Spacer(Modifier.width(6.dp))
                MacroMiniTextBiblioteca("P", alimento.proteinas.toInt().toString(), AzulProte)
                Spacer(Modifier.width(6.dp))
                MacroMiniTextBiblioteca("G", alimento.grasas.toInt().toString(), AmarilloGrasas)
            }
        }
    }
}

@Composable
fun MacroMiniTextBiblioteca(label: String, value: String, color: Color) {
    Row {
        Text("$label: ", color = GrisSuave, fontSize = 11.sp)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// Helper para verificar red
private fun checkNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}