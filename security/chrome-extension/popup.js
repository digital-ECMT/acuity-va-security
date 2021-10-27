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

'use strict';

var domain = "";
function refresh() {

    removeCookies();
    changeTabLocation("");
}

function developerUser() {

    var developer = document.getElementById('developerUser').value;
    if (developer === "") {
        developer = "developer";
        localStorage["savedDev"] = "";
    } else {
        localStorage["savedDev"] = developer;
    }

    removeCookies();
    changeTabLocation("?swappedUser=" + developer + "&swappedRoles=TRAINED_USER,ACL_ADMINISTRATOR,DEVELOPMENT_TEAM");
}

function trainedUser() {

    var trainedUser = document.getElementById('trainedUser').value;
    if (trainedUser === "") {
        trainedUser = "trainedUser";
        localStorage["savedTrained"] = "";
    } else {
        localStorage["savedTrained"] = trainedUser;
    }

    removeCookies();
    changeTabLocation("?swappedUser=" + trainedUser + "&swappedRoles=TRAINED_USER,ACL_ADMINISTRATOR");
}

function unknownUser() {

    var unknownUser = document.getElementById('unknownUser').value;
    if (unknownUser === "") {
        unknownUser = "unknownUser";
    }

    removeCookies();
    changeTabLocation("?swappedUser=" + unknownUser + "&swappedRoles=DEFAULT_ROLE");
}

function changeTabLocation(swappedInformation) {

    chrome.tabs.getSelected(null, function(tab) {
        var url = getLocation(tab.url);
        var port = "";
        var http_protocol = "https";
        // if not port 80 then its local, normally 9090
        // so local its http://localhost:8080
        if (url.port !== 80) {
            port = ":" + url.port;
            http_protocol = "http";
        }

        var newLocation = http_protocol + "://" + url.hostname + port + "/" + swappedInformation;
        console.log(newLocation);
        chrome.tabs.update(tab.id, {url: newLocation});
    });
}

function getLocation(location) {
    var l = document.createElement("a");
    l.href = location;
    return l;
}

function removeCookies() {

    if (chrome.cookies !== undefined) {
        chrome.cookies.getAll({
            'domain': domain
        }, function(cookies) {
            console.log('Number of cookies at ' + domain + ': ' + cookies.length);
            for (var i = 0; i < cookies.length; i++) {
                console.log(cookies[i]);
                chrome.cookies.remove({'url': "http" + (cookies[i].secure ? "s" : "") + "://" + cookies[i].domain + cookies[i].path, 'name': cookies[i].name});
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {

    chrome.tabs.getSelected(null, function(tab) {
        var url = getLocation(tab.url);
        domain = url.hostname;
    });

    // load input values from local storage
    var savedDev = localStorage["savedDev"];
    var savedTrained = localStorage["savedTrained"];
    if (savedDev) {
        document.getElementById('developerUser').value = savedDev;
    }
    if (savedTrained) {
        document.getElementById('trainedUser').value = savedTrained;
    }

    document.getElementById('normally').addEventListener('click', refresh);
    document.getElementById('developer').addEventListener('click', developerUser);
    document.getElementById('trained').addEventListener('click', trainedUser);
    document.getElementById('unknown').addEventListener('click', unknownUser);
});


