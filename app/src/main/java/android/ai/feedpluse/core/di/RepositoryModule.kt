package android.ai.feedpluse.core.di

import android.ai.feedpluse.feature.articles.data.repository.ArticleRepositoryImpl
import android.ai.feedpluse.feature.articles.domain.repository.ArticleRepository
import android.ai.feedpluse.feature.sources.data.repository.SourceRepositoryImpl
import android.ai.feedpluse.feature.sources.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindSourceRepository(
        impl: SourceRepositoryImpl
    ): SourceRepository
}
