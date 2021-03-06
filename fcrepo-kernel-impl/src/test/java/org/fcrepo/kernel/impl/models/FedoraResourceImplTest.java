/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.kernel.impl.models;

import org.fcrepo.kernel.api.identifiers.FedoraId;
import org.fcrepo.kernel.api.models.FedoraResource;
import org.fcrepo.kernel.api.models.ResourceFactory;
import org.fcrepo.kernel.api.models.TimeMap;
import org.fcrepo.kernel.api.services.VersionService;
import org.fcrepo.persistence.api.PersistentStorageSessionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;

import static org.fcrepo.kernel.api.FedoraTypes.FCR_VERSIONS;
import static org.fcrepo.kernel.api.FedoraTypes.FEDORA_ID_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FedoraResourceImplTest {

    @Mock
    private TimeMap timeMap;

    @Mock
    private PersistentStorageSessionManager sessionManager;

    @Mock
    private ResourceFactory resourceFactory;

    private static final String ID = "info:fedora/test";

    private static final FedoraId FEDORA_ID = FedoraId.create(ID);

    @Test
    public void findMementoWhenOnlyOneAndBeforeSearch() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos("20200309172117");
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertEquals(FEDORA_ID_PREFIX + "/0", match.getId());
    }

    @Test
    public void findClosestMementoWhenMultiple() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos("20200309172117", "20200309172118", "20200309172119");
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertEquals(FEDORA_ID_PREFIX + "/1", match.getId());
    }

    @Test
    public void findClosestMementoWhenMultipleNoneExact() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos("20200309172116", "20200309172117", "20200309172119");
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertEquals(FEDORA_ID_PREFIX + "/1", match.getId());
    }

    @Test
    public void findClosestMementoMultipleSameSecond() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos("20200309172117", "20200309172117", "20200309172117");
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertEquals(FEDORA_ID_PREFIX + "/2", match.getId());
    }

    @Test
    public void findMementoWhenNonBeforeSearch() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos("20200309172119", "20200309172120", "20200309172121");
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertEquals(FEDORA_ID_PREFIX + "/0", match.getId());
    }

    @Test
    public void findNoMementoWhenThereAreNone() {
        final var resource = resourceWithMockedTimeMap();
        expectMementos();
        final var match = resource.findMementoByDatetime(instant("20200309172118"));
        assertNull("Should not find a memento", match);
    }

    private void expectMementos(final String... instants) {
        final var mementos = new ArrayList<FedoraResource>(instants.length);
        for (int i = 0; i < instants.length; i++) {
            mementos.add(memento(String.valueOf(i), instant(instants[i])));
        }
        when(timeMap.getChildren()).thenReturn(mementos.stream());
    }

    private FedoraResource resourceWithMockedTimeMap() {
        final var resource = spy(new FedoraResourceImpl(FEDORA_ID, null, sessionManager, resourceFactory));
        doReturn(timeMap).when(resource).getTimeMap();
        return resource;
    }

    private FedoraResource memento(final String id, final Instant instant) {
        final String mementoTime = VersionService.MEMENTO_LABEL_FORMATTER.format(instant);
        final FedoraId fedoraID = FedoraId.create(id, FCR_VERSIONS, mementoTime);
        final var memento = new FedoraResourceImpl(fedoraID, null, sessionManager, resourceFactory);
        memento.setIsMemento(true);
        memento.setMementoDatetime(instant);
        return memento;
    }

    private Instant instant(final String timestamp) {
        return Instant.from(VersionService.MEMENTO_LABEL_FORMATTER.parse(timestamp));
    }

}
