package fr.enssat.sharemybook.lewisgillian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.enssat.sharemybook.lewisgillian.data.local.AppDatabase
import fr.enssat.sharemybook.lewisgillian.data.remote.RetrofitClient
import fr.enssat.sharemybook.lewisgillian.ui.screens.BookDetailScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.BorrowsScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.LibraryScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.LoansScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.OnboardingScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.ProfileScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.ScanBorrowScreen
import fr.enssat.sharemybook.lewisgillian.ui.screens.TransactionScreen
import fr.enssat.sharemybook.lewisgillian.ui.theme.ShareMyBookTheme
import fr.enssat.sharemybook.lewisgillian.data.repository.BookRepository
import fr.enssat.sharemybook.lewisgillian.data.repository.TransactionRepository
import fr.enssat.sharemybook.lewisgillian.data.repository.UserRepository
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.BorrowsViewModel
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.BorrowsViewModelFactory
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.LibraryViewModel
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.LibraryViewModelFactory
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.LoansViewModel
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.LoansViewModelFactory
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.ProfileViewModel
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.ProfileViewModelFactory
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.TransactionViewModel
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.TransactionViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShareMyBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShareMyBookApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Bibliothèque", Icons.Default.Home)
    object Loans : Screen("loans", "Prêts", Icons.Default.ShoppingCart)
    object Borrows : Screen("borrows", "Emprunts", Icons.Default.List)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
    
    object BookDetail : Screen("book_detail/{bookUid}", "Détails", Icons.Default.Home) {
        fun createRoute(bookUid: String) = "book_detail/$bookUid"
    }
    object Transaction : Screen("transaction/{bookUid}/{action}", "Transaction", Icons.Default.Home) {
        fun createRoute(bookUid: String, action: String) = "transaction/$bookUid/$action"
    }
    object ScanBorrow : Screen("scan_borrow", "Emprunter", Icons.Default.List)
}

@Composable
fun ShareMyBookApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as android.app.Application

    val database = AppDatabase.getDatabase(context)

    val bookRepository = BookRepository(
        bookDao = database.bookDao(),
        openLibraryApi = RetrofitClient.openLibraryApi
    )

    val userRepository = UserRepository(
        userDao = database.userDao()
    )

    val transactionRepository = TransactionRepository(
        shareMyBookApi = RetrofitClient.shareMyBookApi
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(application, userRepository)
    )
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(application, bookRepository, userRepository)
    )
    val loansViewModel: LoansViewModel = viewModel(
        factory = LoansViewModelFactory(bookRepository, userRepository)
    )
    val borrowsViewModel: BorrowsViewModel = viewModel(
        factory = BorrowsViewModelFactory(bookRepository, userRepository)
    )
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(application, bookRepository, userRepository, transactionRepository)
    )

    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val hasValidProfile = !profileState.isLoading && profileState.user?.isValid() == true
    
    if (profileState.isLoading) {
        fr.enssat.sharemybook.lewisgillian.ui.components.LoadingIndicator()
        return
    }
    
    if (!hasValidProfile) {
        OnboardingScreen(viewModel = profileViewModel)
        return
    }

    val bottomBarScreens = listOf(
        Screen.Library,
        Screen.Loans,
        Screen.Borrows,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.route }
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = {
                                Text(
                                    text = screen.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onBookClick = { bookUid ->
                        navController.navigate(Screen.BookDetail.createRoute(bookUid))
                    }
                )
            }
            
            composable(Screen.Loans.route) {
                LoansScreen(
                    viewModel = loansViewModel,
                    onBookClick = { bookUid ->
                        navController.navigate(Screen.BookDetail.createRoute(bookUid))
                    }
                )
            }
            
            composable(Screen.Borrows.route) {
                BorrowsScreen(
                    viewModel = borrowsViewModel,
                    onBookClick = { bookUid ->
                        navController.navigate(Screen.BookDetail.createRoute(bookUid))
                    },
                    onScanBorrow = {
                        navController.navigate(Screen.ScanBorrow.route)
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }
            
            composable(
                route = "book_detail/{bookUid}",
                arguments = listOf(navArgument("bookUid") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookUid = backStackEntry.arguments?.getString("bookUid") ?: return@composable
                BookDetailScreen(
                    bookUid = bookUid,
                    bookRepository = bookRepository,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onStartLoan = { uid ->
                        navController.navigate(Screen.Transaction.createRoute(uid, "loan"))
                    },
                    onStartReturn = { uid ->
                        navController.navigate(Screen.Transaction.createRoute(uid, "return"))
                    },
                    onScanReturn = {
                        navController.navigate(Screen.ScanBorrow.route)
                    }
                )
            }
            
            composable(
                route = "transaction/{bookUid}/{action}",
                arguments = listOf(
                    navArgument("bookUid") { type = NavType.StringType },
                    navArgument("action") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookUid = backStackEntry.arguments?.getString("bookUid") ?: return@composable
                val action = backStackEntry.arguments?.getString("action") ?: return@composable
                
                TransactionScreen(
                    viewModel = transactionViewModel,
                    bookUid = bookUid,
                    action = action,
                    onTransactionComplete = { 
                        navController.popBackStack(Screen.Library.route, false)
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ScanBorrow.route) {
                ScanBorrowScreen(
                    viewModel = transactionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onTransactionComplete = {
                        navController.popBackStack(Screen.Borrows.route, false)
                    }
                )
            }
        }
    }
}
