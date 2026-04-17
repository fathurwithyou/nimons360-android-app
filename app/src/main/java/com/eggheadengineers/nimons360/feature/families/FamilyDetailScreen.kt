package com.eggheadengineers.nimons360.feature.families

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.eggheadengineers.nimons360.domain.model.FamilyDetail
import com.eggheadengineers.nimons360.domain.model.FamilyMember
import com.eggheadengineers.nimons360.ui.components.AppCompactSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppDestructiveButton
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSearchBar
import com.eggheadengineers.nimons360.ui.components.AppSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.components.AvatarCircle
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.components.showSuccessAlert
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.ErrorColor
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.PrimaryLight
import com.eggheadengineers.nimons360.ui.theme.Surface
import com.eggheadengineers.nimons360.ui.theme.SurfaceContainerLow
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import com.eggheadengineers.nimons360.ui.theme.TextTertiary

private const val INITIAL_MEMBER_LIMIT = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    viewModel: FamilyDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.feedback) {
        state.feedback?.let {
            if (it.isSuccess) {
                snackbarHostState.showSuccessAlert(
                    title = it.title,
                    message = it.message,
                )
            } else {
                snackbarHostState.showErrorAlert(
                    title = it.title,
                    message = it.message,
                )
            }
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = state.detail?.name ?: "Family",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.error!!, color = ErrorColor)
                }
            }

            state.detail != null -> {
                FamilyDetailContent(
                    detail = state.detail!!,
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    onJoinClick = { showJoinDialog = true },
                    onLeaveClick = { showLeaveDialog = true },
                    onCopyCode = { code ->
                        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        manager.setPrimaryClip(ClipData.newPlainText("Family Code", code))
                        viewModel.showFeedback(
                            FamilyDetailFeedback(
                                title = "Code copied",
                                message = "The family code is ready to paste.",
                                isSuccess = true,
                            ),
                        )
                    },
                )
            }
        }
    }

    if (showJoinDialog) {
        JoinFamilyDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code ->
                viewModel.joinFamily(code)
                showJoinDialog = false
            },
        )
    }

    if (showLeaveDialog) {
        FamilyActionDialog(
            title = "Leave family",
            description = "You'll stop seeing updates from this family until you join again.",
            onDismiss = { showLeaveDialog = false },
            actions = {
                AppSecondaryButton(
                    text = "Cancel",
                    onClick = { showLeaveDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                )
                AppDestructiveButton(
                    text = "Leave family",
                    onClick = {
                        viewModel.leaveFamily()
                        showLeaveDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyActionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit) = {},
    actions: @Composable ColumnScope.() -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = AppGrid.ScreenHorizontal),
            shape = RoundedCornerShape(AppGrid.CardRadius),
            color = Surface,
            border = BorderStroke(1.dp, Border.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier.padding(AppGrid.Space5),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }

                content()
                actions()
            }
        }
    }
}

@Composable
private fun FamilyDetailContent(
    detail: FamilyDetail,
    modifier: Modifier = Modifier,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onCopyCode: (String) -> Unit,
) {
    var showAllMembers by remember { mutableStateOf(false) }
    var memberQuery by remember { mutableStateOf("") }
    val filteredMembers = detail.members.filter { member ->
        memberQuery.isBlank() ||
            member.name.contains(memberQuery, ignoreCase = true) ||
            member.email.contains(memberQuery, ignoreCase = true)
    }
    val visibleMembers = if (showAllMembers) filteredMembers else filteredMembers.take(INITIAL_MEMBER_LIMIT)
    val hasMoreMembers = filteredMembers.size > INITIAL_MEMBER_LIMIT

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppGrid.ScreenHorizontal,
            top = AppGrid.Space4,
            end = AppGrid.ScreenHorizontal,
            bottom = AppGrid.ScreenBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space5),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space4)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = PrimaryLight,
                        border = BorderStroke(1.dp, Border.copy(alpha = 0.22f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = detail.iconUrl,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = detail.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${detail.members.size} members",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    if (detail.isMember) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PrimaryLight,
                        ) {
                            Text(
                                text = "Joined",
                                modifier = Modifier.padding(
                                    horizontal = AppGrid.Space3,
                                    vertical = AppGrid.Space1,
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = TextPrimary,
                            )
                        }
                    }
                }

                HorizontalDivider(color = Border.copy(alpha = 0.3f))
            }
        }

        if (detail.isMember && detail.code != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Family code",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppGrid.FieldRadius),
                        color = PrimaryLight,
                        border = BorderStroke(1.dp, Border.copy(alpha = 0.2f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCopyCode(detail.code) }
                                .padding(horizontal = AppGrid.Space4, vertical = AppGrid.Space3),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = detail.code,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    letterSpacing = 3.sp,
                                ),
                                color = TextPrimary,
                            )
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy code",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    HorizontalDivider(color = Border.copy(alpha = 0.3f))
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                    }
                    Text(
                        text = if (memberQuery.isBlank()) {
                            "${detail.members.size} total"
                        } else {
                            "${filteredMembers.size} matching"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                    )
                }

                AppSearchBar(
                    value = memberQuery,
                    onValueChange = {
                        memberQuery = it
                        showAllMembers = false
                    },
                    placeholder = "Search by name or email",
                )

                if (visibleMembers.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppGrid.FieldRadius),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, Border.copy(alpha = 0.3f)),
                    ) {
                        Text(
                            text = "No members match your search.",
                            modifier = Modifier.padding(AppGrid.Space4),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                } else {
                    Column {
                        visibleMembers.forEachIndexed { index, member ->
                            MemberListItem(member = member)
                            if (index != visibleMembers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = AppGrid.Space2),
                                    color = Border.copy(alpha = 0.25f),
                                )
                            }
                        }
                    }
                }

                if (hasMoreMembers) {
                    val remaining = filteredMembers.size - INITIAL_MEMBER_LIMIT
                    AppCompactSecondaryButton(
                        text = if (showAllMembers) {
                            "Show less"
                        } else {
                            "Show $remaining more members"
                        },
                        onClick = { showAllMembers = !showAllMembers },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                HorizontalDivider(color = Border.copy(alpha = 0.3f))
            }
        }

        item {
            if (detail.isMember) {
                AppDestructiveButton(
                    text = "Leave family",
                    onClick = onLeaveClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                AppDarkButton(
                    text = "Join family",
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MemberListItem(member: FamilyMember) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarCircle(
            initial = member.name.firstOrNull()?.uppercaseChar() ?: '?',
            size = 36,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = member.email,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun JoinFamilyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }

    FamilyActionDialog(
        title = "Join family",
        description = "Enter the 6-character code shared by the family organizer.",
        onDismiss = onDismiss,
        content = {
            AppTextField(
                value = code,
                onValueChange = {
                    if (it.length <= 6) code = it.uppercase()
                },
                label = { Text("6-character code") },
                singleLine = true,
            )
        },
        actions = {
            AppDarkButton(
                text = "Join family",
                onClick = { onConfirm(code) },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.length == 6,
            )
            AppSecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
