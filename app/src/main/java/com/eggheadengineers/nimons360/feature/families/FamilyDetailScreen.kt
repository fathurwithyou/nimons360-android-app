package com.eggheadengineers.nimons360.feature.families

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.eggheadengineers.nimons360.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.eggheadengineers.nimons360.core.share.createQrBitmap
import com.eggheadengineers.nimons360.core.share.writeShareFile
import com.eggheadengineers.nimons360.domain.model.FamilyDetail
import com.eggheadengineers.nimons360.domain.model.FamilyMember
import com.eggheadengineers.nimons360.domain.model.LiveStream
import com.eggheadengineers.nimons360.feature.livestream.LiveStreamsSection
import com.eggheadengineers.nimons360.ui.components.AppCompactSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppDestructiveButton
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppFilterPill
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
import java.io.ByteArrayOutputStream
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val INITIAL_MEMBER_LIMIT = 5

@Composable
private fun GoLiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_go_live),
            contentDescription = "Go Live",
            tint = Color.Black,
            modifier = Modifier.size(36.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    viewModel: FamilyDetailViewModel,
    initialJoinCode: String? = null,
    onBack: () -> Unit,
    onGoLive: () -> Unit = {},
    onWatchStream: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showJoinDialog by rememberSaveable { mutableStateOf(false) }
    var joinDialogInitialCode by rememberSaveable { mutableStateOf("") }
    var consumedInitialJoinCode by rememberSaveable { mutableStateOf(false) }
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    var showNotificationSheet by rememberSaveable { mutableStateOf(false) }
    var showQrSheet by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(state.detail?.id, initialJoinCode) {
        val code = initialJoinCode?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val detail = state.detail ?: return@LaunchedEffect
        if (!detail.isMember && !consumedInitialJoinCode) {
            joinDialogInitialCode = code
            showJoinDialog = true
            consumedInitialJoinCode = true
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
                    liveStreams = state.liveStreams,
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    onJoinClick = {
                        joinDialogInitialCode = ""
                        showJoinDialog = true
                    },
                    onLeaveClick = { showLeaveDialog = true },
                    onGoLive = onGoLive,
                    onWatchStream = onWatchStream,
                    onNotifyClick = { showNotificationSheet = true },
                    onQrClick = { showQrSheet = true },
                    onShareClick = {
                        val detail = state.detail
                        if (detail?.code.isNullOrBlank()) {
                            viewModel.showFeedback(
                                FamilyDetailFeedback(
                                    title = "Family code unavailable",
                                    message = "Reload the family detail before sharing the invite link.",
                                    isSuccess = false,
                                ),
                            )
                        } else if (detail != null) {
                            shareFamilyLink(context, detail)
                        }
                    },
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
            initialCode = joinDialogInitialCode,
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

    if (showNotificationSheet) {
        state.detail?.let { detail ->
            FamilyNotificationBottomSheet(
                detail = detail,
                onDismiss = { showNotificationSheet = false },
                onSendBroadcast = { message ->
                    viewModel.sendFamilyNotification(message)
                    showNotificationSheet = false
                },
                onSendGreeting = { targetUserId, message ->
                    viewModel.sendGreeting(targetUserId, message)
                    showNotificationSheet = false
                },
            )
        }
    }

    if (showQrSheet) {
        state.detail?.let { detail ->
            FamilyQrBottomSheet(
                detail = detail,
                onDismiss = { showQrSheet = false },
            )
        }
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
    liveStreams: List<LiveStream>,
    modifier: Modifier = Modifier,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onGoLive: () -> Unit,
    onWatchStream: (String) -> Unit,
    onNotifyClick: () -> Unit,
    onQrClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyCode: (String) -> Unit,
) {
    var showAllMembers by rememberSaveable { mutableStateOf(false) }
    var memberQuery by rememberSaveable { mutableStateOf("") }
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

        if (liveStreams.isNotEmpty()) {
            item {
                LiveStreamsSection(
                    streams = liveStreams,
                    onStreamClick = { onWatchStream(it.id) },
                )
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
            Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                if (detail.isMember) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                    ) {
                        GoLiveIconButton(
                            onClick = onGoLive,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        )
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !detail.code.isNullOrBlank(),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share family link",
                                tint = if (!detail.code.isNullOrBlank()) Primary else TextSecondary,
                            )
                        }
                        IconButton(
                            onClick = onQrClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !detail.code.isNullOrBlank(),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode,
                                contentDescription = "Show family QR code",
                                tint = if (!detail.code.isNullOrBlank()) Primary else TextSecondary,
                            )
                        }
                        IconButton(
                            onClick = onNotifyClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notify family",
                                tint = Primary,
                            )
                        }
                    }
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
}

@Composable
private fun MemberListItem(member: FamilyMember) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MemberAvatar(member = member)
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
private fun MemberAvatar(member: FamilyMember) {
    val imageUrl = member.profileImageUrl?.let(::resolveUserImageUrl)
    if (imageUrl.isNullOrBlank()) {
        AvatarCircle(
            initial = member.name.firstOrNull()?.uppercaseChar() ?: '?',
            size = 36,
        )
    } else {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(50),
            color = SurfaceContainerLow,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "${member.name} profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun JoinFamilyDialog(
    initialCode: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by rememberSaveable(initialCode) { mutableStateOf(initialCode.uppercase().take(6)) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyNotificationBottomSheet(
    detail: FamilyDetail,
    onDismiss: () -> Unit,
    onSendBroadcast: (String) -> Unit,
    onSendGreeting: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val greetingMembers = detail.members.filter { it.id.isNotBlank() }
    var message by rememberSaveable { mutableStateOf("") }
    var selectedMemberId by remember(greetingMembers) { mutableStateOf(greetingMembers.firstOrNull()?.id.orEmpty()) }
    var greeting by rememberSaveable { mutableStateOf(defaultGreetingMessage()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppGrid.ScreenHorizontal)
                .padding(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space1)) {
                Text(
                    text = "Notify family",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            AppTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Broadcast message") },
                singleLine = false,
            )
            AppDarkButton(
                text = "Send to family",
                onClick = { onSendBroadcast(message) },
                enabled = message.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = Border.copy(alpha = 0.3f))

            Text(
                text = "Quick greeting",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )

            if (greetingMembers.isEmpty()) {
                Text(
                    text = "No targetable member IDs are available in this response.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                ) {
                    items(greetingMembers, key = { it.id }) { member ->
                        AppFilterPill(
                            text = member.name.ifBlank { member.email },
                            selected = member.id == selectedMemberId,
                            onClick = { selectedMemberId = member.id },
                        )
                    }
                }

                AppTextField(
                    value = greeting,
                    onValueChange = { greeting = it },
                    label = { Text("Greeting message") },
                    singleLine = false,
                )

                AppSecondaryButton(
                    text = "Send greeting",
                    onClick = { onSendGreeting(selectedMemberId, greeting) },
                    enabled = selectedMemberId.isNotBlank() && greeting.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyQrBottomSheet(
    detail: FamilyDetail,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val link = detail.familyDeepLink()
    val qrBitmap = remember(link) { link?.let(::createQrBitmap) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppGrid.ScreenHorizontal)
                .padding(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space1),
            ) {
                Text(
                    text = "Family QR code",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Family invite QR code",
                    modifier = Modifier.size(220.dp),
                )
                AppDarkButton(
                    text = "Share QR code",
                    onClick = { shareQrCode(context, detail, qrBitmap) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Family code is unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

private fun defaultGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning!"
        in 12..17 -> "Good Afternoon!"
        else -> "Good Night!"
    }
}

private fun shareFamilyLink(context: Context, detail: FamilyDetail) {
    val link = detail.familyDeepLink() ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Join ${detail.name} on Nimons360: $link")
    }
    context.startActivity(Intent.createChooser(intent, "Share family link"))
}

private fun shareQrCode(context: Context, detail: FamilyDetail, bitmap: Bitmap) {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    val uri = writeShareFile(context, "nimons360-family-${detail.id}-qr.png", output.toByteArray())
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Join ${detail.name} on Nimons360: ${detail.familyDeepLink()}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share family QR code"))
}

private fun FamilyDetail.familyDeepLink(): String? {
    val code = this.code ?: return null
    return "nimons360://family/$id?code=$code"
}

private fun resolveUserImageUrl(value: String): String =
    if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "https://mad.labpro.hmif.dev/${value.removePrefix("/")}"
    }
