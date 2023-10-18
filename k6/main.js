import http from 'k6/http';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

import { sleep } from 'k6';

export let options = {
    vus: 10,  // 동시 실행 사용자 수를 10개로 설정
    // duration: '10s',  // duration of the test
    iterations: 500000, // 총 요청 수를 500,000건으로 설정
};

export function handleSummary(data) {
    return {
        "result.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}
  
export default function () {
    const url = 'http://localhost:8080/insertdata-perform-transit';
    // const url = 'http://localhost:8080/insertdata-perform-raw';
    const payload = 'data=YOUR_DATA_HERE';
    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };

    http.post(url, payload, params);

    // sleep(0.01);  // wait for 1s before the next request
}