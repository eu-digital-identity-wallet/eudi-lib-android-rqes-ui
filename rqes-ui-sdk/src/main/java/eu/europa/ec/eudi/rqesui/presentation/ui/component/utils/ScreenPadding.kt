/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.rqesui.presentation.ui.component.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

internal enum class TopSpacing {
    WithToolbar, WithoutToolbar, MediumSpacing
}

internal fun screenPaddings(
    append: PaddingValues? = null,
    topSpacing: TopSpacing = TopSpacing.WithToolbar
) = PaddingValues(
    start = SPACING_LARGE.dp,
    top = calculateTopSpacing(topSpacing).dp + (append?.calculateTopPadding() ?: 0.dp),
    end = SPACING_LARGE.dp,
    bottom = SPACING_LARGE.dp + (append?.calculateBottomPadding() ?: 0.dp)
)

internal fun stickyBottomPaddings(
    contentScreenPaddings: PaddingValues,
    layoutDirection: LayoutDirection
): PaddingValues {
    return PaddingValues(
        start = contentScreenPaddings.calculateStartPadding(layoutDirection),
        end = contentScreenPaddings.calculateEndPadding(layoutDirection),
        top = contentScreenPaddings.calculateBottomPadding(),
        bottom = contentScreenPaddings.calculateBottomPadding()
    )
}

private fun calculateTopSpacing(topSpacing: TopSpacing): Int = when (topSpacing) {
    TopSpacing.WithToolbar -> SPACING_SMALL
    TopSpacing.WithoutToolbar -> SPACING_EXTRA_LARGE
    TopSpacing.MediumSpacing -> SPACING_MEDIUM
}