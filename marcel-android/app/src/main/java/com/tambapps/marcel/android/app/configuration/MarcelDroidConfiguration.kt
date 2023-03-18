package com.tambapps.marcel.android.app.configuration

import android.content.Context
import android.content.SharedPreferences
import com.tambapps.marcel.android.app.marcel.compiler.AndroidMarcelCompiler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import marcel.lang.android.dex.MarcelDexClassLoader
import java.io.File

@Module
@InstallIn(ActivityComponent::class, FragmentComponent::class)
class MarcelDroidConfiguration {

    @Provides
    fun classesDir(@ApplicationContext context: Context): File {
        return context.getDir("classes", Context.MODE_PRIVATE)
    }

    @Provides
    fun marcelDexClassLoader(): MarcelDexClassLoader {
        return MarcelDexClassLoader()
    }

    @Provides
    fun marcelCompiler(dexClassLoader: MarcelDexClassLoader): AndroidMarcelCompiler {
        return AndroidMarcelCompiler(dexClassLoader)
    }
}