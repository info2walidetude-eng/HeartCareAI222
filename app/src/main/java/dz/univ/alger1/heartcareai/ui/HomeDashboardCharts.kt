package dz.univ.alger1.heartcareai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dz.univ.alger1.heartcareai.data.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** Two clinical variables are never mixed on one axis. BLE pulse is not MaxHR. */
@Composable
fun HomeTrendPanel(records: List<Assessment>, modifier: Modifier = Modifier) {
    var showBp by remember { mutableStateOf(true) }
    val items = remember(records, showBp) {
        HomeInsights.lastPoints(records.map { item ->
            HomePoint(item.createdAt, if (showBp) item.input.restingBP else item.input.maxHR)
        })
    }
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ShowChart, null, tint = Mint, modifier = Modifier.size(23.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Évolution des mesures", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Jusqu'aux 7 derniers bilans", style = MaterialTheme.typography.bodySmall, color = Slate)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = showBp, onClick = { showBp = true },
                    label = { Text("Tension au repos") })
                FilterChip(selected = !showBp, onClick = { showBp = false },
                    label = { Text("FC maximale") })
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(150.dp)
                    .background(PageBg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("Aucune donnée : créez votre premier bilan.", color = Slate)
                }
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${items.last().value}", style = MaterialTheme.typography.headlineMedium,
                        color = if (showBp) Cardio else Mint, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(if (showBp) "mmHg (systolique)" else "bpm (max. à l'effort)",
                        color = Slate, style = MaterialTheme.typography.bodySmall)
                }
                TrendLine(items.map { it.value }, if (showBp) Cardio else Mint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val df = remember { SimpleDateFormat("dd/MM", Locale.FRANCE) }
                    Text(df.format(Date(items.first().at)), color = Slate,
                        style = MaterialTheme.typography.labelSmall)
                    Text(if (items.size == 1) "1 bilan enregistré" else
                        "${items.size} bilans • ${df.format(Date(items.last().at))}", color = Slate,
                        style = MaterialTheme.typography.labelSmall)
                }
                Text("Courbe descriptive des valeurs saisies. Elle ne permet pas de conclure à une amélioration ou à une maladie.",
                    style = MaterialTheme.typography.bodySmall, color = Slate)
            }
        }
    }
}

@Composable
private fun TrendLine(values: List<Int>, lineColor: Color) {
    val low = values.minOrNull()?.toFloat() ?: 0f
    val high = values.maxOrNull()?.toFloat() ?: 1f
    val span = max(10f, high - low)
    val chartMin = low - span * 0.15f
    val chartMax = high + span * 0.15f
    Canvas(Modifier.fillMaxWidth().height(165.dp)) {
        val xPad = 14.dp.toPx()
        val yPad = 16.dp.toPx()
        val usableW = (size.width - 2f * xPad).coerceAtLeast(1f)
        val usableH = (size.height - 2f * yPad).coerceAtLeast(1f)
        val baseline = size.height - yPad
        for (i in 0..3) {
            val y = yPad + i * usableH / 3f
            drawLine(BorderLight, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        if (values.isEmpty()) return@Canvas
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2f else xPad + index * usableW / (values.size - 1)
            val y = baseline - ((value - chartMin) / (chartMax - chartMin)) * usableH
            Offset(x, y)
        }
        if (points.size > 1) {
            val fill = Path().apply {
                moveTo(points.first().x, baseline)
                lineTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, baseline)
                close()
            }
            drawPath(fill, lineColor.copy(alpha = 0.10f))
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        points.forEach { point ->
            drawCircle(Color.White, 6.dp.toPx(), point)
            drawCircle(lineColor, 4.dp.toPx(), point)
        }
    }
}

@Composable
fun HomeDailyPanel(didToday: Boolean, latestToday: Assessment?, onEvaluate: () -> Unit,
    onHistory: () -> Unit, modifier: Modifier = Modifier) {
    val good = didToday
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (good) MintSoft else CardioSoft)) {
        Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (good) Icons.Rounded.CheckCircle else Icons.Rounded.EventNote,
                    null, tint = if (good) Mint else Cardio, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("Bilan d'aujourd'hui", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(if (good) "Effectué" else "Pas encore effectué", color = if (good) Mint else CardioDark,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            Text(if (good) "Vous avez enregistré ${latestToday?.let { formatDate(it.createdAt) } ?: "un bilan"}. Vous pouvez consulter vos données."
                 else "Prenez quelques minutes pour enregistrer vos indicateurs si vous disposez de mesures fiables.",
                color = InkSoft, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = if (good) onHistory else onEvaluate,
                colors = ButtonDefaults.buttonColors(containerColor = if (good) Mint else Cardio),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Icon(if (good) Icons.Rounded.Visibility else Icons.Rounded.AddCircle, null)
                Spacer(Modifier.width(8.dp))
                Text(if (good) "Voir mes bilans" else "Faire mon bilan")
            }
        }
    }
}

@Composable
fun HomeHeartTips(latest: Assessment?, modifier: Modifier = Modifier) {
    val snapshot = latest?.input?.let { input ->
        HomeSnapshot(input.restingBP, input.cholesterol, input.fastingBS == 1, input.exerciseAngina == "Y")
    }
    val tips = HomeInsights.education(snapshot)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Conseils pour la santé du cœur", style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(if (latest == null) "Conseils généraux, avant votre premier bilan" else
            "Informations pédagogiques selon le dernier bilan enregistré",
            color = Slate, style = MaterialTheme.typography.bodySmall)
        tips.forEach { tip ->
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (tip.needsAttention) AmberSoft else Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(if (tip.needsAttention) Icons.Rounded.PriorityHigh else Icons.Rounded.FavoriteBorder,
                        contentDescription = null, tint = if (tip.needsAttention) CardioDark else Mint)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(tip.title, style = MaterialTheme.typography.titleMedium, color = Ink)
                        Text(tip.message, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    }
                }
            }
        }
        Text("Conseils éducatifs uniquement. Le questionnaire et le modèle IA ne peuvent pas poser de diagnostic, prescrire un traitement ni confirmer l'aptitude au sport.",
            style = MaterialTheme.typography.bodySmall, color = Slate)
    }
}
