package dz.univ.alger1.heartcareai.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import android.util.Patterns
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Stockage de démonstration : SQLite sur un seul appareil, sans serveur.
 * Les mots de passe sont dérivés avec PBKDF2 (sel aléatoire par compte).
 * Les données médicales ne sont PAS chiffrées au repos : ne pas utiliser
 * avec de vraies données de patients ni diffuser comme service de santé.
 */
class LocalStore(private val context: Context) : SQLiteOpenHelper(context, "heartcare_local.db", null, 2) {
    private val session = context.getSharedPreferences("heartcare_session", Context.MODE_PRIVATE)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              email TEXT NOT NULL UNIQUE COLLATE NOCASE,
              password_hash TEXT NOT NULL,
              salt TEXT NOT NULL,
              age INTEGER,
              sex TEXT,
              chest_pain_type TEXT
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE assessments (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              user_id INTEGER NOT NULL,
              payload TEXT NOT NULL,
              score REAL,
              source TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_assessment_owner ON assessments(user_id, created_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration sans suppression des comptes et des évaluations existantes.
        // Les utilisateurs V1 devront compléter leur profil une seule fois.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN age INTEGER")
            db.execSQL("ALTER TABLE users ADD COLUMN sex TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN chest_pain_type TEXT")
        }
    }

    fun createUser(name: String, email: String, password: String, medicalProfile: MedicalProfile): AppUser {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        require(cleanName.length >= 2) { "Nom : au moins deux caractères." }
        require(Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) { "Adresse e-mail invalide." }
        require(password.length >= 8) { "Mot de passe : au moins huit caractères." }
        medicalProfile.validate()
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cv = ContentValues().apply {
            put("name", cleanName)
            put("email", cleanEmail)
            put("salt", Base64.encodeToString(saltBytes, Base64.NO_WRAP))
            put("password_hash", hash(password, saltBytes))
            put("age", medicalProfile.age)
            put("sex", medicalProfile.sex)
            put("chest_pain_type", medicalProfile.chestPainType)
        }
        val id = writableDatabase.insertWithOnConflict(
            "users", null, cv, SQLiteDatabase.CONFLICT_IGNORE
        )
        require(id != -1L) { "Cet e-mail est déjà enregistré." }
        session.edit().putLong("current_user", id).apply()
        return requireNotNull(getUser(id))
    }

    fun login(email: String, password: String): AppUser {
        val cleanEmail = email.trim().lowercase()
        val cursor = readableDatabase.query(
            "users", arrayOf("id", "name", "email", "salt", "password_hash"),
            "email = ?", arrayOf(cleanEmail), null, null, null
        )
        cursor.use {
            require(it.moveToFirst()) { "E-mail ou mot de passe incorrect." }
            val salt = Base64.decode(it.getString(it.getColumnIndexOrThrow("salt")), Base64.NO_WRAP)
            val candidate = hash(password, salt)
            val stored = it.getString(it.getColumnIndexOrThrow("password_hash"))
            require(MessageDigest.isEqual(candidate.toByteArray(), stored.toByteArray())) {
                "E-mail ou mot de passe incorrect."
            }
            val id = it.getLong(it.getColumnIndexOrThrow("id"))
            session.edit().putLong("current_user", id).apply()
            return requireNotNull(getUser(id))
        }
    }

    fun currentUser(): AppUser? {
        val id = session.getLong("current_user", -1)
        return if (id <= 0) null else getUser(id)
    }

    fun getUser(id: Long): AppUser? {
        readableDatabase.query("users",
            arrayOf("id", "name", "email", "age", "sex", "chest_pain_type"),
            "id = ?", arrayOf(id.toString()), null, null, null).use { c ->
            if (!c.moveToFirst()) return null
            val profile = if (!c.isNull(3) && !c.isNull(4) && !c.isNull(5)) {
                MedicalProfile(c.getInt(3), c.getString(4), c.getString(5))
            } else null
            return AppUser(c.getLong(0), c.getString(1), c.getString(2), profile)
        }
    }

    /** Uniquement pour les comptes créés avant la mise à jour V2.1. */
    fun completeMedicalProfileOnce(userId: Long, profile: MedicalProfile): AppUser {
        profile.validate()
        val values = ContentValues().apply {
            put("age", profile.age)
            put("sex", profile.sex)
            put("chest_pain_type", profile.chestPainType)
        }
        val count = writableDatabase.update("users", values,
            "id = ? AND age IS NULL AND sex IS NULL AND chest_pain_type IS NULL",
            arrayOf(userId.toString()))
        require(count == 1) { "Le profil est déjà enregistré ou le compte est introuvable." }
        return requireNotNull(getUser(userId))
    }

    fun updateName(userId: Long, name: String): AppUser {
        val trimmed = name.trim()
        require(trimmed.length >= 2) { "Nom trop court." }
        val values = ContentValues().apply { put("name", trimmed) }
        require(writableDatabase.update("users", values, "id = ?", arrayOf(userId.toString())) == 1) {
            "Compte introuvable."
        }
        return requireNotNull(getUser(userId))
    }

    fun logout() { session.edit().remove("current_user").apply() }

    fun deleteAccount(userId: Long) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("assessments", "user_id = ?", arrayOf(userId.toString()))
            writableDatabase.delete("users", "id = ?", arrayOf(userId.toString()))
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
        logout()
    }

    fun saveAssessment(userId: Long, data: HeartInputs, score: Double?, source: String, editId: Long? = null): Long {
        val profile = requireNotNull(getUser(userId)?.medicalProfile) {
            "Complétez les informations de votre profil avant une évaluation."
        }
        require(data.age == profile.age && data.sex == profile.sex && data.chestPainType == profile.chestPainType) {
            "Les trois informations du profil doivent rester identiques dans chaque bilan."
        }
        val values = ContentValues().apply {
            put("payload", data.toJson())
            if (score == null) putNull("score") else put("score", score)
            put("source", source)
        }
        return if (editId == null) {
            values.put("user_id", userId)
            values.put("created_at", System.currentTimeMillis())
            val id = writableDatabase.insert("assessments", null, values)
            require(id > 0) { "Impossible d'enregistrer l'évaluation." }
            id
        } else {
            val count = writableDatabase.update("assessments", values,
                "id = ? AND user_id = ?", arrayOf(editId.toString(), userId.toString()))
            require(count == 1) { "Évaluation introuvable." }
            editId
        }
    }

    fun listAssessments(userId: Long): List<Assessment> {
        val result = mutableListOf<Assessment>()
        readableDatabase.query("assessments", null, "user_id = ?", arrayOf(userId.toString()),
            null, null, "created_at DESC, id DESC").use { c ->
            while (c.moveToNext()) result.add(mapAssessment(c))
        }
        return result
    }

    fun getAssessment(userId: Long, id: Long): Assessment? {
        readableDatabase.query("assessments", null, "user_id = ? AND id = ?",
            arrayOf(userId.toString(), id.toString()), null, null, null).use { c ->
            return if (c.moveToFirst()) mapAssessment(c) else null
        }
    }

    fun deleteAssessment(userId: Long, id: Long): Boolean =
        writableDatabase.delete("assessments", "id = ? AND user_id = ?",
            arrayOf(id.toString(), userId.toString())) == 1

    private fun mapAssessment(c: Cursor): Assessment {
        val scoreIdx = c.getColumnIndexOrThrow("score")
        return Assessment(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            userId = c.getLong(c.getColumnIndexOrThrow("user_id")),
            input = HeartInputs.fromJson(c.getString(c.getColumnIndexOrThrow("payload"))),
            score = if (c.isNull(scoreIdx)) null else c.getDouble(scoreIdx),
            source = c.getString(c.getColumnIndexOrThrow("source")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 150_000, 256)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally { spec.clearPassword() }
    }
}
