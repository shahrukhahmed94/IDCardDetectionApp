package com.shahrukh.idcarddetectionapp.di

import android.app.Application
import android.content.Context
import com.shahrukh.idcarddetectionapp.data.manager.ObjectDetectionManagerImpl
import com.shahrukh.idcarddetectionapp.domain.manager.ObjectDetectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [ApplicationContextModule::class])
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideObjectDetectionManager(
        @ApplicationContext context: Context
    ): ObjectDetectionManager = ObjectDetectionManagerImpl(
        context
    )





}