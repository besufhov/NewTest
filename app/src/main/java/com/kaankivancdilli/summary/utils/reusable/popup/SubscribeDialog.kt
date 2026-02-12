package com.kaankivancdilli.summary.utils.reusable.popup

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.utils.admob.adhandler.RewardedAdHandler

@Composable
fun SubscribeDialog(
    onDismiss: () -> Unit,
    onSubscribeClick: () -> Unit,
    onWatchAdsClick: () -> Unit,
    activity: Activity
) {
    var visible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var showAd by remember { mutableStateOf(false) } // ✅ Ad trigger

    val subscriptionRequiredLabel = stringResource(R.string.subscription_required)
    val firstWeekisFreeLabel = stringResource(R.string.first_week)
    val cancelAnytimeLabel = stringResource(R.string.cancel_anytime)
    val noAdsLabel = stringResource(R.string.no_ads)
    val withoutSubscriptionLabel = stringResource(R.string.without_subs)

    val watchAdsOrSubscribeLabel = stringResource(R.string.watch_ads_or_subscribe)
    val watchAdsForLabel = stringResource(R.string.watch_ads_for)
    val youCanResetEverytimeLabel = stringResource(R.string.you_can_reset_everytime)
    val subscribeForNoAdsLabel = stringResource(R.string.subsribe_for_no_ads)


    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        finishedListener = { if (!visible) onDismiss() }
    )

    // Listen for purchase acknowledged externally
    LaunchedEffect(Unit) {
        BillingManagerHolder.setOnAcknowledged {
            isSubscribed = true
            isLoading = false
        }
        BillingManagerHolder.setOnPurchaseCancelled {
            isLoading = false // ← stop spinning if cancelled
        }
    }

    DialogContainer(alpha = alpha) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CloseButton { visible = false }
         //   TitleText(subscriptionRequiredLabel) // Changed text
            TitleText(watchAdsOrSubscribeLabel)
            InfoText(watchAdsForLabel)
            FreeSummariesText()
            InfoText(youCanResetEverytimeLabel)
            Spacer(Modifier.height(16.dp))
            WatchAdsButton {
                showAd = true // ✅ Show rewarded ad when clicked
            }
            Spacer(Modifier.height(16.dp))
            InfoText(subscribeForNoAdsLabel)
            InfoText(firstWeekisFreeLabel)
            PriceText()
            InfoText(cancelAnytimeLabel)
            InfoText(noAdsLabel)
          //  InfoText(withoutSubscriptionLabel)
            Spacer(Modifier.height(16.dp))
            SubscribeButton(
                isLoading = isLoading,
                isSubscribed = isSubscribed,
                onClick = {
                    isLoading = true
                    onSubscribeClick()
                }
            )


        }
    }
    // ✅ Show rewarded ad when triggered
    RewardedAdHandler(
        showAd = showAd,
        activity = activity,
        onUserEarnedReward = {
            onWatchAdsClick()
        },
        onAdHandled = {
            showAd = false
        }
    )
}


@Composable
private fun DialogContainer(alpha: Float, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFB3B3B3), RoundedCornerShape(12.dp))
                .padding(6.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // ✅ Make scrollable
        ) {
            content()
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun TitleText(text: String) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        textAlign = TextAlign.Center,
        lineHeight = 40.sp
    )
}

@Composable
private fun InfoText(text: String) {
    Spacer(Modifier.height(12.dp))
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        textAlign = TextAlign.Center,
        lineHeight = 30.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PriceText() {
    val subscriptionIsLabel = stringResource(R.string.subscription_is)
    val subscriptionPriceLabel = stringResource(R.string.subscription_price)
    val monthlyLabel = stringResource(R.string.subscription_monthly)

    Spacer(Modifier.height(12.dp))
    Text(
        buildAnnotatedString {
            appendStyled(subscriptionIsLabel, FontWeight.Medium)
            appendStyled(subscriptionPriceLabel, FontWeight.Bold, Color.Red)
            appendStyled(monthlyLabel, FontWeight.Medium)
        },
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        lineHeight = 30.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FreeSummariesText() {

    val tenLabel = stringResource(R.string.ten)
    val freeLabel = stringResource(R.string.free)
    val summariesLabel = stringResource(R.string.summaries)

    Spacer(Modifier.height(12.dp))
    Text(
        buildAnnotatedString {
            appendStyled(tenLabel, FontWeight.Bold, Color.Red)
            appendStyled(freeLabel, FontWeight.Medium, Color.Black)
            appendStyled(summariesLabel, FontWeight.Medium, Color.Black)
        },
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        lineHeight = 30.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SubscribeButton(
    isLoading: Boolean,
    isSubscribed: Boolean,
    onClick: () -> Unit
) {

    val subscribeLabel = stringResource(R.string.subscribe)
    val subscribedLabel = stringResource(R.string.subscribed)

    Button(
        onClick = onClick,
        enabled = !isLoading && !isSubscribed,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        } else {
            Text(
                text = if (isSubscribed) subscribedLabel else subscribeLabel,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun WatchAdsButton(onClick: () -> Unit) {
    val watchAdsLabel = stringResource(R.string.watch_ads)

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // optional spacing
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(2.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = watchAdsLabel,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp) // padding for long texts
        )
    }
}




private fun AnnotatedString.Builder.appendStyled(
    text: String,
    weight: FontWeight,
    color: Color = Color.Black
) {
    withStyle(SpanStyle(fontWeight = weight, color = color)) {
        append(text)
    }
}

object BillingManagerHolder {

    private var onAcknowledged: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null

    fun setOnAcknowledged(callback: () -> Unit) {
        onAcknowledged = callback
    }

    fun notifyAcknowledged() {
        onAcknowledged?.invoke()
    }

    fun setOnPurchaseCancelled(callback: () -> Unit) {
        onCancelled = callback
    }

    fun notifyPurchaseCancelled() {
        onCancelled?.invoke()
    }
}







