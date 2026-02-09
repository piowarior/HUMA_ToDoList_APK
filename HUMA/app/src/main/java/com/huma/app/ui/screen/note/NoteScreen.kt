package com.huma.app.ui.screen.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController, globalNotes: List<NoteData>, onDeleteNote: (NoteData) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter logika
    val filteredNotes = remember(searchQuery, globalNotes) {
        if (searchQuery.isEmpty()) globalNotes
        else globalNotes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.previewContent.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("note_editor") },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        containerColor = Color(0xFFF8F9FE) // Warna background halus
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- HEADER ---
            HeaderNoteSection(onBack = { navController.popBackStack() })

            // --- SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari catatanmu...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF6C63FF)
                )
            )

            Spacer(Modifier.height(12.dp))

            // --- LIST NOTES (Persegi Panjang ke Bawah) ---
            if (globalNotes.isEmpty()) {
                EmptyNotesPlaceholder()
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        // DI SINI TEMPATNYA:
                        NoteItemCard(
                            note = note,
                            onClick = {
                                navController.navigate("note_editor?noteId=${note.id}")
                            },
                            onDelete = {
                                onDeleteNote(note) // Memanggil fungsi hapus
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItemCard(note: NoteData, onDelete: () -> Unit, onClick: () -> Unit) {
    // State untuk kontrol menu hapus
    var showMenu by remember { mutableStateOf(false) }
    // Untuk efek getar
    val haptic = LocalHapticFeedback.current

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                // GANTI: Gunakan combinedClickable untuk deteksi long click
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // JUDUL UTAMA
                Text(
                    text = note.title.ifEmpty { "Tanpa Judul" },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // PREVIEW ISI (Heading, Text, Bullet, Checkbox)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.previewBlocks.forEach { block ->
                        when (block) {
                            is NoteBlock.Heading -> {
                                Text(
                                    text = block.content,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            is NoteBlock.Text -> {
                                Text(
                                    text = block.content,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            is NoteBlock.BulletList -> {
                                if (block.items.isNotEmpty()) {
                                    Text(text = "• ${block.items.first()}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                            is NoteBlock.CheckboxGroup -> {
                                if (block.items.isNotEmpty()) {
                                    Text(text = "☐ ${block.items.first().text}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // TANGGAL
                Text(text = note.date, fontSize = 10.sp, color = Color.LightGray)
            }
        }

        // MENU POPUP (Muncul saat ditekan lama)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Hapus Catatan", color = Color.Red) },
                // Ganti Icons.Default.Delete menjadi Icons.Default.DeleteOutline atau Icons.Default.Clear
                // Ganti baris leadingIcon di dalam DropdownMenuItem:
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red) },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun HeaderNoteSection(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .size(40.dp)
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF2C3E50))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) { append("My ") }
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("Notes") }
            },
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2C3E50)
        )
    }
}

@Composable
fun EmptyNotesPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Belum ada catatan", color = Color.Gray, fontWeight = FontWeight.Medium)
            Text("Klik + untuk membuat baru", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}