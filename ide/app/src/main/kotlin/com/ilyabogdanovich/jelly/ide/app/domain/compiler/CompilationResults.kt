package com.ilyabogdanovich.jelly.ide.app.domain.compiler

import kotlin.time.Duration

/**
 * Results of the compilation, as received from [CompilationServiceClient].
 * @property out Standard output of the compilation.
 * @property err Error output of the compilation. Most likely will be changed soon to some structure with markups.
 * @property duration time taken to process the given input.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
data class CompilationResults(val out: String, val err: String, val duration: Duration)
