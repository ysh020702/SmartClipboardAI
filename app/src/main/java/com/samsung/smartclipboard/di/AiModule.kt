package com.samsung.smartclipboard.di

import com.samsung.smartclipboard.data.ai.HeuristicAiProposalGenerator
import com.samsung.smartclipboard.domain.ai.AiProposalGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiProposalGenerator(
        impl: HeuristicAiProposalGenerator
    ): AiProposalGenerator
}