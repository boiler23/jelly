package com.ilyabogdanovich.jelly.ide.app.data.documents

import okio.Path.Companion.toPath

/**
 * Documents-related helpers.
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
// this is var - to be able to override it on Android
var INTERNAL_DIR = System.getProperty("user.home").toPath() / ".jelly"
