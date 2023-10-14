package com.ilyabogdanovich.jelly.ide.app.documents

/**
 * Persistent state of the document, opened in the editor.
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
@JvmInline
value class Document(val text: String) {
    companion object {
        fun empty() = Document("")
    }
}
