package com.exmple.cinelog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exmple.cinelog.ui.theme.bounceClick
import com.exmple.cinelog.ui.theme.glassCard
import com.exmple.cinelog.ui.theme.glassSurface
import com.exmple.cinelog.ui.theme.regalDivider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BoothGold = Color(0xFFF5C518)
private val BoothInk = Color(0xFF0A0A0A)
private val BoothSmoke = Color(0xFFB8B0A0)
private val BoothQuickPrompts = listOf(
    "Give me three swooning romances that still have bite.",
    "Recommend a dark, gorgeous thriller for tonight.",
    "I want a movie that feels like midnight and neon.",
    "Build me a double feature from my taste."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectionistBoothScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProjectionistBoothViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val archiveVoices = remember(uiState.messages) { uiState.messages.count { !it.isUser } }
    val userPrompts = remember(uiState.messages) { uiState.messages.count { it.isUser } }

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        val listTailIndex = uiState.messages.lastIndex + if (uiState.isLoading) 1 else 0
        if (listTailIndex >= 0) {
            scrollState.animateScrollToItem(listTailIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050505),
                        Color(0xFF0E0C0A),
                        Color(0xFF15110D)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .offset(y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BoothGold.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Projectionist's Booth",
                                style = MaterialTheme.typography.titleLarge,
                                color = BoothGold
                            )
                            Text(
                                text = "Curated conversation for long nights and better movies",
                                style = MaterialTheme.typography.labelSmall,
                                color = BoothSmoke,
                                letterSpacing = 1.2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BoothGold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = BoothGold
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BoothHeroCard(
                    archiveVoices = archiveVoices,
                    userPrompts = userPrompts
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "QUICK CUES",
                        style = MaterialTheme.typography.labelSmall,
                        color = BoothSmoke,
                        letterSpacing = 1.8.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BoothQuickPrompts.forEach { prompt ->
                            QuickCueChip(
                                text = prompt,
                                onClick = { viewModel.onInputChanged(prompt) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .regalDivider()
                )

                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .glassSurface(cornerRadius = 28.dp, alpha = 0.34f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CONVERSATION REEL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BoothGold,
                                    letterSpacing = 1.6.sp
                                )
                                Text(
                                    text = "Ask for moods, eras, double features, or something gloriously specific.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BoothSmoke
                                )
                            }

                            ArchiveMiniBadge(
                                icon = Icons.Default.Forum,
                                text = "${uiState.messages.size} notes"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState,
                            contentPadding = PaddingValues(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.messages) { message ->
                                MessageBubble(message)
                            }

                            if (uiState.isLoading) {
                                item {
                                    ProjectionistLoadingCard()
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChatInput(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChanged,
                        onSend = viewModel::sendMessage,
                        isLoading = uiState.isLoading
                    )
                    Text(
                        text = "The more specific you are about mood, decade, language, or runtime, the better the booth gets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BoothSmoke,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoothHeroCard(
    archiveVoices: Int,
    userPrompts: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 28.dp, alpha = 0.62f, borderAlpha = 0.14f)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(BoothGold)
                )
                Text(
                    text = "BOOTH OPEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = BoothGold,
                    letterSpacing = 1.8.sp
                )
            }

            Text(
                text = "Archive-aware",
                style = MaterialTheme.typography.labelSmall,
                color = BoothSmoke
            )
        }

        Text(
            text = "Sharp recommendations, affectionate debate, and a little projector-room attitude.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "The booth is built for long-form movie talk, tailored picks, and conversations that actually sound like someone who loves cinema.",
            style = MaterialTheme.typography.bodyMedium,
            color = BoothSmoke
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ArchiveMiniBadge(
                icon = Icons.Default.AutoStories,
                text = "$archiveVoices booth replies",
                modifier = Modifier.weight(1f)
            )
            ArchiveMiniBadge(
                icon = Icons.Default.MovieFilter,
                text = "$userPrompts viewer prompts",
                modifier = Modifier.weight(1f)
            )
            ArchiveMiniBadge(
                icon = Icons.Default.Tune,
                text = "Direct picks",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ArchiveMiniBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, BoothGold.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BoothGold,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickCueChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BoothGold.copy(alpha = 0.12f)),
        modifier = Modifier.bounceClick(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.035f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .width(150.dp)
                .wrapContentHeight()
        )
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleAlignment = if (message.isUser) Alignment.End else Alignment.Start
    val speaker = if (message.isUser) "YOU" else "THE PROJECTIONIST"
    val timeStamp = remember(message.timestamp) { message.timestamp.asBoothTime() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = bubbleAlignment
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.86f else 0.92f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$speaker  •  $timeStamp",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) BoothGold.copy(alpha = 0.85f) else BoothSmoke,
                    letterSpacing = 1.2.sp
                )
            }

            Surface(
                color = if (message.isUser) Color.Transparent else Color.Transparent,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (message.isUser) 20.dp else 8.dp,
                    bottomEnd = if (message.isUser) 8.dp else 20.dp
                ),
                border = BorderStroke(
                    1.dp,
                    if (message.isUser) BoothGold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = if (message.isUser) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF17110A),
                                Color(0xFF22180D)
                            )
                        ),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 8.dp
                        )
                    )
                } else {
                    Modifier.glassCard(cornerRadius = 22.dp, alpha = 0.48f, borderAlpha = 0.1f)
                }
            ) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun ProjectionistLoadingCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .glassCard(cornerRadius = 22.dp, alpha = 0.4f, borderAlpha = 0.08f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = BoothGold,
            strokeWidth = 2.dp,
            trackColor = BoothGold.copy(alpha = 0.16f),
            strokeCap = StrokeCap.Round
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "The Projectionist is threading the next reel...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Expect something opinionated and lovingly chosen.",
                style = MaterialTheme.typography.bodySmall,
                color = BoothSmoke
            )
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 28.dp, alpha = 0.58f, borderAlpha = 0.14f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    "Ask for a mood, a director, a double feature, or a dangerous recommendation...",
                    color = BoothSmoke,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = BoothGold
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 5
        )

        FilledIconButton(
            onClick = onSend,
            enabled = !isLoading && value.isNotBlank(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (value.isNotBlank()) BoothGold else BoothGold.copy(alpha = 0.18f),
                contentColor = BoothInk,
                disabledContainerColor = BoothGold.copy(alpha = 0.18f),
                disabledContentColor = BoothSmoke
            ),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}

private fun Long.asBoothTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}
