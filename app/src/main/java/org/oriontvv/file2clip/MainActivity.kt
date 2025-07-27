package org.oriontvv.file2clip

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSelect = findViewById<Button>(R.id.btnSelectFile)
        btnSelect.setOnClickListener { openFilePicker() }

        // TV-специфичные настройки
        btnSelect.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                openFilePicker()
                true
            } else {
                false
            }
        }

        btnSelect.requestFocus()
    }

    private fun openFilePicker() {

        try {
            // Используем простейший вариант выбора файлов
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"  // Разрешаем все типы файлов
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

//        try {
//            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "text/*"
//                addCategory(Intent.CATEGORY_OPENABLE)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(this, "No file picker available", Toast.LENGTH_LONG).show()
//        }
        showToast("open picker")
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        startActivityForResult(intent, REQUEST_CODE_PICK_FILE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    val text = readFileContent(uri)
                    if (text.isNotEmpty()) {
                        copyToClipboard(text)
                        Toast.makeText(this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "File is empty", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun readFileContent(uri: Uri): String {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun copyToClipboard(text: String) {
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
            setPrimaryClip(ClipData.newPlainText("file_content", text))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 42
    }
}