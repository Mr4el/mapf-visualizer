import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import gui.style.CustomColors.BACKGROUND
import gui.style.CustomColors.ERROR
import gui.style.CustomColors.ON_BACKGROUND
import gui.style.CustomColors.ON_ERROR
import gui.style.CustomColors.ON_PRIMARY
import gui.style.CustomColors.ON_SECONDARY
import gui.style.CustomColors.ON_SURFACE
import gui.style.CustomColors.PRIMARY
import gui.style.CustomColors.PRIMARY_VARIANT
import gui.style.CustomColors.SECONDARY
import gui.style.CustomColors.SECONDARY_VARIANT
import gui.style.CustomColors.SURFACE
import gui.views.mapfView

fun main() {
    composeMain()
}

fun composeMain() {
    // Compose for Desktop entry point
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            appTheme {
                mapfView()
            }
        }
    }
}

@Composable
fun appTheme(content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = PRIMARY,
        primaryVariant = PRIMARY_VARIANT,
        secondary = SECONDARY,
        secondaryVariant = SECONDARY_VARIANT,
        background = BACKGROUND,
        surface = SURFACE,
        error = ERROR,
        onPrimary = ON_PRIMARY,
        onSecondary = ON_SECONDARY,
        onBackground = ON_BACKGROUND,
        onSurface = ON_SURFACE,
        onError = ON_ERROR,
    )

    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}