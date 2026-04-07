import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminApi } from '../services/api'

export default function Admin() {
  const [user, setUser] = useState(null)
  const [tab, setTab] = useState('tickets')
  const [tickets, setTickets] = useState([])
  const [users, setUsers] = useState([])
  const [selected, setSelected] = useState(null)
  const [solution, setSolution] = useState('')
  const [status, setStatus] = useState('OPEN')
  const nav = useNavigate()

  useEffect(() => {
    const u = JSON.parse(localStorage.getItem('user') || '{}')
    if (!u.id || u.role !== 'ADMIN') { nav('/login'); return }
    setUser(u)
    loadTickets()
  }, [nav])

  const loadTickets = async () => { try { const { data } = await adminApi.getTickets(); setTickets(data) } catch (e) { console.error(e) } }
  const loadUsers = async () => { try { const { data } = await adminApi.getUsers(); setUsers(data) } catch (e) { console.error(e) } }

  const openTicket = (t) => { setSelected(t); setSolution(t.solution || ''); setStatus(t.status) }

  const updateTicket = async () => {
    try {
      await adminApi.updateTicket(selected.id, solution, status)
      setSelected(null)
      loadTickets()
    } catch (e) { alert('Error updating ticket') }
  }

  if (!user) return null

  return (
    <div className="admin">
      <header>
        <h2>Admin Dashboard</h2>
        <div className="user-bar">
          <span>{user.name}</span>
          <button onClick={() => { localStorage.removeItem('user'); nav('/login') }}>Logout</button>
        </div>
      </header>
      <div className="tabs">
        <button className={tab === 'tickets' ? 'active' : ''} onClick={() => { setTab('tickets'); loadTickets() }}>Tickets</button>
        <button className={tab === 'users' ? 'active' : ''} onClick={() => { setTab('users'); loadUsers() }}>Users</button>
      </div>

      {tab === 'tickets' && (
        <div className="grid">
          {tickets.map(t => (
            <div key={t.id} className={`card status-${t.status.toLowerCase()}`} onClick={() => openTicket(t)}>
              <div className="card-head"><span>#{t.id}</span><span className="badge">{t.status}</span></div>
              <strong>{t.subject}</strong>
              <p>{t.customerName} — {t.priority}</p>
            </div>
          ))}
          {tickets.length === 0 && <p>No tickets found.</p>}
        </div>
      )}

      {tab === 'users' && (
        <div className="grid">
          {users.map(u => (
            <div key={u.id} className="card">
              <strong>{u.name}</strong>
              <p>{u.email}</p>
              <span className="badge">{u.role}</span>
            </div>
          ))}
        </div>
      )}

      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Ticket #{selected.id}</h3>
            <p><strong>Customer:</strong> {selected.customerName}</p>
            <p><strong>Email:</strong> {selected.email}</p>
            <p><strong>Subject:</strong> {selected.subject}</p>
            <p><strong>Description:</strong> {selected.description}</p>
            <p><strong>Priority:</strong> {selected.priority}</p>
            <textarea value={solution} onChange={e => setSolution(e.target.value)} placeholder="Enter solution..." />
            <select value={status} onChange={e => setStatus(e.target.value)}>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
            <button onClick={updateTicket}>Update Ticket</button>
          </div>
        </div>
      )}
    </div>
  )
}
