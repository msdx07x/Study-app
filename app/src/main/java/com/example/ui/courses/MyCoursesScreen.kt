package com.example.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.CourseEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.CourseProgressBar
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

enum class CourseSortOrder {
    RECENT, TITLE, PROGRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    courses: List<CourseEntity>,
    courseStats: Map<Long, Pair<Int, Int>>, // courseId -> (completed, total)
    onOpenCourse: (Long) -> Unit,
    onContinueCourse: (Long) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    onRenameCourse: (Long, String) -> Unit,
    onImportClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(CourseSortOrder.RECENT) }
    var showSortMenu by remember { mutableStateOf(false) }

    var courseToDelete by remember { mutableStateOf<CourseEntity?>(null) }
    var courseToRename by remember { mutableStateOf<CourseEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    // Filter and sort
    val filteredCourses = remember(courses, searchQuery, sortOrder, courseStats) {
        courses.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    (it.description?.contains(searchQuery, ignoreCase = true) == true)
        }.let { list ->
            when (sortOrder) {
                CourseSortOrder.RECENT -> list.sortedByDescending { it.lastStudiedAt ?: it.importedAt }
                CourseSortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
                CourseSortOrder.PROGRESS -> list.sortedByDescending { course ->
                    val (completed, total) = courseStats[course.id] ?: (0 to 0)
                    if (total > 0) completed.toFloat() / total.toFloat() else 0f
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Courses",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_courses_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Studied", color = TextPrimary) },
                                onClick = {
                                    sortOrder = CourseSortOrder.RECENT
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Course Title", color = TextPrimary) },
                                onClick = {
                                    sortOrder = CourseSortOrder.TITLE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Progress", color = TextPrimary) },
                                onClick = {
                                    sortOrder = CourseSortOrder.PROGRESS
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onImportClick,
                containerColor = ElectricBlue,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_import_course")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Course")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            if (courses.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search imported courses...", color = TextTertiary) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextTertiary)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_courses_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredCourses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.School,
                    title = if (courses.isEmpty()) "No Courses Imported Yet" else "No Matching Courses",
                    subtitle = if (courses.isEmpty())
                        "Import any educational playlist from YouTube to create structured courses with chapters and reminders."
                    else "Try a different search term.",
                    actionLabel = if (courses.isEmpty()) "Import YouTube Course" else null,
                    onActionClick = if (courses.isEmpty()) onImportClick else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("courses_list")
                ) {
                    items(filteredCourses, key = { it.id }) { course ->
                        var showCardMenu by remember { mutableStateOf(false) }
                        val (completed, total) = courseStats[course.id] ?: (0 to course.totalKnownVideos)
                        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("course_card_${course.id}")
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                                .clickable { onOpenCourse(course.id) },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Thumbnail banner
                                if (!course.thumbnailUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(DarkSurfaceVariant)
                                    ) {
                                        AsyncImage(
                                            model = course.thumbnailUrl,
                                            contentDescription = course.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = course.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2
                                        )

                                        Box {
                                            IconButton(
                                                onClick = { showCardMenu = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = TextSecondary
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showCardMenu,
                                                onDismissRequest = { showCardMenu = false },
                                                modifier = Modifier.background(DarkSurface)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Rename", color = TextPrimary) },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue) },
                                                    onClick = {
                                                        showCardMenu = false
                                                        courseToRename = course
                                                        renameText = course.title
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                    onClick = {
                                                        showCardMenu = false
                                                        courseToDelete = course
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CourseProgressBar(
                                        progress = progress,
                                        completedCount = completed,
                                        totalCount = total
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (course.lastStudiedAt != null) "Studied recently" else "Not started yet",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                                        )

                                        Button(
                                            onClick = { onContinueCourse(course.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("continue_btn_${course.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (completed > 0) "Continue" else "Start",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    courseToDelete?.let { course ->
        ConfirmDialog(
            title = "Delete Course",
            message = "Are you sure you want to delete \"${course.title}\"? All saved playback positions, chapters, lesson notes, and scheduled reminders for this course will also be deleted.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteCourse(course.id)
                courseToDelete = null
            },
            onDismiss = { courseToDelete = null }
        )
    }

    // Rename Dialog
    courseToRename?.let { course ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { courseToRename = null },
            title = { Text("Rename Course", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Course Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameCourse(course.id, renameText.trim())
                            courseToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(onClick = { courseToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
