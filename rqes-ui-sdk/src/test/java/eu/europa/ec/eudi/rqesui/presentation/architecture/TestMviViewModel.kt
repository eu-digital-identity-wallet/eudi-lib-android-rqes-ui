/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.eudi.rqesui.presentation.architecture

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestMviViewModel {

    private data object TestEvent : ViewEvent
    private data object TestState : ViewState
    private data object TestEffect : ViewSideEffect

    private class TestViewModel : MviViewModel<TestEvent, TestState, TestEffect>() {
        override fun setInitialState(): TestState = TestState
        override fun handleEvents(event: TestEvent) = Unit
    }

    @Test
    fun `Given the same key, When runOncePerInstance is called repeatedly on one instance, Then the block runs once`() {
        val viewModel = TestViewModel()
        var runs = 0

        repeat(5) { viewModel.runOncePerInstance { runs++ } }

        assertEquals(1, runs)
    }

    @Test
    fun `Given a new instance, When runOncePerInstance is called, Then the block runs again`() {
        var runs = 0

        TestViewModel().runOncePerInstance { runs++ }
        assertEquals(1, runs)

        TestViewModel().runOncePerInstance { runs++ }
        assertEquals(2, runs)
    }

    @Test
    fun `Given distinct keys, When runOncePerInstance is called on one instance, Then each block runs once`() {
        val viewModel = TestViewModel()
        val ran = mutableListOf<String>()

        repeat(3) {
            viewModel.runOncePerInstance("a") { ran += "a" }
            viewModel.runOncePerInstance("b") { ran += "b" }
        }

        assertEquals(listOf("a", "b"), ran)
    }

    @Test
    fun `Given no key, When runOncePerInstance is called, Then ONE_TIME_INIT_KEY is used`() {
        val viewModel = TestViewModel()
        var runs = 0

        viewModel.runOncePerInstance { runs++ }
        viewModel.runOncePerInstance(MviViewModel.ONE_TIME_INIT_KEY) { runs++ }

        assertEquals(1, runs)
    }
}
