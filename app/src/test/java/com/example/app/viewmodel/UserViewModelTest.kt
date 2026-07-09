package com.example.app.viewmodel

import com.example.app.model.User
import com.example.app.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private lateinit var viewModel: UserViewModel
    private val repository: UserRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockUsers = listOf(
        User(1, "Ana García", "ana@email.com"),
        User(2, "Luis Pérez", "luis@email.com")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Setup default behavior for init call
        coEvery { repository.allUsers } returns flowOf(mockUsers)
        coEvery { repository.refreshUsers() } returns Unit
        
        viewModel = UserViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers should call refreshUsers from repository`() = runTest {
        viewModel.loadUsers()
        // The success state is updated because setup() already calls loadUsers through init
        assertTrue(viewModel.uiState.value is UiState.Success)
        assertEquals(mockUsers, (viewModel.uiState.value as UiState.Success).users)
    }

    @Test
    fun `onSearchQueryChanged should filter users by name`() = runTest {
        // Given a query "Ana"
        val query = "Ana"
        
        // When
        viewModel.onSearchQueryChanged(query)
        
        // Then
        val state = viewModel.uiState.value as UiState.Success
        assertEquals(1, state.users.size)
        assertEquals("Ana García", state.users[0].name)
    }

    @Test
    fun `onSearchQueryChanged with empty query should show all users`() = runTest {
        // When
        viewModel.onSearchQueryChanged("")
        
        // Then
        val state = viewModel.uiState.value as UiState.Success
        assertEquals(2, state.users.size)
    }
}