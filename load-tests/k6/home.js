// Run with: K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=html-report.html k6 run load-tests/k6/home.js

import http from 'k6/http';

export const options = {
    scenarios: {
        display_homepage: {
            executor: 'per-vu-iterations',
            vus: 20,
            iterations: 100,
            startTime: '5s',
        },
    },
};

const BASE_URL = 'https://develop-api.onlydust.com/api/v1';

export default function () {
    http.get(`${BASE_URL}/projects`);
    http.get(`${BASE_URL}/public-activity?pageIndex=0&pageSize=10`);
}