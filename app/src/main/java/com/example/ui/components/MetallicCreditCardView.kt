package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardNetwork
import com.example.model.CardTheme
import com.example.model.CreditCard
import com.example.model.RewardType
import com.example.model.formatCurrency
import com.example.model.formatPercent
import com.example.ui.theme.CardCrimsonEnd
import com.example.ui.theme.CardCrimsonStart
import com.example.ui.theme.CardEmeraldEnd
import com.example.ui.theme.CardEmeraldStart
import com.example.ui.theme.CardGoldEnd
import com.example.ui.theme.CardGoldStart
import com.example.ui.theme.CardObsidianEnd
import com.example.ui.theme.CardObsidianStart
import com.example.ui.theme.CardSapphireEnd
import com.example.ui.theme.CardSapphireStart
import com.example.ui.theme.CardSlateEnd
import com.example.ui.theme.CardSlateStart
import com.example.ui.theme.JetBrainsMono

fun cardThemeGradient(theme: CardTheme): List<Color> = when (theme) {
    CardTheme.OBSIDIAN -> listOf(CardObsidianStart, CardObsidianEnd)
    CardTheme.EMERALD -> listOf(CardEmeraldStart, CardEmeraldEnd)
    CardTheme.SAPPHIRE -> listOf(CardSapphireStart, CardSapphireEnd)
    CardTheme.GOLD -> listOf(CardGoldStart, CardGoldEnd)
    CardTheme.CRIMSON -> listOf(CardCrimsonStart, CardCrimsonEnd)
    CardTheme.SLATE -> listOf(CardSlateStart, CardSlateEnd)
}

/**
 * Interactive metallic FinTech credit card with optional 60fps 3D perspective tilt,
 * dynamic holographic specular sheen, and 180-degree 3D flip showcase.
 */
@Composable
fun MetallicCreditCardView(
    card: CreditCard,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enableInteractive3D: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradientColors = cardThemeGradient(CardTheme.fromRaw(card.cardTheme))
    val shape = RoundedCornerShape(22.dp)
    val density = LocalDensity.current.density

    var targetTiltX by remember { mutableFloatStateOf(0f) }
    var targetTiltY by remember { mutableFloatStateOf(0f) }
    var isFlipped by remember { mutableStateOf(false) }

    val animatedTiltX by animateFloatAsState(
        targetValue = targetTiltX,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "card_tilt_x"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = targetTiltY,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "card_tilt_y"
    )
    val flipRotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessLow),
        label = "card_flip_y"
    )

    val totalRotationY = flipRotationY + animatedTiltY
    val showingBack = totalRotationY > 90f && totalRotationY < 270f

    val interactiveModifier = if (enableInteractive3D) {
        Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        targetTiltX = 0f
                        targetTiltY = 0f
                    },
                    onDragCancel = {
                        targetTiltX = 0f
                        targetTiltY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        targetTiltY = (targetTiltY + dragAmount.x * 0.16f).coerceIn(-18f, 18f)
                        targetTiltX = (targetTiltX - dragAmount.y * 0.16f).coerceIn(-14f, 14f)
                    }
                )
            }
            .clickable { isFlipped = !isFlipped }
    } else if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 168.dp else 210.dp)
            .graphicsLayer {
                rotationX = if (enableInteractive3D) animatedTiltX else 0f
                rotationY = if (enableInteractive3D) totalRotationY else 0f
                cameraDistance = 14f * density
            }
            .clip(shape)
            .then(interactiveModifier)
            .testTag("credit_card_${card.id}"),
        shape = shape,
        shadowElevation = if (enableInteractive3D) 10.dp else 6.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(900f, 700f)
                    )
                )
        ) {
            // Dynamic specular light reflection tracking 3D tilt angle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val shiftX = (animatedTiltY / 18f) * size.width * 0.28f
                val shiftY = (-animatedTiltX / 14f) * size.height * 0.25f

                drawCircle(
                    color = Color.White.copy(alpha = 0.11f),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.88f + shiftX, size.height * 0.14f + shiftY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 0.14f - shiftX * 0.5f, size.height * 0.92f - shiftY * 0.5f)
                )
            }

            if (showingBack) {
                // Mirror back content horizontally so text reads properly at 180 degrees
                CardBackFace(
                    card = card,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                )
            } else {
                CardFrontFace(
                    card = card,
                    compact = compact,
                    enableInteractive3D = enableInteractive3D
                )
            }
        }
    }
}

@Composable
private fun CardFrontFace(
    card: CreditCard,
    compact: Boolean,
    enableInteractive3D: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Issuer + Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.issuer.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.4.sp,
                        color = Color.White.copy(alpha = 0.78f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.cardName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (enableInteractive3D) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Autorenew,
                                contentDescription = "3D tilt and tap to flip",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "3D",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                if (card.sampleData) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFF59E0B).copy(alpha = 0.28f)
                    ) {
                        Text(
                            text = "SAMPLE",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFEF3C7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Middle Row: EMV Metallic Chip + Last 4 Digits + Network
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFDE68A), Color(0xFFD97706))
                            )
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Contactless,
                    contentDescription = "Contactless enabled",
                    tint = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = card.maskedEnding,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        color = Color.White,
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = CardNetwork.fromRaw(card.network).displayName.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            )
        }

        // Bottom Row: Balance / Limit & Utilization
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "STATEMENT BALANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = "${formatCurrency(card.statementBalance, card.currencySymbol)} / ${formatCurrency(card.creditLimit, card.currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DUE DAY ${card.dueDay} • ${card.rules.size} RULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            if (!compact && card.creditLimit > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { card.utilizationRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (card.utilizationRatio > 0.65f) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                    trackColor = Color.White.copy(alpha = 0.22f)
                )
            }
        }
    }
}

@Composable
private fun CardBackFace(
    card: CreditCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Magnetic Stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(Color(0xFF0A0E14).copy(alpha = 0.85f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "BASE REWARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = "${formatPercent(card.baseRewardRatePercent)} ${RewardType.fromRaw(card.rewardType).displayName}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = JetBrainsMono,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BILLING / DUE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = "Day ${card.billingDay} / Day ${card.dueDay}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = JetBrainsMono,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "Zero-CVV Privacy Vault • Tap or drag card to rotate in 3D",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
