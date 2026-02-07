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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController, globalNotes: MutableList<NoteData>) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter logika
    val filteredNotes = remember(searchQuery, globalNotes.size) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Jarak antar kotak
                ) {
                    items(filteredNotes) { note ->
                        NoteItemCard(note) {
                            // Navigasi edit (sesuaikan rute di MainActivity)
                            navController.navigate("note_editor?noteId=${note.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItemCard(note: NoteData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) { // Padding dalam lebih besar
            Text(
                text = note.title.ifEmpty { "Untitled" },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, // Judul lebih besar
                color = Color(0xFF2C3E50),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = note.previewContent,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp,
                maxLines = 6, // Menampilkan lebih banyak baris teks
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.date,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )

                // Indikator kecil agar terlihat estetik
                Surface(
                    color = Color(0xFF6C63FF).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Note",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color(0xFF6C63FF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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