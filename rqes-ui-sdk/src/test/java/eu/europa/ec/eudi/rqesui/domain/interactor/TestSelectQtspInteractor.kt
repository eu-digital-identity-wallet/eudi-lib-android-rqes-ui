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

package eu.europa.ec.eudi.rqesui.domain.interactor

import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class TestSelectQtspInteractor {

    @Mock
    private lateinit var eudiController: RqesController

    @Mock
    private lateinit var qtspData: QtspData

    @Mock
    private lateinit var rqesService: RQESService

    private lateinit var closeable: AutoCloseable

    private lateinit var interactor: SelectQtspInteractor

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        interactor = SelectQtspInteractorImpl(eudiController)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getQtsps
    // Case 1:
    // 1. Interactor's getQtsps is called.
    // Case 1 Expected Result:
    // 1. eudiController's getQtsps is called exactly once.
    @Test
    fun `Verify that When getQtsps is called, Then getQtsps is executed on the eudiController`() {
        // When
        interactor.getQtsps()

        // Then
        verify(eudiController, times(1))
            .getQtsps()
    }
    //endregion

    //region getSelectedFile
    // Case 1:
    // 1. Interactor's getSelectedFile is called.
    // Case 1 Expected Result:
    // 1. eudiController's getSelectedFile is called exactly once.
    @Test
    fun `Verify that When getSelectedFile is called, Then getSelectedFile is executed on the eudiController`() {
        // When
        interactor.getSelectedFile()

        // Then
        verify(eudiController, times(1))
            .getSelectedFile()
    }
    //endregion

    //region updateQtspUserSelection
    // Case 1:
    // 1. Interactor's updateQtspUserSelection is called.
    // Case 1 Expected Result:
    // 1. eudiController's setSelectedQtsp is called exactly once.
    @Test
    fun `Verify that When updateQtspUserSelection is called, Then setSelectedQtsp is executed on the eudiController`() {
        interactor.updateQtspUserSelection(qtspData)

        verify(eudiController, times(1))
            .setSelectedQtsp(qtspData)
    }
    //endregion

    //region getServiceAuthorizationUrl
    // Case 1:
    // 1. Interactor's getServiceAuthorizationUrl is called.
    // Case 1 Expected Result:
    // 1. eudiController's getServiceAuthorizationUrl is called exactly once.
    @Test
    fun `Verify that When getServiceAuthorizationUrl is called, Then getServiceAuthorizationUrl is executed on the eudiController`() =
        runTest {
            interactor.getServiceAuthorizationUrl(rqesService)

            verify(eudiController, times(1))
                .getServiceAuthorizationUrl(rqesService)
        }
    //endregion
}