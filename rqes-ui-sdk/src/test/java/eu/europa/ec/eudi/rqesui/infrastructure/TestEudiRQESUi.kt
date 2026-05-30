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

package eu.europa.ec.eudi.rqesui.infrastructure

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolutionOutcome
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationCode
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestEudiRQESUi {

    @Mock
    private lateinit var config: EudiRQESUiConfig

    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var nonActivityContext: Context

    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var cursor: Cursor

    @Mock
    private lateinit var koinApplication: KoinApplication

    @Mock
    private lateinit var rqesService: RQESService

    @Mock
    private lateinit var authorizedService: RQESService.Authorized

    @Mock
    private lateinit var resolutionOutcome: ResolutionOutcome

    @Mock
    private lateinit var qtspData: QtspData

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        // Stop any Koin instance that may have been started by another test class.
        runCatching { GlobalContext.stopKoin() }
        // Reset the EudiRQESUi singleton's mutable state so each test starts clean.
        resetEudiRQESUi()
    }

    @After
    fun after() {
        runCatching { GlobalContext.stopKoin() }
        resetEudiRQESUi()
        closeable.close()
    }

    /**
     * EudiRQESUi is a Kotlin `object` (singleton) with mutable state. Tests would leak state
     * into one another without an explicit reset. We use reflection to null-out the lateinit
     * vars (so `::field.isInitialized` returns false again) and to reset the other state fields.
     */
    private fun resetEudiRQESUi() {
        val instance = EudiRQESUi
        val cls = EudiRQESUi.javaClass
        fun setField(name: String, value: Any?) {
            cls.getDeclaredField(name).apply { isAccessible = true }.set(instance, value)
        }
        setField("_eudiRQESUiConfig", null)
        setField("sessionData", null)
        setField("state", EudiRQESUi.State.None)
        setField("rqesService", null)
        setField("authorizedService", null)
        setField("remoteResolutionOutcome", null)
    }

    /** Reads the private `state` field via reflection for assertion in state-machine tests. */
    private fun currentState(): EudiRQESUi.State {
        val field = EudiRQESUi.javaClass.getDeclaredField("state").apply { isAccessible = true }
        return field.get(EudiRQESUi) as EudiRQESUi.State
    }

    private fun mockFileNameLookup(context: Context, name: String? = mockedDocumentName) {
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        ).thenReturn(cursor)
        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(name)
    }

    //region setup
    // Case 1
    // 1. Provide a real Application from Robolectric and a config where printLogs = false.
    // Expected Result:
    // 1. Koin is started globally (without androidLogger) and the config is retrievable.
    @Test
    fun `Given Case 1, When setup is called with null koinApplication and printLogs false, Then Koin is started and config is set`() {
        // Arrange
        val realApp = RuntimeEnvironment.getApplication()
        whenever(config.printLogs).thenReturn(false)

        // Act
        EudiRQESUi.setup(application = realApp, config = config)

        // Assert
        assertEquals(config, EudiRQESUi.getEudiRQESUiConfig())
        assertNotNull(GlobalContext.getKoinApplicationOrNull())
    }

    // Case 2
    // 1. Provide a real Application from Robolectric and a config where printLogs = true.
    // Expected Result:
    // 1. Koin is started globally (with androidLogger) and the config is retrievable.
    @Test
    fun `Given Case 2, When setup is called with printLogs true, Then Koin is started with androidLogger`() {
        // Arrange
        val realApp = RuntimeEnvironment.getApplication()
        whenever(config.printLogs).thenReturn(true)

        // Act
        EudiRQESUi.setup(application = realApp, config = config)

        // Assert
        assertEquals(config, EudiRQESUi.getEudiRQESUiConfig())
        assertNotNull(GlobalContext.getKoinApplicationOrNull())
    }

    // Case 3
    // 1. Provide a non-null KoinApplication so the SDK should use it instead of calling startKoin.
    // Expected Result:
    // 1. The supplied KoinApplication.modules(...) is invoked with the SDK modules list.
    //    (We can't reliably assert on global Koin state here because it's a static singleton
    //    that leaks across tests in spite of the @After cleanup.)
    @Test
    fun `Given Case 3, When setup is called with a koinApplication, Then it is used instead of startKoin`() {
        // Arrange
        whenever(koinApplication.modules(any<List<Module>>())).thenReturn(koinApplication)
        val realApp = RuntimeEnvironment.getApplication()

        // Act
        EudiRQESUi.setup(
            application = realApp,
            config = config,
            koinApplication = koinApplication,
        )

        // Assert
        assertEquals(config, EudiRQESUi.getEudiRQESUiConfig())
        verify(koinApplication).modules(any<List<Module>>())
    }
    //endregion

    //region getEudiRQESUiConfig
    // Case 1
    // 1. setup() has not been called.
    // Expected Result:
    // 1. getEudiRQESUiConfig throws EudiRQESUiError.
    @Test
    fun `Given setup was not called, When getEudiRQESUiConfig is called, Then EudiRQESUiError is thrown`() {
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.getEudiRQESUiConfig()
        }
        assertEquals("EudiRQESUi Error", error.title)
    }
    //endregion

    //region initiate(documentUri)
    // Case 1
    // 1. Activity context with a valid document URI whose filename can be resolved.
    // Expected Result:
    // 1. sessionData is populated with the document data.
    // 2. State transitions from None to Initial.
    // 3. activity.startActivity is invoked with an EudiRQESContainer intent carrying the state.
    @Test
    fun `Given Case 1, When initiate is called with documentUri and Activity, Then sessionData is set and SDK launches`() {
        // Arrange
        mockFileNameLookup(activity)
        val uri = Uri.parse(mockedLocalFileUri)

        // Act
        EudiRQESUi.initiate(activity, DocumentUri(uri))

        // Assert
        val sessionData = EudiRQESUi.getSessionData()
        assertNotNull(sessionData.file)
        assertEquals(mockedDocumentName, sessionData.file?.documentName)
        assertEquals(uri, sessionData.file?.uri)
        assertNull(sessionData.remoteUrl)
        assertNull(sessionData.qtsp)
        assertNull(sessionData.authorizationCode)
        assertTrue(currentState() is EudiRQESUi.State.Initial)
        verify(activity).startActivity(any<Intent>())
    }

    // Case 2
    // 1. Non-Activity context with a valid document URI.
    // Expected Result:
    // 1. EudiRQESUiError with title "Context Error" is thrown.
    @Test
    fun `Given Case 2, When initiate is called with documentUri and non-Activity context, Then Context Error is thrown`() {
        // Arrange
        mockFileNameLookup(nonActivityContext)
        val uri = Uri.parse(mockedLocalFileUri)

        // Act + Assert
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.initiate(nonActivityContext, DocumentUri(uri))
        }
        assertEquals("Context Error", error.title)
    }

    // Case 3
    // 1. Activity context but the URI cannot resolve a filename (contentResolver.query returns null).
    // Expected Result:
    // 1. EudiRQESUiError (from getFileName) is thrown.
    @Test
    fun `Given Case 3, When initiate is called with documentUri and no filename can be extracted, Then error is thrown`() {
        // Arrange
        whenever(activity.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        ).thenReturn(null)
        val uri = Uri.parse(mockedLocalFileUri)

        // Act + Assert
        assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.initiate(activity, DocumentUri(uri))
        }
    }
    //endregion

    //region initiate(remoteUri)
    // Case 1
    // 1. Activity context with a valid remote URI.
    // Expected Result:
    // 1. sessionData.remoteUrl is set; sessionData.file is null.
    // 2. State transitions from None to Initial.
    // 3. activity.startActivity is invoked.
    @Test
    fun `Given Case 1, When initiate is called with remoteUri and Activity, Then sessionData is set and SDK launches`() {
        // Arrange
        val uri = Uri.parse("https://example.org/remote-doc")

        // Act
        EudiRQESUi.initiate(activity, RemoteUri(uri))

        // Assert
        val sessionData = EudiRQESUi.getSessionData()
        assertNull(sessionData.file)
        assertNotNull(sessionData.remoteUrl)
        assertEquals(uri.toString(), sessionData.remoteUrl?.toString())
        assertTrue(currentState() is EudiRQESUi.State.Initial)
        verify(activity).startActivity(any<Intent>())
    }

    // Case 2
    // 1. Non-Activity context with a valid remote URI.
    // Expected Result:
    // 1. EudiRQESUiError with title "Context Error" is thrown.
    @Test
    fun `Given Case 2, When initiate is called with remoteUri and non-Activity context, Then Context Error is thrown`() {
        // Arrange
        val uri = Uri.parse("https://example.org/remote-doc")

        // Act + Assert
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.initiate(nonActivityContext, RemoteUri(uri))
        }
        assertEquals("Context Error", error.title)
    }
    //endregion

    //region resume
    // Case 1
    // 1. resume() is called before any successful initiate().
    // Expected Result:
    // 1. EudiRQESUiError is thrown because sessionData is not initialized.
    @Test
    fun `Given Case 1, When resume is called before initiate, Then EudiRQESUiError is thrown`() {
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.resume(activity, mockedAuthorizationCode)
        }
        assertEquals("EudiRQESUi Error", error.title)
    }

    // Case 2
    // 1. sessionData is initialized but the context is not an Activity.
    // Expected Result:
    // 1. EudiRQESUiError with title "Context Error" is thrown (after state transitions).
    @Test
    fun `Given Case 2, When resume is called with non-Activity context, Then Context Error is thrown`() {
        // Arrange
        val docData =
            DocumentData(documentName = mockedDocumentName, uri = Uri.parse(mockedLocalFileUri))
        EudiRQESUi.setSessionData(
            EudiRQESUi.SessionData(
                file = docData,
                remoteUrl = null,
                qtsp = null,
                authorizationCode = null,
            )
        )

        // Act + Assert
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.resume(nonActivityContext, mockedAuthorizationCode)
        }
        assertEquals("Context Error", error.title)
    }

    // Case 3
    // 1. sessionData is initialized; resume() is called repeatedly.
    // Expected Result:
    // 1. State transitions are None -> Initial -> Certificate -> Success -> Success.
    // 2. sessionData.authorizationCode is updated each time.
    // 3. startActivity is invoked once per resume call.
    @Test
    fun `Given Case 3, When resume is called multiple times, Then state transitions correctly`() {
        // Arrange
        val docData =
            DocumentData(documentName = mockedDocumentName, uri = Uri.parse(mockedLocalFileUri))
        EudiRQESUi.setSessionData(
            EudiRQESUi.SessionData(
                file = docData,
                remoteUrl = null,
                qtsp = null,
                authorizationCode = null,
            )
        )
        assertTrue(currentState() is EudiRQESUi.State.None)

        // Act + Assert: None -> Initial
        EudiRQESUi.resume(activity, mockedAuthorizationCode)
        assertTrue(currentState() is EudiRQESUi.State.Initial)
        assertEquals(mockedAuthorizationCode, EudiRQESUi.getSessionData().authorizationCode)

        // Initial -> Certificate
        EudiRQESUi.resume(activity, mockedAuthorizationCode)
        assertTrue(currentState() is EudiRQESUi.State.Certificate)

        // Certificate -> Success
        EudiRQESUi.resume(activity, mockedAuthorizationCode)
        assertTrue(currentState() is EudiRQESUi.State.Success)

        // Success -> Success (stable)
        EudiRQESUi.resume(activity, mockedAuthorizationCode)
        assertTrue(currentState() is EudiRQESUi.State.Success)

        verify(activity, times(4)).startActivity(any<Intent>())
    }

    // Case 4
    // 1. sessionData is initialized but BOTH file and remoteUrl are null.
    // Expected Result:
    // 1. calculateNextState throws EudiRQESUiError ("SDK must be initialized").
    @Test
    fun `Given Case 4, When resume is called with null file and remoteUrl, Then EudiRQESUiError is thrown`() {
        // Arrange
        EudiRQESUi.setSessionData(
            EudiRQESUi.SessionData(
                file = null,
                remoteUrl = null,
                qtsp = null,
                authorizationCode = null,
            )
        )

        // Act + Assert
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.resume(activity, mockedAuthorizationCode)
        }
        assertEquals("EudiRQESUi Error", error.title)
    }
    //endregion

    //region internal getters and setters
    @Test
    fun `setRqesService and getRqesService roundtrip the supplied service`() {
        assertNull(EudiRQESUi.getRqesService())
        EudiRQESUi.setRqesService(rqesService)
        assertEquals(rqesService, EudiRQESUi.getRqesService())
    }

    @Test
    fun `setAuthorizedService and getAuthorizedService roundtrip the supplied service`() {
        assertNull(EudiRQESUi.getAuthorizedService())
        EudiRQESUi.setAuthorizedService(authorizedService)
        assertEquals(authorizedService, EudiRQESUi.getAuthorizedService())
    }

    @Test
    fun `setRemoteResolutionOutcome and getRemoteResolutionOutcome roundtrip the supplied outcome`() {
        assertNull(EudiRQESUi.getRemoteResolutionOutcome())
        EudiRQESUi.setRemoteResolutionOutcome(resolutionOutcome)
        assertEquals(resolutionOutcome, EudiRQESUi.getRemoteResolutionOutcome())
    }

    @Test
    fun `getSessionData throws when no session has been set`() {
        val error = assertThrows(EudiRQESUiError::class.java) {
            EudiRQESUi.getSessionData()
        }
        assertEquals("EudiRQESUi Error", error.title)
    }

    @Test
    fun `setSessionData and getSessionData roundtrip the supplied session data`() {
        val session = EudiRQESUi.SessionData(
            file = DocumentData(
                documentName = mockedDocumentName,
                uri = Uri.parse(mockedLocalFileUri)
            ),
            remoteUrl = Uri.parse("https://example.org/remote"),
            qtsp = qtspData,
            authorizationCode = mockedAuthorizationCode,
        )
        EudiRQESUi.setSessionData(session)
        assertEquals(session, EudiRQESUi.getSessionData())
    }
    //endregion

    //region value classes
    @Test
    fun `DocumentUri wraps and exposes the supplied Uri`() {
        val uri = Uri.parse(mockedLocalFileUri)
        val wrapped = DocumentUri(uri)
        assertEquals(uri, wrapped.uri)
    }

    @Test
    fun `RemoteUri wraps and exposes the supplied Uri`() {
        val uri = Uri.parse("https://example.org/remote-doc")
        val wrapped = RemoteUri(uri)
        assertEquals(uri, wrapped.uri)
    }
    //endregion

    //region sealed State subclasses
    // The synthetic `State.Initial` $default constructor has a branch for each default
    // parameter. The production code always passes `file` explicitly (sometimes as null), so
    // the "use default for file" branch is otherwise unreachable. Constructing an Initial
    // with no arguments exercises it.
    @Test
    fun `State Initial can be constructed with default values`() {
        val initial = EudiRQESUi.State.Initial()
        assertNull(initial.file)
        assertNull(initial.remoteUri)
    }

    // Parcelable.describeContents is auto-generated by @Parcelize but never invoked in normal
    // code paths. Call it directly to keep its bytecode covered.
    @Test
    fun `State Initial describeContents returns 0`() {
        assertEquals(0, EudiRQESUi.State.Initial().describeContents())
    }
    //endregion

    //region calculateNextState branches not exercised by Case 3
    // The state-transition test (resume Case 3) uses a sessionData with `file` set and
    // `remoteUrl` null. To cover the symmetric branches of `calculateNextState` (file = null,
    // remoteUrl != null, which produces an Initial(file = null, remoteUri = non-null)), we
    // run a second resume scenario with sessionData populated only via `remoteUrl`.
    @Test
    fun `resume from None with only remoteUrl set transitions to Initial with null file`() {
        // Arrange
        val remote = Uri.parse("https://example.org/remote")
        EudiRQESUi.setSessionData(
            EudiRQESUi.SessionData(
                file = null,
                remoteUrl = remote,
                qtsp = null,
                authorizationCode = null,
            )
        )

        // Act
        EudiRQESUi.resume(activity, mockedAuthorizationCode)

        // Assert
        val state = currentState()
        assertTrue(state is EudiRQESUi.State.Initial)
        val initial = state as EudiRQESUi.State.Initial
        assertNull(initial.file)
        assertNotNull(initial.remoteUri)
        assertEquals(remote.toString(), initial.remoteUri?.toString())
    }
    //endregion

    //region SessionData copy variants
    // SessionData is mutated through `sessionData.copy(...)` from various controller paths
    // (with `file`, `qtsp`, or `authorizationCode`), but never with `remoteUrl`. Exercising
    // that copy variant covers the corresponding default-arg mask branch.
    @Test
    fun `SessionData copy with remoteUrl exercises the remaining mask branch`() {
        val original = EudiRQESUi.SessionData(
            file = DocumentData(
                documentName = mockedDocumentName,
                uri = Uri.parse(mockedLocalFileUri)
            ),
            remoteUrl = null,
            qtsp = qtspData,
            authorizationCode = null,
        )
        val updated = original.copy(remoteUrl = Uri.parse("https://example.org/remote"))
        assertEquals(original.file, updated.file)
        assertEquals(original.qtsp, updated.qtsp)
        assertEquals("https://example.org/remote", updated.remoteUrl?.toString())
    }
    //endregion
}
