package dz.univ.alger1.heartcareai.data

import java.util.Calendar
import java.util.TimeZone

/** Pure Kotlin: no Android SDK needed. Values are observations, not medical diagnoses. */
data class HomePoint(val at: Long, val value: Int)

data class HomeSnapshot(
    val restingBp: Int,
    val cholesterol: Int,
    val fastingBsOver120: Boolean,
    val exerciseAngina: Boolean
)

data class HeartEducationTip(val title: String, val message: String, val needsAttention: Boolean = false)

object HomeInsights {
    fun isToday(timestamp: Long, now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Boolean {
        val start = Calendar.getInstance(zone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrow = Calendar.getInstance(zone).apply {
            timeInMillis = start
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        return timestamp >= start && timestamp < tomorrow
    }

    fun lastPoints(items: List<HomePoint>, count: Int = 7): List<HomePoint> =
        items.sortedBy { it.at }.takeLast(count.coerceAtLeast(0))

    /** Never infer a heart diagnosis or fitness clearance from unvalidated prediction scores. */
    fun education(snapshot: HomeSnapshot?): List<HeartEducationTip> {
        if (snapshot == null) return listOf(
            HeartEducationTip("Commencez votre suivi", "Enregistrez un premier bilan pour consulter votre évolution."),
            HeartEducationTip("Des habitudes protectrices", "Privilégiez une alimentation variée, le sommeil et une activité adaptée à votre état de santé."),
            HeartEducationTip("Parlez à un professionnel", "Les bilans et conseils de l'application ne remplacent pas un avis médical.")
        )
        val tips = mutableListOf<HeartEducationTip>()
        if (snapshot.exerciseAngina) {
            tips += HeartEducationTip(
                "Symptômes lors de l'effort",
                "Vous avez signalé une gêne de type angine à l'effort. Évitez de forcer et parlez-en à un professionnel avant de reprendre un effort intense. En cas de douleur thoracique actuelle, surtout avec essoufflement ou malaise, appelez les urgences.",
                needsAttention = true
            )
        }
        if (snapshot.restingBp >= 140) {
            tips += HeartEducationTip(
                "Vérifier la tension",
                "Une valeur de tension systolique au repos de ${snapshot.restingBp} mmHg a été saisie. Une mesure isolée ne suffit pas pour conclure : reprenez-la correctement au repos et faites interpréter des valeurs répétées par un professionnel.",
                needsAttention = true
            )
        } else {
            tips += HeartEducationTip(
                "Suivre la tension au repos",
                "Votre bilan indique ${snapshot.restingBp} mmHg pour la tension systolique au repos. Comparez des mesures prises dans des conditions similaires, sans conclure sur la base d'une seule valeur."
            )
        }
        if (snapshot.fastingBsOver120) {
            tips += HeartEducationTip(
                "Glycémie à faire interpréter",
                "Le bilan indique une glycémie à jeun classée au-dessus de 120 mg/dL. Cette case ne permet pas de diagnostiquer un diabète : demandez un avis et des examens appropriés."
            )
        } else if (snapshot.cholesterol > 200) {
            tips += HeartEducationTip(
                "Mieux comprendre le cholestérol",
                "Un taux de cholestérol total de ${snapshot.cholesterol} mg/dL figure dans le bilan. Son interprétation nécessite le contexte médical et, si besoin, un bilan lipidique complet."
            )
        }
        val habit = HeartEducationTip(
            "Un geste pour votre cœur",
            "Adoptez une alimentation variée, évitez le tabac et privilégiez une activité physique adaptée à votre état de santé et aux recommandations de votre soignant."
        )
        return tips.take(2) + habit
    }
}
