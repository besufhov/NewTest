package com.kaankivancdilli.summary.ui.component.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.ui.screens.sub.history.type.Type

@Composable
fun HistoryItem(
    message: Any,
    onClick: (String) -> Unit,
    timestamp: String,
    type: String? = null,
    isFirstBox: Boolean = false,
    enumType: Type
) {

    val context = LocalContext.current
    val typeName = enumType.displayName(context)

    val summarizeLabel = stringResource(R.string.summarize)
    val paraphraseLabel = stringResource(R.string.paraphrase)
    val rephraseLabel = stringResource(R.string.rephrase)
    val expandLabel = stringResource(R.string.expand)
    val bulletPointLabel = stringResource(R.string.bullet_point)
    val savedLabel = stringResource(R.string.saved)

    val expandedSection = remember { mutableStateOf<String?>(null) }

    val summarizeText: String
    val paraphraseText: String?
    val rephraseText: String?
    val expandText: String?
    val bulletpointText: String?
    val typeText: String?
    val name: String?
    val episode: String?
    val year: String?
    val author: String?
    val source: String?
    val director: String?
    val chapter: String?
    val season: String?

    when (message) {
        is SaveTexts -> {
            summarizeText = message.summarize
            paraphraseText = message.paraphrase
            rephraseText = message.rephrase
            expandText = message.expand
            bulletpointText = message.bulletpoint
            typeText = null
            name = null; episode = null; year = null; author = null; source = null; director = null; chapter = null; season = null
        }
        is SaveAnything -> {
            summarizeText = message.summarize
            paraphraseText = message.paraphrase
            rephraseText = message.rephrase
            expandText = message.expand
            bulletpointText = message.bulletpoint
            typeText = message.type
            name = message.name
            episode = message.episode
            year = message.year
            author = message.author
            source = message.source
            director = message.director
            chapter = message.chapter
            season = message.season
        }
        else -> {
            summarizeText = "Unknown"
            paraphraseText = null
            rephraseText = null
            expandText = null
            bulletpointText = null
            typeText = null
            name = null; episode = null; year = null; author = null; source = null; director = null; chapter = null; season = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isFirstBox) 10.dp else 4.dp,
                    bottom = 4.dp
                )
            )
            .background(Color.White, shape = RoundedCornerShape(12.dp))
    )
    {
        Column(modifier = Modifier
        ) {

            @Composable
            fun ExpandableSectionBox(
                title: String,
                text: String,
                isExpanded: Boolean,
                isFirstItem: Boolean,
                onLongPress: () -> Unit,
                onDismiss: () -> Unit,
                onClick: () -> Unit
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (isFirstItem) 0.dp else 4.dp,
                            bottom = 4.dp
                        )
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = {
                                if (isExpanded) onDismiss() else onLongPress()
                            }
                        )
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = false
                        )
                        .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val cleaned = text.replace(Regex("[+#*\"]"), "").trim()
                            val lines = (if (isExpanded) text else cleaned.take(125) + "...").split("\n")

                            lines.forEachIndexed { index, line ->
                                val cleanedLine = if (index == 0) line.replace(Regex("[+*#\"]"), "") else line
                                val indent = "\u2003\u2003" // two em-spaces
                                val displayLine = if (index == 0) cleanedLine else indent + cleanedLine // 4 spaces = soft tab look

                                Text(
                                    text = displayLine,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = Color.Black,
                                    textAlign = if (index == 0) TextAlign.Center else TextAlign.Justify,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            typeText?.let {
                val details = when (enumType) {
                    Type.TVSHOW -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                    Type.BOOK -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                    Type.MOVIE -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                    Type.ARTICLE -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                    Type.BIOGRAPHY -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                    Type.ANIME -> listOfNotNull(
                        name?.takeIf { it.isNotBlank() },
                    )
                }

                if (details.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .shadow(
                                elevation = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                clip = false
                            )
                            .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = typeName + ": " + details
                                .joinToString(", ") {
                                    it.split(" ")
                                        .joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }
                                },
                            color = Color.DarkGray,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            var isFirstItemHandled = false

            listOf(
                summarizeLabel to summarizeText,
                paraphraseLabel to paraphraseText,
                rephraseLabel to rephraseText,
                expandLabel to expandText,
                bulletPointLabel to bulletpointText
            ).forEach { (title, text) ->
                if (!text.isNullOrBlank()) {
                    ExpandableSectionBox(
                        title = title,
                        text = text,
                        isExpanded = expandedSection.value == title,
                        isFirstItem = !isFirstItemHandled,
                        onLongPress = { expandedSection.value = title },
                        onDismiss = { if (expandedSection.value == title) expandedSection.value = null },
                        onClick = { onClick(title) }
                    )
                    isFirstItemHandled = true
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$savedLabel $timestamp",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}