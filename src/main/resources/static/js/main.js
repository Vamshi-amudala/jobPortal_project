// js/main.js
function apiRequest(url, method, data) {
  return fetch(url, {
    method: method,
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include', // important for session-based auth
    body: JSON.stringify(data)
  })
  .then(res => {
    if (!res.ok) throw new Error('Request failed');
    return res.json();
  });
}
