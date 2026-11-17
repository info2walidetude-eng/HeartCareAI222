package dz.univ.alger1.heartcareai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dz.univ.alger1.heartcareai.ble.HeartRateBleManager
import dz.univ.alger1.heartcareai.data.*
import dz.univ.alger1.heartcareai.ml.OnDevicePredictor
import dz.univ.alger1.heartcareai.ui.*
import kotlinx.coroutines.launch

private enum class Page { LOGIN, HOME, FORM, HISTORY, DETAIL, SENSOR, GUIDE, PROFILE, ABOUT }

class MainActivity : ComponentActivity() {
    private lateinit var store: LocalStore
    private lateinit var sensor: HeartRateBleManager
    private lateinit var predictor: OnDevicePredictor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = LocalStore(applicationContext)
        sensor = HeartRateBleManager(applicationContext)
        predictor = OnDevicePredictor(applicationContext)
        setContent {
            HeartCareTheme {
                AppNavigation(store = store, sensor = sensor, predictor = predictor)
            }
        }
    }

    override fun onDestroy() {
        sensor.stop()
        store.close()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNavigation(store: LocalStore, sensor: HeartRateBleManager, predictor: OnDevicePredictor) {
    var user by remember { mutableStateOf(store.currentUser()) }
    var page by remember { mutableStateOf(if (user == null) Page.LOGIN else Page.HOME) }
    var isRegistering by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf("") }
    var records by remember { mutableStateOf<List<Assessment>>(emptyList()) }
    var selected by remember { mutableStateOf<Assessment?>(null) }
    var edit by remember { mutableStateOf<Assessment?>(null) }
    val bleState by sensor.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun reload() { user?.let { records = store.listAssessments(it.id) } }
    LaunchedEffect(user?.id) { reload() }
    fun inform(message: String) { scope.launch { snackbar.showSnackbar(message) } }
    fun goHome() { edit = null; page = Page.HOME }

    BackHandler(enabled = user != null && page != Page.HOME) {
        page = when (page) {
            Page.DETAIL -> Page.HISTORY
            Page.FORM -> Page.HOME
            else -> Page.HOME
        }
    }

    Scaffold(
        containerColor = PageBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (user != null) {
                TopAppBar(
                    title = {
                        Text(if (page == Page.HOME) "HeartCare AI" else when (page) {
                            Page.FORM -> "Évaluation"
                            Page.HISTORY -> "Mes bilans"
                            Page.DETAIL -> "Résultat"
                            Page.SENSOR -> "Capteur BLE"
                            Page.GUIDE -> "Guide santé"
                            Page.PROFILE -> "Mon compte"
                            Page.ABOUT -> "À propos"
                            else -> "HeartCare AI"
                        }, style = MaterialTheme.typography.titleLarge, color = Ink)
                    },
                    navigationIcon = {
                        if (page != Page.HOME) {
                            IconButton(onClick = { page = if (page == Page.DETAIL) Page.HISTORY else Page.HOME }) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Retour", tint = Ink)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { page = Page.GUIDE }) {
                            Icon(Icons.Rounded.HelpOutline, "Guide santé", tint = Mint)
                        }
                        IconButton(onClick = { page = Page.PROFILE }) {
                            Icon(Icons.Rounded.AccountCircle, "Profil", tint = Cardio)
                        }
                        IconButton(onClick = { page = Page.ABOUT }) {
                            Icon(Icons.Rounded.Info, "À propos", tint = Slate)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
                )
            }
        },
        bottomBar = {
            if (user != null) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White, tonalElevation = 3.dp) {
                    val menus = listOf(
                        Triple(Page.HOME, "Accueil", Icons.Rounded.Home),
                        Triple(Page.FORM, "Évaluer", Icons.Rounded.AddCircle),
                        Triple(Page.HISTORY, "Historique", Icons.Rounded.History),
                        Triple(Page.SENSOR, "Capteur", Icons.Rounded.Bluetooth)
                    )
                    menus.forEach { (target, label, icon) ->
                        NavigationBarItem(
                            selected = page == target,
                            onClick = {
                                if (target == Page.FORM) edit = null
                                if (target == Page.HISTORY) reload()
                                page = target
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Cardio,
                                selectedTextColor = Cardio,
                                indicatorColor = CardioSoft,
                                unselectedIconColor = Slate,
                                unselectedTextColor = Slate
                            )
                        )
                    }
                }
            }
        }
    ) { inset ->
        val contentModifier = Modifier.padding(inset)
        if (user == null) {
            AuthScreen(
                modifier = contentModifier,
                isRegistering = isRegistering,
                error = authError,
                onToggle = { isRegistering = !isRegistering; authError = "" },
                onSubmit = { name, email, password, medicalProfile ->
                    runCatching {
                        if (isRegistering) store.createUser(name, email, password, requireNotNull(medicalProfile))
                        else store.login(email, password)
                    }.onSuccess { account ->
                        user = account
                        authError = ""
                        isRegistering = false
                        page = Page.HOME
                    }.onFailure { authError = it.message ?: "Échec de la connexion." }
                }
            )
        } else {
            val account = requireNotNull(user)
            when (page) {
                Page.HOME -> HomeScreen(
                    modifier = contentModifier,
                    user = account,
                    records = records,
                    bleState = bleState,
                    modelReady = predictor.isReady,
                    onEvaluate = { edit = null; page = Page.FORM },
                    onHistory = { reload(); page = Page.HISTORY },
                    onSensor = { page = Page.SENSOR },
                    onSelect = { selected = it; page = Page.DETAIL },
                    onGuide = { page = Page.GUIDE }
                )
                Page.FORM -> if (account.medicalProfile == null) {
                    LaunchedEffect(account.id) {
                        page = Page.PROFILE
                        inform("Complétez votre profil avant une évaluation.")
                    }
                } else AssessmentFormScreen(
                    modifier = contentModifier,
                    editing = edit,
                    latestPulse = bleState.pulse,
                    medicalProfile = requireNotNull(account.medicalProfile),
                    onCancel = { goHome() },
                    onSubmit = { input ->
                        runCatching {
                            val score = predictor.score(input)
                            val source = if (input.pulseBle != null) "Saisie manuelle + BLE" else "Saisie manuelle"
                            val id = store.saveAssessment(account.id, input, score, source, edit?.id)
                            requireNotNull(store.getAssessment(account.id, id))
                        }.onSuccess {
                            selected = it
                            edit = null
                            reload()
                            page = Page.DETAIL
                            inform("Évaluation enregistrée")
                        }.onFailure { inform(it.message ?: "Erreur pendant l'analyse.") }
                    }
                )
                Page.HISTORY -> HistoryScreen(
                    modifier = contentModifier,
                    records = records,
                    onSelect = { selected = it; page = Page.DETAIL },
                    onEdit = { edit = it; page = Page.FORM },
                    onDelete = { item ->
                        if (store.deleteAssessment(account.id, item.id)) {
                            reload()
                            inform("Évaluation supprimée")
                        }
                    }
                )
                Page.DETAIL -> selected?.let { item ->
                    DetailScreen(modifier = contentModifier, item = item,
                        onBack = { page = Page.HISTORY },
                        onEdit = { edit = item; page = Page.FORM })
                }
                Page.SENSOR -> SensorScreen(
                    modifier = contentModifier,
                    state = bleState,
                    onScan = { sensor.startScan() },
                    onConnect = { address -> sensor.connect(address) },
                    onStop = { sensor.stop() }
                )
                Page.GUIDE -> GuideScreen(
                    modifier = contentModifier,
                    onEvaluate = { edit = null; page = Page.FORM }
                )
                Page.PROFILE -> ProfileScreen(
                    modifier = contentModifier,
                    user = account,
                    onSaveName = { newName ->
                        runCatching { store.updateName(account.id, newName) }
                            .onSuccess { user = it; inform("Profil actualisé") }
                            .onFailure { inform(it.message ?: "Mise à jour impossible") }
                    },
                    onCompleteMedicalProfile = { medicalProfile ->
                        runCatching { store.completeMedicalProfileOnce(account.id, medicalProfile) }
                            .onSuccess { user = it; inform("Informations enregistrées dans le profil") }
                            .onFailure { inform(it.message ?: "Enregistrement impossible") }
                    },
                    onLogout = {
                        sensor.stop()
                        store.logout()
                        user = null
                        records = emptyList()
                        page = Page.LOGIN
                    },
                    onDeleteAccount = {
                        sensor.stop()
                        store.deleteAccount(account.id)
                        user = null
                        records = emptyList()
                        page = Page.LOGIN
                    }
                )
                Page.ABOUT -> AboutScreen(modifier = contentModifier)
                Page.LOGIN -> Unit
            }
        }
    }
}
