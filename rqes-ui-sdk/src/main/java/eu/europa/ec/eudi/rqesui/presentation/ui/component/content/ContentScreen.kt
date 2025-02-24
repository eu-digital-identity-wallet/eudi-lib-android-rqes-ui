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

package eu.europa.ec.eudi.rqesui.presentation.ui.component.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.IconData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.loader.LoadingIndicator
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.MAX_TOOLBAR_ACTIONS
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.TopSpacing
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.Z_STICKY
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.screenPaddings
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.stickyBottomPaddings
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIcon
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIconButton

internal enum class LoadingType {
    NORMAL, NONE
}

internal data class ToolbarAction(
    val icon: IconData,
    val order: Int = 100,
    val enabled: Boolean = true,
    val customTint: Color? = null,
    val clickable: Boolean = true,
    val onClick: () -> Unit,
)

internal data class ToolbarConfig(
    val title: String = "",
    val actions: List<ToolbarAction> = listOf(),
    val hasShadow: Boolean = false
)

internal enum class ScreenNavigateAction {
    BACKABLE, CANCELABLE, NONE
}

@Composable
internal fun ContentScreen(
    isLoading: Boolean = false,
    toolBarConfig: ToolbarConfig? = null,
    navigatableAction: ScreenNavigateAction = ScreenNavigateAction.BACKABLE,
    onBack: (() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    stickyBottom: @Composable ((PaddingValues) -> Unit)? = null,
    fab: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    contentErrorConfig: ContentErrorConfig? = null,
    bodyContent: @Composable (PaddingValues) -> Unit
) {
    ContentScreen(
        loadingType = if (isLoading) LoadingType.NORMAL else LoadingType.NONE,
        toolBarConfig = toolBarConfig,
        navigatableAction = navigatableAction,
        onBack = onBack,
        topBar = topBar,
        bottomBar = bottomBar,
        stickyBottom = stickyBottom,
        fab = fab,
        fabPosition = fabPosition,
        contentErrorConfig = contentErrorConfig,
        bodyContent = bodyContent
    )
}

@Composable
internal fun ContentScreen(
    loadingType: LoadingType = LoadingType.NONE,
    toolBarConfig: ToolbarConfig? = null,
    navigatableAction: ScreenNavigateAction = ScreenNavigateAction.BACKABLE,
    onBack: (() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    stickyBottom: @Composable ((PaddingValues) -> Unit)? = null,
    fab: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    contentErrorConfig: ContentErrorConfig? = null,
    bodyContent: @Composable (PaddingValues) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasToolBar = contentErrorConfig != null
            || navigatableAction != ScreenNavigateAction.NONE
            || topBar != null
    val topSpacing = TopSpacing.WithToolbar.takeIf { hasToolBar } ?: TopSpacing.MediumSpacing

    Scaffold(
        topBar = {
            if (topBar != null && contentErrorConfig == null) topBar.invoke()
            else if (hasToolBar) {
                DefaultToolBar(
                    navigatableAction = contentErrorConfig?.let {
                        ScreenNavigateAction.CANCELABLE
                    } ?: navigatableAction,
                    onBack = contentErrorConfig?.onCancel ?: onBack,
                    keyboardController = keyboardController,
                    toolbarConfig = toolBarConfig,
                    contentErrorConfig = contentErrorConfig
                )
            }
        },
        bottomBar = bottomBar ?: {},
        floatingActionButton = fab,
        floatingActionButtonPosition = fabPosition,
        snackbarHost = {},

        ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if (contentErrorConfig != null) {
                ContentError(
                    config = contentErrorConfig,
                    paddingValues = screenPaddings(padding)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(modifier = Modifier.weight(1f)) {
                        bodyContent(
                            screenPaddings(padding, topSpacing)
                        )
                    }

                    stickyBottom?.let { stickyBottomContent ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(Z_STICKY),
                            contentAlignment = Alignment.Center
                        ) {
                            stickyBottomContent(
                                stickyBottomPaddings(
                                    contentScreenPaddings = screenPaddings(padding),
                                    layoutDirection = LocalLayoutDirection.current
                                )
                            )
                        }
                    }
                }

                if (loadingType == LoadingType.NORMAL) LoadingIndicator()
            }
        }
    }

    BackHandler(enabled = true) {
        contentErrorConfig?.let {
            contentErrorConfig.onCancel()
        } ?: onBack?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultToolBar(
    navigatableAction: ScreenNavigateAction,
    onBack: (() -> Unit)?,
    keyboardController: SoftwareKeyboardController?,
    toolbarConfig: ToolbarConfig?,
    contentErrorConfig: ContentErrorConfig?
) {

    val title = contentErrorConfig?.errorTitle ?: toolbarConfig?.title
    val isTitlePresent = !title.isNullOrEmpty()

    val modifier = Modifier
        .fillMaxWidth()
        .then(
            if (toolbarConfig?.hasShadow == true) {
                Modifier.shadow(elevation = SPACING_SMALL.dp)
            } else {
                Modifier
            }
        )

    val colors = TopAppBarDefaults.topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.surface
    )

    AdaptiveTopAppBar(
        modifier = modifier,
        hasTitle = isTitlePresent,
        title = {
            if (isTitlePresent) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (navigatableAction != ScreenNavigateAction.NONE) {
                val navigationIcon = when (navigatableAction) {
                    ScreenNavigateAction.CANCELABLE -> AppIcons.Close
                    else -> AppIcons.ArrowBack
                }

                ToolbarIcon(
                    toolbarAction = ToolbarAction(
                        icon = navigationIcon,
                        onClick = {
                            onBack?.invoke()
                            keyboardController?.hide()
                        }
                    )
                )
            }
        },
        actions = {
            ToolBarActions(toolBarActions = toolbarConfig?.actions)
        },
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptiveTopAppBar(
    modifier: Modifier,
    hasTitle: Boolean,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    colors: TopAppBarColors,
) {
    if (hasTitle) {
        MediumTopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors
        )
    }
}

@Composable
private fun ToolBarActions(
    toolBarActions: List<ToolbarAction>?,
    maxActionsShown: Int = MAX_TOOLBAR_ACTIONS,
) {
    toolBarActions?.let { actions ->

        var dropDownMenuExpanded by remember { mutableStateOf(false) }

        // Show first [MAX_TOOLBAR_ACTIONS] actions.
        actions
            .sortedByDescending { it.order }
            .take(maxActionsShown)
            .map { visibleToolbarAction ->
                ToolbarIcon(toolbarAction = visibleToolbarAction)
            }

        // Check if there are more actions to show.
        if (actions.size > maxActionsShown) {
            Box {
                ToolbarIcon(
                    toolbarAction = ToolbarAction(
                        icon = AppIcons.VerticalMore,
                        onClick = { dropDownMenuExpanded = !dropDownMenuExpanded },
                        enabled = true,
                    )
                )
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = { dropDownMenuExpanded = false }
                ) {
                    actions
                        .sortedByDescending { it.order }
                        .drop(maxActionsShown)
                        .map { dropDownMenuToolbarAction ->
                            ToolbarIcon(toolbarAction = dropDownMenuToolbarAction)
                        }
                }
            }
        }
    }
}

@Composable
private fun ToolbarIcon(toolbarAction: ToolbarAction) {
    val customIconTint = toolbarAction.customTint
        ?: MaterialTheme.colorScheme.onSurface

    if (toolbarAction.clickable) {
        WrapIconButton(
            iconData = toolbarAction.icon,
            onClick = toolbarAction.onClick,
            enabled = toolbarAction.enabled,
            customTint = customIconTint,
        )
    } else {
        WrapIcon(
            modifier = Modifier.minimumInteractiveComponentSize(),
            iconData = toolbarAction.icon,
            enabled = toolbarAction.enabled,
            customTint = customIconTint,
        )
    }
}

@ThemeModePreviews
@Composable
private fun ToolbarIconClickablePreview() {
    PreviewTheme {
        val action = ToolbarAction(
            icon = AppIcons.Verified,
            onClick = {},
            enabled = true,
            clickable = true,
        )

        ToolbarIcon(toolbarAction = action)
    }
}

@ThemeModePreviews
@Composable
private fun ToolbarIconNotClickablePreview() {
    PreviewTheme {
        val action = ToolbarAction(
            icon = AppIcons.Verified,
            onClick = {},
            enabled = true,
            clickable = false,
        )

        ToolbarIcon(toolbarAction = action)
    }
}

@ThemeModePreviews
@Composable
private fun ToolBarActionsWithTwoActionsPreview() {
    PreviewTheme {
        val toolBarActions = listOf(
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = true,
                clickable = true,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = false,
                clickable = true,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = true,
                clickable = false,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = false,
                clickable = false,
            )
        )
        Row {
            ToolBarActions(
                toolBarActions = toolBarActions,
                maxActionsShown = MAX_TOOLBAR_ACTIONS,
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun ToolBarActionsWithFourActionsPreview() {
    PreviewTheme {
        val toolBarActions = listOf(
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = true,
                clickable = true,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = false,
                clickable = true,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = true,
                clickable = false,
            ),
            ToolbarAction(
                icon = AppIcons.Verified,
                onClick = {},
                enabled = false,
                clickable = false,
            )
        )
        Row {
            ToolBarActions(
                toolBarActions = toolBarActions,
                maxActionsShown = 4,
            )
        }
    }
}