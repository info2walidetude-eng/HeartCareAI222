package dz.univ.alger1.heartcareai.data

/** Informations saisies à la création du compte, mémorisées dans le profil. */
data class MedicalProfile(
    val age: Int,
    val sex: String,
    val chestPainType: String
) {
    fun validate() {
        require(age in 1..120) { "Âge invalide : saisissez un âge de 1 à 120 ans." }
        require(sex in setOf("M", "F")) { "Veuillez choisir un sexe." }
        require(chestPainType in setOf("TA", "ATA", "NAP", "ASY")) {
            "Veuillez choisir le type de douleur thoracique."
        }
    }
}

