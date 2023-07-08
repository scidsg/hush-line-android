package org.scidsg.hushline.android

import android.Manifest
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var notificationManager: NotificationManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //val navController = findNavController(R.id.nav_host_fragment)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController)//, appBarConfiguration)

        navController.enableOnBackPressed(true)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // Handle the destination changed event here
            val destinationLabel = resources.getResourceEntryName(destination.id)
            when {
                destinationLabel.equals("HomeFragment", false) -> {
                    setToolbarTitle(Gravity.CENTER, getString(R.string.home_fragment_label))
                }
                destinationLabel.equals("MessageListFragment", false) -> {
                    setToolbarTitle(Gravity.START, getString(R.string.messages_list_fragment_label))
                    menu.findItem(R.id.action_settings).isVisible = false
                }
                destinationLabel.equals("MessageFragment", false) -> {
                    val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arguments?.getParcelable(MessageListFragment.MESSAGE, MessageEntity::class.java)
                                as MessageEntity
                    } else {
                        arguments?.getParcelable(MessageListFragment.MESSAGE) as MessageEntity?
                    }
                    setToolbarTitle(Gravity.START,
                        message?.timestamp ?: "")
                    menu.findItem(R.id.action_settings).isVisible = false
                }
                destinationLabel.equals("SettingsFragment", false) -> {
                    setToolbarTitle(Gravity.START, getString(R.string.settings_fragment_label))
                    menu.findItem(R.id.action_settings).isVisible = false
                }
                destinationLabel.equals("NotificationsFragment", false) -> {
                    setToolbarTitle(Gravity.START, getString(R.string.notification_fragment_label))
                    menu.findItem(R.id.action_settings).isVisible = false
                }
                destinationLabel.equals("EncryptionFragment", false) -> {
                    setToolbarTitle(Gravity.START, getString(R.string.encryption_fragment_label))
                    menu.findItem(R.id.action_settings).isVisible = false
                }
                destinationLabel.equals("AboutFragment", false) -> {
                    setToolbarTitle(Gravity.START, getString(R.string.about_fragment_label))
                    menu.findItem(R.id.action_settings).isVisible = false
                }
            }
        }

        if (!isNotificationPermissionGranted())
            requestNotificationPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu

        //hide options menu where it's not needed
        val menuItem = menu.findItem(R.id.action_settings)
        // Determine the current fragment being displayed
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        menuItem.isVisible = currentFragment is HomeFragment

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                navController.navigate(R.id.action_HomeFragment_to_SettingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setToolbarTitle(gravity: Int, text: String, font: Int = 0) {
        binding.toolbarTitle.text = text
        binding.toolbarTitle.gravity = gravity

        if (font != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.toolbarTitle.typeface = resources.getFont(font)
            } else {
                val typeFace = Typeface.createFromAsset(
                    assets, "fonts/roboto_regular.ttf")
                binding.toolbarTitle.typeface = typeFace
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.toolbarTitle.typeface = resources.getFont(R.font.roboto_regular)
            } else {
                val typeFace = Typeface.createFromAsset(
                    assets, "fonts/roboto_regular.ttf")
                binding.toolbarTitle.typeface = typeFace
            }
        }
    }

    private fun isNotificationPermissionGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else true

    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                return
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                //In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using the app without granting the permission.
                //TODO show dialog to explain why this permission is needed.
                NotificationPermissionRationaleDialog.newInstance (object : DialogClickListener {
                    override fun onPositiveButtonClick(dialog: Dialog) {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    override fun onNegativeButtonClick(dialog: Dialog) {
                        dialog.dismiss()
                    }
                }).show(supportFragmentManager, "permission_rationale")
            }
            else -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                //ignore
            }
        }
}