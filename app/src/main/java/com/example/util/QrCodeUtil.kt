package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.data.model.StudentSubmission
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject

object QrCodeUtil {

    /**
     * Generates a square QR Code Bitmap from the given string content.
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Decodes a QR code string from a Bitmap.
     */
    fun decodeQrFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    // --- JSON Serialization for Teacher Puzzle ---
    fun serializePuzzle(puzzle: PuzzlePackage): String {
        val root = JSONObject()
        root.put("type", "EDUTK_PUZZLE")
        root.put("id", puzzle.id)
        root.put("title", puzzle.title)
        root.put("desc", puzzle.description)
        root.put("author", puzzle.author)

        val questionsArray = JSONArray()
        for (q in puzzle.questions) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("question", q.question)
            qObj.put("emoji", q.animalEmoji)
            val optArr = JSONArray()
            q.options.forEach { optArr.put(it) }
            qObj.put("options", optArr)
            qObj.put("ans", q.correctIndex)
            qObj.put("expl", q.explanation)
            qObj.put("sound", q.soundText)
            questionsArray.put(qObj)
        }
        root.put("questions", questionsArray)
        return root.toString()
    }

    fun deserializePuzzle(jsonString: String): PuzzlePackage? {
        return try {
            val root = JSONObject(jsonString)
            val id = root.optString("id", "puz_${System.currentTimeMillis()}")
            val title = root.optString("title", "Teka-Teki TK")
            val desc = root.optString("desc", "Teka-teki logika hewan")
            val author = root.optString("author", "Guru")

            val questionsList = mutableListOf<AnimalQuestion>()
            val questionsArray = root.optJSONArray("questions") ?: JSONArray()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val qId = qObj.optString("id", "q_$i")
                val question = qObj.optString("question", "")
                val emoji = qObj.optString("emoji", "🐾")
                val ans = qObj.optInt("ans", 0)
                val expl = qObj.optString("expl", "")
                val sound = qObj.optString("sound", "")

                val optArr = qObj.optJSONArray("options") ?: JSONArray()
                val options = mutableListOf<String>()
                for (j in 0 until optArr.length()) {
                    options.add(optArr.getString(j))
                }
                questionsList.add(
                    AnimalQuestion(
                        id = qId,
                        question = question,
                        animalEmoji = emoji,
                        options = options,
                        correctIndex = ans,
                        explanation = expl,
                        soundText = sound
                    )
                )
            }
            PuzzlePackage(
                id = id,
                title = title,
                description = desc,
                questions = questionsList,
                author = author
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- JSON Serialization for Student Result ---
    fun serializeStudentResult(result: StudentSubmission): String {
        val root = JSONObject()
        root.put("type", "EDUTK_RESULT")
        root.put("student", result.studentName)
        root.put("puzzle", result.puzzleTitle)
        root.put("score", result.score)
        root.put("total", result.totalQuestions)
        root.put("stars", result.stars)
        root.put("ts", result.timestamp)
        root.put("notes", result.notes)
        return root.toString()
    }

    fun deserializeStudentResult(jsonString: String): StudentSubmission? {
        return try {
            val root = JSONObject(jsonString)
            if (root.optString("type") != "EDUTK_RESULT" && !root.has("student")) {
                return null
            }
            StudentSubmission(
                studentName = root.optString("student", "Siswa TK"),
                puzzleTitle = root.optString("puzzle", "Teka-Teki Hewan"),
                score = root.optInt("score", 0),
                totalQuestions = root.optInt("total", 0),
                stars = root.optInt("stars", 3),
                timestamp = root.optLong("ts", System.currentTimeMillis()),
                notes = root.optString("notes", "")
            )
        } catch (e: Exception) {
            null
        }
    }
}
