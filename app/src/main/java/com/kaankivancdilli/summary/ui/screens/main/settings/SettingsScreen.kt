package com.kaankivancdilli.summary.ui.screens.main.settings

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.viewmodel.main.settings.SettingsScreenViewModel
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.ui.component.admob.adhandler.RewardedAdHandler
import com.kaankivancdilli.summary.core.review.requestInAppReview
import com.kaankivancdilli.summary.ui.component.reusable.buttons.BigBlackButton
import com.kaankivancdilli.summary.ui.component.reusable.bar.CustomTopBar
import com.kaankivancdilli.summary.ui.screens.main.settings.sub.SettingsItem
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
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
    val isSubscribed by subscriptionViewModel.isSubscribed.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val subscribeLabel = stringResource(R.string.subscribe)
    val subscribedLabel = stringResource(R.string.subscribed)
    val settingsLabel = stringResource(R.string.settings)
    val checkSubscriptionLabel = stringResource(R.string.check_subscription)
    val freeLabel = stringResource(R.string.free)
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
                viewModel.resetFreeUsageCount()
            },
            onAdHandled = {
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
            }
        }
    }