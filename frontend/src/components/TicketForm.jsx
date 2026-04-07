import { useState } from 'react'
import { ticketApi } from '../services/api'

export default function TicketForm({ user }) {
  const [form, setForm] = useState({ subject: '', description: '', priority: 'MEDIUM' })
  const [msg, setMsg] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    try {
      const { data } = await ticketApi.create({
        customerName: user.name, email: user.email,
        subject: form.subject, description: form.description,
        priority: form.priority, userId: user.id
      })
      setMsg(`Ticket #${data.id} created!`)
      setForm({ subject: '', description: '', priority: 'MEDIUM' })
    } catch { setMsg('Error creating ticket.') }
  }

  return (
    <div className="ticket-form">
      <h3>Create Support Ticket</h3>
      <form onSubmit={submit}>
        <input placeholder="Subject" value={form.subject} onChange={e => setForm({ ...form, subject: e.target.value })} required />
        <textarea placeholder="Describe your issue..." value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} required />
        <select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="URGENT">Urgent</option>
        </select>
        <button type="submit">Create Ticket</button>
      </form>
      {msg && <p className="msg">{msg}</p>}
    </div>
  )
}
