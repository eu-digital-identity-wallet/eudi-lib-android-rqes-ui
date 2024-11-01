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

package eu.europa.ec.rqesui.presentation.ui.component.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.europa.ec.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.rqesui.presentation.ui.component.IconData
import eu.europa.ec.rqesui.presentation.ui.component.loader.LoadingIndicator
import eu.europa.ec.rqesui.presentation.ui.component.utils.MAX_TOOLBAR_ACTIONS
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.TopSpacing
import eu.europa.ec.rqesui.presentation.ui.component.utils.Z_STICKY
import eu.europa.ec.rqesui.presentation.ui.component.utils.screenPaddings
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapIconButton

internal enum class LoadingType {
    NORMAL, NONE
}

internal data class ToolbarAction(
    val icon: IconData,
    val order: Int = 100,
    val enabled: Boolean = true,
    val customTint: Color? = null,
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
    stickyBottom: @Composable (() -> Unit)? = null,
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
    stickyBottom: @Composable (() -> Unit)? = null,
    fab: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    contentErrorConfig: ContentErrorConfig? = null,
    bodyContent: @Composable (PaddingValues) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val hasToolBar = contentErrorConfig != null
            || navigatableAction != ScreenNavigateAction.NONE
            || topBar != null
    val topSpacing = TopSpacing.MediumSpacing

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
                    hasShadow = toolBarConfig?.hasShadow ?: false
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
                                .padding(screenPaddings(padding))
                                .zIndex(Z_STICKY),
                            contentAlignment = Alignment.Center
                        ) {
                            stickyBottomContent()
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
    hasShadow: Boolean,
) {
    TopAppBar(
        modifier = Modifier
            .shadow(elevation = SPACING_SMALL.dp)
            .takeIf { hasShadow } ?: Modifier,
        title = {
            Text(
                text = toolbarConfig?.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            // Check if we should add back/close button.
            if (navigatableAction != ScreenNavigateAction.NONE) {
                val navigationIcon = when (navigatableAction) {
                    ScreenNavigateAction.CANCELABLE -> AppIcons.Close
                    else -> AppIcons.ArrowBack
                }

                Row(modifier = Modifier.padding(start = SPACING_EXTRA_SMALL.dp)) {
                    WrapIconButton(
                        iconData = navigationIcon,
                        onClick = {
                            onBack?.invoke()
                            keyboardController?.hide()
                        },
                        customTint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        // Add toolbar actions.
        actions = {
            ToolBarActions(toolBarActions = toolbarConfig?.actions)
        },
        colors = TopAppBarDefaults.topAppBarColors()
            .copy(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
internal fun ToolBarActions(toolBarActions: List<ToolbarAction>?) {
    toolBarActions?.let { actions ->

        var dropDownMenuExpanded by remember {
            mutableStateOf(false)
        }

        // Show first [MAX_TOOLBAR_ACTIONS] actions.
        actions
            .sortedByDescending { it.order }
            .take(MAX_TOOLBAR_ACTIONS)
            .map { visibleToolbarAction ->
                WrapIconButton(
                    iconData = visibleToolbarAction.icon,
                    onClick = visibleToolbarAction.onClick,
                    enabled = visibleToolbarAction.enabled,
                    customTint = visibleToolbarAction.customTint
                        ?: MaterialTheme.colorScheme.primary
                )
            }
    }
}