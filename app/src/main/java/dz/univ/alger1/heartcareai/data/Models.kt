package dz.univ.alger1.heartcareai.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppUser(
    val id: Long,
    val name: String,
    val email: String,
    val medicalProfile: MedicalProfile? = null
)

data class HeartInputs(
    val age: Int,
    val sex: String,
    val chestPainType: String,
    val restingBP: Int,
    val cholesterol: Int,
    val fastingBS: Int,
    val restingECG: String,
    val maxHR: Int,
    val exerciseAngina: String,
    val oldpeak: Double,
    val stSlope: String,
    val pulseBle: Int? = null
) {
    fun toJson(): String = JSONObject().apply {
        put("Age", age)
        put("Sex", sex)
        put("ChestPainType", chestPainType)
        put("RestingBP", restingBP)
        put("Cholesterol", cholesterol)
        put("FastingBS", fastingBS)
        put("RestingECG", restingECG)
        put("MaxHR", maxHR)
        put("ExerciseAngina", exerciseAngina)
        put("Oldpeak", oldpeak)
        put("ST_Slope", stSlope)
        if (pulseBle != null) put("PulseBLE", pulseBle)
    }.toString()

    companion object {
        fun fromJson(raw: String): HeartInputs {
            val json = JSONObject(raw)
            return HeartInputs(
                age = json.getInt("Age"),
                sex = json.getString("Sex"),
                chestPainType = json.getString("ChestPainType"),
                restingBP = json.getInt("RestingBP"),
                cholesterol = json.getInt("Cholesterol"),
                fastingBS = json.getInt("FastingBS"),
                restingECG = json.getString("RestingECG"),
                maxHR = json.getInt("MaxHR"),
                exerciseAngina = json.getString("ExerciseAngina"),
                oldpeak = json.getDouble("Oldpeak"),
                stSlope = json.getString("ST_Slope"),
                pulseBle = if (json.has("PulseBLE")) json.getInt("PulseBLE") else null
            )
        }
    }
}

data class Assessment(
    val id: Long,
    val userId: Long,
    val input: HeartInputs,
    val score: Double?, // score de classification du modèle, non probabilité clinique
    val source: String,
    val createdAt: Long
)

fun formatDate(epoch: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(epoch))
