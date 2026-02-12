package com.kaankivancdilli.summary.ui.screens.main.settings

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.viewmodel.main.settings.SettingsScreenViewModel
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.utils.admob.adhandler.RewardedAdHandler
import com.kaankivancdilli.summary.utils.rate.requestInAppReview
import com.kaankivancdilli.summary.utils.reusable.design.CustomTopBar
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
) {

    fun Int.toLocalizedDigits(): String {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        return numberFormat.format(this)
    }

    val usageCount by viewModel.usageCount.collectAsState()
    val totalUsageCount by viewModel.totalUsageCount.collectAsState()

    val numberLabel = 20

    val darkMode by viewModel.darkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val isSubscribed by subscriptionViewModel.isSubscribed.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val context = LocalContext.current
    val activity = context as? Activity

    val subscribeLabel = stringResource(R.string.subscribe)
    val subscribedLabel = stringResource(R.string.subscribed)
    val settingsLabel = stringResource(R.string.settings)
    val darkModeLabel = stringResource(R.string.dark_mode)
    val onLabel = stringResource(R.string.on)
    val offLabel = stringResource(R.string.off)
    val checkSubscriptionLabel = stringResource(R.string.check_subscription)
    val refreshLabel = stringResource(R.string.refresh)
    val manageSubscriptionLabel = stringResource(R.string.manage_subscription)
    val aboutLabel = stringResource(R.string.about)
    val infoLabel = stringResource(R.string.info)
    val freeLabel = stringResource(R.string.free)
    val tenLabel = stringResource(R.string.ten)
    val summariesLabel = stringResource(R.string.summaries)
    val totalLabel = stringResource(R.string.total)
    val resetLabel = stringResource(R.string.reset)
    val rateLabel = stringResource(R.string.rate)

    var showRewardedAd by remember { mutableStateOf(false) }

    if (showRewardedAd && activity != null) {
        RewardedAdHandler(
            showAd = true,
            activity = activity,
            onUserEarnedReward = {
                // ✅ Reset usage count after user earns reward
                viewModel.resetFreeUsageCount()
            },
            onAdHandled = {
                // ✅ Always reset ad trigger
                showRewardedAd = false
            }
        )
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CustomTopBar(title = settingsLabel)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // between items
            ) {
                //  SettingsItem(title = darkModeLabel) {
                //        BigBlackButton(
                //             text = if (darkMode) onLabel else offLabel,
                //             modifier = Modifier.weight(1f),  // <-- each button gets equal weight
                //            onClick = { viewModel.toggleDarkMode() }
                //         )
                //     }
                if (!isSubscribed) {

                SettingsItem(
                    title = freeLabel + summariesLabel + ": " + usageCount.toLocalizedDigits() + "/" + numberLabel.toLocalizedDigits(),
                    titleFontSize = 20.sp
                ) {
                    BigBlackButton(
                        text = resetLabel,
                        height = 50.dp,
                        onClick = {
                            showRewardedAd = true
                        }
                    )
                }
            }
                SettingsItem(title = totalLabel + summariesLabel + ": " + totalUsageCount.toLocalizedDigits(),
                    titleFontSize = 20.sp) {

                }

                    BigBlackButton(
                        text = checkSubscriptionLabel,
                        height = 50.dp,
                        onClick = { subscriptionViewModel.checkSubscription() }
                    )


                    BigBlackButton(
                        text = if (isSubscribed) subscribedLabel else subscribeLabel,
                        height = 50.dp,
                        onClick = {
                            activity?.let {
                                viewModel.subscribe(
                                    it,
                                    subscriptionViewModel
                                )
                            }
                        }
                    )


                    BigBlackButton(
                        text = rateLabel,
                        height = 50.dp,
                        onClick = {
                            activity?.let {
                                requestInAppReview(it)
                            }
                        }
                    )


            //    SettingsItem(title = aboutLabel) {
               //     BigBlackButton(
               //         text = infoLabel,
               //         modifier = Modifier.weight(1f),
               //         onClick = { /* Open About Screen */ }
              //      )
            //    }
            }

        }
    }


@Composable
fun SettingsItem(
    title: String? = null,              // optional
    titleFontSize: TextUnit = 20.sp,    // default size
    buttonContent: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false
                )
                .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Only show Text if title is not null
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = titleFontSize)
                )
            }

            buttonContent()
        }
    }
}




@Composable
private fun BigBlackButton(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp, // fixed height
    horizontalPadding: Dp = 0.dp, // padding from left/right
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()               // stretch horizontally
            .height(height)               // fixed height
            .padding(horizontal = horizontalPadding, vertical = 2.dp), // gaps on left/right
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray,
            disabledContainerColor = Color(0xFFFAFAFA),
            disabledContentColor = Color.LightGray
        ),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(1.dp),
        border = BorderStroke(0.05.dp, Color.LightGray)
    ) {
        Text(
            text = text,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}


