package com.shahrukh.idcarddetectionapp.presentation.common



import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.shahrukh.idcarddetectionapp.R
import com.shahrukh.idcarddetectionapp.presentation.utils.Dimens


/**
 * Composable function that creates an image button with a given resource and content description.
 *
 * @param drawableResourceId The resource ID of the drawable to be used as the image source.
 * @param contentDescriptionResourceId The resource ID for the content description of the image,
 *        which is used for accessibility purposes.
 * @param modifier A [Modifier] for this image button, which can be used to adjust layout
 *        parameters or add decoration, like padding or clickable effects.
 */
@Composable
fun ImageButton(
    drawableResourceId: Int,
    contentDescriptionResourceId: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = drawableResourceId),
        contentDescription = stringResource(id = contentDescriptionResourceId),
        modifier = modifier
    )
}



@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun CaptureImageButtonPreview() {
    ImageButton(
        drawableResourceId = R.drawable.ic_capture,
        contentDescriptionResourceId = R.string.capture_button_description,
        modifier = Modifier
            .size(Dimens.CaptureButtonSize)
            .clip(CircleShape)
            .clickable {

            }
    )
}