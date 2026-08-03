const API_BASE = '/api';

/**
 * Backend'e istek atan ortak fonksiyon.
 * ApiError şeklindeki hata gövdesini ({status, error, message, timestamp})
 * okuyup düz bir Error olarak fırlatır — sayfalar sadece err.message'a bakar.
 */
async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    body: options.body,
  });

  let data = null;
  try {
    data = await response.json();
  } catch {
    // Gövde boş veya JSON değil (örn. 204 No Content)
  }

  if (!response.ok) {
    throw new Error(data?.message || 'Sunucuya ulaşılamadı, lütfen tekrar deneyin.');
  }

  return data;
}

export function registerUser({ fullName, email, password, phone }) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ fullName, email, password, phone }),
  });
}

export function loginUser({ email, password }) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}
