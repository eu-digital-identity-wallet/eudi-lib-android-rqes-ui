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

package eu.europa.ec.rqesui.uilogic.component.wrap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.infrastructure.theme.backgroundDefault
import eu.europa.ec.rqesui.infrastructure.theme.dividerDefault
import eu.europa.ec.rqesui.infrastructure.theme.textPrimaryDark
import eu.europa.ec.rqesui.infrastructure.theme.textSecondaryDark
import eu.europa.ec.rqesui.infrastructure.theme.topCorneredShapeDefault
import eu.europa.ec.rqesui.infrastructure.theme.value.Typography
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.extension.throttledClickable
import eu.europa.ec.rqesui.uilogic.component.AppIcons
import eu.europa.ec.rqesui.uilogic.component.ModalOptionUi
import eu.europa.ec.rqesui.uilogic.component.preview.PreviewTheme
import eu.europa.ec.rqesui.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.uilogic.component.utils.HSpacer
import eu.europa.ec.rqesui.uilogic.component.utils.SIZE_SMALL
import eu.europa.ec.rqesui.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.rqesui.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.rqesui.uilogic.component.utils.VSpacer


/** value set to SPACING_LARGE, 24dp */
private val defaultBottomSheetPadding: PaddingValues = PaddingValues(
    start = SPACING_LARGE.dp,
    end = SPACING_LARGE.dp,
    bottom = SPACING_LARGE.dp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrapModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    dragHandle: @Composable (() -> Unit)? = null,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        dragHandle = dragHandle,
        content = sheetContent,
    )
}

@Composable
fun GenericBaseSheetContent(
    title: String,
    bodyContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(defaultBottomSheetPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.background, // Change this to any color you prefer
                    shape = MaterialTheme.shapes.topCorneredShapeDefault // Rounded corners with a radius of 12.dp
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WrapIcon(
                iconData = AppIcons.HandleBar,
                customTint = MaterialTheme.colorScheme.dividerDefault
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.textPrimaryDark
            )
        )

        VSpacer.Small()
        bodyContent()
    }
}

@Composable
fun GenericBaseSheetContent(
    titleContent: @Composable () -> Unit,
    bodyContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(defaultBottomSheetPadding)
    ) {
        titleContent()
        VSpacer.Large()
        bodyContent()
    }
}

@Composable
fun DialogBottomSheet(
    title: String,
    message: String,
    positiveButtonText: String? = null,
    negativeButtonText: String? = null,
    onPositiveClick: () -> Unit? = {},
    onNegativeClick: () -> Unit? = {}
) {
    GenericBaseSheetContent(
        title = title,
        bodyContent = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.textSecondaryDark
                )
            )
            VSpacer.Large()

            Row {
                negativeButtonText?.let {
                    WrapSecondaryButton(
                        onClick = { onNegativeClick.invoke() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = negativeButtonText,
                            style = Typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HSpacer.Small()

                positiveButtonText?.let {
                    WrapPrimaryButton(
                        onClick = { onPositiveClick.invoke() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = positiveButtonText
                        )
                    }
                }
            }

        }
    )
}

@Composable
fun <T : ViewEvent> BottomSheetWithOptionsList(
    title: String,
    message: String,
    options: List<ModalOptionUi<T>>,
    onEventSent: (T) -> Unit
) {
    if (options.isNotEmpty()) {
        GenericBaseSheetContent(
            title = title,
            bodyContent = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.textSecondaryDark
                    )
                )
                VSpacer.Large()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    OptionsList(
                        optionItems = options,
                        itemSelected = onEventSent
                    )
                }
            }
        )
    }
}

@Composable
private fun <T : ViewEvent> OptionsList(
    optionItems: List<ModalOptionUi<T>>,
    itemSelected: (T) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
    ) {
        items(optionItems) { item ->
            OptionListItem(
                item = item,
                itemSelected = itemSelected
            )
        }
    }
}

@Composable
private fun <T : ViewEvent> OptionListItem(
    item: ModalOptionUi<T>,
    itemSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SIZE_SMALL.dp))
            .background(MaterialTheme.colorScheme.backgroundDefault)
            .throttledClickable {
                itemSelected(item.event)
            }
            .padding(
                horizontal = SPACING_SMALL.dp,
                vertical = SPACING_MEDIUM.dp
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.title,
            style = MaterialTheme.typography.bodyMedium
        )
        WrapIcon(
            modifier = Modifier.wrapContentWidth(),
            iconData = item.icon,
            customTint = MaterialTheme.colorScheme.primary
        )
    }
}

@ThemeModePreviews
@Composable
private fun DialogBottomSheetPreview() {
    PreviewTheme {
        DialogBottomSheet(
            title = "Title",
            message = "Message",
            positiveButtonText = "OK",
            negativeButtonText = "Cancel"
        )
    }
}

@ThemeModePreviews
@Composable
private fun BottomSheetWithOptionsListPreview() {
    PreviewTheme {
        BottomSheetWithOptionsList(
            title = "Title",
            message = "Message",
            options = listOf<ModalOptionUi<ViewEvent>>(),
            onEventSent = {}
        )
    }
}