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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.*;

/**
 *
 * @author Glen
 */
public class CreatingDatasetsTest {

    @Test
    public void shouldCreateDetectDatasetUsingUtilMethods() {

        Datasets datasets = Datasets.toDetectDataset(1, 2);

        assertThat(datasets.getFirstId()).isEqualTo(1L);
        assertThat(datasets.getIdsAsString()).isEqualTo("::1::2::");
        assertThat(datasets.getIds()).containsSequence(1L, 2L);
        assertThat(datasets.isDetectType()).isTrue();
        assertThat(datasets.isAcuityType()).isFalse();
        assertThat(datasets.getDatasetsList()).extracting("id").contains(1L, 2L);
        assertThat(datasets.getDatasets()).extracting("id").contains(1L, 2L);
        assertThat(datasets.getShortNameByType()).isEqualTo("detect");
    }

    @Test
    public void shouldCreateAcuityDatasetUsingUtilMethods() {

        Datasets datasets = Datasets.toAcuityDataset(3L, 2L);

        assertThat(datasets.getFirstId()).isEqualTo(2L);
        assertThat(datasets.getIdsAsString()).isEqualTo("::2::3::");
        assertThat(datasets.getIds()).containsSequence(2L, 3L);
        assertThat(datasets.isAcuityType()).isTrue();
        assertThat(datasets.isDetectType()).isFalse();
        assertThat(datasets.getDatasetsList()).extracting("id").contains(2L, 3L);
        assertThat(datasets.getDatasets()).extracting("id").contains(2L, 3L);
        assertThat(datasets.getShortNameByType()).isEqualTo("acuity");
    }
}
