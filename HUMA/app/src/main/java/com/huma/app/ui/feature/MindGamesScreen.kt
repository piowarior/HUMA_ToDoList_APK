package com.huma.app.ui.feature

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- ENUMS & DATA CLASSES ---
enum class GameType { QUICK_MATH, WORD_SCRAMBLE, MEMORY_FLIP, EMOJI_HUNT }
data class GameResult(val question: String, val correct: String, val user: String, val isCorrect: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindGamesScreen(navController: NavController) {
    var selectedGame by remember { mutableStateOf<GameType?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mind Gym 🧠", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedGame != null) selectedGame = null
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFFF6F7FB), Color(0xFFEDEBFF))))
        ) {
            AnimatedContent(
                targetState = selectedGame,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                }, label = ""
            ) { game ->
                if (game == null) {
                    GameSelectionMenu { selectedGame = it }
                } else {
                    when (game) {
                        GameType.QUICK_MATH -> QuickMathGame { selectedGame = null }
                        GameType.WORD_SCRAMBLE -> WordScrambleGame { selectedGame = null }
                        GameType.MEMORY_FLIP -> MemoryFlipGame { selectedGame = null }
                        GameType.EMOJI_HUNT -> EmojiHuntGame { selectedGame = null }
                    }
                }
            }
        }
    }
}

@Composable
fun GameSelectionMenu(onSelect: (GameType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Siap mengasah otakmu hari ini?", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { GameCard("Quick Math", "Hitung cepat dalam waktu terbatas!", Icons.Default.Calculate, Color(0xFF6C63FF)) { onSelect(GameType.QUICK_MATH) } }
            item { GameCard("Word Scramble", "Susun kata yang acak berantakan", Icons.Default.Abc, Color(0xFFFF7043)) { onSelect(GameType.WORD_SCRAMBLE) } }
            item { GameCard("Memory Flip", "Ingat dan temukan pasangan emoji", Icons.Default.GridOn, Color(0xFF4CAF50)) { onSelect(GameType.MEMORY_FLIP) } }
            item { GameCard("Emoji Hunt", "Cari satu emoji yang paling beda", Icons.Default.EmojiEmotions, Color(0xFFFFA726)) { onSelect(GameType.EMOJI_HUNT) } }
        }
    }
}

// --- KOMPONEN REUSABLE ---

@Composable
fun TimerBar(timeLeft: Int, totalTime: Int, color: Color) {
    val progress = timeLeft.toFloat() / totalTime
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Waktu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Text("${timeLeft}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun GameCard(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(desc, color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

// --- GAME 1: QUICK MATH ---
@Composable
fun QuickMathGame(onExit: () -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var questionCount by remember { mutableIntStateOf(1) }
    var currentQuestion by remember { mutableStateOf(generateMathProblem()) }
    var userAnswer by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(30) } // 30 Detik total
    val results = remember { mutableStateListOf<GameResult>() }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) isGameOver = true
    }

    if (isGameOver) {
        GameOverResultView(results, onExit)
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TimerBar(timeLeft, 30, Color(0xFF6C63FF))
            Spacer(Modifier.height(40.dp))
            Text("Soal ke-$questionCount", fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(currentQuestion.text, fontSize = 64.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 32.dp))

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { if (it.length <= 4) userAnswer = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 32.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val isCorrect = userAnswer == currentQuestion.answer.toString()
                    results.add(GameResult(currentQuestion.text, currentQuestion.answer.toString(), userAnswer, isCorrect))
                    if (isCorrect) score++

                    if (questionCount < 10) {
                        questionCount++
                        currentQuestion = generateMathProblem()
                        userAnswer = ""
                    } else { isGameOver = true }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) { Text("JAWAB", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        }
    }
}

// --- GAME 2: WORD SCRAMBLE ---
@Composable
fun WordScrambleGame(onExit: () -> Unit) {
    val words = listOf("FOCUS", "STREAK", "HABIT", "HUMAN", "DASHBOARD", "CAPSULE", "WHEEL", "REWARD", "COMMIT", "DREAM")
    var currentWordIdx by remember { mutableIntStateOf(0) }
    val scrambled = remember(currentWordIdx) { words[currentWordIdx].toList().shuffled().joinToString("") }
    var userAnswer by remember { mutableStateOf("") }
    val results = remember { mutableStateListOf<GameResult>() }
    var timeLeft by remember { mutableIntStateOf(45) }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) isGameOver = true
    }

    if (isGameOver) {
        GameOverResultView(results, onExit)
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TimerBar(timeLeft, 45, Color(0xFFFF7043))
            Spacer(Modifier.height(40.dp))
            Text("Kata ke-${currentWordIdx + 1}/5", color = Color.Gray)
            Text(scrambled, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, color = Color(0xFFFF7043), modifier = Modifier.padding(vertical = 32.dp))

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it.uppercase() },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Susun kata...") },
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val original = words[currentWordIdx]
                    val isCorrect = userAnswer == original
                    results.add(GameResult(scrambled, original, userAnswer, isCorrect))
                    if (currentWordIdx < 4) {
                        currentWordIdx++
                        userAnswer = ""
                    } else { isGameOver = true }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
            ) { Text("SUBMIT", fontWeight = FontWeight.Bold) }
        }
    }
}

// --- GAME 3: MEMORY FLIP ---
@Composable
fun MemoryFlipGame(onExit: () -> Unit) {
    val icons = listOf("🍎", "🍌", "🍇", "🍉", "🍒", "🍍")
    val cards = remember { (icons + icons).shuffled().map { CardState(it) } }
    var flippedIndices by remember { mutableStateOf(setOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }
    var moves by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(40) }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && matchedIndices.size < cards.size) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) isGameOver = true
    }

    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            val list = flippedIndices.toList()
            if (cards[list[0]].emoji == cards[list[1]].emoji) {
                matchedIndices = matchedIndices + flippedIndices
            }
            delay(600)
            flippedIndices = emptySet()
            moves++
        }
    }

    if (isGameOver || matchedIndices.size == cards.size) {
        GameOverResultView(emptyList(), onExit, if (timeLeft == 0) "Waktu Habis!" else "Game Clear! Moves: $moves")
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            TimerBar(timeLeft, 40, Color(0xFF4CAF50))
            Spacer(Modifier.height(24.dp))
            Text("Langkah: $moves", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards.size) { index ->
                    val isFlipped = index in flippedIndices || index in matchedIndices
                    val scale by animateFloatAsState(if (isFlipped) 1f else 0.9f, label = "")

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isFlipped) Color.White else Color(0xFF4CAF50))
                            .clickable(enabled = !isFlipped && flippedIndices.size < 2) {
                                flippedIndices = flippedIndices + index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFlipped) Text(cards[index].emoji, fontSize = 40.sp)
                        else Icon(Icons.Default.QuestionMark, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// --- GAME 4: EMOJI HUNT ---
@Composable
fun EmojiHuntGame(onExit: () -> Unit) {
    val emojis = listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "☺️", "😊", "😇", "🙂")
    var round by remember { mutableIntStateOf(1) }
    val results = remember { mutableStateListOf<GameResult>() }
    var timeLeft by remember { mutableIntStateOf(20) }
    var isGameOver by remember { mutableStateOf(false) }

    val targetEmoji = remember(round) { emojis.random() }
    val gridEmojis = remember(round) {
        (List(11) { emojis.filter { it != targetEmoji }.random() } + targetEmoji).shuffled()
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) isGameOver = true
    }

    if (isGameOver) {
        GameOverResultView(results, onExit)
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TimerBar(timeLeft, 20, Color(0xFFFFA726))
            Spacer(Modifier.height(32.dp))
            Text("Cari emoji ini:", fontSize = 14.sp, color = Color.Gray)
            Text(targetEmoji, fontSize = 80.sp, modifier = Modifier.padding(16.dp))

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gridEmojis.size) { index ->
                    Card(
                        modifier = Modifier.aspectRatio(1f).clickable {
                            val correct = gridEmojis[index] == targetEmoji
                            results.add(GameResult("Cari $targetEmoji", targetEmoji, gridEmojis[index], correct))
                            if (round < 10) round++ else isGameOver = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(gridEmojis[index], fontSize = 40.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- RESULT VIEW ---

@Composable
fun GameOverResultView(results: List<GameResult>, onExit: () -> Unit, title: String = "Selesai! 🏁") {
    val score = results.count { it.isCorrect }
    val total = results.size

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 32.sp, fontWeight = FontWeight.Black)
        if (total > 0) {
            Text("Skor kamu: $score / $total", fontSize = 20.sp, color = Color(0xFF6C63FF), fontWeight = FontWeight.ExtraBold)
        }

        Spacer(Modifier.height(24.dp))

        if (results.isEmpty()) {
            Text("Hebat! Kamu berhasil menyelesaikan tantangan tepat waktu.", textAlign = TextAlign.Center, color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(results) { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (res.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (res.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (res.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(res.question, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (res.isCorrect) "Benar! ✅" else "Salah (Jawaban: ${res.correct})",
                                    fontSize = 12.sp,
                                    color = if (res.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text("KEMBALI KE MENU", fontWeight = FontWeight.Bold)
        }
    }
}

// --- LOGIC HELPER ---
data class CardState(val emoji: String)
data class MathProblem(val text: String, val answer: Int)
fun generateMathProblem(): MathProblem {
    val a = Random.nextInt(1, 20)
    val b = Random.nextInt(1, 20)
    val op = listOf("+", "-", "×").random()
    val ans = when (op) {
        "+" -> a + b
        "-" -> a - b
        else -> a * Random.nextInt(1, 10)
    }
    return MathProblem(if (op == "×") "$a × ${ans/a}" else "$a $op $b", ans)
}