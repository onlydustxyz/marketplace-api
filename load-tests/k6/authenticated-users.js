// Run with: K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=html-report.html k6 run load-tests/k6/authenticated-users.js

import http from 'k6/http';
import exec from 'k6/execution';
import {userIds} from "./users/develop.js";

export const options = {
    scenarios: {
        display_homepage: {
            executor: 'per-vu-iterations',
            vus: 5,
            iterations: 10,
            startTime: '5s',
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