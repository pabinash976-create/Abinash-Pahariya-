package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.ImportantQuestionEntity
import com.example.data.LocalVaultMatch
import com.example.data.QuizQuestionEntity
import com.example.data.StudyLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiStudyRepository"
private const val MODEL_NAME = "gemini-3.8-flash"

enum class AiStudyMode(val label: String, val hint: String) {
    AI_SEARCH("AI Search", "Search any concept, exam question, or your notes..."),
    OPEN_CHAT("Ask Anything", "Ask any academic question in English, Assamese, or Hindi..."),
    EXPLAIN_TOPIC("Explain Topic", "Enter a concept or chapter topic to break down clearly..."),
    SUMMARIZE_LESSON("Summarize Lesson", "Paste lecture notes or scanned text to summarize..."),
    GENERATE_QUESTIONS("Important Qs", "Enter a topic to generate exam-ready important questions..."),
    ASSIGNMENT_HELP("Assignment Help", "Describe your assignment prompt or paste draft points..."),
    GENERATE_QUIZ("Quiz Builder", "Enter a topic to generate 4 interactive MCQ quiz questions...")
}

enum class AiSearchScope(val label: String) {
    ALL_KNOWLEDGE("AI + My Vault"),
    MY_VAULT_ONLY("My Vault Focus")
}

data class AiSearchResult(
    val query: String,
    val directAnswer: String,
    val keyPoints: List<String>,
    val examTip: String,
    val relatedQueries: List<String>
)

sealed interface AiStreamEvent {
    data class Chunk(val accumulatedText: String) : AiStreamEvent
    data class Completed(val finalText: String) : AiStreamEvent
    data class Error(val message: String) : AiStreamEvent
}

class AiStudyRepository {

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are WOLF STUDY AI, a dedicated personal academic tutor for a university/college student. " +
                        "Provide accurate, structured, exam-oriented study help across Education (Major), Assamese (Minor), " +
                        "AEC, GEC, VAC, SEC, and Government Current Affairs. " +
                        "Respond strictly in the language requested by the user (English, Assamese in অসমীয়া script, or Hindi in देवनागरी script). " +
                        "Keep explanations crisp, well-organized with bullet points, and encouraging."
                )
            }
        )
    }

    private val importantQuestionsSchema = Schema.obj(
        properties = mapOf(
            "items" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Exam-style academic question"),
                        "answer" to Schema.string(description = "Clear model answer"),
                        "priority" to Schema.enumeration(
                            values = listOf("HIGH", "MEDIUM", "LOW"),
                            description = "Exam importance level"
                        )
                    )
                ),
                description = "List of important study questions and answers"
            )
        )
    )

    private val quizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Multiple-choice question"),
                        "optionA" to Schema.string(),
                        "optionB" to Schema.string(),
                        "optionC" to Schema.string(),
                        "optionD" to Schema.string(),
                        "correctIndex" to Schema.integer(description = "Zero-based index 0 to 3 of the right option"),
                        "explanation" to Schema.string(description = "Why the answer is correct")
                    )
                ),
                description = "4 multiple choice questions"
            )
        )
    )

    private val searchSchema = Schema.obj(
        properties = mapOf(
            "directAnswer" to Schema.string(description = "Clear, direct answer to the search query"),
            "keyPoints" to Schema.array(
                items = Schema.string(),
                description = "3 to 5 concise study bullet points or definitions"
            ),
            "examTip" to Schema.string(description = "Practical exam or memory tip for this topic"),
            "relatedQueries" to Schema.array(
                items = Schema.string(),
                description = "3 short follow-up study search queries"
            )
        )
    )

    private val searchModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = searchSchema
            }
        )
    }

    private val importantQuestionsModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = importantQuestionsSchema
            }
        )
    }

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizSchema
            }
        )
    }

    fun streamStudyHelp(
        prompt: String,
        mode: AiStudyMode,
        language: StudyLanguage,
        subjectName: String,
        attachedImage: Bitmap? = null
    ): Flow<AiStreamEvent> = flow {
        val fullInstruction = buildPrompt(prompt, mode, language, subjectName)
        val accumulated = StringBuilder()

        withTimeout(60_000L) {
            val requestContent = content {
                if (attachedImage != null) {
                    image(attachedImage)
                }
                text(fullInstruction)
            }
            chatModel.generateContentStream(requestContent).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(AiStreamEvent.Chunk(accumulated.toString()))
                }
            }
        }

        val finalOutput = accumulated.toString().trim()
        if (finalOutput.isNotEmpty()) {
            emit(AiStreamEvent.Completed(finalOutput))
        } else {
            emit(AiStreamEvent.Error("AI returned an empty response. Please rephrase your question."))
        }
    }.catch { error ->
        if (error is CancellationException && error !is TimeoutCancellationException) {
            throw error
        }
        Log.e(TAG, "Firebase AI stream error", error)
        val message = when (error) {
            is TimeoutCancellationException -> "Request timed out after 60 seconds. Check your connection and try again."
            else -> error.localizedMessage ?: "Unable to reach AI Study Assistant."
        }
        emit(AiStreamEvent.Error(message))
    }

    suspend fun generateStructuredImportantQuestions(
        topic: String,
        subjectId: Int,
        subjectName: String,
        language: StudyLanguage
    ): Result<List<ImportantQuestionEntity>> {
        return try {
            val prompt = "Generate 4 important exam questions and concise model answers for subject '$subjectName' " +
                "about: $topic. Write both questions and answers in ${language.displayName} (${language.nativeLabel})."
            val response = withTimeout(60_000L) {
                importantQuestionsModel.generateContent(prompt)
            }
            val jsonText = response.text ?: return Result.failure(IllegalStateException("Model returned empty JSON."))
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("items")
            val list = buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ImportantQuestionEntity(
                            subjectId = subjectId,
                            question = obj.getString("question"),
                            answer = obj.getString("answer"),
                            priority = obj.optString("priority", "HIGH")
                        )
                    )
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Structured questions generation timed out", e)
                Result.failure(IllegalStateException("Timed out after 60 seconds. Please try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate structured important questions", e)
            Result.failure(e)
        }
    }

    suspend fun generateStructuredQuiz(
        topic: String,
        subjectId: Int,
        subjectName: String,
        language: StudyLanguage
    ): Result<List<QuizQuestionEntity>> {
        return try {
            val prompt = "Create 4 multiple-choice quiz questions with 4 distinct options (A-D), correctIndex (0-3), " +
                "and a short explanation for subject '$subjectName' about: $topic. " +
                "Write everything in ${language.displayName} (${language.nativeLabel})."
            val response = withTimeout(60_000L) {
                quizModel.generateContent(prompt)
            }
            val jsonText = response.text ?: return Result.failure(IllegalStateException("Model returned empty JSON."))
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("questions")
            val list = buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        QuizQuestionEntity(
                            subjectId = subjectId,
                            question = obj.getString("question"),
                            optionA = obj.getString("optionA"),
                            optionB = obj.getString("optionB"),
                            optionC = obj.getString("optionC"),
                            optionD = obj.getString("optionD"),
                            correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, 3),
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Quiz generation timed out", e)
                Result.failure(IllegalStateException("Timed out after 60 seconds. Please try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate quiz JSON", e)
            Result.failure(e)
        }
    }

    suspend fun performAiSearch(
        query: String,
        language: StudyLanguage,
        subjectName: String,
        scope: AiSearchScope,
        localMatches: List<LocalVaultMatch>,
        attachedImage: Bitmap? = null
    ): Result<AiSearchResult> {
        return try {
            val vaultContext = if (localMatches.isEmpty()) {
                "No matching items found in the student's saved local vault."
            } else {
                localMatches.joinToString("\n") { m ->
                    "- [${m.kind}] ${m.title}: ${m.snippet}"
                }
            }
            val scopeInstruction = when (scope) {
                AiSearchScope.ALL_KNOWLEDGE ->
                    "Synthesize an accurate academic answer using general academic knowledge AND integrate any relevant context from the student's saved study vault below."
                AiSearchScope.MY_VAULT_ONLY ->
                    "Focus primarily on the student's saved study vault excerpts below. Summarize and clarify what their saved notes/questions say; if the vault lacks an answer, briefly state what is missing and give a concise foundational explanation."
            }
            val promptText = buildString {
                appendLine("You are WOLF STUDY AI Search.")
                appendLine("Response language: ${language.displayName} (${language.nativeLabel}).")
                appendLine("Subject context: $subjectName.")
                appendLine("Search scope: $scopeInstruction")
                appendLine()
                appendLine("Student's Matching Study Vault Items:")
                appendLine(vaultContext)
                appendLine()
                appendLine("Search Query: $query")
            }

            val response = withTimeout(60_000L) {
                val requestContent = content {
                    if (attachedImage != null) {
                        image(attachedImage)
                    }
                    text(promptText)
                }
                searchModel.generateContent(requestContent)
            }
            val jsonText = response.text ?: return Result.failure(IllegalStateException("AI Search returned an empty response."))
            val root = JSONObject(jsonText)
            val keyPointsArr = root.optJSONArray("keyPoints")
            val keyPoints = buildList {
                if (keyPointsArr != null) {
                    for (i in 0 until keyPointsArr.length()) {
                        add(keyPointsArr.getString(i))
                    }
                }
            }
            val relatedArr = root.optJSONArray("relatedQueries")
            val related = buildList {
                if (relatedArr != null) {
                    for (i in 0 until relatedArr.length()) {
                        add(relatedArr.getString(i))
                    }
                }
            }
            Result.success(
                AiSearchResult(
                    query = query,
                    directAnswer = root.optString("directAnswer", ""),
                    keyPoints = keyPoints,
                    examTip = root.optString("examTip", ""),
                    relatedQueries = related
                )
            )
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "AI Search timed out", e)
                Result.failure(IllegalStateException("AI Search timed out after 60 seconds. Please try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI Search failed", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        userPrompt: String,
        mode: AiStudyMode,
        language: StudyLanguage,
        subjectName: String
    ): String {
        val langInstruction = "Response language: ${language.displayName} (${language.nativeLabel})."
        val subjectContext = "Subject context: $subjectName."
        val taskInstruction = when (mode) {
            AiStudyMode.AI_SEARCH -> "Answer this study search query clearly with structured key takeaways and exam relevance."
            AiStudyMode.OPEN_CHAT -> "Answer the student's academic question clearly with key takeaways."
            AiStudyMode.EXPLAIN_TOPIC -> "Explain this topic step-by-step from fundamentals to exam-level clarity with examples."
            AiStudyMode.SUMMARIZE_LESSON -> "Summarize these study notes into structured key points, definitions, and quick revision bullets."
            AiStudyMode.GENERATE_QUESTIONS -> "Generate 5 high-yield exam questions (short and long answer) with brief answer outlines."
            AiStudyMode.ASSIGNMENT_HELP -> "Provide an assignment outline, key arguments, structure, and academic writing tips without fabricating citations."
            AiStudyMode.GENERATE_QUIZ -> "Create 4 practice MCQ questions with answers and brief explanations."
        }
        return "$langInstruction\n$subjectContext\nTask: $taskInstruction\n\nStudent input:\n$userPrompt"
    }
}
