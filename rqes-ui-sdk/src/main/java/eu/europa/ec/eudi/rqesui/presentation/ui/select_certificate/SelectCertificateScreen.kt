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

package eu.europa.ec.eudi.rqesui.presentation.ui.select_certificate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.eudi.rqes.AuthorizationMode
import eu.europa.ec.eudi.rqes.CredentialAuthorization
import eu.europa.ec.eudi.rqes.CredentialCertificate
import eu.europa.ec.eudi.rqes.CredentialCertificateStatus
import eu.europa.ec.eudi.rqes.CredentialID
import eu.europa.ec.eudi.rqes.CredentialInfo
import eu.europa.ec.eudi.rqes.CredentialKey
import eu.europa.ec.eudi.rqes.CredentialKeyStatus
import eu.europa.ec.eudi.rqes.SCAL
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.divider
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.extension.finish
import eu.europa.ec.eudi.rqesui.presentation.extension.openUrl
import eu.europa.ec.eudi.rqesui.presentation.extension.throttledClickable
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentTitle
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SIZE_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.DialogBottomSheet
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapBottomBarPrimaryButton
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapModalBottomSheet
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapRadioButton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectCertificateScreen(
    navController: NavController,
    viewModel: SelectCertificateViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.CANCELABLE,
        onBack = { viewModel.setEvent(Event.Pop) },
        contentErrorConfig = state.error,
        stickyBottom = { paddingValues ->
            WrapBottomBarPrimaryButton(
                stickyBottomContentModifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                buttonText = state.bottomBarButtonText,
                enabled = state.isBottomBarButtonEnabled,
                onButtonClick = {
                    viewModel.setEvent(
                        Event.BottomBarButtonPressed
                    )
                }
            )
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onEventSend = { viewModel.setEvent(it) },
            onNavigationRequested = { navigationEffect ->
                when (navigationEffect) {
                    is Effect.Navigation.Finish -> context.finish()
                    is Effect.Navigation.SwitchScreen -> navController.navigate(navigationEffect.screenRoute)
                }
            },
            paddingValues = paddingValues,
            modalBottomSheetState = bottomSheetState
        )

        if (isBottomSheetOpen) {
            WrapModalBottomSheet(
                onDismissRequest = {
                    viewModel.setEvent(
                        Event.BottomSheet.UpdateBottomSheetState(isOpen = false)
                    )
                },
                sheetState = bottomSheetState
            ) {
                SelectCertificateSheetContent(
                    sheetTextData = state.sheetTextData,
                    onEventSent = { event ->
                        viewModel.setEvent(event)
                    }
                )
            }
        }
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Init)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
    modalBottomSheetState: SheetState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        ContentTitle(
            title = state.title,
            subtitle = state.subtitle,
        )

        VSpacer.Medium()

        state.selectionItem?.let { safeSelectionItem ->
            SelectionItem(
                modifier = Modifier.fillMaxWidth(),
                data = safeSelectionItem,
                onClick = null,
            )
        }

        VSpacer.Medium()

        ContentTitle(
            subtitle = state.certificatesSectionTitle
        )

        VSpacer.Small()

        CertificatesList(
            certificateItems = state.certificates,
            selectedIndex = state.selectedCertificateIndex,
            onEventSend = onEventSend
        )

    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)

                is Effect.CloseBottomSheet -> {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }.invokeOnCompletion {
                        if (!modalBottomSheetState.isVisible) {
                            onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                        }
                    }
                }

                is Effect.ShowBottomSheet -> {
                    onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                }

                is Effect.OnSelectionItemCreated -> {
                    onEventSend(Event.AuthorizeServiceAndFetchCertificates)
                }

                is Effect.OpenUrl -> {
                    context.openUrl(effect.uri)
                }
            }
        }.collect()
    }
}

@Composable
private fun CertificatesList(
    certificateItems: List<CertificateData>,
    selectedIndex: Int,
    onEventSend: (Event) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(SPACING_EXTRA_SMALL.dp)
    ) {
        itemsIndexed(certificateItems) { index, item ->
            CertificateListItem(
                optionName = item.name,
                isSelected = selectedIndex == index,
                onClick = {
                    onEventSend(Event.CertificateSelected(index = index))
                }
            )

            if (index < certificateItems.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.divider
                )
            }
        }
    }
}

@Composable
private fun CertificateListItem(
    optionName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SIZE_SMALL.dp))
            .throttledClickable {
                onClick.invoke()
            }
            .padding(
                horizontal = SPACING_SMALL.dp,
                vertical = SPACING_MEDIUM.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = optionName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        WrapRadioButton(
            isSelected = isSelected,
        )
    }
}

@Composable
private fun SelectCertificateSheetContent(
    sheetTextData: BottomSheetTextData,
    onEventSent: (event: Event) -> Unit,
) {
    DialogBottomSheet(
        textData = sheetTextData,
        onPositiveClick = {
            onEventSent(Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed)
        },
        onNegativeClick = {
            onEventSent(Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun SelectCertificateScreenPreview() {
    PreviewTheme {
        val dummyCredentialInfo = CredentialInfo(
            credentialID = CredentialID("1234567890"),
            description = null,
            signatureQualifier = null,
            key = CredentialKey(
                status = CredentialKeyStatus.Enabled,
                supportedAlgorithms = listOf(),
                length = 6808,
                curve = null
            ),
            certificate = CredentialCertificate(
                status = CredentialCertificateStatus.Valid,
                certificates = listOf(),
                issuerDN = null,
                serialNumber = null,
                subjectDN = null,
                validFrom = null,
                validTo = null
            ),
            authorization = CredentialAuthorization.OAuth2Code(
                authorizationMode = AuthorizationMode.OAuth2Code
            ),
            scal = SCAL.One,
            multisign = 8779,
            lang = null
        )
        Content(
            state = State(
                title = "Sign document",
                subtitle = "You have chosen to sign the following document:",
                selectionItem = SelectionItemUi(
                    documentData = DocumentData(
                        documentName = "Document name.PDF",
                        uri = "".toUri()
                    ),
                    subtitle = "Signed by: Entrust",
                    trailingIconTint = ThemeColors.success
                ),
                certificatesSectionTitle = "Please confirm signing with one of the following certificates:",
                certificates = listOf(
                    CertificateData(
                        name = "Certificate 1",
                        certificate = dummyCredentialInfo,
                    ),
                    CertificateData(
                        name = "Certificate 2",
                        certificate = dummyCredentialInfo,
                    ),
                    CertificateData(
                        name = "Certificate 3",
                        certificate = dummyCredentialInfo,
                    ),
                ),
                bottomBarButtonText = "Sign",
                sheetTextData = BottomSheetTextData(
                    title = "title",
                    message = "message",
                )
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
            modalBottomSheetState = rememberModalBottomSheetState(),
        )
    }
}