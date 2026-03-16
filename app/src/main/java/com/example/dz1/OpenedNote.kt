@file:OptIn(ExperimentalUuidApi::class)

package com.example.dz1

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun EditingNote(note: Note, modifier: Modifier, onSave: (Note) -> Unit) {
    var newTitle by rememberSaveable(note) { mutableStateOf(note.title) }
    var newBody by rememberSaveable(note) { mutableStateOf(note.body) }
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newTitle,
            onValueChange = { newTitle = it },
            maxLines = 1,
            label = { Text("Название") },
            textStyle = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = newBody,
            onValueChange = { newBody = it },
            label = { Text("Заметка") },
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = { onSave(note.copy(title = newTitle, body = newBody)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ) {
            Text("Сохранить")
        }

    }
}