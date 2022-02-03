package team.nexters.kida.ui.keyword

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import team.nexters.kida.R
import team.nexters.kida.component.HorizontalPagerIndicator
import team.nexters.kida.data.keyword.Keyword
import team.nexters.kida.ui.theme.Theme
import team.nexters.kida.util.DateUtils
import kotlin.math.absoluteValue

@Composable
fun KeywordSelectScreen(
    onNavigate: (Keyword) -> Unit,
    viewModel: KeywordViewModel = hiltViewModel(),
) {
    val scaffoldState = rememberScaffoldState()
    val keywords by viewModel.keywords.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = DateUtils.today()) },
                backgroundColor = MaterialTheme.colors.background,
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        KeywordSelectContent(
            onClickButton = {
                // TODO filtering
                onNavigate(keywords.random())
            },
            keywords
        )
    }
}

@Composable
private fun KeywordSelectContent(
    onClickButton: () -> Unit,
    keywords: List<Keyword>
) {
    val pagerState = rememberPagerState()
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        Spacer(modifier = Modifier.size(20.dp))

        KeywordSelectHeader(pagerState, onConfirmClick = onClickButton)
        Spacer(modifier = Modifier.size(20.dp))
        HorizontalPager(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            count = 8,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) { page ->
            // Calculate the absolute offset for the current page from the
            // scroll position. We use the absolute value which allows us to mirror
            // any effects for both directions
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
            KeywordSelectPagerCardItem(page = page, pageOffset = pageOffset)
        }

        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun KeywordSelectHeader(
    pagerState: PagerState,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = context.getString(R.string.keyword_highlight_1),
                fontSize = 30.sp,
                textAlign = TextAlign.Start
            )

            HorizontalPagerIndicator(
                modifier = Modifier.padding(top = 24.dp),
                pagerState = pagerState,
                spacing = 6.dp,
                activeColor = Theme.colors.primary,
                activeIndicatorWidth = 18.dp,
                activeIndicatorHeight = 6.dp,
                inactiveColor = Theme.colors.disabled,
                inactiveIndicatorHeight = 6.dp,
                inactiveIndicatorWidth = 6.dp
            )
        }

        KeywordSelectConfirmButton(
            onClick = onConfirmClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Preview
@Composable
fun KeywordSelectHeaderPreview() {
    KeywordSelectHeader(pagerState = rememberPagerState(), onConfirmClick = {})
}

@Composable
fun KeywordSelectPagerCardItem(page: Int, pageOffset: Float) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // We animate the scaleX + scaleY, between 85% and 100%
                lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            },
        backgroundColor = Color.Green,
        shape = RoundedCornerShape(10.dp)
    ) {
    }
}

@Composable
fun KeywordSelectConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val backgroundColors by animateColorAsState(targetValue = if (enabled) Theme.colors.primary else Theme.colors.disabled)
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColors)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "confirm")
    }
}

@Preview
@Composable
fun KeywordSelectConfirmButtonEnablePreview() {
    KeywordSelectConfirmButton(onClick = { })
}

@Preview
@Composable
fun KeywordSelectConfirmButtonDisabledPreview() {
    KeywordSelectConfirmButton(onClick = { }, enabled = false)
}
