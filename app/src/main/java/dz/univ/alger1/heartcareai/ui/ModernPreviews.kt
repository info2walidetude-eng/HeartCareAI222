package dz.univ.alger1.heartcareai.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dz.univ.alger1.heartcareai.ble.SensorState
import dz.univ.alger1.heartcareai.data.AppUser

/** Prévisualisations accessibles dans Android Studio sans démarrer l'émulateur. */
@Preview(name = "Connexion", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun AuthenticationPreview() {
    HeartCareTheme {
        AuthScreen(
            isRegistering = false,
            error = "",
            onToggle = {},
            onSubmit = { _, _, _, _ -> }
        )
    }
}

@Preview(name = "Créer un compte · profil", showBackground = true, widthDp = 393, heightDp = 850)
@Composable
private fun RegistrationPreview() {
    HeartCareTheme {
        AuthScreen(
            isRegistering = true,
            error = "",
            onToggle = {},
            onSubmit = { _, _, _, _ -> }
        )
    }
}

@Preview(name = "Tableau de bord", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun HomePreview() {
    HeartCareTheme {
        HomeScreen(
            modifier = androidx.compose.ui.Modifier,
            user = AppUser(1L, "Étudiant Démo", "demo@example.test"),
            records = emptyList(),
            bleState = SensorState(),
            modelReady = false,
            onEvaluate = {},
            onHistory = {},
            onSensor = {},
            onSelect = {},
            onGuide = {}
        )
    }
}

@Preview(name = "Capteurs BLE · Catalogue", showBackground = true, widthDp = 393, heightDp = 850)
@Composable
private fun BleSensorPreview() {
    HeartCareTheme {
        SensorScreen(
            state = SensorState(message = "Aucun appareil connecté"),
            onScan = {},
            onConnect = {},
            onStop = {}
        )
    }
}
