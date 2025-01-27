package ui

import data.source.MjImagesDataSource
import di.initKoin
import domain.model.State
import fakes.EmptyMjImagesDataSource
import fakes.ErrorMjImagesDataSource
import fakes.MjImagesLocalFakeDataSource
import fakes.SuccessMjImagesDataSource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import util.getDispatcherProvider

@OptIn(ExperimentalCoroutinesApi::class)
class MjImagesViewModelTest : KoinTest {

    @BeforeTest
    fun setUp() {
        initKoin {
            modules(module {
                factory { MjImagesViewModel(get(), get()) }
                factory<MjImagesDataSource.Local> { MjImagesLocalFakeDataSource() }
            })
        }
        Dispatchers.setMain(getDispatcherProvider().unconfined)
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `when fetch images gets success then state should be updated`() = runTest {
        // given
        setupDataSource(SuccessMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()

        // then
        viewModel.state.onEach {
            assertEquals(State.CONTENT, it)
        }
    }

    @Test
    fun `when fetch images gets success then images should be updated`() = runTest {
        // given
        setupDataSource(SuccessMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()

        // then
        viewModel.images.onEach {
            assertEquals(1, it.images.size)
        }
    }

    @Test
    fun `when refresh images called images should be empty`() = runTest {
        // given
        setupDataSource(SuccessMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()
        viewModel.refreshImages()

        // then
        viewModel.images.onEach {
            assertEquals(0, it.images.size)
        }
    }

    @Test
    fun `when load more called with page=maxPage then nothing happens`() = runTest {
        // given
        setupDataSource(SuccessMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()
        viewModel.loadMore()

        // then
        viewModel.images.onEach {
            assertEquals(it.currentPage, 1)
        }
    }

    @Test
    fun `when fetch images gets error then state should be updated`() = runTest {
        // given
        setupDataSource(ErrorMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()

        // then
        viewModel.state.onEach {
            assertEquals(State.ERROR, it)
        }
    }

    @Test
    fun `when fetch images gets empty result then images should be updated`() = runTest {
        // given
        setupDataSource(EmptyMjImagesDataSource())

        // when
        val viewModel = get<MjImagesViewModel>()

        // then
        viewModel.images.onEach {
            assertEquals(0, it.images.size)
        }
    }

    private fun setupDataSource(
        dataSource: MjImagesDataSource.Remote
    ) {
        loadKoinModules(module {
            factory { dataSource }
        })
    }
}
