package com.vishnu.habittracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.habittracker.ui.theme.HabitTrackerColors

/**
 * Login/Signup screen — pixel-perfect port of the webapp's login modal.
 *
 * Features:
 * - Google OAuth button (primary action)
 * - Email/Password login form
 * - Sign up form (toggle)
 * - Error/success messages
 * - Loading states
 *
 * Webapp CSS reference: .login-modal in index.html
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel
) {
    val formState by viewModel.formState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HabitTrackerColors.DarkBgPrimary,
                        Color(0xFF1A1033),
                        HabitTrackerColors.DarkBgPrimary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── App Icon ──────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                HabitTrackerColors.GradientPrimaryStart,
                                HabitTrackerColors.GradientPrimaryEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "App Icon",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Brand Text ────────────────────────────────
            Text(
                text = "Habit Tracker",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = HabitTrackerColors.DarkTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (formState.isLogin) "Welcome back! Sign in to continue."
                       else "Create your account to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = HabitTrackerColors.DarkTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Google OAuth Button ───────────────────────
            Button(
                onClick = { viewModel.signInWithGoogle() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333)
                ),
                enabled = !formState.isLoading
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = HabitTrackerColors.DarkBorder
                )
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = HabitTrackerColors.DarkTextTertiary
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = HabitTrackerColors.DarkBorder
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Error Message ─────────────────────────────
            AnimatedVisibility(
                visible = formState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                formState.errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HabitTrackerColors.Danger.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = HabitTrackerColors.Danger
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Success Message ───────────────────────────
            AnimatedVisibility(
                visible = formState.successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                formState.successMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HabitTrackerColors.Success.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = HabitTrackerColors.Success
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Email Field ───────────────────────────────
            OutlinedTextField(
                value = formState.email,
                onValueChange = { viewModel.updateEmail(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = HabitTrackerColors.DarkTextTertiary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HabitTrackerColors.Primary,
                    unfocusedBorderColor = HabitTrackerColors.DarkBorder,
                    focusedLabelColor = HabitTrackerColors.Primary,
                    unfocusedLabelColor = HabitTrackerColors.DarkTextTertiary,
                    cursorColor = HabitTrackerColors.Primary,
                    focusedTextColor = HabitTrackerColors.DarkTextPrimary,
                    unfocusedTextColor = HabitTrackerColors.DarkTextPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !formState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password Field ────────────────────────────
            OutlinedTextField(
                value = formState.password,
                onValueChange = { viewModel.updatePassword(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = HabitTrackerColors.DarkTextTertiary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = HabitTrackerColors.DarkTextTertiary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HabitTrackerColors.Primary,
                    unfocusedBorderColor = HabitTrackerColors.DarkBorder,
                    focusedLabelColor = HabitTrackerColors.Primary,
                    unfocusedLabelColor = HabitTrackerColors.DarkTextTertiary,
                    cursorColor = HabitTrackerColors.Primary,
                    focusedTextColor = HabitTrackerColors.DarkTextPrimary,
                    unfocusedTextColor = HabitTrackerColors.DarkTextPrimary
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (formState.isLogin) viewModel.signInWithEmail()
                        else viewModel.signUp()
                    }
                ),
                singleLine = true,
                enabled = !formState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Submit Button ─────────────────────────────
            Button(
                onClick = {
                    if (formState.isLogin) viewModel.signInWithEmail()
                    else viewModel.signUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                enabled = !formState.isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    HabitTrackerColors.GradientPrimaryStart,
                                    HabitTrackerColors.GradientPrimaryEnd
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (formState.isLogin) "Sign In" else "Create Account",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Toggle Login/Signup ───────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (formState.isLogin) "Don't have an account?"
                           else "Already have an account?",
                    style = MaterialTheme.typography.bodySmall,
                    color = HabitTrackerColors.DarkTextSecondary
                )
                TextButton(onClick = { viewModel.toggleForm() }) {
                    Text(
                        text = if (formState.isLogin) "Sign Up" else "Sign In",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HabitTrackerColors.Primary
                    )
                }
            }
        }
    }
}
