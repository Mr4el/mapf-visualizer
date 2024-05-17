package gui.components.mapf

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import gui.Constants.TARGET_SIZE_RELATIVE_TO_CELL
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.Constants.ARROW_HEAD_ANGLE
import gui.Constants.ARROW_HEAD_LENGTH_PX
import gui.Constants.ARROW_RADIUS_PX
import gui.Constants.ARROW_SHORTEN_LENGTH_PX
import gui.Constants.ARROW_WIDTH_PX
import gui.Constants.TRANSITION_DURATION_MS
import gui.style.CustomColors.BLACK
import kotlinx.coroutines.launch
import problem.Agent
import problem.obj.Point
import problem.obj.Point.Companion.equal
import kotlin.math.*

@Composable
fun agentData(scaledCellSize: Dp, scale: Float, currentTimeStep: Int, agents: Set<Agent>) {
    agents.forEach { agent ->
        agent.drawPaths(currentTimeStep, scaledCellSize, scale)
    }

    agents.forEach { agent ->
        agent.drawStartPoint(scaledCellSize, scale)
        agent.drawTargetPoint(scaledCellSize, scale)
    }

    agents.forEach { agent ->
        animateAgentToStep(
            scaledCellSize = scaledCellSize,
            scale = scale,
            agent = agent,
            currentStep = currentTimeStep,
        )
    }
}

@Composable
private fun animateAgentToStep(scaledCellSize: Dp, scale: Float, agent: Agent, currentStep: Int) {
    val animatedX = remember(agent) { Animatable(agent.startPosition.x.toFloat()) }
    val animatedY = remember(agent) { Animatable(agent.startPosition.y.toFloat()) }
    val targetPoint = agent.path.timeStepPosition(currentStep)

    LaunchedEffect(currentStep) {
        launch {
            animatedX.animateTo(
                targetValue = targetPoint.x.toFloat(),
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION_MS,
                    easing = LinearEasing,
                )
            )
        }
        launch {
            animatedY.animateTo(
                targetValue = targetPoint.y.toFloat(),
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION_MS,
                    easing = LinearEasing,
                )
            )
        }
    }

    agent.drawAgent(animatedX, animatedY, scaledCellSize, scale)
}

@Composable
private fun Agent.drawPaths(
    currentStep: Int,
    scaledCellSize: Dp,
    scale: Float,
) {
    if (this.showPath) {
        val pathLength = this.path.length

        var anglePoint = Point(-1, -1)
        for (i in currentStep until pathLength - 1) {
            val currentPoint = this.path.timeStepPosition(i)
            val (nextPoint, pointIndex) = this.path.takeNextCell(i)
            val afterNextPoint = this.path.takeNextCellOrNull(pointIndex)

            val isNextAngle = afterNextPoint?.let {
                val dx = currentPoint.x - afterNextPoint.x
                val dy = currentPoint.y - afterNextPoint.y

                return@let dx != 0 && dy != 0
            } ?: false

            if (isNextAngle) {
                arrowRounder(
                    scale = scale,
                    scaledCellSize = scaledCellSize,
                    startPoint = currentPoint,
                    middlePoint = nextPoint,
                    endPoint = afterNextPoint!!,
                    color = this.primaryColor,
                    hasArrow = i + 3 == pathLength,
                )

                anglePoint = nextPoint
            } else if (!anglePoint.equal(currentPoint) && !anglePoint.equal(nextPoint)) {
                arrowLine(
                    scale = scale,
                    scaledCellSize = scaledCellSize,
                    startPoint = currentPoint,
                    endPoint = nextPoint,
                    color = this.primaryColor,
                    hasArrow = i + 2 == pathLength,
                )
            }
        }
    }
}

@Composable
fun arrowLine(
    scale: Float,
    scaledCellSize: Dp,
    startPoint: Point,
    endPoint: Point,
    color: Color,
    hasArrow: Boolean,
) {
    val compensator = scaledCellSize / 2
    val start = startPoint.toGridPoint(scaledCellSize, compensator)
    val end = endPoint.toGridPoint(scaledCellSize, compensator)
    val shortenedEnd = shortenLine(scale, start, end, ARROW_SHORTEN_LENGTH_PX)

    Box(modifier = Modifier.drawBehind {
        val path = Path().apply {
            moveTo(start.floatX, start.floatY)
            if (hasArrow) lineTo(shortenedEnd.floatX, shortenedEnd.floatY) else lineTo(end.floatX, end.floatY)
        }

        drawIntoCanvas { canvas ->
            canvas.drawPath(
                path = path,
                paint = Paint().apply {
                    this.color = color
                    this.strokeWidth = ARROW_WIDTH_PX * scale
                    this.isAntiAlias = true
                    this.style = PaintingStyle.Stroke
                }
            )

            if (hasArrow) {
                drawArrowHead(scale, shortenedEnd, start, color)
            }
        }
    })
}

@Composable
fun arrowRounder(
    scale: Float,
    scaledCellSize: Dp,
    startPoint: Point,
    middlePoint: Point,
    endPoint: Point,
    color: Color,
    hasArrow: Boolean,
) {
    val compensator = scaledCellSize / 2
    val start = startPoint.toGridPoint(scaledCellSize, compensator)
    val middle = middlePoint.toGridPoint(scaledCellSize, compensator)
    val end = endPoint.toGridPoint(scaledCellSize, compensator)
    val shortenedEnd = shortenLine(scale, middle, end, ARROW_SHORTEN_LENGTH_PX)

    Box(modifier = Modifier.drawBehind {
        val path = Path().apply {
            moveTo(start.floatX, start.floatY)

            val shortenedFromStartToMiddle = shortenLine(scale, start, middle, ARROW_RADIUS_PX)
            val shortenedFromEndToMiddle = shortenLine(scale, end, middle, ARROW_RADIUS_PX)

            lineTo(shortenedFromStartToMiddle.floatX, shortenedFromStartToMiddle.floatY)
            quadraticBezierTo(
                middle.floatX,
                middle.floatY,
                shortenedFromEndToMiddle.floatX,
                shortenedFromEndToMiddle.floatY
            )

            if (hasArrow) lineTo(shortenedEnd.floatX, shortenedEnd.floatY) else lineTo(end.floatX, end.floatY)
        }

        drawIntoCanvas { canvas ->
            canvas.drawPath(
                path = path,
                paint = Paint().apply {
                    this.color = color
                    this.strokeWidth = ARROW_WIDTH_PX * scale
                    this.isAntiAlias = true
                    this.style = PaintingStyle.Stroke
                }
            )

            if (hasArrow) {
                drawArrowHead(scale, shortenedEnd, middle, color)
            }
        }
    })
}

fun DrawScope.drawArrowHead(
    scale: Float,
    endPointGrid: Point,
    startPointGrid: Point,
    color: Color,
) {
    val angle = atan2((endPointGrid.y - startPointGrid.y).toDouble(), (endPointGrid.x - startPointGrid.x).toDouble())

    val arrowHeadPath = Path().apply {
        moveTo(endPointGrid.floatX, endPointGrid.floatY)
        lineTo(
            x = endPointGrid.x - ARROW_HEAD_LENGTH_PX * scale * cos(angle - ARROW_HEAD_ANGLE).toFloat(),
            y = endPointGrid.y - ARROW_HEAD_LENGTH_PX * scale * sin(angle - ARROW_HEAD_ANGLE).toFloat()
        )
        moveTo(endPointGrid.floatX, endPointGrid.floatY)
        lineTo(
            x = endPointGrid.x - ARROW_HEAD_LENGTH_PX * scale * cos(angle + ARROW_HEAD_ANGLE).toFloat(),
            y = endPointGrid.y - ARROW_HEAD_LENGTH_PX * scale * sin(angle + ARROW_HEAD_ANGLE).toFloat()
        )
    }

    drawPath(
        path = arrowHeadPath,
        color = color,
        style = Stroke(
            width = ARROW_WIDTH_PX * scale,
            cap = StrokeCap.Round,
        )
    )
}

fun shortenLine(scale: Float, start: Point, end: Point, shortenBy: Float): Point {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = sqrt((dx * dx + dy * dy).toDouble())
    val ratio = (length - shortenBy * scale) / length
    return Point(
        (start.x + dx * ratio).toInt(),
        (start.y + dy * ratio).toInt()
    )
}

@Composable
private fun Agent.drawStartPoint(
    scaledCellSize: Dp,
    scale: Float,
) {
    val agentName = this.name
    val agentSecondaryColor = this.secondaryColor

    val compensator = scaledCellSize * (1 - TARGET_SIZE_RELATIVE_TO_CELL) / 2
    val start = this.startPosition.toGridPoint(scaledCellSize, compensator)

    Box(
        modifier = Modifier
            .offset(start.dpX, start.dpY)
            .size(scaledCellSize * TARGET_SIZE_RELATIVE_TO_CELL)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path().apply {
                val width = size.width
                val height = size.height
                moveTo(width / 2, 0f)
                lineTo(width, height / 2)
                lineTo(width / 2, height)
                lineTo(0f, height / 2)
                close()
            }
            drawPath(path = path, color = agentSecondaryColor)
            drawPath(
                path = path,
                color = BLACK,
                style = Stroke(
                    width = (2.dp * scale).toPx(),
                    pathEffect = dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f),
                )
            )
        }

        Text(
            text = agentName,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = (12.sp)),
            modifier = Modifier
                .scale(scale)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun Agent.drawTargetPoint(
    scaledCellSize: Dp,
    scale: Float,
) {
    val agentName = this.name
    val agentSecondaryColor = this.secondaryColor

    val compensator = scaledCellSize * (1 - TARGET_SIZE_RELATIVE_TO_CELL) / 2
    val target = this.targetPosition.toGridPoint(scaledCellSize, compensator)

    Box(
        modifier = Modifier
            .offset(target.dpX, target.dpY)
            .size(scaledCellSize * TARGET_SIZE_RELATIVE_TO_CELL)
            .drawBehind {
                drawRoundRect(
                    color = BLACK,
                    size = this.size,
                    cornerRadius = CornerRadius(this.size.width / 2, this.size.height / 2),
                    style = Stroke(
                        width = (4.dp * scale).toPx(),
                        pathEffect = dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f),
                    )
                )
            }
            .background(
                color = agentSecondaryColor,
                shape = CircleShape
            )
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = agentName,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .scale(scale)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun Agent.drawAgent(
    animatedX: Animatable<Float, AnimationVector1D>,
    animatedY: Animatable<Float, AnimationVector1D>,
    scaledCellSize: Dp,
    scale: Float,
) {
    val agentName = this.name
    val agentPrimaryColor = this.primaryColor

    val compensator = scaledCellSize * (1 - TARGET_SIZE_RELATIVE_TO_CELL) / 2
    val currentX = animatedX.value.dp * scaledCellSize.value + compensator
    val currentY = animatedY.value.dp * scaledCellSize.value + compensator

    Box(
        modifier = Modifier
            .offset(currentX - 1.dp, currentY - 1.dp)
            .size(scaledCellSize * TARGET_SIZE_RELATIVE_TO_CELL)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path().apply {
                val width = size.width
                val height = size.height
                moveTo(width / 2, 0f)
                lineTo(width, height / 2)
                lineTo(width / 2, height)
                lineTo(0f, height / 2)
                close()
            }
            drawPath(path = path, color = agentPrimaryColor)
            drawPath(
                path = path,
                color = BLACK,
                style = Stroke(width = (2.dp * scale).toPx())
            )
        }

        Text(
            text = agentName,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = (12.sp)),
            modifier = Modifier
                .scale(scale)
                .align(Alignment.Center)
        )
    }
}
