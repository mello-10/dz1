@file:OptIn(ExperimentalUuidApi::class)

package com.example.dz1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dz1.ui.theme.Dz1Theme
import kotlin.uuid.ExperimentalUuidApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MainViewModel by viewModels()
        setContent {
            Dz1Theme {
                App(viewModel = viewModel)

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (state.openedNote == null) {
                        Text(
                            "Супер классные заметки",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            "Редактирование заметки",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (state.openedNote == null) {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (state.openedNote != null) {
                        IconButton(onClick = { viewModel.closeNote() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back button"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.openedNote == null) {
                FloatingActionButton(onClick = { viewModel.createNote() }) {
                    Icon(Icons.Filled.Add, "Localized description")
                }
            }
        }

    ) { innerPadding ->
        if (state.openedNote == null) {
            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(state.notes) { note ->
                    NoteItem(note, onOpen = { viewModel.openNote(note.id) })
                }
            }

        } else {
            EditingNote(
                state.openedNote!!,
                onSave = {
                    viewModel.saveNote(it)
                    viewModel.closeNote()
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}






