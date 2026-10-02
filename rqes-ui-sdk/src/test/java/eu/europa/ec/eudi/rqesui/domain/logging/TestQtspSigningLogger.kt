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

package eu.europa.ec.eudi.rqesui.domain.logging

import eu.europa.ec.eudi.rqes.core.RqesSigningLogger
import eu.europa.ec.eudi.rqes.core.RqesSigningRecord
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class TestQtspSigningLogger {

    @Mock
    private lateinit var delegate: RqesSigningLogger

    @Mock
    private lateinit var otherDelegate: RqesSigningLogger

    private lateinit var signingLogger: QtspSigningLogger

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        signingLogger = QtspSigningLogger(
            delegate = delegate,
            qtspName = mockedQtspName
        )
    }

    @After
    fun after() {
        closeable.close()
    }

    //region onSigningCompleted

    // Case 1:
    // A completed record contains two documents with the same label and mixed optional metadata.
    // Expected: one callback preserves both documents and fills the missing service name.
    @Test
    fun `Given a completed multi-document record, When notified, Then only the service name is enriched`() {
        // Act
        signingLogger.onSigningCompleted(mockedRecord)

        // Assert
        val expectedRecord = mockedRecord.copy(
            serviceName = RqesSigningRecord.LocalizedName(
                languageTag = "und",
                name = mockedQtspName
            )
        )
        verify(delegate).onSigningCompleted(expectedRecord)
        verifyNoMoreInteractions(delegate)
    }

    // Case 2:
    // A failed record contains a reason and document metadata.
    // Expected: the failed outcome, exact reason and metadata are preserved.
    @Test
    fun `Given a failed record with a reason, When notified, Then the failure is forwarded unchanged`() {
        // Arrange
        val record = mockedRecord.copy(
            outcome = RqesSigningRecord.Outcome.Failed(reason = "  Signing service declined  ")
        )

        // Act
        signingLogger.onSigningCompleted(record)

        // Assert
        val expectedRecord = record.copy(
            serviceName = RqesSigningRecord.LocalizedName(
                languageTag = "und",
                name = mockedQtspName
            )
        )
        verify(delegate).onSigningCompleted(expectedRecord)
        verifyNoMoreInteractions(delegate)
    }

    // Case 3:
    // A failed record has no reason, certificate or documents.
    // Expected: the callback preserves the absent metadata and empty document list.
    @Test
    fun `Given an empty failed record, When notified, Then missing metadata and the empty list are retained`() {
        // Arrange
        val record = RqesSigningRecord(
            outcome = RqesSigningRecord.Outcome.Failed(reason = null),
            certificateSerialNumber = null,
            documents = emptyList(),
            serviceName = null
        )

        // Act
        signingLogger.onSigningCompleted(record)

        // Assert
        val expectedRecord = record.copy(
            serviceName = RqesSigningRecord.LocalizedName(
                languageTag = "und",
                name = mockedQtspName
            )
        )
        verify(delegate).onSigningCompleted(expectedRecord)
        verifyNoMoreInteractions(delegate)
    }

    // Case 4:
    // The record contains a nonblank localized name different from the selected QTSP name.
    // Expected: its language tag and exact text, including surrounding spaces, take precedence.
    @Test
    fun `Given a recorded service name, When notified, Then its original name and language are retained`() {
        // Arrange
        val record = mockedRecord.copy(
            serviceName = RqesSigningRecord.LocalizedName(
                languageTag = "fr-CA",
                name = "  Service de signature  "
            )
        )

        // Act
        signingLogger.onSigningCompleted(record)

        // Assert
        verify(delegate).onSigningCompleted(record)
        verifyNoMoreInteractions(delegate)
    }

    // Case 5:
    // The record contains an empty or whitespace-only service name.
    // Expected: each callback uses the captured QTSP name with an undetermined language.
    @Test
    fun `Given blank recorded service names, When notified, Then the selected QTSP name is used`() {
        // Arrange
        val records = listOf("", "   ").map { name ->
            mockedRecord.copy(
                serviceName = RqesSigningRecord.LocalizedName(languageTag = "fr", name = name)
            )
        }

        // Act
        records.forEach(signingLogger::onSigningCompleted)

        // Assert
        val expectedRecord = mockedRecord.copy(
            serviceName = RqesSigningRecord.LocalizedName(
                languageTag = "und",
                name = mockedQtspName
            )
        )
        verify(delegate, times(records.size)).onSigningCompleted(expectedRecord)
        verifyNoMoreInteractions(delegate)
    }

    // Case 6:
    // Neither the record nor the selected QTSP provides a usable name.
    // Expected: every input record is forwarded with an absent service name.
    @Test
    fun `Given no usable service name, When notified, Then no name or language is invented`() {
        // Arrange
        val records = listOf(
            null,
            RqesSigningRecord.LocalizedName(languageTag = "en", name = ""),
            RqesSigningRecord.LocalizedName(languageTag = "en", name = "   ")
        ).map { name -> mockedRecord.copy(serviceName = name) }
        val qtspNames = listOf("", "   ")

        // Act
        qtspNames.forEach { qtspName ->
            val logger = QtspSigningLogger(delegate = delegate, qtspName = qtspName)
            records.forEach(logger::onSigningCompleted)
        }

        // Assert
        verify(delegate, times(qtspNames.size * records.size)).onSigningCompleted(mockedRecord)
        verifyNoMoreInteractions(delegate)
    }

    // Case 7:
    // Two adapters are configured with different QTSP names and delegates.
    // Expected: a later callback to the first adapter still uses its original QTSP name and delegate.
    @Test
    fun `Given two service adapters, When callbacks arrive, Then each retains its QTSP name and delegate`() {
        // Arrange
        val otherQtspName = "  Another signing service  "
        val otherSigningLogger = QtspSigningLogger(
            delegate = otherDelegate,
            qtspName = otherQtspName
        )

        // Act
        otherSigningLogger.onSigningCompleted(mockedRecord)
        signingLogger.onSigningCompleted(mockedRecord)

        // Assert
        verify(delegate).onSigningCompleted(
            mockedRecord.copy(
                serviceName = RqesSigningRecord.LocalizedName(
                    languageTag = "und",
                    name = mockedQtspName
                )
            )
        )
        verify(otherDelegate).onSigningCompleted(
            mockedRecord.copy(
                serviceName = RqesSigningRecord.LocalizedName(
                    languageTag = "und",
                    name = otherQtspName
                )
            )
        )
        verifyNoMoreInteractions(delegate, otherDelegate)
    }

    // Case 8:
    // The adapter receives successive completed and failed records.
    // Expected: both callbacks reach the delegate once in their original order.
    @Test
    fun `Given successive signing attempts, When notified, Then each outcome reaches the delegate in order`() {
        // Arrange
        val records = listOf(
            mockedRecord,
            mockedRecord.copy(outcome = RqesSigningRecord.Outcome.Failed(reason = null))
        )

        // Act
        records.forEach(signingLogger::onSigningCompleted)

        // Assert
        val recordCaptor = argumentCaptor<RqesSigningRecord>()
        verify(delegate, times(records.size)).onSigningCompleted(recordCaptor.capture())
        assertEquals(records.map { it.outcome }, recordCaptor.allValues.map { it.outcome })
        assertEquals(records.map { it.documents }, recordCaptor.allValues.map { it.documents })
        verifyNoMoreInteractions(delegate)
    }

    //endregion

    //region mock data

    private val mockedRecord = RqesSigningRecord(
        outcome = RqesSigningRecord.Outcome.Completed,
        certificateSerialNumber = "00A1",
        documents = listOf(
            RqesSigningRecord.SignedDocument(
                label = "contract.pdf",
                dtbsr = "AQIDBA==",
                sizeBytes = 2048L
            ),
            RqesSigningRecord.SignedDocument(
                label = "contract.pdf",
                dtbsr = null,
                sizeBytes = null
            )
        ),
        serviceName = null
    )

    //endregion
}