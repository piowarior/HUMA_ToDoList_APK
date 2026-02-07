package com.huma.app.ui.screen.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.*

// 🔥 PINDAHKAN INI KE LUAR ATAU KE FILE TERPISAH (NoteData.kt)
data class NoteData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val blocks: List<NoteBlock>,
    val date: String
) {
    val previewContent: String get() = blocks.filterIsInstance<NoteBlock.Text>().firstOrNull()?.content ?: ""
}

// --- MODELS (Penting: Gunakan var agar data di dalam class bisa diubah) ---
sealed class NoteBlock(val id: String = UUID.randomUUID().toString()) {
    class Text(initialContent: String = "") : NoteBlock() {
        var content by mutableStateOf(initialContent)
    }
    class Heading(initialContent: String = "") : NoteBlock() {
        var content by mutableStateOf(initialContent)
    }
    class BulletList(val items: MutableList<String> = mutableStateListOf("")) : NoteBlock()
    class CheckboxGroup(val items: MutableList<CheckItem> = mutableStateListOf(CheckItem())) : NoteBlock()
}

data class CheckItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    var isChecked: Boolean = false
)

// 🔥 TAMBAHKAN PARAMETER 'onSave' di sini
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    navController: NavController,
    noteId: String? = null, // Tambah ini
    globalNotes: List<NoteData> = emptyList(), // Tambah ini
    onSave: (NoteData) -> Unit
) {
    val blocks = remember { mutableStateListOf<NoteBlock>() }
    var noteTitle by remember { mutableStateOf("") }
    var activeBlockId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // 🔥 LOGIKA LOAD DATA: Jika ada noteId, ambil data dari globalNotes
    LaunchedEffect(noteId) {
        if (noteId != null && blocks.isEmpty()) {
            val existingNote = globalNotes.find { it.id == noteId }
            existingNote?.let {
                noteTitle = it.title
                blocks.clear()
                blocks.addAll(it.blocks)
            }
        }
    }

    val handleSaveAndExit = {
        if (noteTitle.isNotEmpty() || blocks.isNotEmpty()) {
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val newNote = NoteData(
                id = noteId ?: UUID.randomUUID().toString(), // Pakai ID lama jika edit
                title = if (noteTitle.isEmpty()) "Untitled" else noteTitle,
                blocks = blocks.toList(),
                date = currentDate
            )
            onSave(newNote)
        }
        navController.popBackStack()
    }

    // Pastikan tombol back HP juga memicu simpan
    BackHandler { handleSaveAndExit() }

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    BasicTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                        cursorBrush = SolidColor(Color(0xFF6C63FF)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (noteTitle.isEmpty()) Text("Judul Catatan", color = Color.LightGray)
                            inner()
                        }
                    )
                },
                navigationIcon = {
                    // 🔥 GANTI onSaveAndExit jadi handleSaveAndExit
                    IconButton(onClick = { handleSaveAndExit() }) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    // 🔥 GANTI onSaveAndExit jadi handleSaveAndExit
                    IconButton(onClick = { handleSaveAndExit() }) {
                        Icon(Icons.Default.Done, null, tint = Color(0xFF6C63FF))
                    }
                }
            )
        },
        floatingActionButton = {
            if (blocks.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val b = NoteBlock.Text("")
                        blocks.add(b)
                        activeBlockId = b.id
                    },
                    containerColor = Color(0xFF6C63FF),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mulai Menulis")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                // LazyColumn ini yang bikin bisa GULIR/SCROLL kebawah
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
                        BlockWrapper(
                            isActive = activeBlockId == block.id,
                            onActivate = { activeBlockId = block.id },
                            onDelete = { blocks.remove(block) },
                            onAddBelow = { type ->
                                val new = when(type) {
                                    "heading" -> NoteBlock.Heading()
                                    "check" -> NoteBlock.CheckboxGroup()
                                    "bullet" -> NoteBlock.BulletList()
                                    else -> NoteBlock.Text()
                                }
                                blocks.add(index + 1, new)
                                activeBlockId = new.id
                            }
                        ) {
                            when (block) {
                                is NoteBlock.Heading -> HeadingCell(block)
                                is NoteBlock.Text -> TextCell(block)
                                is NoteBlock.CheckboxGroup -> CheckboxCellGroup(block)
                                is NoteBlock.BulletList -> BulletCellList(block)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PERBAIKAN: Ganti data class ke class biasa dengan mutableStateOf supaya BISA DIKETIK ---

@Composable
fun HeadingCell(block: NoteBlock.Heading) {
    BasicTextField(
        value = block.content,
        onValueChange = { block.content = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black),
        cursorBrush = SolidColor(Color(0xFF6C63FF)),
        decorationBox = { inner ->
            if (block.content.isEmpty()) Text("Subjudul...", color = Color.LightGray)
            inner()
        }
    )
}

@Composable
fun TextCell(block: NoteBlock.Text) {
    BasicTextField(
        value = block.content,
        onValueChange = { block.content = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp, color = Color.DarkGray),
        cursorBrush = SolidColor(Color(0xFF6C63FF)),
        decorationBox = { inner ->
            if (block.content.isEmpty()) Text("Ketik sesuatu...", color = Color.LightGray)
            inner()
        }
    )
}

@Composable
fun CheckboxCellGroup(block: NoteBlock.CheckboxGroup) {
    Column {
        block.items.forEachIndexed { index, item ->
            val focusRequester = remember { FocusRequester() }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { block.items[index] = item.copy(isChecked = it) }
                )
                BasicTextField(
                    value = item.text,
                    onValueChange = { block.items[index] = item.copy(text = it) },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                        color = if (item.isChecked) Color.Gray else Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        block.items.add(index + 1, CheckItem())
                    })
                )
            }
            // Munculkan keyboard otomatis di item baru
            LaunchedEffect(block.items.size) {
                if (index == block.items.size - 1 && item.text.isEmpty()) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun BulletCellList(block: NoteBlock.BulletList) {
    Column {
        block.items.forEachIndexed { index, content ->
            val focusRequester = remember { FocusRequester() }
            Row {
                Text("• ", modifier = Modifier.padding(start = 4.dp), color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold)
                BasicTextField(
                    value = content,
                    onValueChange = { block.items[index] = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    textStyle = TextStyle(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        block.items.add(index + 1, "")
                    })
                )
            }
            LaunchedEffect(block.items.size) {
                if (index == block.items.size - 1 && content.isEmpty()) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun BlockWrapper(
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onAddBelow: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF6C63FF).copy(alpha = 0.05f) else Color.Transparent)
            .pointerInput(Unit) { detectTapGestures(onTap = { onActivate() }) }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { content() }
            if (isActive) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
        if (isActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickAddButton(Icons.Default.Title, "Judul") { onAddBelow("heading") }
                QuickAddButton(Icons.Default.ShortText, "Teks") { onAddBelow("text") }
                QuickAddButton(Icons.Default.CheckBox, "List") { onAddBelow("check") }
                QuickAddButton(Icons.Default.FormatListBulleted, "Poin") { onAddBelow("bullet") }
            }
        }
    }
}

@Composable
fun QuickAddButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onClick() }.padding(4.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6C63FF))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}