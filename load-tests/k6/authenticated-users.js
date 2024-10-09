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
            executor: 'constant-vus',
            vus: 1,
            duration: '60s',
        },
    },
};

const ENV = 'staging';
const BASE_URL = ENV === 'production' ? 'https://api.onlydust.com/api/v1' : `https://${ENV}-api.onlydust.com/api/v1`;
const ACCESS_TOKEN = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IklNX3NVb0w3a1o3WVFTUTF5NlVYZCJ9.eyJpc3MiOiJodHRwczovL3N0YWdpbmctb25seWR1c3QuZXUuYXV0aDAuY29tLyIsInN1YiI6ImdpdGh1Ynw1OTU1MDUiLCJhdWQiOlsiaHR0cHM6Ly9zdGFnaW5nLWFwaS5vbmx5ZHVzdC5jb20vYXBpIiwiaHR0cHM6Ly9zdGFnaW5nLW9ubHlkdXN0LmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE3Mjg0NTkyNjYsImV4cCI6MTcyODQ2MDQ2Niwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBvZmZsaW5lX2FjY2VzcyIsImF6cCI6IjdNWUR3RHg2elNtdUt0T2lkV3dzSVNXNVlFaE9OZUR6In0.CFtdg0gWchbVr2brEtCTGeGmNiFrVM1-gXgYCxithri8SvBipxokcLnb32-qOOnoX5nZShH0HRHga-IEDBwpFJKX24m07kDvwjY24JmomSSYEIgB5CvPIF5-JOqCJ5DUx27vm-P20DAc1a98_2enRYw_8OyYL6WXhft1IOecvy_Z4szClf2A6YRGE8-vVrZEUg8q2rn95ity8IixlvQZHI5N9xujFWZhdVjLl5iPJRjhihDFDywjx79f7U-HOqh0oIfEaXyR3_28OCpB5HUanBFMjgH2YzZfdkXI69QQZU2CHio0CHNtf0EThmLJQ_Pp5Xj3FfsvICFFJd31QxipWQ';

function userIds() {
    return ENV === 'production' ? userIdsProduction : ENV === 'staging' ? userIdsStaging : userIdsDevelop;
}

function userId() {
    return userIds()[exec.vu.idInTest % userIds().length];
}


export default function () {
    const params = {
        headers: {
            'Authorization': `Bearer ${ACCESS_TOKEN}`,
            'X-Impersonation-Claims': `{"sub":"github|${userId()}"}`,
        },
    };

    globalRequests(params);
    hackathonsRequests(params);
}

function globalRequests(params) {
    http.get(`${BASE_URL}/me/billing-profiles`, params);
    http.get(`${BASE_URL}/me/profile`, params);
    http.get(`${BASE_URL}/me/onboarding`, params);
    http.get(`${BASE_URL}/me/notifications/count?status=UNREAD`, params);
    http.get(`${BASE_URL}/banner?hiddenIgnoredByMe=true`, params);
}

function hackathonsRequests(params) {
    const hackathons = http.get(`${BASE_URL}/hackathons`, params);
    const hackathonId = hackathons.json().hackathons[0].id;
    const hackathonSlug = hackathons.json().hackathons[0].slug;
    console.info(`Selected hackathon: ${hackathonSlug}`);
    registerToHackathon(hackathonId, params);
    hackathonRequests(hackathonSlug, params);
}

function registerToHackathon(hackathonId, params) {
    const res = http.put(`${BASE_URL}/me/hackathons/${hackathonId}/registrations`, null, params);
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    console.info(`User ${userId()} registered to hackathon ${hackathonId}`);
}

function hackathonRequests(hackathonSlug, params) {
    const hackathon = http.get(`${BASE_URL}/hackathons/slug/${hackathonSlug}`, params);
    const hackathonId = hackathon.json().id;
    const hackathonProjectIssues = http.get(`${BASE_URL}/hackathons/${hackathonId}/project-issues?statuses=OPEN`, params);
    const projectIds = hackathonProjectIssues.json().projects.map(p => p.project.id);
    console.info(`Hackathon projects: ${projectIds}`);
    projectIds.forEach(projectId => hackathonProjectsRequests(hackathonId, projectId, params));
}

function hackathonProjectsRequests(hackathonId, projectId, params) {
    http.get(`${BASE_URL}/projects/${projectId}/public-issues?statuses=OPEN&hackathonId=${hackathonId}`, params);
}