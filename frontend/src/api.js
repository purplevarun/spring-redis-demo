const BASE = '/api'

export async function addNumber(value) {
	const res = await fetch(`${BASE}/numbers`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ value: Number(value) })
	})
	if (!res.ok) throw new Error('Failed to add number')
	return res.json()
}

export async function getNumbers() {
	const res = await fetch(`${BASE}/numbers`)
	if (!res.ok) throw new Error('Failed to fetch numbers')
	const data = await res.json()
	const cacheStatus = res.headers.get('X-Cache-Status')
	return { data, cacheStatus }
}

export async function getNumberById(id) {
	const res = await fetch(`${BASE}/numbers/${id}`)
	if (!res.ok) throw new Error(`Failed to fetch number ${id}`)
	const data = await res.json()
	const cacheStatus = res.headers.get('X-Cache-Status')
	return { data, cacheStatus }
}

export async function getCacheStats() {
	const res = await fetch(`${BASE}/cache/stats`)
	if (!res.ok) throw new Error('Failed to fetch cache stats')
	return res.json()
}

export async function clearCache() {
	const res = await fetch(`${BASE}/cache`, { method: 'DELETE' })
	if (!res.ok) throw new Error('Failed to clear cache')
}
