import { authFetch } from './auth';

export async function getLeagues() {
  const res = await authFetch('/leagues');
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function connectSleeper(username) {
  const res = await authFetch('/leagues/connect/sleeper', {
    method: 'POST',
    body: JSON.stringify({ username }),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function deleteLeague(id) {
  const res = await authFetch(`/leagues/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error(await res.text());
}
