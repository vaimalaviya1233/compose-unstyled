package com.composables.core

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
public fun rememberFocusRingIndication(
    ringColor: Color = Color.Unspecified,
    ringWidth: Dp = Dp.Unspecified,
    paddingValues: PaddingValues = PaddingValues(),
    cornerRadius: Dp
): Indication {
    return remember { FocusRingIndicationNodeFactory(ringColor, ringWidth, paddingValues, cornerRadius) }
}

internal class FocusRingIndicationNodeFactory internal constructor(
    private val ringColor: Color,
    private val strokeWidth: Dp,
    private val paddingValues: PaddingValues,
    private val cornerRadius: Dp,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return FocusRingIndicationNode(interactionSource, ringColor, strokeWidth, paddingValues, cornerRadius)
    }

    internal class FocusRingIndicationNode internal constructor(
        private val interactionSource: InteractionSource,
        private val ringColor: Color,
        private val strokeWidth: Dp,
        private val paddingValues: PaddingValues,
        private val cornerRadius: Dp,
    ) : Modifier.Node(), DrawModifierNode {

        var isFocused by mutableStateOf(false)


        override fun ContentDrawScope.draw() {
            drawContent()
            if (isFocused) {
                val cornerRadiusObj = CornerRadius(cornerRadius.toPx())
                val ringWidthFloat = strokeWidth.toPx()

                val paddingFloatTop = paddingValues.calculateTopPadding().toPx()
                val paddingFloatBottom = paddingValues.calculateBottomPadding().toPx()
                val paddingFloatStart = paddingValues.calculateStartPadding(layoutDirection).toPx()
                val paddingFloatEnd = paddingValues.calculateEndPadding(layoutDirection).toPx()

                val ringSize = Size(
                    width = size.width + paddingFloatStart + paddingFloatEnd,
                    height = size.height + paddingFloatTop + paddingFloatBottom
                )

                val topLeft = Offset(-paddingFloatStart, -paddingFloatTop)

                val ringPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(offset = topLeft, size = ringSize),
                            cornerRadius = cornerRadiusObj
                        )
                    )
                }
                drawPath(ringPath, color = ringColor, style = Stroke(width = ringWidthFloat))
            }
        }

        override fun onAttach() {
            coroutineScope.launch {
                interactionSource.interactions.collectLatest { interaction ->
                    when (interaction) {
                        is FocusInteraction.Focus -> isFocused = true
                        is FocusInteraction.Unfocus -> isFocused = true
                    }
                }
            }
            super.onAttach()
        }
    }

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this
}
