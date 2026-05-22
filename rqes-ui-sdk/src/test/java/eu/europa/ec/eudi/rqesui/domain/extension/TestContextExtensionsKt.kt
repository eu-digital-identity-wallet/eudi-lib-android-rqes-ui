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

package eu.europa.ec.eudi.rqesui.domain.extension

import android.content.Context
import android.content.res.Resources
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithNoMessage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.security.cert.X509Certificate

class TestContextExtensionsKt {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var resources: Resources

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        whenever(context.resources).thenReturn(resources)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getCertificate
    // Case 1
    // 1. Provide a valid X.509 certificate as a stream from the raw resource.
    // Expected Result:
    // 1. The function should return a successful Result containing an X509Certificate.
    @Test
    fun `When getCertificate is called with valid certificate bytes, Then success result with X509Certificate is returned`() {
        // Arrange
        whenever(resources.openRawResource(certRawResId))
            .thenReturn(ByteArrayInputStream(validCertificateBytes))

        // Act
        val result = context.getCertificate(certRawResId)

        // Assert
        assertTrue(result.isSuccess)
        val cert = result.getOrThrow()
        assertNotNull(cert)
        assertTrue(cert is X509Certificate)
    }

    // Case 2
    // 1. The openRawResource call throws an exception with a localized message.
    // Expected Result:
    // 1. The function should return a failure Result with an EudiRQESUiError containing the exception's message.
    @Test
    fun `When getCertificate is called and openRawResource throws with message, Then failure with that message is returned`() {
        // Arrange
        whenever(resources.openRawResource(certRawResId))
            .thenThrow(mockedExceptionWithMessage)

        // Act
        val result = context.getCertificate(certRawResId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
        assertEquals(mockedExceptionWithMessage.message, (exception as EudiRQESUiError).message)
        assertEquals("Certificate Error", exception.title)
    }

    // Case 3
    // 1. The openRawResource call throws an exception without a localized message.
    // Expected Result:
    // 1. The function should return a failure Result with EudiRQESUiError containing the fallback message
    //    referencing the resource id.
    @Test
    fun `When getCertificate is called and openRawResource throws without message, Then failure with fallback message is returned`() {
        // Arrange
        whenever(resources.openRawResource(certRawResId))
            .thenThrow(mockedExceptionWithNoMessage)

        // Act
        val result = context.getCertificate(certRawResId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
        assertEquals("Resource $certRawResId not found", (exception as EudiRQESUiError).message)
        assertEquals("Certificate Error", exception.title)
    }

    // Case 4
    // 1. Provide an invalid byte stream that cannot be parsed as a certificate.
    // Expected Result:
    // 1. The function should return a failure Result with an EudiRQESUiError.
    @Test
    fun `When getCertificate is called with invalid certificate bytes, Then failure result is returned`() {
        // Arrange
        whenever(resources.openRawResource(certRawResId))
            .thenReturn(ByteArrayInputStream("not a certificate".toByteArray()))

        // Act
        val result = context.getCertificate(certRawResId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
    }
    //endregion

    //region mock data
    private val certRawResId = 1234

    // Self-signed test certificate (DER encoded, base64). This is an "Acme Co" sample
    // certificate widely used in open-source tests (e.g., the Go standard library).
    private val validCertificatePem = """
        -----BEGIN CERTIFICATE-----
        MIIBhTCCASugAwIBAgIQIRi6zePL6mKjOipn+dNuaTAKBggqhkjOPQQDAjASMRAw
        DgYDVQQKEwdBY21lIENvMB4XDTE3MTAyMDE5NDMwNloXDTE4MTAyMDE5NDMwNlow
        EjEQMA4GA1UEChMHQWNtZSBDbzBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABD0d
        7VNhbWvZLWPuj/RtHFjvtJBEwOkhbN/BnnE8rnZR8+sbwnc/KhCk3FhnpHZnQz7B
        5aETbbIgmuvewdjvSBSjYzBhMA4GA1UdDwEB/wQEAwICpDATBgNVHSUEDDAKBggr
        BgEFBQcDATAPBgNVHRMBAf8EBTADAQH/MCkGA1UdEQQiMCCCDmxvY2FsaG9zdDo1
        NDUzgg4xMjcuMC4wLjE6NTQ1MzAKBggqhkjOPQQDAgNIADBFAiEA2zpJEPQyz6/l
        Wf86aX6PepsntZv2GYlA5UpabfT2EZICICpJ5h/iI+i341gBmLiAFQOyTDT+/wQc
        6MF9+Yw1Yy0t
        -----END CERTIFICATE-----
    """.trimIndent()

    private val validCertificateBytes by lazy {
        validCertificatePem.toByteArray()
    }
    //endregion
}
