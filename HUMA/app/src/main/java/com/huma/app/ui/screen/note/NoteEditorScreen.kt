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

// --- DATA MODELS (Tetap Sesuai Kode Asli Kamu) ---
data class NoteData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val blocks: List<NoteBlock>,
    val date: String
) {
    val previewBlocks: List<NoteBlock> get() = blocks.take(3)
    val previewContent: String get() {
        return blocks.joinToString(separator = " ") { block ->
            when (block) {
                is NoteBlock.Text -> block.content
                is NoteBlock.Heading -> block.content
                is NoteBlock.BulletList -> block.items.joinToString(" ")
                is NoteBlock.CheckboxGroup -> block.items.joinToString(" ") { it.text }
            }
        }.trim()
    }
}

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

// --- SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    navController: NavController,
    noteId: String? = null,
    globalNotes: List<NoteData> = emptyList(),
    onSave: (NoteData) -> Unit
) {
    val blocks = remember { mutableStateListOf<NoteBlock>() }
    var noteTitle by remember { mutableStateOf("") }
    var activeBlockId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // State untuk kontrol opsi di baris terakhir
    var showInitialOptions by remember { mutableStateOf(false) }

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
                id = noteId ?: UUID.randomUUID().toString(),
                title = if (noteTitle.isEmpty()) "Untitled" else noteTitle,
                blocks = blocks.toList(),
                date = currentDate
            )
            onSave(newNote)
        }
        navController.popBackStack()
    }

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
                navigationIcon = { IconButton(onClick = { handleSaveAndExit() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = { handleSaveAndExit() }) { Icon(Icons.Default.Done, null, tint = Color(0xFF6C63FF)) } }
            )
        },
        floatingActionButton = {
            // FAB muncul jika tidak ada blok yang aktif (Mode Baca)
            if (activeBlockId == null && !showInitialOptions) {
                ExtendedFloatingActionButton(
                    onClick = { showInitialOptions = true },
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
        Box(modifier = Modifier.padding(padding).fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                // LOGIKA AUTO-HAPUS: Hapus block teks/heading yang kosong saat ditinggalkan
                val toRemove = blocks.filter { block ->
                    (block is NoteBlock.Text && block.content.isEmpty()) ||
                            (block is NoteBlock.Heading && block.content.isEmpty())
                }
                blocks.removeAll(toRemove)

                activeBlockId = null
                showInitialOptions = false
            })
        }) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Render semua blok yang ada
                    itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
                        BlockWrapper(
                            isActive = activeBlockId == block.id,
                            onActivate = { activeBlockId = block.id },
                            onDelete = { blocks.remove(block) },
                            onAddAbove = { type ->
                                val new = createBlockByType(type)
                                blocks.add(index, new); activeBlockId = new.id
                            },
                            onAddBelow = { type ->
                                val new = createBlockByType(type)
                                blocks.add(index + 1, new); activeBlockId = new.id
                            }
                        ) {
                            when (block) {
                                is NoteBlock.Heading -> HeadingCell(block, activeBlockId == block.id)
                                is NoteBlock.Text -> TextCell(block, activeBlockId == block.id)
                                is NoteBlock.CheckboxGroup -> {
                                    CheckboxCellGroup(
                                        block = block,
                                        isActive = activeBlockId == block.id,
                                        onRemoveBlock = { blocks.remove(block) }
                                    )
                                }
                                is NoteBlock.BulletList -> BulletCellList(block, activeBlockId == block.id)
                            }
                        }
                    }

                    // --- INI PERBAIKANNYA ---
                    // Opsi Menu muncul sebagai BARIS TERAKHIR di dalam LazyColumn
                    if (showInitialOptions) {
                        item {
                            Box(modifier = Modifier.padding(vertical = 16.dp)) {
                                InsertSection(onAdd = { type ->
                                    val new = createBlockByType(type)
                                    blocks.add(new) // Menambah ke posisi paling akhir
                                    activeBlockId = new.id
                                    showInitialOptions = false
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

fun createBlockByType(type: String) = when(type) {
    "heading" -> NoteBlock.Heading()
    "check" -> NoteBlock.CheckboxGroup()
    "bullet" -> NoteBlock.BulletList()
    else -> NoteBlock.Text()
}

// --- CELLS ---

@Composable
fun HeadingCell(block: NoteBlock.Heading, isActive: Boolean) {
    if (isActive) {
        BasicTextField(
            value = block.content,
            onValueChange = { block.content = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            cursorBrush = SolidColor(Color(0xFF6C63FF)),
            decorationBox = { inner -> if (block.content.isEmpty()) Text("Subjudul...", color = Color.LightGray); inner() }
        )
    } else {
        // GANTI: Jika kosong tidak tampil apa-apa (karena logic cleanup akan menghapusnya)
        if (block.content.isNotEmpty()) {
            Text(block.content, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black))
        }
    }
}

@Composable
fun TextCell(block: NoteBlock.Text, isActive: Boolean) {
    if (isActive) {
        val focusRequester = remember { FocusRequester() }
        BasicTextField(
            value = block.content,
            onValueChange = { block.content = it },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            cursorBrush = SolidColor(Color(0xFF6C63FF))
        )
        LaunchedEffect(Unit) { if (block.content.isEmpty()) focusRequester.requestFocus() }
    } else {
        // GANTI: Tidak menampilkan "Ketik sesuatu" di mode baca agar benar-benar kosong
        Text(block.content, color = Color.Black)
    }
}

@Composable
fun CheckboxCellGroup(block: NoteBlock.CheckboxGroup, isActive: Boolean, onRemoveBlock: () -> Unit) {
    Column {
        block.items.forEachIndexed { index, item ->
            val focusRequester = remember { FocusRequester() }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isChecked, onCheckedChange = { block.items[index] = item.copy(isChecked = it) })
                if (isActive) {
                    BasicTextField(
                        value = item.text,
                        onValueChange = { block.items[index] = item.copy(text = it) },
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        textStyle = TextStyle(fontSize = 16.sp, textDecoration = if (item.isChecked) TextDecoration.LineThrough else null, color = if (item.isChecked) Color.Gray else Color.Black),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { block.items.add(index + 1, CheckItem()) })
                    )
                } else {
                    Text(item.text, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 16.sp, textDecoration = if (item.isChecked) TextDecoration.LineThrough else null, color = if (item.isChecked) Color.Gray else Color.Black))
                }

                // PERBAIKAN 2: Tombol hapus item (Muncul di mode baca & edit)
                IconButton(onClick = {
                    if (block.items.size > 1) {
                        block.items.removeAt(index)
                    } else {
                        // Jika item terakhir dihapus, hapus seluruh block checkboxnya
                        onRemoveBlock()
                    }
                }) {
                    Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }
            if (isActive) LaunchedEffect(block.items.size) { if (index == block.items.size - 1 && item.text.isEmpty()) focusRequester.requestFocus() }
        }
    }
}

@Composable
fun BulletCellList(block: NoteBlock.BulletList, isActive: Boolean) {
    Column {
        block.items.forEachIndexed { index, content ->
            val focusRequester = remember { FocusRequester() }
            Row {
                Text("• ", modifier = Modifier.padding(start = 4.dp), color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold)
                if (isActive) {
                    BasicTextField(
                        value = content,
                        onValueChange = { block.items[index] = it },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        textStyle = TextStyle(fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { block.items.add(index + 1, "") })
                    )
                } else {
                    Text(content, modifier = Modifier.fillMaxWidth())
                }
            }
            if (isActive) LaunchedEffect(block.items.size) { if (index == block.items.size - 1 && content.isEmpty()) focusRequester.requestFocus() }
        }
    }
}

// --- WRAPPERS ---

@Composable
fun BlockWrapper(isActive: Boolean, onActivate: () -> Unit, onDelete: () -> Unit, onAddAbove: (String) -> Unit, onAddBelow: (String) -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTapGestures(onTap = { onActivate() }) }) {
        if (isActive) InsertSection(onAdd = onAddAbove)
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isActive) Color(0xFF6C63FF).copy(alpha = 0.05f) else Color.Transparent).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) { content() }
            if (isActive) IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
        }
        if (isActive) InsertSection(onAdd = onAddBelow)
    }
}

@Composable
fun InsertSection(onAdd: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    // Logic awal: kalau dipanggil di start, default expanded
    val expanded = isExpanded || true // Dibuat selalu bisa diklik atau terlihat di awal

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isExpanded) {
            Box(modifier = Modifier.fillMaxWidth().height(20.dp).clickable { isExpanded = true }, contentAlignment = Alignment.Center) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Surface(shape = RoundedCornerShape(50), color = Color(0xFF6C63FF), modifier = Modifier.size(18.dp)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.padding(2.dp)) }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickAddButton(Icons.Default.Title, "H") { onAdd("heading"); isExpanded = false }
                QuickAddButton(Icons.Default.ShortText, "Txt") { onAdd("text"); isExpanded = false }
                QuickAddButton(Icons.Default.CheckBox, "Cek") { onAdd("check"); isExpanded = false }
                QuickAddButton(Icons.Default.FormatListBulleted, "List") { onAdd("bullet"); isExpanded = false }
                IconButton(onClick = { isExpanded = false }) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
            }
        }
    }
}

@Composable
fun QuickAddButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onClick() }.padding(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6C63FF))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}