package dz.univ.alger1.heartcareai.ml

import android.content.Context
import dz.univ.alger1.heartcareai.data.HeartInputs
import org.json.JSONObject
import kotlin.math.exp

/**
 * Inférence hors-ligne du modèle scikit-learn exporté par ml/train_export.py.
 * Si heart_model.json n'existe pas, aucune prédiction artificielle n'est produite.
 */
class OnDevicePredictor(context: Context) {
    private val model: JSONObject? = try {
        JSONObject(context.assets.open("heart_model.json").bufferedReader().use { it.readText() })
    } catch (_: Exception) { null }

    val isReady: Boolean get() = model != null
    val dataCount: Int get() = model?.optInt("training_rows") ?: 0
    val testAccuracy: Double? get() = model?.optDouble("test_accuracy")?.takeUnless { it.isNaN() }

    fun score(input: HeartInputs): Double? {
        val m = model ?: return null
        val features = m.getJSONArray("features")
        val mean = m.getJSONArray("means")
        val scale = m.getJSONArray("scales")
        val coef = m.getJSONArray("coefficients")
        val raw = buildFeatures(input)
        var logit = m.getDouble("intercept")
        for (i in 0 until features.length()) {
            val key = features.getString(i)
            val x = raw[key] ?: error("Caractéristique de modèle inconnue : $key")
            val sigma = scale.getDouble(i).let { if (it == 0.0) 1.0 else it }
            logit += ((x - mean.getDouble(i)) / sigma) * coef.getDouble(i)
        }
        return 1.0 / (1.0 + exp(-logit.coerceIn(-40.0, 40.0)))
    }

    private fun buildFeatures(input: HeartInputs): Map<String, Double> {
        val values = linkedMapOf(
            "Age" to input.age.toDouble(),
            "RestingBP" to input.restingBP.toDouble(),
            "Cholesterol" to input.cholesterol.toDouble(),
            "FastingBS" to input.fastingBS.toDouble(),
            "MaxHR" to input.maxHR.toDouble(),
            "Oldpeak" to input.oldpeak
        )
        mapOf(
            "Sex" to input.sex,
            "ChestPainType" to input.chestPainType,
            "RestingECG" to input.restingECG,
            "ExerciseAngina" to input.exerciseAngina,
            "ST_Slope" to input.stSlope
        ).forEach { (name, chosen) ->
            val categories = when (name) {
                "Sex" -> listOf("M", "F")
                "ChestPainType" -> listOf("TA", "ATA", "NAP", "ASY")
                "RestingECG" -> listOf("Normal", "ST", "LVH")
                "ExerciseAngina" -> listOf("Y", "N")
                else -> listOf("Up", "Flat", "Down")
            }
            categories.forEach { category ->
                values["$name=$category"] = if (chosen == category) 1.0 else 0.0
            }
        }
        return values
    }
}
