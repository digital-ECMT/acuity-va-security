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

import com.acuity.va.security.acl.domain.auditlogger.DatasetsLoggingObject;
import com.google.common.base.Joiner;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.primitives.Longs;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

/**
 * Class representing the list of datasets
 *
 * @author Glen
 */
@Data
@ToString
@EqualsAndHashCode
public class Datasets implements Serializable {

    public static void toAcuityDataset(int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // needs to be Serializable
    class MyComparator implements Comparator<Dataset>, Serializable {

        @Override
        public int compare(Dataset t, Dataset t1) {
            return Longs.compare(t.getId(), t1.getId());
        }
    }
    
    private Set<Dataset> datasets = new TreeSet<>(new MyComparator());

    public Datasets(List<Dataset> datasets) {
        Validate.isTrue(CollectionUtils.isNotEmpty(datasets), "Datasets should not be null or empty");
        
        this.datasets.addAll(datasets);
        
        Validate.isTrue(areDatasetsSameType(), "All datasets should be either all acuity or detect types");
    }

    public Datasets(Dataset... datasets) {
        this(newArrayList(datasets));
    }

    public Datasets(Dataset dataset) {
        this(newArrayList(dataset));
    }
    
    public DatasetsLoggingObject getDatasetsLoggingObject() {
        Set<Long> ids = getIds();
        
        return new DatasetsLoggingObject() {
            @Override
            public Set<Long> getIds() {
                return ids;
            }

            @Override
            public String getType() {
                return datasets.iterator().next().getClass().getSimpleName();
            }            
        };
    }

    public boolean isDetectType() {
        return this.datasets.stream().allMatch(ds -> ds.thisDetectType());
    }

    public boolean isAcuityType() {
        return this.datasets.stream().allMatch(ds -> ds.thisAcuityType());
    }

    private boolean areDatasetsSameType() {
        return this.datasets.stream().map(ds -> ds.getShortNameByType()).distinct().count() == 1;
    }

    public String getShortNameByType() {
        return datasets.iterator().next().getShortNameByType();
    }

    public Set<Long> getIds() {
        return datasets.stream().map(ds -> ds.getId()).collect(toSet());
    }
    
    public List<Long> getIdsList() {
        return datasets.stream().map(ds -> ds.getId()).collect(toList());
    }

    private final String SEPERATOR = "::";
    public String getIdsAsString() {
        return SEPERATOR + Joiner.on(SEPERATOR).join(getIds()) + SEPERATOR;
    }

    public Set<Dataset> getDatasets() {
        return Collections.unmodifiableSet(datasets);
    }

    public List<Dataset> getDatasetsList() {
        return Collections.unmodifiableList(newArrayList(datasets));
    }

    @Deprecated
    public Long getFirstId() {
        return datasets.stream().findFirst().get().getId();
    }

    public static Datasets toDetectDataset(Integer... ids) {

        return toDetectDataset(Arrays.asList(ids).stream().map(i -> new Long(i)).collect(toList()));
    }
    
    public static Datasets toDetectDataset(Long... ids) {

        return toDetectDataset(Arrays.asList(ids));
    }

    public static Datasets toDetectDataset(List<Long> ids) {

        List<Dataset> dss = ids.stream().map(id -> new DetectDataset(id)).collect(toList());

        return new Datasets(dss);
    }

    public static Datasets toAcuityDataset(Long... ids) {

        return toAcuityDataset(Arrays.asList(ids));
    }

    public static Datasets toAcuityDataset(List<Long> ids) {

        List<Dataset> dss = ids.stream().map(id -> new AcuityDataset(id)).collect(toList());

        return new Datasets(dss);
    }
}
