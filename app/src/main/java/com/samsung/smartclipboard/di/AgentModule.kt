package com.samsung.smartclipboard.di

import android.content.Context
import com.samsung.smartclipboard.data.agent.FallbackActionPlanner
import com.samsung.smartclipboard.data.agent.FallbackClusterTopicAgent
import com.samsung.smartclipboard.data.agent.FallbackItemRecommendationAgent
import com.samsung.smartclipboard.data.agent.FallbackPurposeAnalyzer
import com.samsung.smartclipboard.data.agent.FallbackTopicPlanner
import com.samsung.smartclipboard.data.gemini.GeminiActionPlanner
import com.samsung.smartclipboard.data.gemini.GeminiClusterTopicAgent
import com.samsung.smartclipboard.data.gemini.GeminiClusterer
import com.samsung.smartclipboard.data.gemini.GeminiItemRecommendationAgent
import com.samsung.smartclipboard.data.gemini.GeminiPurposeAnalyzer
import com.samsung.smartclipboard.data.gemini.GeminiTopicPlanner
import com.samsung.smartclipboard.data.retrieval.LocalCandidateItemRanker
import com.samsung.smartclipboard.data.retrieval.LocalClusterer
import com.samsung.smartclipboard.data.retrieval.LocalDataRetriever
import com.samsung.smartclipboard.data.tool.ToolExecutorImpl
import com.samsung.smartclipboard.data.tool.ToolRegistryImpl
import com.samsung.smartclipboard.data.tool.ToolRouterImpl
import com.samsung.smartclipboard.domain.agent.ActionPlanner
import com.samsung.smartclipboard.domain.agent.ClusterTopicAgent
import com.samsung.smartclipboard.domain.agent.ItemRecommendationAgent
import com.samsung.smartclipboard.domain.agent.TopicPlanner
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.ai.PurposeAnalyzer
import com.samsung.smartclipboard.domain.repository.DataRepository
import com.samsung.smartclipboard.domain.retrieval.CandidateItemRanker
import com.samsung.smartclipboard.domain.retrieval.DataClusterer
import com.samsung.smartclipboard.domain.retrieval.DataRetriever
import com.samsung.smartclipboard.domain.tool.ToolExecutor
import com.samsung.smartclipboard.domain.tool.ToolRegistry
import com.samsung.smartclipboard.domain.tool.ToolRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides @Singleton
    fun provideTopicPlanner(geminiManager: GeminiManager): TopicPlanner {
        return GeminiTopicPlanner(geminiManager, FallbackTopicPlanner())
    }

    @Provides @Singleton
    fun provideDataRetriever(dataRepository: DataRepository): DataRetriever {
        return LocalDataRetriever(dataRepository)
    }

    @Provides @Singleton
    fun provideCandidateItemRanker(): CandidateItemRanker {
        return LocalCandidateItemRanker()
    }

    @Provides @Singleton
    fun provideItemRecommendationAgent(geminiManager: GeminiManager): ItemRecommendationAgent {
        return GeminiItemRecommendationAgent(geminiManager, FallbackItemRecommendationAgent())
    }

    @Provides @Singleton
    fun provideActionPlanner(geminiManager: GeminiManager): ActionPlanner {
        return GeminiActionPlanner(geminiManager, FallbackActionPlanner())
    }

    @Provides @Singleton
    fun provideToolRegistry(): ToolRegistry {
        return ToolRegistryImpl()
    }

    @Provides @Singleton
    fun provideToolRouter(toolRegistry: ToolRegistry): ToolRouter {
        return ToolRouterImpl(toolRegistry)
    }

    @Provides @Singleton
    fun provideToolExecutor(@ApplicationContext context: Context): ToolExecutor {
        return ToolExecutorImpl(context)
    }

    @Provides @Singleton
    fun provideLocalClusterer(): LocalClusterer {
        return LocalClusterer()
    }

    @Provides @Singleton
    fun provideDataClusterer(geminiManager: GeminiManager, localClusterer: LocalClusterer): DataClusterer {
        return GeminiClusterer(geminiManager, localClusterer)
    }

    @Provides @Singleton
    fun provideClusterTopicAgent(geminiManager: GeminiManager): ClusterTopicAgent {
        return GeminiClusterTopicAgent(geminiManager, FallbackClusterTopicAgent())
    }

    @Provides @Singleton
    fun provideRefineAgent(geminiManager: GeminiManager): com.samsung.smartclipboard.domain.agent.RefineAgent {
        return com.samsung.smartclipboard.data.gemini.GeminiRefineAgent(geminiManager)
    }

    @Provides @Singleton
    fun providePurposeAnalyzer(geminiManager: GeminiManager): PurposeAnalyzer {
        return GeminiPurposeAnalyzer(geminiManager, FallbackPurposeAnalyzer())
    }
}
