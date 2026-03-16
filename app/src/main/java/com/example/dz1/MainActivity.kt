@file:OptIn(ExperimentalUuidApi::class)

package com.example.dz1

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dz1.ui.theme.Dz1Theme
import kotlinx.coroutines.launch
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
            }
            viewModel.importNotesFromUri(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shareFileFlow.collect { fileName ->
                    shareInternalFile(fileName)
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importResultFlow.collect { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }


        setContent {
            Dz1Theme {
                App(viewModel = viewModel, onImport = { openImportPicker() })


            }
        }
    }

    private fun shareInternalFile(fileName: String) {
        val file = File(filesDir, fileName)
        if (!file.exists()) return

        val authority = "${applicationContext.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(this, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val resolvedActivities =
            packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (ri in resolvedActivities) {
            grantUriPermission(
                ri.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        startActivity(Intent.createChooser(shareIntent, "Share notes.json"))
    }

    fun openImportPicker() {
        openDocumentLauncher.launch(arrayOf("application/json", "text/json", "text/*", "*/*"))
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel, onImport: () -> Unit) {
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
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Импорт/экспорт")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Импортировать заметки") },
                                    onClick = { onImport(); expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Экспортировать заметки") },
                                    onClick = { viewModel.exportNotes(); expanded = false }
                                )
                            }
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