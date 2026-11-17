package dz.univ.alger1.heartcareai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dz.univ.alger1.heartcareai.R
import dz.univ.alger1.heartcareai.ble.SensorState
import dz.univ.alger1.heartcareai.data.*
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector

// Les composants visuels sont réutilisés dans toutes les pages : aucune dépendance d'interface externe.
private val ShapeBig = RoundedCornerShape(26.dp)
private val ShapeCard = RoundedCornerShape(22.dp)
private val ShapeField = RoundedCornerShape(16.dp)

@Composable
private fun ScrollScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().background(PageBg).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp), content = content
    )
}

@Composable
private fun Heading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = Ink, style = MaterialTheme.typography.headlineMedium)
        if (!subtitle.isNullOrBlank()) Text(subtitle, color = Slate, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Label(text: String, color: Color = Mint, background: Color = MintSoft) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ModernCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier, shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun InformationCard(title: String, body: String, bg: Color = Color.White, icon: ImageVector = Icons.Rounded.Info) {
    Card(shape = ShapeCard, colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .75f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (bg == CardioSoft) Cardio else Mint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
        }
    }
}

// IMPORTANT : onClick reste le dernier paramètre pour autoriser les trailing lambdas Kotlin.
@Composable
private fun PrimaryButton(
    text: String, modifier: Modifier = Modifier, enabled: Boolean = true,
    icon: ImageVector? = null, onClick: () -> Unit
) {
    Button(onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 55.dp),
        shape = ShapeField, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp)) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = ShapeField, border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        if (icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, color = Ink)
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, tint: Color = Cardio, bg: Color = CardioSoft, size: Int = 47) {
    Box(Modifier.size(size.dp).background(bg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * .48f).dp))
    }
}

@Composable
private fun ECGLine(modifier: Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h * .55f)
            lineTo(w * .22f, h * .55f)
            lineTo(w * .32f, h * .47f)
            lineTo(w * .40f, h * .68f)
            lineTo(w * .52f, h * .17f)
            lineTo(w * .62f, h * .83f)
            lineTo(w * .71f, h * .55f)
            lineTo(w, h * .55f)
        }
        drawPath(path, color, style = Stroke(width = 3.3.dp.toPx()))
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    isRegistering: Boolean,
    error: String,
    onToggle: () -> Unit,
    onSubmit: (name: String, email: String, password: String, profile: MedicalProfile?) -> Unit
) {
    var name by remember(isRegistering) { mutableStateOf("") }
    var email by remember(isRegistering) { mutableStateOf("") }
    var password by remember(isRegistering) { mutableStateOf("") }
    var showPassword by remember(isRegistering) { mutableStateOf(false) }
    var age by remember(isRegistering) { mutableStateOf("") }
    var sex by remember(isRegistering) { mutableStateOf("") }
    var chest by remember(isRegistering) { mutableStateOf("") }
    var localError by remember(isRegistering) { mutableStateOf("") }

    ScrollScreen(modifier) {
        Spacer(Modifier.height(8.dp))
        // Identité de marque : sans références universitaires sur l'écran de connexion.
        Card(
            shape = ShapeBig,
            colors = CardDefaults.cardColors(containerColor = Ink)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Mint.copy(alpha = .16f), radius = size.width * .42f,
                        center = androidx.compose.ui.geometry.Offset(size.width * .95f, 0f))
                    drawCircle(Cardio.copy(alpha = .25f), radius = size.width * .24f,
                        center = androidx.compose.ui.geometry.Offset(size.width * .10f, size.height * 1.1f))
                }
                Column(Modifier.fillMaxWidth().padding(23.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(60.dp).background(
                        Brush.linearGradient(listOf(Cardio, CardioDark)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Favorite, "HeartCare AI", tint = Color.White,
                            modifier = Modifier.size(32.dp))
                    }
                    Text("HeartCare AI", color = Color.White, style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    Text("Comprendre votre santé, jour après jour.", color = Color.White.copy(alpha = .82f),
                        style = MaterialTheme.typography.bodyLarge)
                    ECGLine(Modifier.fillMaxWidth().height(34.dp), color = Mint)
                }
            }
        }
        Heading(if (isRegistering) "Créer mon espace personnel" else "Se connecter",
            if (isRegistering) "Un profil enregistré une seule fois pour simplifier vos évaluations."
            else "Retrouvez vos informations et l’historique de vos bilans.")
        ModernCard {
            if (isRegistering) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom complet") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = ShapeField)
            }
            OutlinedTextField(email, { email = it }, label = { Text("Adresse e-mail") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Rounded.Email, null) }, modifier = Modifier.fillMaxWidth(), shape = ShapeField)
            OutlinedTextField(password, { password = it }, label = { Text("Mot de passe (8 caractères minimum)") },
                singleLine = true,
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (showPassword) "Masquer" else "Afficher")
                    }
                },
                leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                modifier = Modifier.fillMaxWidth(), shape = ShapeField)

            if (isRegistering) {
                HorizontalDivider(color = BorderLight)
                Text("Mon profil cardiovasculaire", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Ces informations seront enregistrées dans votre profil et reprises automatiquement lors des évaluations.",
                    color = Slate, style = MaterialTheme.typography.bodySmall)
                NumericField("Âge", age, { age = it }, suffix = "ans")
                ChoiceField("Sexe", sex, listOf("M" to "Masculin", "F" to "Féminin")) { sex = it }
                ChoiceField("Type de douleur thoracique", chest,
                    listOf("TA" to "Typique", "ATA" to "Atypique", "NAP" to "Non-angineuse", "ASY" to "Asymptomatique")) { chest = it }
                Text("Ces valeurs restent fixes dans le formulaire d’analyse. Pour corriger une erreur de profil, une évolution spécifique sera nécessaire.",
                    color = Slate, style = MaterialTheme.typography.bodySmall)
            }
            if (localError.isNotBlank()) Text(localError, color = CardioDark, style = MaterialTheme.typography.bodyMedium)
            if (error.isNotBlank()) Text(error, color = CardioDark, style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(if (isRegistering) "Créer mon compte" else "Me connecter", icon = Icons.Rounded.ArrowForward) {
                localError = ""
                val profile = if (isRegistering) {
                    val parsedAge = age.toIntOrNull()
                    when {
                        parsedAge == null || parsedAge !in 1..120 -> localError = "Indiquez un âge entre 1 et 120 ans."
                        sex.isBlank() -> localError = "Sélectionnez le sexe."
                        chest.isBlank() -> localError = "Sélectionnez le type de douleur thoracique."
                    }
                    if (localError.isBlank()) MedicalProfile(requireNotNull(parsedAge), sex, chest) else null
                } else null
                if (localError.isBlank()) onSubmit(name, email, password, profile)
            }
            TextButton(onClick = { localError = ""; onToggle() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isRegistering) "Déjà inscrit ? Se connecter" else "Créer un compte")
            }
        }
        InformationCard("Confidentialité et prévention",
            "Ce prototype enregistre les données sur l'appareil sans chiffrement médical des bilans. Ne saisissez pas de données de patients réels. Les analyses IA ne sont pas des diagnostics.",
            MintSoft, Icons.Rounded.Security)
    }
}

@Composable
private fun MetricCard(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier,
                       accent: Color = Cardio, light: Color = CardioSoft) {
    ModernCard(modifier) {
        CircleIcon(icon, accent, light, 42)
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate, maxLines = 2)
    }
}

@Composable
private fun QuickLink(label: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = ShapeCard, color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleIcon(icon, Mint, MintSoft, 45)
            Column(Modifier.weight(1f)) {
                Text(label, color = Ink, style = MaterialTheme.typography.titleMedium)
                Text(description, color = Slate, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = Slate)
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier, user: AppUser, records: List<Assessment>, bleState: SensorState,
    modelReady: Boolean, onEvaluate: () -> Unit, onHistory: () -> Unit,
    onSensor: () -> Unit, onSelect: (Assessment) -> Unit, onGuide: () -> Unit
) {
    val newest = remember(records) { records.maxByOrNull { it.createdAt } }
    val todayLatest = records.filter { HomeInsights.isToday(it.createdAt) }
        .maxByOrNull { it.createdAt }
    ScrollScreen(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BONJOUR 👋", color = Mint, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold)
                Text(user.name.substringBefore(' ').ifBlank { "Bienvenue" },
                    color = Ink, style = MaterialTheme.typography.headlineLarge)
                Text("Votre espace de santé numérique", color = Slate)
            }
            Box(Modifier.size(52.dp).background(MintSoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Favorite, null, tint = Mint, modifier = Modifier.size(27.dp))
            }
        }
        Card(shape = ShapeBig, colors = CardDefaults.cardColors(containerColor = Ink)) {
            Box(Modifier.fillMaxWidth()) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Mint.copy(alpha = .14f), radius = size.width * .37f,
                        center = androidx.compose.ui.geometry.Offset(size.width * .98f, size.height * -.1f))
                    drawCircle(Cardio.copy(alpha = .18f), radius = size.width * .32f,
                        center = androidx.compose.ui.geometry.Offset(size.width * .92f, size.height * 1.13f))
                }
                Column(Modifier.padding(23.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Label("MON ESPACE CARDIO", Color.White, Mint.copy(alpha = .35f))
                    Text("Comprenez l'évolution de vos mesures.", color = Color.White,
                        style = MaterialTheme.typography.headlineMedium)
                    Text("Bilans personnels • historique • conseils éducatifs",
                        color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                    ECGLine(Modifier.fillMaxWidth(.9f).height(47.dp), Cardio)
                    Button(onClick = onEvaluate, shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cardio, contentColor = Color.White)) {
                        Icon(Icons.Rounded.Add, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nouvelle évaluation")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            }
        }
        HomeDailyPanel(todayLatest != null, todayLatest, onEvaluate, onHistory)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(records.size.toString(), "Bilans enregistrés", Icons.Rounded.FolderOpen, Modifier.weight(1f))
            MetricCard(bleState.pulse?.let { "$it bpm" } ?: "— bpm", "Pouls du capteur",
                Icons.Rounded.FavoriteBorder, Modifier.weight(1f), Mint, MintSoft)
        }
        HomeTrendPanel(records)
        HomeHeartTips(newest)
        InformationCard(
            if (modelReady) "Modèle expérimental disponible" else "IA non activée",
            if (modelReady) "Les scores expérimentaux ne sont pas des diagnostics cliniques."
            else "Les bilans, graphiques et conseils pédagogiques fonctionnent sans modèle IA. Ajoutez le modèle entraîné pour activer les scores expérimentaux.",
            if (modelReady) MintSoft else AmberSoft,
            if (modelReady) Icons.Rounded.CheckCircle else Icons.Rounded.Info
        )
        Heading("Explorer", "Accédez rapidement aux fonctionnalités")
        QuickLink("Mes évaluations", "Rechercher et consulter mes bilans", Icons.Rounded.History, onHistory)
        QuickLink("Appareil connecté", "Lire le pouls depuis un capteur BLE", Icons.Rounded.Bluetooth, onSensor)
        QuickLink("Guide santé", "Comprendre les indicateurs et l’IA", Icons.Rounded.MenuBook, onGuide)
        if (records.isNotEmpty()) {
            Heading("Derniers bilans")
            records.take(2).forEach { AssessmentTile(it, onSelect = { onSelect(it) }) }
        }
        InformationCard("Suivi éducatif",
            "Ces mesures et graphiques ne diagnostiquent aucune maladie cardiaque et ne remplacent pas le suivi médical.")
    }
}

@Composable
private fun NumericField(label: String, value: String, onValueChange: (String) -> Unit,
                         decimal: Boolean = false, suffix: String? = null) {
    OutlinedTextField(value = value,
        onValueChange = { next ->
            val allowed = if (decimal) next.all { it.isDigit() || it == '-' || it == '.' || it == ',' }
                          else next.all(Char::isDigit)
            if (allowed && next.length <= 9) onValueChange(next)
        },
        label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        suffix = if (suffix == null) null else ({ Text(suffix, color = Slate) }),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        shape = ShapeField
    )
}

@Composable
private fun ChoiceField(label: String, selected: String, options: List<Pair<String, String>>, onChoose: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Ink, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            options.forEach { (value, name) ->
                FilterChip(selected = selected == value, onClick = { onChoose(value) },
                    label = { Text(name) }, shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (selected == value) ({ Icon(Icons.Rounded.Check, null, Modifier.size(16.dp)) }) else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintSoft, selectedLabelColor = Mint
                    ))
            }
        }
    }
}

@Composable
fun AssessmentFormScreen(
    modifier: Modifier, editing: Assessment?, latestPulse: Int?, medicalProfile: MedicalProfile,
    onCancel: () -> Unit, onSubmit: (HeartInputs) -> Unit
) {
    val previous = editing?.input
    // Valeurs du compte : elles ne sont plus modifiables dans une évaluation.
    val age = medicalProfile.age
    val sex = medicalProfile.sex
    val chest = medicalProfile.chestPainType
    var restingBP by remember(editing?.id) { mutableStateOf(previous?.restingBP?.toString() ?: "") }
    var cholesterol by remember(editing?.id) { mutableStateOf(previous?.cholesterol?.toString() ?: "") }
    var fastingBS by remember(editing?.id) { mutableStateOf((previous?.fastingBS ?: 0).toString()) }
    var restingECG by remember(editing?.id) { mutableStateOf(previous?.restingECG ?: "Normal") }
    var maxHR by remember(editing?.id) { mutableStateOf(previous?.maxHR?.toString() ?: "") }
    var exerciseAngina by remember(editing?.id) { mutableStateOf(previous?.exerciseAngina ?: "N") }
    var oldpeak by remember(editing?.id) { mutableStateOf(previous?.oldpeak?.toString() ?: "0") }
    var stSlope by remember(editing?.id) { mutableStateOf(previous?.stSlope ?: "Up") }
    var includePulse by remember(editing?.id) { mutableStateOf(previous?.pulseBle != null) }
    var error by remember(editing?.id) { mutableStateOf("") }
    var step by remember(editing?.id) { mutableIntStateOf(0) }
    val availablePulse = latestPulse ?: previous?.pulseBle
    val titles = listOf("Profil", "Mesures", "Effort & ECG")

    fun validate(index: Int): String = when (index) {
        0 -> if (age !in 1..120) "Complétez d’abord votre profil." else ""
        1 -> when {
            restingBP.toIntOrNull() !in 1..300 -> "Pression au repos : entre 1 et 300 mmHg."
            cholesterol.toIntOrNull() !in 0..700 -> "Cholestérol : entre 0 et 700 mg/dL."
            else -> ""
        }
        else -> when {
            maxHR.toIntOrNull() !in 40..250 -> "MaxHR : entre 40 et 250 bpm."
            oldpeak.replace(',', '.').toDoubleOrNull()?.let { it in -5.0..10.0 } != true ->
                "Oldpeak : entre -5 et 10."
            else -> ""
        }
    }
    ScrollScreen(modifier) {
        Heading(if (editing == null) "Nouvelle évaluation" else "Modifier le bilan",
            "Renseignez les 11 variables du Heart Failure Prediction Dataset.")
        ModernCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Label("ÉTAPE ${step + 1} / 3")
                Text("${((step + 1) * 100) / 3}%", color = Slate, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth().height(8.dp)
                .clip(CircleShape), color = Mint, trackColor = MintSoft)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                titles.forEachIndexed { index, label ->
                    Surface(onClick = { if (index <= step) { error = ""; step = index } },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        color = if (index == step) Ink else PageBg) {
                        Text(label, modifier = Modifier.padding(vertical = 10.dp, horizontal = 3.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (index == step) Color.White else Slate, textAlign = TextAlign.Center,
                            fontWeight = if (index == step) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1)
                    }
                }
            }
        }
        when (step) {
            0 -> {
                InformationCard("Informations du compte", "Ces trois données ont été enregistrées lors de l’inscription. Elles sont reprises automatiquement et non modifiables dans le bilan.", MintSoft, Icons.Rounded.Person)
                ModernCard {
                    ValueLine("Âge", "$age ans")
                    ValueLine("Sexe", if (sex == "M") "Masculin" else "Féminin")
                    ValueLine("Douleur thoracique", when (chest) {
                        "TA" -> "Typique (TA)"
                        "ATA" -> "Atypique (ATA)"
                        "NAP" -> "Non-angineuse (NAP)"
                        else -> "Asymptomatique (ASY)"
                    })
                }
            }
            1 -> {
                InformationCard("Mesures au repos", "Entrez les valeurs connues de votre démonstration. N’inventez pas de données réelles.", MintSoft, Icons.Rounded.MonitorHeart)
                ModernCard {
                    NumericField("Pression artérielle au repos", restingBP, { restingBP = it }, suffix = "mmHg")
                    NumericField("Cholestérol sérique", cholesterol, { cholesterol = it }, suffix = "mg/dL")
                    ChoiceField("Glycémie à jeun > 120 mg/dL", fastingBS,
                        listOf("0" to "Non", "1" to "Oui")) { fastingBS = it }
                    ChoiceField("ECG au repos", restingECG,
                        listOf("Normal" to "Normal", "ST" to "ST", "LVH" to "LVH")) { restingECG = it }
                }
            }
            else -> {
                InformationCard("Effort et tracé ECG", "MaxHR correspond à une fréquence maximale atteinte à l'effort, pas au pouls BLE actuel.", MintSoft, Icons.Rounded.Favorite)
                ModernCard {
                    NumericField("MaxHR à l'effort", maxHR, { maxHR = it }, suffix = "bpm")
                    ChoiceField("Angine induite par l'effort", exerciseAngina,
                        listOf("N" to "Non", "Y" to "Oui")) { exerciseAngina = it }
                    NumericField("Oldpeak (dépression ST)", oldpeak, { oldpeak = it }, decimal = true)
                    ChoiceField("Pente du segment ST", stSlope,
                        listOf("Up" to "Montante", "Flat" to "Plate", "Down" to "Descendante")) { stSlope = it }
                }
                ModernCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        CircleIcon(Icons.Rounded.Bluetooth, Mint, MintSoft, 44)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Ajouter le pouls BLE", style = MaterialTheme.typography.titleMedium)
                            Text(availablePulse?.let { "$it bpm disponibles" } ?: "Capteur non connecté",
                                color = Slate, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = includePulse, onCheckedChange = { includePulse = it },
                            enabled = availablePulse != null)
                    }
                }
            }
        }
        if (error.isNotBlank()) InformationCard("Vérification nécessaire", error, CardioSoft, Icons.Rounded.Warning)
        PrimaryButton(if (step < 2) "Continuer" else if (editing == null) "Enregistrer et analyser" else "Enregistrer les modifications",
            icon = if (step < 2) Icons.Rounded.ArrowForward else Icons.Rounded.Check) {
            error = validate(step)
            if (error.isBlank()) {
                if (step < 2) step++ else onSubmit(HeartInputs(
                    age = age, sex = sex, chestPainType = chest,
                    restingBP = requireNotNull(restingBP.toIntOrNull()),
                    cholesterol = requireNotNull(cholesterol.toIntOrNull()), fastingBS = fastingBS.toInt(),
                    restingECG = restingECG, maxHR = requireNotNull(maxHR.toIntOrNull()),
                    exerciseAngina = exerciseAngina, oldpeak = requireNotNull(oldpeak.replace(',', '.').toDoubleOrNull()),
                    stSlope = stSlope, pulseBle = if (includePulse) availablePulse else null
                ))
            }
        }
        SecondaryButton(if (step > 0) "Étape précédente" else "Annuler",
            icon = Icons.Rounded.ArrowBack) {
            if (step > 0) { error = ""; step-- } else onCancel()
        }
        Text("Démonstrateur éducatif : le cholestérol = 0 peut correspondre à une valeur absente dans le jeu de données.",
            style = MaterialTheme.typography.bodySmall, color = Slate)
    }
}

@Composable
private fun AssessmentTile(item: Assessment, onSelect: () -> Unit) {
    Surface(onClick = onSelect, shape = ShapeCard, color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Row(Modifier.fillMaxWidth().padding(17.dp), horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically) {
            CircleIcon(Icons.Rounded.Favorite, Cardio, CardioSoft, 46)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Bilan · ${formatDate(item.createdAt)}", style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.input.age} ans · ${item.source}", style = MaterialTheme.typography.bodySmall,
                    color = Slate, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (item.score == null) Label("ML —", Slate, PageBg)
            else Label("${(item.score * 100).toInt()} %", if (item.score >= .5) Cardio else Mint,
                if (item.score >= .5) CardioSoft else MintSoft)
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier, records: List<Assessment>,
                  onSelect: (Assessment) -> Unit, onEdit: (Assessment) -> Unit, onDelete: (Assessment) -> Unit) {
    var confirming by remember { mutableStateOf<Assessment?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) }
    var newestFirst by remember { mutableStateOf(true) }
    val shown = remember(records, query, filter, newestFirst) {
        records.filter { item ->
            val matches = query.isBlank() || listOf(formatDate(item.createdAt), item.source,
                item.input.age.toString(), item.input.chestPainType).any { it.contains(query.trim(), ignoreCase = true) }
            val rightCategory = when (filter) { 1 -> item.score != null; 2 -> item.score == null; else -> true }
            matches && rightCategory
        }.let { if (newestFirst) it.sortedByDescending(Assessment::createdAt) else it.sortedBy(Assessment::createdAt) }
    }
    if (confirming != null) AlertDialog(onDismissRequest = { confirming = null },
        icon = { Icon(Icons.Rounded.DeleteOutline, null, tint = Cardio) },
        title = { Text("Supprimer ce bilan ?") },
        text = { Text("Cette suppression est définitive sur votre appareil.") },
        confirmButton = { TextButton(onClick = { confirming?.let(onDelete); confirming = null }) { Text("Supprimer", color = Cardio) } },
        dismissButton = { TextButton(onClick = { confirming = null }) { Text("Annuler") } }
    )
    ScrollScreen(modifier) {
        Heading("Historique", "Retrouvez et gérez vos évaluations enregistrées.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(records.size.toString(), "Bilans au total", Icons.Rounded.FolderOpen, Modifier.weight(1f))
            MetricCard(records.count { it.score != null }.toString(), "Avec modèle ML", Icons.Rounded.AutoGraph,
                Modifier.weight(1f), Mint, MintSoft)
        }
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Rechercher un bilan") }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, shape = ShapeField,
            placeholder = { Text("Date, âge, source...") })
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tous", "Avec score", "Sans score").forEachIndexed { index, text ->
                FilterChip(selected = filter == index, onClick = { filter = index }, label = { Text(text) },
                    shape = RoundedCornerShape(12.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${shown.size} résultat(s)", color = Slate, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { newestFirst = !newestFirst }) {
                Icon(Icons.Rounded.Sort, null, Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (newestFirst) "Plus récents" else "Plus anciens")
            }
        }
        if (shown.isEmpty()) {
            InformationCard("Aucun bilan trouvé", "Modifiez la recherche ou réalisez votre première évaluation.", MintSoft, Icons.Rounded.SearchOff)
        } else {
            shown.forEach { item ->
                AssessmentTile(item) { onSelect(item) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onEdit(item) }) {
                        Icon(Icons.Rounded.Edit, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Modifier")
                    }
                    TextButton(onClick = { confirming = item }) {
                        Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(17.dp), tint = Cardio)
                        Spacer(Modifier.width(4.dp)); Text("Supprimer", color = Cardio)
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Slate, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End)
    }
    HorizontalDivider(color = BorderLight)
}

@Composable
fun DetailScreen(modifier: Modifier, item: Assessment, onBack: () -> Unit, onEdit: () -> Unit) {
    ScrollScreen(modifier) {
        Heading("Détail du bilan", formatDate(item.createdAt))
        val score = item.score
        if (score == null) {
            InformationCard("Classification non disponible",
                "Le bilan est enregistré. Pour obtenir une classification expérimentale, entraînez un modèle avec heart.csv et reconstruisez l’application.",
                AmberSoft, Icons.Rounded.Info)
        } else {
            Card(shape = ShapeBig, colors = CardDefaults.cardColors(containerColor = Ink)) {
                Column(Modifier.padding(23.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Label("RÉSULTAT DU MODÈLE", Color.White, Mint.copy(alpha = .35f))
                    Text("Classe prédite : ${if (score >= 0.5) "1" else "0"}",
                        style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text("Score expérimental ${"%.1f".format(Locale.FRANCE, score * 100)} %",
                        color = Color.White.copy(alpha = .84f))
                    LinearProgressIndicator(progress = { score.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                        color = if (score >= .5) Cardio else Mint, trackColor = Color.White.copy(alpha = .22f))
                    Text("Ce score n’est pas une probabilité de maladie validée cliniquement.",
                        color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
                }
            }
            InformationCard("Lecture de la classe",
                "Dans le dataset Kaggle, 1 correspond à la présence enregistrée de maladie cardiaque, 0 à son absence. Ce résultat n'est pas un diagnostic.",
                if (score >= .5) CardioSoft else MintSoft, Icons.Rounded.Info)
        }
        ModernCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Assignment, null, tint = Mint)
                Spacer(Modifier.width(9.dp))
                Text("Indicateurs du bilan", style = MaterialTheme.typography.titleLarge)
            }
            listOf(
                "Âge" to "${item.input.age} ans", "Sexe (dataset)" to item.input.sex,
                "Douleur thoracique" to item.input.chestPainType,
                "Pression artérielle" to "${item.input.restingBP} mmHg",
                "Cholestérol" to "${item.input.cholesterol} mg/dL",
                "Glycémie à jeun > 120" to (if (item.input.fastingBS == 1) "Oui" else "Non"),
                "ECG au repos" to item.input.restingECG,
                "MaxHR à l'effort" to "${item.input.maxHR} bpm",
                "Angine à l'effort" to item.input.exerciseAngina,
                "Oldpeak" to item.input.oldpeak.toString(),
                "Pente ST" to item.input.stSlope,
                "Pouls BLE (hors modèle)" to (item.input.pulseBle?.let { "$it bpm" } ?: "Non mesuré"),
                "Source" to item.source
            ).forEach { (name, value) -> ValueLine(name, value) }
        }
        PrimaryButton("Modifier ce bilan", icon = Icons.Rounded.Edit, onClick = onEdit)
        SecondaryButton("Retour à l'historique", icon = Icons.Rounded.ArrowBack, onClick = onBack)
        InformationCard("Avertissement médical",
            "Cette application est un prototype pédagogique. N’utilisez pas le résultat pour poser un diagnostic ou modifier un traitement.",
            CardioSoft, Icons.Rounded.Warning)
    }
}

@Composable
fun ProfileScreen(modifier: Modifier, user: AppUser, onSaveName: (String) -> Unit,
                  onCompleteMedicalProfile: (MedicalProfile) -> Unit,
                  onLogout: () -> Unit, onDeleteAccount: () -> Unit) {
    var name by remember(user.id) { mutableStateOf(user.name) }
    var askDelete by remember { mutableStateOf(false) }
    var ageText by remember(user.id) { mutableStateOf("") }
    var sexSelection by remember(user.id) { mutableStateOf("") }
    var chestSelection by remember(user.id) { mutableStateOf("") }
    var medicalError by remember(user.id) { mutableStateOf("") }
    if (askDelete) AlertDialog(onDismissRequest = { askDelete = false },
        icon = { Icon(Icons.Rounded.Warning, null, tint = Cardio) },
        title = { Text("Supprimer le compte ?") },
        text = { Text("Toutes vos données locales et vos évaluations seront supprimées définitivement.") },
        confirmButton = { TextButton(onClick = { askDelete = false; onDeleteAccount() }) { Text("Supprimer", color = Cardio) } },
        dismissButton = { TextButton(onClick = { askDelete = false }) { Text("Annuler") } })
    ScrollScreen(modifier) {
        Heading("Mon profil", "Votre espace personnel sur cet appareil")
        ModernCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Box(Modifier.size(72.dp).background(CardioSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Text(user.name.trim().take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = Cardio)
                }
                Column(Modifier.weight(1f)) {
                    Text(user.name, style = MaterialTheme.typography.titleLarge)
                    Text(user.email, color = Slate, style = MaterialTheme.typography.bodyMedium)
                    Label("Compte local")
                }
            }
        }
        ModernCard {
            Text("Modifier mes informations", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(name, { name = it }, label = { Text("Nom complet") },
                leadingIcon = { Icon(Icons.Rounded.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = ShapeField)
            OutlinedTextField(user.email, {}, label = { Text("Adresse e-mail") },
                enabled = false, modifier = Modifier.fillMaxWidth(), shape = ShapeField)
            PrimaryButton("Enregistrer mon profil", icon = Icons.Rounded.Save) { onSaveName(name) }
        }
        ModernCard {
            Text("Informations cardiovasculaires", style = MaterialTheme.typography.titleLarge, color = Ink)
            val saved = user.medicalProfile
            if (saved != null) {
                Label("PROFIL ENREGISTRÉ", Mint, MintSoft)
                ValueLine("Âge à l’inscription", "${saved.age} ans")
                ValueLine("Sexe", if (saved.sex == "M") "Masculin" else "Féminin")
                ValueLine("Douleur thoracique", when (saved.chestPainType) {
                    "TA" -> "Typique (TA)"
                    "ATA" -> "Atypique (ATA)"
                    "NAP" -> "Non-angineuse (NAP)"
                    else -> "Asymptomatique (ASY)"
                })
                Text("Ces valeurs enregistrées sont réutilisées lors des évaluations. L’âge et les symptômes peuvent évoluer : ne les considérez pas comme une description médicale toujours actuelle.",
                    style = MaterialTheme.typography.bodySmall, color = Slate)
            } else {
                Text("Ancien compte : complétez ces informations une seule fois pour accéder à l’évaluation.",
                    color = Slate, style = MaterialTheme.typography.bodyMedium)
                NumericField("Âge", ageText, { ageText = it }, suffix = "ans")
                ChoiceField("Sexe", sexSelection,
                    listOf("M" to "Masculin", "F" to "Féminin")) { sexSelection = it }
                ChoiceField("Douleur thoracique", chestSelection,
                    listOf("TA" to "Typique", "ATA" to "Atypique", "NAP" to "Non-angineuse", "ASY" to "Asymptomatique")) { chestSelection = it }
                if (medicalError.isNotBlank()) Text(medicalError, color = CardioDark)
                PrimaryButton("Enregistrer définitivement", icon = Icons.Rounded.Check) {
                    val enteredAge = ageText.toIntOrNull()
                    medicalError = when {
                        enteredAge == null || enteredAge !in 1..120 -> "Saisissez un âge entre 1 et 120 ans."
                        sexSelection.isBlank() -> "Sélectionnez le sexe."
                        chestSelection.isBlank() -> "Sélectionnez le type de douleur."
                        else -> ""
                    }
                    if (medicalError.isBlank()) onCompleteMedicalProfile(
                        MedicalProfile(requireNotNull(enteredAge), sexSelection, chestSelection))
                }
            }
        }
        SecondaryButton("Se déconnecter", icon = Icons.Rounded.Logout, onClick = onLogout)
        TextButton(onClick = { askDelete = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.DeleteOutline, null, tint = Cardio)
            Spacer(Modifier.width(9.dp))
            Text("Supprimer le compte", color = Cardio)
        }
        InformationCard("Confidentialité",
            "La base SQLite du prototype n’est pas chiffrée. Ne saisissez pas de vraies données médicales personnelles.",
            AmberSoft, Icons.Rounded.Security)
    }
}

@Composable
fun GuideScreen(modifier: Modifier, onEvaluate: () -> Unit) {
    ScrollScreen(modifier) {
        Heading("Comprendre HeartCare", "Les notions essentielles avant une évaluation")
        Card(shape = ShapeBig, colors = CardDefaults.cardColors(containerColor = MintSoft)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.MenuBook, null, tint = Mint, modifier = Modifier.size(34.dp))
                Text("Un outil pour apprendre, pas pour diagnostiquer.",
                    color = Ink, style = MaterialTheme.typography.titleLarge)
                Text("Ce démonstrateur analyse les variables du dataset Kaggle afin d'illustrer le Machine Learning appliqué à la santé.",
                    color = Slate)
            }
        }
        InformationCard("01 · Pression artérielle au repos",
            "Mesure de pression exprimée en mmHg. Elle est différente d'une mesure de fréquence cardiaque.",
            Color.White, Icons.Rounded.MonitorHeart)
        InformationCard("02 · Cholestérol et glycémie",
            "Le jeu de données contient des valeurs numériques de cholestérol et un indicateur binaire pour la glycémie à jeun supérieure à 120 mg/dL.",
            Color.White, Icons.Rounded.Science)
        InformationCard("03 · Pouls Bluetooth et MaxHR",
            "Un capteur peut transmettre un pouls instantané. La variable MaxHR correspond, elle, à la fréquence maximale observée à l’effort.",
            Color.White, Icons.Rounded.Bluetooth)
        InformationCard("04 · Score Machine Learning",
            "Le score indique la sortie d'un modèle entraîné sur un petit dataset, sans validation clinique. La classe 1 correspond aux cas étiquetés HeartDisease dans les données.",
            Color.White, Icons.Rounded.AutoGraph)
        InformationCard("05 · Précautions",
            "N’utilisez pas l’application pour confirmer une maladie, déterminer une urgence médicale ou modifier un traitement. En cas de symptôme inquiétant, consultez un professionnel.",
            CardioSoft, Icons.Rounded.Warning)
        PrimaryButton("Créer une évaluation de démonstration", icon = Icons.Rounded.ArrowForward, onClick = onEvaluate)
    }
}

@Composable
fun AboutScreen(modifier: Modifier) {
    ScrollScreen(modifier) {
        Heading("À propos", "HeartCare AI · Votre compagnon de suivi cardiovasculaire")
        ModernCard {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(60.dp).background(CardioSoft, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null,
                        tint = Cardio, modifier = Modifier.size(32.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("HeartCare AI", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Comprendre ses indicateurs cardiovasculaires au quotidien.",
                        color = Slate, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        InformationCard("Équipe de développement", "Bouzid Youcef\nAtek Yanis\nCherifi Walid Zakaria",
            MintSoft, Icons.Rounded.Groups)
        InformationCard("Fonctionnement", "Création de compte, profil enregistré, suivi des évaluations, historique local et mesure complémentaire du pouls avec un capteur BLE compatible.",
            Color.White, Icons.Rounded.Info)
        InformationCard("Technologies", "Android Studio · Kotlin · Jetpack Compose · SQLite · BLE · Python/scikit-learn · Machine Learning hors ligne",
            Color.White, Icons.Rounded.Code)
        InformationCard("Données du modèle", "Dataset Heart Failure Prediction de Kaggle : 11 caractéristiques d'entrée et une cible HeartDisease (0/1). La cible ne correspond pas spécifiquement à l'insuffisance cardiaque.",
            Color.White, Icons.Rounded.Dataset)
        InformationCard("Utilisation responsable", "La classification est expérimentale, non validée cliniquement. L’application ne diagnostique aucune maladie et ne remplace pas une consultation médicale.",
            CardioSoft, Icons.Rounded.Warning)
        Text("HeartCare AI", modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center, color = Slate, style = MaterialTheme.typography.bodySmall)
    }
}
