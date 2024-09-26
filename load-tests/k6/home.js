import http from 'k6/http';

export const options = {
    scenarios: {
        display_homepage: {
            executor: 'per-vu-iterations',
            vus: 20,
            iterations: 20,
            startTime: '5s',
        },
    },
};

export default function () {
    http.get('https://api.onlydust.com/api/v1/projects');
    http.get('https://api.onlydust.com/api/v1/public-activity?pageIndex=0&pageSize=10');
    http.get('https://api.onlydust.com/api/v1/hackathons');
}