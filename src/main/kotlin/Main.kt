import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gui.views.mapfView

fun main() {
    composeMain()
}

fun composeMain() {
    // Compose for Desktop entry point
    application {
        Window(onCloseRequest = ::exitApplication) {
            appTheme {
                mapfView()
            }
        }
    }
}

@Composable
fun appTheme(content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = Color(0xff715189),
        primaryVariant = Color(0xfff2daff),
        secondary = Color(0xff675a6e),
        secondaryVariant = Color(0xffeeddf5),
        background = Color(0xFFF7FBF2),
        surface = Color(0xfffff7fd),
        error = Color(0xffba1a1a),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color(0xFF181D18),
        onSurface = Color(0xff1e1a20),
        onError = Color.White
    )

    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}