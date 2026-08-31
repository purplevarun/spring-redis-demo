import { useState } from 'react'
import { addNumber } from '../api.js'

export default function AddNumberForm({ onAdd, style }) {
	const [value, setValue] = useState('')
	const [loading, setLoading] = useState(false)
	const [err, setErr] = useState(null)

	const handleSubmit = async (e) => {
		e.preventDefault()
		if (!value) return
		setLoading(true)
		setErr(null)
		try {
			await addNumber(value)
			setValue('')
			onAdd()
		} catch (e) {
			setErr(e.message)
		} finally {
			setLoading(false)
		}
	}

	return (
		<div style={style}>
			<h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16 }}>Add a number</h2>
			<form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
				<input
					type="number"
					value={value}
					onChange={e => setValue(e.target.value)}
					placeholder="Enter integer"
					style={{ flex: 1, padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: 6, fontSize: 14 }}
				/>
				<button
					type="submit"
					disabled={loading}
					style={{ padding: '8px 16px', background: '#4f46e5', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 14 }}
				>
					{loading ? '...' : 'Post'}
				</button>
			</form>
			{err && <p style={{ color: '#dc2626', fontSize: 12, marginTop: 8 }}>{err}</p>}
		</div>
	)
}
