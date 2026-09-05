package com.example.kasa.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.kasa.ui.screens.welcome.WelcomeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      WelcomeScreen(
        onGetStarted = {},
        onJoinHousehold = {}
      )
    }
  }

  @Test
  fun welcomeScreen_elements_exist() {
    composeTestRule.onNodeWithText("Unknown").assertExists()
    composeTestRule.onNodeWithText("Créer un foyer").assertExists()
  }
}
