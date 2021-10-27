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

/*global it, beforeEach, afterEach, describe, expect, inject, module, spyOn, $q */
"use strict";

describe('GIVEN moduleTemplateController', function () {
    var controller;
    var scope;
    var timeout;

    beforeEach(module('acuityApp', 'acuityApp.templateControllers'));

    beforeEach(inject(function ($rootScope, $controller, $timeout) {
        scope = $rootScope.$new();
        timeout = $timeout;


        controller = $controller('moduleTemplateController', {
            $scope: scope,
            $timeout:timeout
        });
    }));

    describe('WHEN showSidebar is called', function () {
        it('THEN scope.UI.showSidebar == true', inject(function () {
            scope.UI.showSidebar = false;
            scope.showSidebar();
            expect(scope.UI.showSidebar).toBe(true);
        }));
    });

    describe('WHEN hideSidebar is called', function () {
        it('THEN scope.UI.showSidebar == false', inject(function () {
            scope.hideSidebar();
            expect(scope.UI.showSidebar).toBe(false);
        }));
    });

    describe('WHEN openPreview is called', function () {
        it('THEN scope.UI.sidebarRunning is set to true while the timer is running, and false when the timer finishes', function () {
            scope.openPreview();
            expect(scope.UI.timerRunning).toBe(true);

            timeout.flush();
            expect(scope.UI.timerRunning).toBe(false);
        });

        it('THEN the sidebar is opened in preview mode', function () {
            scope.hideSidebar();
            expect(scope.UI.showSidebar).toBe(false);

            scope.openPreview();
            timeout.flush();

            expect(scope.UI.previewSidebar).toBe(true);
            expect(scope.UI.showSidebar).toBe(true);
        });

        it('THEN preview is not true if the sidebar was already open, and the sidebar is left open', function () {
            scope.showSidebar();
            expect(scope.UI.showSidebar).toBe(true);

            scope.openPreview();
            timeout.flush();

            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.showSidebar).toBe(true);
        });
    });

    describe('WHEN closePreview is called', function () {
        it('THEN the sidebar is closed if preview mode was true, and preview is set to false.', function () {
            scope.hideSidebar();
            expect(scope.UI.showSidebar).toBe(false);

            scope.openPreview();
            timeout.flush();

            expect(scope.UI.previewSidebar).toBe(true);
            expect(scope.UI.showSidebar).toBe(true);

            scope.closePreview();

            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.showSidebar).toBe(false);

        });

        it('THEN the sidebar is not closed if preview mode was false.', function () {
            scope.showSidebar();
            expect(scope.UI.showSidebar).toBe(true);

            scope.openPreview();
            timeout.flush();

            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.showSidebar).toBe(true);

            scope.closePreview();

            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.showSidebar).toBe(true);
        });
    });

    describe('WHEN cancelPreview is called', function () {
        it('THEN nothing happens if openPreview is not called', function () {
            scope.hideSidebar();
            expect(scope.UI.showSidebar).toBe(false);
            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(false);

            scope.cancelPreview();

            expect(scope.UI.showSidebar).toBe(false);
            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(false);
        });

        it('THEN nothing happens if openPreview is was called but the timer finished', function () {
            scope.hideSidebar();
            scope.closePreview();
            expect(scope.UI.showSidebar).toBe(false);
            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(false);

            scope.openPreview();
            timeout.flush();
            scope.cancelPreview();

            expect(scope.UI.showSidebar).toBe(true);
            expect(scope.UI.previewSidebar).toBe(true);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(false);
        });

        it('THEN the preview is cancelled if openPreview has not finished', function () {
            scope.hideSidebar();
            scope.closePreview();
            expect(scope.UI.showSidebar).toBe(false);
            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(false);

            scope.openPreview();
            scope.cancelPreview();


            expect(scope.UI.showSidebar).toBe(false);
            expect(scope.UI.previewSidebar).toBe(false);
            expect(scope.UI.timerRunning).toBe(false);
            expect(scope.UI.timerCancelled).toBe(true);
        });
    });

    describe('WHEN the sidebar is resized with the test method', function () {
        it('THEN the sidebar size is set accordingly', function () {
            scope.setSidebarSize(35);
            expect(scope.UI.sidebarPosition).toEqual(35);

            scope.setSidebarSize(120);
            expect(scope.UI.sidebarPosition).toEqual(120);

            scope.setSidebarSize(1800);
            expect(scope.UI.sidebarPosition).toEqual(1800);
        });
    });

    describe('WHEN isSmSidebar is assessed', function () {
        it('THEN method returns false if sidebar is too small', function () {
            scope.setSidebarSize(scope.UI.minSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(false);
        });

        it('THEN returns false if sidebar is too large', function () {
            scope.setSidebarSize(scope.UI.smSidebarWidth + 2);
            expect(scope.isSmSidebar()).toBe(false);
        });

        it('THEN returns true if sidebar is within range', function () {
            scope.setSidebarSize(scope.UI.smSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(true);
        });

        it('THEN returns true if sidebar width is exactly on the upper limit', function () {
            scope.setSidebarSize(scope.UI.smSidebarWidth);
            expect(scope.isSmSidebar()).toBe(true);
        });

        it('THEN returns false if sidebar width is exactly on the lower limit', function () {
            scope.setSidebarSize(scope.UI.minSidebarWidth);
            expect(scope.isSmSidebar()).toBe(false);
        });
    });

    describe('WHEN isMdSidebar is assessed', function () {
        it('THEN method returns false if sidebar is too small', function () {
            scope.setSidebarSize(scope.UI.smSidebarWidth - 2);
            expect(scope.isMdSidebar()).toBe(false);
        });

        it('THEN returns false if sidebar is too large', function () {
            scope.setSidebarSize(scope.UI.mdSidebarWidth + 2);
            expect(scope.isMdSidebar()).toBe(false);
        });

        it('THEN returns true if sidebar is within range', function () {
            scope.setSidebarSize(scope.UI.mdSidebarWidth - 2);
            expect(scope.isMdSidebar()).toBe(true);
        });

        it('THEN returns true if sidebar width is exactly on the upper limit', function () {
            scope.setSidebarSize(scope.UI.mdSidebarWidth);
            expect(scope.isMdSidebar()).toBe(true);
        });

        it('THEN returns false if sidebar width is exactly on the lower limit', function () {
            scope.setSidebarSize(scope.UI.smSidebarWidth);
            expect(scope.isMdSidebar()).toBe(false);
        });
    });

    describe('WHEN isLgSidebar is assessed', function () {
        it('THEN method returns false if sidebar is too small', function () {
            scope.setSidebarSize(scope.UI.mdSidebarWidth - 2);
            expect(scope.isLgSidebar()).toBe(false);
        });

        it('THEN returns false if sidebar is too large', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth + 2);
            expect(scope.isLgSidebar()).toBe(false);
        });

        it('THEN returns true if sidebar is within range', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth - 2);
            expect(scope.isLgSidebar()).toBe(true);
        });

        it('THEN returns true if sidebar width is exactly on the upper limit', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth);
            expect(scope.isLgSidebar()).toBe(true);
        });

        it('THEN returns false if sidebar width is exactly on the lower limit', function () {
            scope.setSidebarSize(scope.UI.mdSidebarWidth);
            expect(scope.isLgSidebar()).toBe(false);
        });
    });

    describe('WHEN isXlSidebar is assessed', function () {
        it('THEN method returns false if sidebar is too small', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth - 2);
            expect(scope.isXlSidebar()).toBe(false);
        });

        it('THEN returns true if sidebar is above large size', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth + 2);
            expect(scope.isXlSidebar()).toBe(true);
        });

        it('THEN returns false if sidebar width is exactly on the lower limit', function () {
            scope.setSidebarSize(scope.UI.lgSidebarWidth);
            expect(scope.isXlSidebar()).toBe(false);
        });
    });

    describe('WHEN the size of the sidebar is assessed', function () {
        it('THEN only one size can be present at any time', function () {
            scope.setSidebarSize(scope.UI.minSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(false);
            expect(scope.isMdSidebar()).toBe(false);
            expect(scope.isLgSidebar()).toBe(false);
            expect(scope.isXlSidebar()).toBe(false);

            scope.setSidebarSize(scope.UI.smSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(true);
            expect(scope.isMdSidebar()).toBe(false);
            expect(scope.isLgSidebar()).toBe(false);
            expect(scope.isXlSidebar()).toBe(false);

            scope.setSidebarSize(scope.UI.mdSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(false);
            expect(scope.isMdSidebar()).toBe(true);
            expect(scope.isLgSidebar()).toBe(false);
            expect(scope.isXlSidebar()).toBe(false);

            scope.setSidebarSize(scope.UI.lgSidebarWidth - 2);
            expect(scope.isSmSidebar()).toBe(false);
            expect(scope.isMdSidebar()).toBe(false);
            expect(scope.isLgSidebar()).toBe(true);
            expect(scope.isXlSidebar()).toBe(false);

            scope.setSidebarSize(scope.UI.lgSidebarWidth + 2);
            expect(scope.isSmSidebar()).toBe(false);
            expect(scope.isMdSidebar()).toBe(false);
            expect(scope.isLgSidebar()).toBe(false);
            expect(scope.isXlSidebar()).toBe(true);
        });
    });
});
