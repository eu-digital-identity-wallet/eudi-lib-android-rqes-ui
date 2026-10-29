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

package eu.europa.ec.eudi.rqesui.presentation.ui.container

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TestEudiRQESContainer {

    @Test
    fun `Given the library manifest, When the EudiRQESContainer declaration is read, Then it is not exported`() {
        // Arrange
        val manifest = File("src/main/AndroidManifest.xml")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifest)

        // Act
        val activities = document.getElementsByTagName("activity")
        val container = (0 until activities.length)
            .map { activities.item(it) as Element }
            .single { EudiRQESContainer::class.java.name.endsWith(it.getAttribute("android:name")) }

        // Assert
        assertEquals("false", container.getAttribute("android:exported"))
    }
}
