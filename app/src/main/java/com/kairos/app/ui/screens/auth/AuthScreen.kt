package com.kairos.app.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.R
import com.kairos.app.data.auth.AuthState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KairosSpacing.screen, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        KairosReadingSurface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KairosGlassSurface(
                    modifier = Modifier.size(76.dp),
                    shape = RoundedCornerShape(KairosRadius.controlLarge),
                    strong = true
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = KairosIcons.Outlined.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(31.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "A quieter way to learn",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Start locally with a daily word, a thought worth keeping, and space to reflect. An account is optional.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = authState is AuthState.Loading,
                    enter = fadeIn()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }

                KairosPrimaryButton(
                    text = "Continue locally",
                    onClick = viewModel::continueLocally,
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        viewModel.getGoogleSignInIntent(context)?.let { intent -> googleSignInLauncher.launch(intent) }
                    },
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(KairosRadius.control),
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = androidx.compose.ui.graphics.Color.Unspecified
                    )
                    Text(
                        text = "Continue with Google",
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = "Everything you need works on this device. Sign in with Google whenever you want your account on another phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                (authState as? AuthState.Error)?.message?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
