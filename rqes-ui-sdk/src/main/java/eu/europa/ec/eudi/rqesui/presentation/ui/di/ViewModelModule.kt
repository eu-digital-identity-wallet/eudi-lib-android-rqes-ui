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

package eu.europa.ec.eudi.rqesui.presentation.ui.di

import eu.europa.ec.eudi.rqesui.presentation.ui.options_selection.OptionsSelectionViewModel
import eu.europa.ec.eudi.rqesui.presentation.ui.success.SuccessViewModel
import eu.europa.ec.eudi.rqesui.presentation.ui.view_document.ViewDocumentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val viewModelModule = module {
    viewModel<OptionsSelectionViewModel> { params ->
        OptionsSelectionViewModel(
            optionsSelectionInteractor = get(),
            resourceProvider = get(),
            uiSerializer = get(),
            serializedOptionsSelectionUiConfig = params.get<String>()
        )
    }
    viewModel<SuccessViewModel> {
        SuccessViewModel(
            successInteractor = get(),
            resourceProvider = get(),
            uiSerializer = get()
        )
    }
    viewModel<ViewDocumentViewModel> { params ->
        ViewDocumentViewModel(
            resourceProvider = get(),
            uiSerializer = get(),
            serializedViewDocumentUiConfig = params.get<String>()
        )
    }
}