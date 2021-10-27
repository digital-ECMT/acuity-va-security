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

/**
 *
 * @author Glen
 */
public interface AcuityObjectIdentityWithPermission extends AcuityObjectIdentity {

    interface PermissionInfo {
        Integer getViewPermissionMask();

        Integer getRolePermissionMask();

        boolean hasRolePermission();

        boolean getCanView();
    }

    Integer getViewPermissionMask();

    void setViewPermissionMask(Integer permissionMask);
    
    Integer getRolePermissionMask();

    void setRolePermissionMask(Integer permissionMask);

    boolean hasRolePermission();
    
    void setCanView(boolean canView);
    
    boolean getCanView();    
}
