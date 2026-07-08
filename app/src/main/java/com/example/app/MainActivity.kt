package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.app.ui.theme.ProfileBackground
import com.example.app.ui.theme.ProfilePrimary
import com.example.app.ui.theme.ProfileSecondary
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.data.AppDatabase
import com.example.app.model.User
import com.example.app.repository.UserRepository
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UiState
import com.example.app.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val database = AppDatabase.getDatabase(context)
                    val repository = UserRepository(database.userDao())
                    
                    val viewModel: UserViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return UserViewModel(repository) as T
                            }
                        }
                    )
                    
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = "userList") {
                        composable("userList") {
                            UserListScreen(
                                viewModel = viewModel,
                                onUserClick = { userId ->
                                    navController.navigate("userDetail/$userId")
                                }
                            )
                        }
                        composable(
                            route = "userDetail/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            UserDetailScreen(
                                userId = userId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    onUserClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            label = { Text("Buscar usuario por nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Success -> {
                    val users = (uiState as UiState.Success).users
                    LazyColumn {
                        items(users) { user ->
                            UserItem(user = user, onClick = { onUserClick(user.id) })
                        }
                    }
                }

                is UiState.Error -> {
                    val message = (uiState as UiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: $message",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadUsers() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.selectedUser.collectAsState()

    LaunchedEffect(userId) {
        viewModel.selectUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfilePrimary
                )
            )
        },
        containerColor = ProfileBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            user?.let {
                // Imagen de usuario (Icono grande)
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Usuario desconocido",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    tint = ProfileSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = ProfilePrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(
                            text = "ID del sistema: ${it.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfileSecondary
                        )
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ProfilePrimary)
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = user.name, style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}
