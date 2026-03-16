@file:OptIn(ExperimentalUuidApi::class)

package com.example.dz1

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MainViewState(
    val notes: List<Note>,
    val openedNote: Note?

) {}

class MainViewModel : ViewModel() {
    val context = NotesApplication.instance ?: error("Application context is null")

    private val _state = MutableStateFlow(
        MainViewState(
            notes = emptyList(),
            openedNote = null,
        )
    )
    val state: StateFlow<MainViewState> = _state

    private val _importResultFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val importResultFlow: SharedFlow<String> = _importResultFlow

    private val _shareFileFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val shareFileFlow: SharedFlow<String> = _shareFileFlow


    init {
        viewModelScope.launch(Dispatchers.IO) {
            _state.emit(state.value.copy(notes = loadNotes()))
        }
    }

    suspend fun loadNotes(): List<Note> {
        val file = context.filesDir.resolve("notes.json")
        val notes = try {
            val content = file.readText()
            Json.decodeFromString<List<Note>>(content)
        } catch (e: Exception) {
            emptyList()
        }
        return notes
    }

    fun openNote(id: Uuid) {
        viewModelScope.launch() {
            val notes = loadNotes()
            val noteToOpen = notes.firstOrNull { it.id == id }
            if (noteToOpen == null) {
                Log.w("SCN", "Заметка не найдена")
                return@launch
            }
            _state.emit(state.value.copy(openedNote = noteToOpen))
        }
    }

    fun closeNote() {
        viewModelScope.launch {
            _state.emit(state.value.copy(openedNote = null))
        }
    }

    fun saveNote(newNote: Note) {
        viewModelScope.launch {
            val notes = loadNotes().filter { it.id != newNote.id }
            val newNotes = notes + newNote
            val file = context.filesDir.resolve("notes.json")
            val content = Json.encodeToString(newNotes)
            file.writeText(content)
            _state.emit(state.value.copy(notes = newNotes))
        }
    }

    fun createNote() {
        viewModelScope.launch {
            val note = Note(title = "", body = "", id = Uuid.random())
            _state.emit(state.value.copy(openedNote = note))
        }
    }

    fun importNotesFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Cannot open input stream")

                val importedNotes: List<Note> = Json.decodeFromString(content)

                val file = context.filesDir.resolve("notes.json")
                val outContent = Json.encodeToString(importedNotes)
                file.writeText(outContent)

                _state.emit(state.value.copy(notes = importedNotes))

                _importResultFlow.emit("Импорт успешно завершён: ${importedNotes.size} заметок")
            } catch (e: Exception) {
                Log.e("MainViewModel", "importNotesFromUri failed", e)
                _importResultFlow.emit("Ошибка импорта: ${e.message ?: e::class.simpleName}")
            }
        }
    }


    fun exportNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notesToExport = state.value.notes
                val content = Json.encodeToString(notesToExport)
                val file = context.filesDir.resolve("notes.json")
                file.writeText(content)
                _shareFileFlow.emit(file.name)
            } catch (e: Exception) {
                Log.e("MainViewModel", "exportNotes failed", e)
            }
        }
    }

}

val testNotes = listOf(
    Note(
        "Список покупок",
        "Нужно зайти в магазин после работы и купить молоко, свежий хлеб, десяток яиц и пачку молотого кофе. Также не забудь проверить, остались ли дома салфетки и стиральный порошок.",
        Uuid.parse("58d5e212-165b-4ca0-909b-c86b9cee0111")
    ),
    Note(
        "Идея для проекта",
        "Создать мобильное приложение для отслеживания полива комнатных растений. Пользователь вводит название цветка, а приложение присылает уведомление, когда почва должна высохнуть. Можно добавить базу знаний об уходе.",
        Uuid.parse("550e8400-e29b-41d4-a716-446655440000")
    ),
    Note(
        "Тренировка в понедельник",
        "Разминка 10 минут, затем три подхода подтягиваний по 12 раз. Жим гантелей лежа и приседания со штангой. В конце обязательная растяжка всех групп мышц и легкое кардио на беговой дорожке.",
        Uuid.parse("36b8f84d-df4e-4d49-b662-bcde71a8764f")
    ),
    Note(
        "Рецепт быстрого завтрака",
        "Разбить два яйца на сковороду, добавить нарезанные помидоры и немного сыра фета. Посыпать сушеным базиликом и подавать с поджаренным тостом. Это занимает всего пять минут и дает заряд энергии.",
        Uuid.parse("44e128a5-ac7a-4c9a-be4c-224b6bf81b20")
    ),
    Note(
        "Заметки с совещания",
        "Обсудили запуск новой рекламной кампании. Нужно подготовить макеты баннеров к четвергу и согласовать бюджет с финансовым отделом. Ответственным за интеграцию API назначен Алексей, срок реализации — две недели.",
        Uuid.parse("93ec6425-bb04-499a-b47b-790fd013ab0d")
    ),
    Note(
        "Список книг на лето",
        "Хочу прочитать 'Дюну' Фрэнка Герберта, 'Sapiens' Юваля Харари и что-нибудь из классики, например, 'Сто лет одиночества' Маркеса. Важно выделять хотя бы 30 минут на чтение перед сном каждый день.",
        Uuid.parse("f553ca75-657b-4b5d-85be-df1082785a0b")
    ),
    Note(
        "Пароль от Wi-Fi",
        "Новый пароль от роутера в гостиной: SuperSecureNet2024. Гостевая сеть работает без пароля, но скорость там ограничена. Не забыть наклеить стикер с этими данными на заднюю панель устройства.",
        Uuid.parse("525ecc38-9e93-4f20-9b97-d0f0db09d9c6")
    ),
    Note(
        "Цитата дня",
        "Успех — это не конечное состояние, а процесс постоянного движения вперед, несмотря на неудачи и трудности. Главное — не терять энтузиазма и продолжать пробовать новые подходы к решению старых задач.",
        Uuid.parse("c2d438c6-20ea-4cd5-baaf-d448f9fe945b")
    ),
    Note(
        "Настройка рабочего окружения",
        "Установить последнюю версию IntelliJ IDEA, скачать JDK 21 и настроить Git. Также нужно импортировать настройки линтера из общего репозитория команды, чтобы код соответствовал принятым стандартам оформления.",
        Uuid.parse("fb4bc573-4931-415e-94e7-8da62cb5fe0d")
    ),
    Note(
        "Маршрут выходного дня",
        "Поехать в загородный парк, арендовать велосипеды и объехать вокруг озера. После этого можно устроить небольшой пикник на поляне и сделать несколько фотографий заката. Главное, чтобы погода не подвела.",
        Uuid.parse("a7eda46a-215c-4e55-be3a-a957ba74ca9c")
    )
)