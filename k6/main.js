import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    vus: 100,  // number of virtual users
    duration: '10s',  // duration of the test
};

export default function () {
    // const url = 'http://localhost:8080/insertdata-perform-transit';
    const url = 'http://localhost:8080/insertdata-perform-raw';
    const payload = 'data=YOUR_DATA_HERE';
    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };

    http.post(url, payload, params);

    // sleep(0.01);  // wait for 1s before the next request
}