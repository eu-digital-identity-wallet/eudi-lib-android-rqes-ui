/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.eudi.rqesui.presentation.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import eu.europa.ec.eudi.rqesui.R


/**
 * Data class to be used when we want to display an Icon.
 * @param resourceId The id of the icon. Can be null
 * @param contentDescriptionId The id of its content description.
 * @param imageVector The [ImageVector] of the icon, null by default.
 * @throws IllegalArgumentException If both [resourceId] AND [imageVector] are null.
 */
@Stable
internal data class IconData(
    @param:DrawableRes val resourceId: Int?,
    @param:StringRes val contentDescriptionId: Int,
    val imageVector: ImageVector? = null,
) {
    init {
        require(
            resourceId == null && imageVector != null
                    || resourceId != null && imageVector == null
                    || resourceId != null && imageVector != null
        ) {
            "An Icon should at least have a valid resourceId or a valid imageVector."
        }
    }
}

/**
 * A Singleton object responsible for providing access to all the app's Icons.
 */
internal object AppIcons {

    val ArrowBack: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_arrow_back_icon,
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
    )

    val KeyboardArrowRight: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_arrow_right_icon,
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    )

    val Close: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_close_icon,
        imageVector = Icons.Filled.Close
    )

    val HandleBar: IconData = IconData(
        resourceId = R.drawable.ic_handle_bar,
        contentDescriptionId = R.string.content_description_handle_bar,
        imageVector = null
    )

    val Verified: IconData = IconData(
        resourceId = R.drawable.ic_rqes_verified,
        contentDescriptionId = R.string.content_description_verified_icon,
        imageVector = null
    )

    val VerifiedBadge: IconData = IconData(
        resourceId = R.drawable.ic_verified_badge,
        contentDescriptionId = R.string.content_description_verified_icon,
        imageVector = null
    )

    val VerticalMore: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_more_vert_icon,
        imageVector = Icons.Filled.MoreVert
    )

    val StepOne: IconData = IconData(
        resourceId = R.drawable.ic_step_one,
        contentDescriptionId = R.string.content_description_selection_step_icon,
        imageVector = null
    )

    val StepTwo: IconData = IconData(
        resourceId = R.drawable.ic_step_two,
        contentDescriptionId = R.string.content_description_selection_step_icon,
        imageVector = null
    )

    val StepThree: IconData = IconData(
        resourceId = R.drawable.ic_step_three,
        contentDescriptionId = R.string.content_description_selection_step_icon,
        imageVector = null
    )

    val LogoPlain: IconData = IconData(
        resourceId = R.drawable.ic_logo_plain,
        contentDescriptionId = R.string.content_description_logo_plain_icon,
        imageVector = null
    )

    val LogoText: IconData = IconData(
        resourceId = R.drawable.ic_logo_text,
        contentDescriptionId = R.string.content_description_logo_text_icon,
        imageVector = null
    )
}