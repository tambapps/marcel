package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.io.IOException

@HiltWorker
class MarcelShellWorker
  @AssistedInject constructor(@Assisted appContext: Context,
                              @Assisted workerParams: WorkerParameters,
                              // this is not a val because hilt doesn't allow final fields when injecting
                              _compilerConfiguration: CompilerConfiguration,):
  Worker(appContext, workerParams) {

  private val compilerConfiguration = _compilerConfiguration
  override fun doWork(): Result {
    val startTime = System.currentTimeMillis()
    val binding = Binding() // TODO set a variable 'out' to allow printing to a file without any conflicts
    val classLoader = MarcelDexClassLoader()
    val typeResolver = ReplJavaTypeResolver(classLoader, binding)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, typeResolver)
    val directory = File(applicationContext.getDir("shell_works", Context.MODE_PRIVATE), "work $id")

    val dataBuilder = Data.Builder()
      .putLong(MarcelShellWorkInfo.START_TIME_KEY, startTime)

    val scriptFilePath = WorkTags.getScriptPath(tags) ?: return Result.failure(build(dataBuilder))
    val text = try {
      File(scriptFilePath).readText()
    } catch (e: IOException) {
      return Result.failure(build(dataBuilder))
    }

    if (!directory.mkdir()) {
      return Result.failure(build(dataBuilder))
    }
    val evaluator = MarcelEvaluator(Binding(), replCompiler, classLoader, DexJarWriterFactory(), directory)

    val result = evaluator.eval(text)
    if (result != null) {
      dataBuilder.putString(MarcelShellWorkInfo.RESULT_KEY, result.toString())
    }

    directory.deleteRecursively()
    return Result.success(build(dataBuilder))
  }

  private fun build(builder: Data.Builder): Data {
    return builder
      .putLong(MarcelShellWorkInfo.END_TIME_KEY, System.currentTimeMillis())
      .build()
  }
}