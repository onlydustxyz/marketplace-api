// Run with: K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=html-report.html k6 run load-tests/k6/authenticated-users.js

import http from 'k6/http';
import exec from 'k6/execution';
import {userIds} from "./users/develop.js";

export const options = {
    scenarios: {
        odhack: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                {duration: '1s', target: 10},
                {duration: '10s', target: 0},
            ],
            gracefulRampDown: '0s',
        },
    },
};

const BASE_URL = 'https://develop-api.onlydust.com/api/v1';
const ACCESS_TOKEN = "";


export default function () {
    const params = {
        headers: {
            'Authorization': `Bearer ${ACCESS_TOKEN}`,
            'X-Impersonation-Claims': `{"sub":"github|${userIds[exec.vu.idInTest % userIds.length]}"}`,
        },
    };

    http.get(`${BASE_URL}/projects`, params);
    http.get(`${BASE_URL}/public-activity?pageIndex=0&pageSize=10`, params);
}

function globalRequests(params) {
    http.get(`${BASE_URL}/me/billing-profiles`, params);
    http.get(`${BASE_URL}/me/profile`, params);
    http.get(`${BASE_URL}/me/onboarding`, params);
    http.get(`${BASE_URL}/me/notifications/count?status=UNREAD`, params);
    http.get(`${BASE_URL}/banner?hiddenIgnoredByMe=true`, params);
}

function hackathonsRequests(params) {
    http.get(`${BASE_URL}/hackathons`, params);
    http.get(`${BASE_URL}/hackathons/slug/odh-hayden-6`, params);
    http.get(`${BASE_URL}/hackathons/ce24272b-377e-48d9-89b9-cb736a02e8e9/project-issues?statuses=OPEN`, params);
}

function hackathonRequests(params) {
    const res = http.get(`${BASE_URL}/hackathons/slug/odh-hayden-6`, params);
    const hackathonId = res.json().id;
    http.get(`${BASE_URL}/hackathons/${hackathonId}/project-issues?statuses=OPEN`, params);
    return hackathonId;
}

function hackathonProjectsRequests(hackathonId, params) {
    const res = http.get(`${BASE_URL}/projects/slug/odh-hayden-6`, params);
    http.get(`${BASE_URL}/projects/${res.json().id}/public-issues?statuses=OPEN&hackathonId=${hackathonId}`, params);
}