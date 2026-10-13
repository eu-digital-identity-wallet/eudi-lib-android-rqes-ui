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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel

/**
 * Runs [block] once per [viewModel] instance for the given [key].
 *
 * The "already ran" marker lives in the ViewModel, not in saved instance state, so after
 * process death the new ViewModel runs [block] again instead of leaving the screen empty.
 *
 * @param viewModel The ViewModel that owns the one-time work.
 * @param key Identifies the one-time work. Defaults to [MviViewModel.ONE_TIME_INIT_KEY].
 * @param block The work to run.
 */
@Composable
internal fun OncePerViewModelEffect(
    viewModel: MviViewModel<*, *, *>,
    key: String = MviViewModel.ONE_TIME_INIT_KEY,
    block: () -> Unit
) {
    LaunchedEffect(viewModel, key) {
        viewModel.runOncePerInstance(key, block)
    }
}