// Run with: K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=html-report.html k6 run load-tests/k6/authenticated-users.js

import {check} from 'k6';
import http from 'k6/http';
import exec from 'k6/execution';
import {userIds as userIdsDevelop} from "./users/develop.js";
import {userIds as userIdsStaging} from "./users/staging.js";
import {userIds as userIdsProduction} from "./users/production.js";

export const options = {
    scenarios: {
        odhack: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                {duration: '10s', target: 1},
                {duration: '1s', target: 1},
            ],
            gracefulRampDown: '0s',
        },
    },
};

const ENV = 'perf';
const BASE_URL = `https://${ENV}-api.onlydust.com/api/v1`;
const ACCESS_TOKEN = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InlvcHBLQzlTTFptT1pBbXhtV2J6NiJ9.eyJpc3MiOiJodHRwczovL3Byb2R1Y3Rpb24tb25seWR1c3QuZXUuYXV0aDAuY29tLyIsInN1YiI6ImdpdGh1Ynw1OTU1MDUiLCJhdWQiOlsiaHR0cHM6Ly9hcGkub25seWR1c3QuY29tL2FwaSIsImh0dHBzOi8vcHJvZHVjdGlvbi1vbmx5ZHVzdC5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNzI4NDc2MDc4LCJleHAiOjE3Mjg0NzcyNzgsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgb2ZmbGluZV9hY2Nlc3MiLCJhenAiOiJ2TkxwYU1EQU1RZVIwZHJhdkxreHdZdlZpVGRXYzZUSSJ9.B4xOBCFoXbGSYlDyy31KJxow4ESEEmduiPwNab05_eHpi9Psr37QMQOkNHEMHz0H8N3ZPrCxBclF74IksKS9vPOFmpB022D7XuoRzb5BlYWteNqGvzHw0vGmvc45hwVO5QoOIRw62Q2yF5-HDOm0wLkv1U6u3F36jVv3YTydXC2mvEfGtOJp3wuFuZ4FfM7p5pWEpo-kdCoEDa6RfjSHkDwpbXsDJ40zEJTks9b5WRkb6o69bevPqKwtkI_Xzf_BUvnwOv3Z795j2NPVaxSdBinOBtKbN7n5SkK8n8Vvzu9wngGjIegB_KkReopPqzuUkEj9oNiW3vxC7u6exUgihw';
const HACKATHON_SLUG = 'odhack-80';


function userIds() {
    return ENV === 'perf' ? userIdsProduction : ENV === 'staging' ? userIdsStaging : userIdsDevelop;
}

function userId() {
    return userIds()[exec.vu.idInTest % userIds().length];
}


export default function () {
    const params = {
        headers: {
            'Authorization': `Bearer ${ACCESS_TOKEN}`,
            'X-Impersonation-Claims': `{"sub":"github|${userId()}"}`,
            'Content-Type': 'application/json',
        },
    };

    console.info(`User ${userId()} started`);
    globalRequests(params);
    homeRequests(params);

    globalRequests(params);
    hackathonsRequests(params);
}

function globalRequests(params) {
    http.get(`${BASE_URL}/me/billing-profiles`, params);
    console.info(`User ${userId()} first global requests done`);
    http.get(`${BASE_URL}/me/profile`, params);
    http.get(`${BASE_URL}/me/onboarding`, params);
    http.get(`${BASE_URL}/banner?hiddenIgnoredByMe=true`, params);
    notificationRequests(params);
}

function notificationRequests(params) {
    http.get(`${BASE_URL}/me/notifications/count?status=UNREAD`, params);
}

function homeRequests(params) {
    http.get(`${BASE_URL}/me/rewards`, params);
    http.get(`${BASE_URL}/me/projects`, params);
    http.get(`${BASE_URL}/me/recommended-projects`, params);
    http.get(`${BASE_URL}/projects`, params);
    http.get(`${BASE_URL}/public-activity`, params);
    console.info('Home requests done');
}

function hackathonsRequests(params) {
    http.get(`${BASE_URL}/hackathons`, params);
    const hackathon = http.get(`${BASE_URL}/hackathons/slug/${HACKATHON_SLUG}`, params);
    const hackathonId = hackathon.json().id;
    console.info(`Selected hackathon: ${HACKATHON_SLUG} (id: ${hackathonId})`);
    registerToHackathon(hackathonId, params);
    hackathonRequests(hackathonId, params);
}

function registerToHackathon(hackathonId, params) {
    const profilePatchRes = http.patch(`${BASE_URL}/me/profile`, JSON.stringify({
        "contacts": [
            {
                "channel": "TELEGRAM",
                "contact": "t.me/foo",
                "visibility": "public"
            }
        ]
    }), params);
    check(profilePatchRes, {
        'successfully patched profile': (r) => r.status === 204,
    });

    const res = http.put(`${BASE_URL}/me/hackathons/${hackathonId}/registrations`, null, params);
    check(res, {
        'successfully registered to hackathon': (r) => r.status === 204,
    });
    console.info(`User ${userId()} registered to hackathon ${hackathonId}`);
}

function hackathonRequests(hackathonId, params) {
    const hackathonProjectIssues = http.get(`${BASE_URL}/hackathons/${hackathonId}/project-issues?statuses=OPEN`, params);
    const projectIds = hackathonProjectIssues.json().projects.map(p => p.project.id);
    console.info(`Hackathon projects: ${projectIds}`);
    projectIds.forEach(projectId => hackathonProjectsRequests(hackathonId, projectId, params));
}

function hackathonProjectsRequests(hackathonId, projectId, params) {
    const res = http.get(`${BASE_URL}/projects/${projectId}/public-issues?statuses=OPEN&hackathonId=${hackathonId}`, params);
    const projectIssueCount = res.json().totalItemNumber;
    console.info(`Fetched ${projectIssueCount} issues from project ${projectId}`);
    notificationRequests(params);
}