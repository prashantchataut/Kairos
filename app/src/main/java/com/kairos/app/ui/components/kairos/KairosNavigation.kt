package com.kairos.app.ui.components.kairos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.navigation.BottomNavItem
import com.kairos.app.ui.theme.KairosMotion
import com.kairos.app.ui.theme.KairosTheme

private val NavCapsuleShape = RoundedCornerShape(36.dp)
private val RailShape = RoundedCornerShape(28.dp)
private val InactiveSize = 48.dp
private val ActiveWidth = 92.dp
private val ActiveWidthVertical = 76.dp

/**
 * Floating frosted-glass navigation capsule with the "expanding active tab"
 * language: inactive destinations are circular icons; the active one expands
 * into a bright blue capsule carrying icon + label. The capsule floats above
 * content with breathing room underneath.
 */
@Composable
fun KairosBottomNavigation(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = KairosTheme.liquidGlass
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        FrostedCapsule(
            shape = NavCapsuleShape,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = selectedRoute == item.destinationRoute
                    KairosNavigationItem(
                        item = item,
                        selected = selected,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Frosted-glass navigation rail for expanded widths (tablets).
 */
@Composable
fun KairosNavigationRail(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(112.dp)
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FrostedCapsule(
            shape = RailShape,
            modifier = Modifier
                .fillMaxHeight()
                .width(92.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                items.forEach { item ->
                    val selected = selectedRoute == item.destinationRoute
                    KairosNavigationItem(
                        item = item,
                        selected = selected,
                        onClick = { onSelect(item) },
                        vertical = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * The frosted glass shell: translucent fill, hairline border with a bright top
 * edge, soft tinted shadow, and a gentle top sheen.
 */
@Composable
private fun FrostedCapsule(
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glass = KairosTheme.liquidGlass
    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = glass.shadow,
                spotColor = glass.shadow
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(glass.fill, glass.fillDeep),
                    startY = 0f,
                    endY = 1400f
                )
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(glass.highlight, glass.border)
                    )
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(glass.sheen, Color.Transparent),
                        startY = 0f,
                        endY = 240f
                    )
                )
        )
        content()
    }
}

@Composable
private fun KairosNavigationItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val pillColor by animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = tween(KairosMotion.state),
        label = "navigation-pill"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        animationSpec = tween(KairosMotion.quick),
        label = "navigation-content"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selected) (if (vertical) ActiveWidthVertical else ActiveWidth) else InactiveSize,
        animationSpec = tween(KairosMotion.state),
        label = "navigation-pill-width"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(KairosMotion.quick),
        label = "navigation-label-alpha"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics {
                this.selected = selected
                role = Role.Tab
            },
        shape = RoundedCornerShape(28.dp),
        color = pillColor,
        contentColor = contentColor
    ) {
        if (vertical) {
            Column(
                modifier = Modifier
                    .width(pillWidth)
                    .height(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null,
                            modifier = Modifier.size(21.dp)
                        )
                        if (selected) {
                            Text(
                                text = stringResource(item.labelResId),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.alpha(labelAlpha)
                            )
                        }
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .width(pillWidth),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = stringResource(item.contentDescriptionResId),
                    modifier = Modifier.size(22.dp)
                )
                if (selected) {
                    Text(
                        text = stringResource(item.labelResId),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .alpha(labelAlpha)
                    )
                }
            }
        }
    }
}
