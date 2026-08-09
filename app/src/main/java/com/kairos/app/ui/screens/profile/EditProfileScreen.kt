package com.kairos.app.ui.screens.profile
import com.kairos.app.ui.icons.KairosIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.kairos.app.ui.theme.isDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.R
import com.kairos.app.ui.components.KairosIconButton
import com.kairos.app.ui.theme.*

import kotlinx.coroutines.delay

/**
 * Edit Profile Screen
 *
 * Design Philosophy:
 * - Minimalism & Cleanliness: Flat, modern, compact design
 * - Clean typography with Poppins font family
 * - Vibrant neon green (#36F97F) for accents
 * - Accessibility: 48dp minimum touch targets
 */

// Design System Colors for Edit Profile — Moment Blue
private object EditProfileColors {
    // Dark Mode — charcoal ink-navy
    val BackgroundDark = Color(0xFF0B0E15)
    val CardBackgroundDark = Color(0xFF11161F)
    val CardBackgroundElevatedDark = Color(0xFF1E2636)
    val AccentGreen = Color(0xFF8FA6FF)      // Periwinkle (night)
    val TextPrimaryDark = Color(0xFFE9EDF6)
    val TextSecondaryDark = Color(0xFF9AA5BE)
    val TextTertiaryDark = Color(0xFF6C7690)
    val BorderDark = Color(0xFF2A3345)
    val InputBackgroundDark = Color(0xFF11161F)

    // Light Mode — pearl
    val BackgroundLight = Color(0xFFF5F7FC)
    val CardBackgroundLight = Color(0xFFFBFCFE)
    val CardBackgroundElevatedLight = Color(0xFFE7ECF7)
    val AccentGreenLight = Color(0xFF2E5BFF) // Royal blue
    val TextPrimaryLight = Color(0xFF101828)
    val TextSecondaryLight = Color(0xFF5A6478)
    val TextTertiaryLight = Color(0xFF8B93A7)
    val BorderLight = Color(0xFFD8DFEC)
    val InputBackgroundLight = Color(0xFFFBFCFE)

    // Locked state
    val LockedOverlay = Color(0x80000000)
    val LockedIcon = Color(0xFF6C7690)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBannerSelection: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkMode = isDarkTheme()

    // Handle back navigation with unsaved changes check
    val handleBack: () -> Unit = {
        if (viewModel.handleBackNavigation()) {
            onNavigateBack()
        }
    }

    // Handle save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            delay(300)
            onNavigateBack()
        }
    }

    // Focus management for keyboard navigation
    val bioFocusRequester = remember { FocusRequester() }

    // Discard changes confirmation dialog
    if (uiState.showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = { viewModel.hideDiscardDialog() },
            onConfirm = {
                viewModel.hideDiscardDialog()
                onNavigateBack()
            }
        )
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            delay(3000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) EditProfileColors.BackgroundDark
                else EditProfileColors.BackgroundLight
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = if (isDarkMode) EditProfileColors.AccentGreen
                        else EditProfileColors.AccentGreenLight
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header
                item {
                    EditProfileHeader(
                        onBackClick = handleBack,
                        isDarkMode = isDarkMode
                    )
                }

                // Avatar Section
                item {
                    AvatarSection(
                        currentAvatarId = uiState.currentAvatarId,
                        avatars = uiState.availableAvatars,
                        onAvatarSelected = { viewModel.selectAvatar(it) },
                        isDarkMode = isDarkMode
                    )
                }

                // Display Name Input
                item {
                    DisplayNameSection(
                        displayName = uiState.displayName,
                        onDisplayNameChange = { viewModel.updateDisplayName(it) },
                        onNext = { bioFocusRequester.requestFocus() },
                        isDarkMode = isDarkMode
                    )
                }

                // Bio Input
                item {
                    BioSection(
                        bio = uiState.bio,
                        onBioChange = { viewModel.updateBio(it) },
                        focusRequester = bioFocusRequester,
                        isDarkMode = isDarkMode
                    )
                }

                // Title Selection
                item {
                    TitleSection(
                        currentTitleId = uiState.currentTitleId,
                        titles = uiState.availableTitles,
                        onTitleSelected = { viewModel.selectTitle(it) },
                        isDarkMode = isDarkMode
                    )
                }

                // Banner Selection (Link to separate screen)
                item {
                    BannerSelectionLink(
                        onClick = onNavigateToBannerSelection,
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Error message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            ) {
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = PoppinsFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Save Button
            SaveProfileButton(
                onClick = { viewModel.saveProfile() },
                enabled = !uiState.isSaving && uiState.hasUnsavedChanges && uiState.displayName.isNotBlank(),
                isLoading = uiState.isSaving,
                isDarkMode = isDarkMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun EditProfileHeader(
    onBackClick: () -> Unit,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = KairosIcons.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = if (isDarkMode) EditProfileColors.TextPrimaryDark
                       else EditProfileColors.TextPrimaryLight,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Edit Profile",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = if (isDarkMode) EditProfileColors.TextPrimaryDark
                    else EditProfileColors.TextPrimaryLight
        )

        // Spacer for alignment
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun AvatarSection(
    currentAvatarId: String,
    avatars: List<AvatarOption>,
    onAvatarSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Current Avatar Display
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) EditProfileColors.CardBackgroundDark
                        else EditProfileColors.CardBackgroundLight
                    )
                    .border(
                        width = 3.dp,
                        color = if (isDarkMode) EditProfileColors.AccentGreen
                                else EditProfileColors.AccentGreenLight,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAvatarIcon(currentAvatarId),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isDarkMode) EditProfileColors.AccentGreen
                           else EditProfileColors.AccentGreenLight
                )
            }

            // Edit badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) EditProfileColors.AccentGreen
                        else EditProfileColors.AccentGreenLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = KairosIcons.Edit,
                    contentDescription = "Change avatar",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Label
        Text(
            text = "Choose avatar",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                    else EditProfileColors.TextTertiaryLight,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Avatar Selection Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avatars) { avatar ->
                AvatarOptionItem(
                    avatar = avatar,
                    isSelected = avatar.id == currentAvatarId,
                    onClick = { onAvatarSelected(avatar.id) },
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun AvatarOptionItem(
    avatar: AvatarOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "avatar_scale"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    if (isDarkMode) EditProfileColors.AccentGreen.copy(alpha = 0.2f)
                    else EditProfileColors.AccentGreenLight.copy(alpha = 0.2f)
                } else {
                    if (isDarkMode) EditProfileColors.CardBackgroundDark
                    else EditProfileColors.CardBackgroundLight
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    if (isDarkMode) EditProfileColors.AccentGreen
                    else EditProfileColors.AccentGreenLight
                } else {
                    if (isDarkMode) EditProfileColors.BorderDark
                    else EditProfileColors.BorderLight
                },
                shape = CircleShape
            )
            .clickable(enabled = !avatar.isLocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (avatar.isLocked) {
            // Locked overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EditProfileColors.LockedOverlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = KairosIcons.Lock,
                    contentDescription = "Locked",
                    tint = EditProfileColors.LockedIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = getAvatarIcon(avatar.id),
                contentDescription = avatar.name,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) {
                    if (isDarkMode) EditProfileColors.AccentGreen
                    else EditProfileColors.AccentGreenLight
                } else {
                    if (isDarkMode) EditProfileColors.TextSecondaryDark
                    else EditProfileColors.TextSecondaryLight
                }
            )
        }
    }
}

@Composable
private fun DisplayNameSection(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onNext: () -> Unit,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Display name",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                    else EditProfileColors.TextTertiaryLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDarkMode) EditProfileColors.InputBackgroundDark
                    else EditProfileColors.InputBackgroundLight
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) EditProfileColors.BorderDark
                            else EditProfileColors.BorderLight,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            textStyle = TextStyle(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isDarkMode) EditProfileColors.TextPrimaryDark
                        else EditProfileColors.TextPrimaryLight
            ),
            singleLine = true,
            cursorBrush = SolidColor(
                if (isDarkMode) EditProfileColors.AccentGreen
                else EditProfileColors.AccentGreenLight
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (displayName.isEmpty()) {
                            Text(
                                text = "Enter your name",
                                fontFamily = PoppinsFamily,
                                fontSize = 16.sp,
                                color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                                        else EditProfileColors.TextTertiaryLight
                            )
                        }
                        innerTextField()
                    }
                    if (displayName.isNotEmpty()) {
                        KairosIconButton(
                            icon = KairosIcons.Clear,
                            onClick = { onDisplayNameChange("") },
                            contentDescription = "Clear name",
                            size = 32.dp,
                            tint = if (isDarkMode) EditProfileColors.AccentGreen
                                   else EditProfileColors.AccentGreenLight
                        )
                    }
                }
            }
        )

        Text(
            text = "${displayName.length}/30",
            fontFamily = PoppinsFamily,
            fontSize = 12.sp,
            color = when {
                displayName.length >= 30 -> MaterialTheme.colorScheme.error
                displayName.length >= 24 -> KairosTokens.SemanticColors.warning
                else -> if (isDarkMode) EditProfileColors.TextTertiaryDark
                        else EditProfileColors.TextTertiaryLight
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun BioSection(
    bio: String,
    onBioChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "BIO",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                    else EditProfileColors.TextTertiaryLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = bio,
            onValueChange = onBioChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDarkMode) EditProfileColors.InputBackgroundDark
                    else EditProfileColors.InputBackgroundLight
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) EditProfileColors.BorderDark
                            else EditProfileColors.BorderLight,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isDarkMode) EditProfileColors.TextPrimaryDark
                        else EditProfileColors.TextPrimaryLight,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(
                if (isDarkMode) EditProfileColors.AccentGreen
                else EditProfileColors.AccentGreenLight
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default // Multi-line, default behavior
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (bio.isEmpty()) {
                        Text(
                            text = "Tell us about yourself...",
                            fontFamily = PoppinsFamily,
                            fontSize = 14.sp,
                            color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                                    else EditProfileColors.TextTertiaryLight
                        )
                    }
                    innerTextField()
                }
            }
        )

        Text(
            text = "${bio.length}/150",
            fontFamily = PoppinsFamily,
            fontSize = 12.sp,
            color = when {
                bio.length >= 150 -> MaterialTheme.colorScheme.error
                bio.length >= 120 -> KairosTokens.SemanticColors.warning
                else -> if (isDarkMode) EditProfileColors.TextTertiaryDark
                        else EditProfileColors.TextTertiaryLight
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun TitleSection(
    currentTitleId: String,
    titles: List<TitleOption>,
    onTitleSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Equipped title",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                    else EditProfileColors.TextTertiaryLight,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Title Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            titles.forEach { title ->
                TitleOptionChip(
                    title = title,
                    isSelected = title.id == currentTitleId,
                    onClick = { onTitleSelected(title.id) },
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun TitleOptionChip(
    title: TitleOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val backgroundColor by animateColorAsState(
        when {
            title.isLocked -> if (isDarkMode) EditProfileColors.CardBackgroundDark.copy(alpha = 0.5f)
                              else EditProfileColors.CardBackgroundLight.copy(alpha = 0.5f)
            isSelected -> if (isDarkMode) EditProfileColors.AccentGreen
                          else EditProfileColors.AccentGreenLight
            else -> if (isDarkMode) EditProfileColors.CardBackgroundDark
                    else EditProfileColors.CardBackgroundLight
        },
        animationSpec = tween(200),
        label = "title_bg"
    )

    val textColor by animateColorAsState(
        when {
            title.isLocked -> EditProfileColors.LockedIcon
            isSelected -> Color.Black
            else -> if (isDarkMode) EditProfileColors.TextSecondaryLight
                    else EditProfileColors.TextSecondaryLight
        },
        animationSpec = tween(200),
        label = "title_text"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = !title.isLocked) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (title.isLocked) {
            Icon(
                imageVector = KairosIcons.Lock,
                contentDescription = "Locked",
                tint = EditProfileColors.LockedIcon,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = title.displayName,
            fontFamily = PoppinsFamily,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
private fun BannerSelectionLink(
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clickable { onClick() },
        color = if (isDarkMode) EditProfileColors.CardBackgroundDark
                else EditProfileColors.CardBackgroundLight,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDarkMode) EditProfileColors.AccentGreen.copy(alpha = 0.15f)
                            else EditProfileColors.AccentGreenLight.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = KairosIcons.Wallpaper,
                        contentDescription = null,
                        tint = if (isDarkMode) EditProfileColors.AccentGreen
                               else EditProfileColors.AccentGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Profile Banner",
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = if (isDarkMode) EditProfileColors.TextPrimaryDark
                                else EditProfileColors.TextPrimaryLight
                    )
                    Text(
                        text = "Customize your profile banner",
                        fontFamily = PoppinsFamily,
                        fontSize = 12.sp,
                        color = if (isDarkMode) EditProfileColors.TextTertiaryDark
                                else EditProfileColors.TextTertiaryLight
                    )
                }
            }

            Icon(
                imageVector = KairosIcons.ChevronRight,
                contentDescription = "Go to banner selection",
                tint = if (isDarkMode) EditProfileColors.TextTertiaryDark
                       else EditProfileColors.TextTertiaryLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SaveProfileButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) {
                    if (isDarkMode) EditProfileColors.AccentGreen
                    else EditProfileColors.AccentGreenLight
                } else {
                    if (isDarkMode) EditProfileColors.AccentGreen.copy(alpha = 0.3f)
                    else EditProfileColors.AccentGreenLight.copy(alpha = 0.3f)
                }
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = KairosIcons.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Save Changes",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Discard Changes?",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. Are you sure you want to discard them?",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Discard",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Keep Editing",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

/**
 * Get icon for avatar ID
 */
private fun getAvatarIcon(avatarId: String): ImageVector {
    return when (avatarId) {
        "default" -> KairosIcons.Person
        "lotus" -> KairosIcons.Spa
        "mountain" -> KairosIcons.Terrain
        "river" -> KairosIcons.Water
        "tree" -> KairosIcons.Park
        "sun" -> KairosIcons.WbSunny
        "moon" -> KairosIcons.DarkMode
        "star" -> KairosIcons.Star
        "flame" -> KairosIcons.LocalFireDepartment
        "diamond" -> KairosIcons.Diamond
        "crown" -> KairosIcons.EmojiEvents
        "phoenix" -> KairosIcons.Whatshot
        else -> KairosIcons.Person
    }
}
