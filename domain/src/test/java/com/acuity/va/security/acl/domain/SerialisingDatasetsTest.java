/*
 * Copyright 2021 The University of Manchester
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

package com.acuity.va.security.acl.domain;

import java.io.IOException;

import org.apache.commons.lang3.SerializationUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.*;

/**
 *
 * @author Glen
 */
public class SerialisingDatasetsTest {

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        AcuityDataset acuityDataset1 = new AcuityDataset(1L, "1sd");
        AcuityDataset acuityDataset2 = new AcuityDataset(2L, "2sd");
        AcuityDataset acuityDataset3 = new AcuityDataset(3L, "3sd");

        Datasets datasets = new Datasets(acuityDataset3, acuityDataset1, acuityDataset2);
        System.out.println(datasets);

        byte[] serialize = SerializationUtils.serialize(datasets);

        Datasets deserialize = SerializationUtils.deserialize(serialize);

        System.out.println(deserialize);

        assertThat(deserialize).isEqualTo(datasets);
    }

    @Test
    public void shouldSerializeAndDeserialize1() throws IOException {
        AcuityDataset acuityDataset1 = new AcuityDataset(1L, "1sd");

        System.out.println(acuityDataset1);

        byte[] serialize = SerializationUtils.serialize(acuityDataset1);

        AcuityDataset deserialize = SerializationUtils.deserialize(serialize);

        System.out.println(deserialize);

        assertThat(deserialize).isEqualTo(acuityDataset1);
    }

    @Test
    public void shouldGetDatasetsLoggingObject() throws IOException {
        AcuityDataset acuityDataset1 = new AcuityDataset(1L, "1sd");
        AcuityDataset acuityDataset2 = new AcuityDataset(2L, "2sd");
        AcuityDataset acuityDataset3 = new AcuityDataset(3L, "3sd");

        Datasets datasets = new Datasets(acuityDataset3, acuityDataset1, acuityDataset2);

        System.out.println(datasets.getDatasetsLoggingObject());

        assertThat(datasets.getDatasetsLoggingObject().getType()).isEqualTo(AcuityDataset.class.getSimpleName());
        assertThat(datasets.getDatasetsLoggingObject().getIds()).containsOnly(1L, 2L, 3L);
    }
}
