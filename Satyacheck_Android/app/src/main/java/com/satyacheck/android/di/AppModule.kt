package com.satyacheck.android.di

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.satyacheck.android.BuildConfig
import com.satyacheck.android.data.remote.api.EducationalService
import com.satyacheck.android.data.remote.api.SatyaCheckApiClient
import com.satyacheck.android.data.remote.api.TokenManager
import com.satyacheck.android.data.remote.auth.SharedPreferencesTokenManager
import com.satyacheck.android.data.repository.AnalysisRepositoryImpl
import com.satyacheck.android.data.repository.ArticleRepository
import com.satyacheck.android.data.repository.ArticleRepositoryImpl
import com.satyacheck.android.domain.analyzer.GeminiTextAnalyzer
import com.satyacheck.android.domain.analyzer.ImageAnalyzer
import com.satyacheck.android.domain.analyzer.OptimizedGeminiTextAnalyzer
import com.satyacheck.android.domain.analyzer.SpeechToTextService
import com.satyacheck.android.domain.analyzer.VisionTextExtractor
import com.satyacheck.android.domain.repository.AnalysisRepository
import com.satyacheck.android.domain.repository.IArticleRepository
import com.satyacheck.android.domain.service.GeminiModelDiscoveryService
import com.satyacheck.android.utils.AnalysisCache
import com.satyacheck.android.utils.TextAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGeminiModelDiscoveryService(): GeminiModelDiscoveryService {
        return GeminiModelDiscoveryService()
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(
        modelDiscoveryService: GeminiModelDiscoveryService
    ): GenerativeModel {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Log if API key is missing or empty (don't log the actual key for security)
        if (apiKey.isBlank()) {
            Log.e("AppModule", "GEMINI_API_KEY is missing or empty in gradle.properties")
        } else {
            Log.d("AppModule", "GEMINI_API_KEY is configured (key length: ${apiKey.length})")
        }
        
        // Enhanced logging and automatic model discovery
        try {
            // Discover the best available model
            val modelName = runBlocking {
                try {
                    modelDiscoveryService.discoverBestModel(apiKey)
                } catch (e: Exception) {
                    Log.w("AppModule", "Model discovery failed, using fallback: ${e.message}")
                    "gemini-2.5-flash" // Fallback if discovery fails
                }
            }
            
            Log.d("AppModule", "Creating GenerativeModel with discovered model: $modelName")
            
            // Use the discovered model name
            return GenerativeModel(
                modelName = modelName,
                apiKey = apiKey
            ).also {
                Log.d("AppModule", "GenerativeModel created successfully")
            }
        } catch (e: Exception) {
            Log.e("AppModule", "Error creating GenerativeModel: ${e.message}", e)
            throw e
        }
    }
    
    @Provides
    @Singleton
    fun provideAnalysisCache(@ApplicationContext context: Context): AnalysisCache {
        return AnalysisCache(context)
    }
    
    @Provides
    @Singleton
    fun provideTextAnalyzer(
        generativeModel: GenerativeModel,
        analysisCache: AnalysisCache
    ): TextAnalyzer {
        return OptimizedGeminiTextAnalyzer(generativeModel, analysisCache)
    }
    
    @Provides
    @Singleton
    fun provideLocalArticleRepository(): ArticleRepository {
        return ArticleRepository()
    }
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return SharedPreferencesTokenManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSatyaCheckApiClient(tokenManager: TokenManager): SatyaCheckApiClient {
        return SatyaCheckApiClient(tokenManager)
    }
    
    @Provides
    @Singleton
    fun provideArticleRepository(
        apiClient: SatyaCheckApiClient,
        localArticleRepository: ArticleRepository
    ): IArticleRepository {
        return ArticleRepositoryImpl(apiClient, localArticleRepository)
    }
    
    @Provides
    @Singleton
    fun provideVisionTextExtractor(): VisionTextExtractor {
        return VisionTextExtractor()
    }
    
    @Provides
    @Singleton
    fun provideImageAnalyzer(
        visionTextExtractor: VisionTextExtractor,
        optimizedGeminiTextAnalyzer: OptimizedGeminiTextAnalyzer
    ): ImageAnalyzer {
        return ImageAnalyzer(visionTextExtractor, optimizedGeminiTextAnalyzer)
    }
    
    @Provides
    @Singleton
    fun provideSpeechToTextService(
        @ApplicationContext context: Context
    ): SpeechToTextService {
        return SpeechToTextService(context)
    }
    
    @Provides
    @Singleton
    fun provideSmartAnalysisCache(
        @ApplicationContext context: Context
    ): com.satyacheck.android.domain.cache.AnalysisCache {
        return com.satyacheck.android.domain.cache.AnalysisCache(context)
    }
    
    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context
    ): com.satyacheck.android.domain.network.NetworkManager {
        return com.satyacheck.android.domain.network.NetworkManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSmartAnalysisCoordinator(
        textAnalyzer: com.satyacheck.android.utils.TextAnalyzer,
        imageAnalyzer: ImageAnalyzer,
        speechToTextService: SpeechToTextService,
        smartAnalysisCache: com.satyacheck.android.domain.cache.AnalysisCache,
        networkManager: com.satyacheck.android.domain.network.NetworkManager
    ): com.satyacheck.android.domain.analyzer.SmartAnalysisCoordinator {
        return com.satyacheck.android.domain.analyzer.SmartAnalysisCoordinator(
            textAnalyzer, imageAnalyzer, speechToTextService, smartAnalysisCache, networkManager
        )
    }
    
    @Provides
    @Singleton
    fun provideAnalysisRepository(
        textAnalyzer: TextAnalyzer, 
        apiClient: SatyaCheckApiClient
    ): AnalysisRepository {
        return AnalysisRepositoryImpl(textAnalyzer, apiClient)
    }
}
