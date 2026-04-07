import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'

export default function Login() {
  const [isLogin, setIsLogin] = useState(true)
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'USER' })
  const [msg, setMsg] = useState('')
  const nav = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMsg('')
    try {
      if (isLogin) {
        const { data } = await authApi.login(form.email, form.password)
        localStorage.setItem('user', JSON.stringify(data.user))
        nav(data.user.role === 'ADMIN' ? '/admin' : '/dashboard')
      } else {
        await authApi.register(form.name, form.email, form.password, form.role)
        setMsg('Registration successful! Please login.')
        setIsLogin(true)
      }
    } catch (err) {
      setMsg(err.response?.data?.error || 'Something went wrong')
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Customer Support Portal</h2>
        <div className="tabs">
          <button className={isLogin ? 'active' : ''} onClick={() => setIsLogin(true)}>Login</button>
          <button className={!isLogin ? 'active' : ''} onClick={() => setIsLogin(false)}>Register</button>
        </div>
        <form onSubmit={handleSubmit}>
          {!isLogin && <input placeholder="Full Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />}
          <input type="email" placeholder="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
          <input type="password" placeholder="Password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required />
          {!isLogin && (
            <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
              <option value="USER">User</option>
              <option value="ADMIN">Admin</option>
            </select>
          )}
          <button type="submit">{isLogin ? 'Login' : 'Register'}</button>
        </form>
        {msg && <p className="msg">{msg}</p>}
        <div className="demo">
          <p>Demo Accounts:</p>
          <small>Admin: admin@support.com / admin123</small><br />
          <small>User: user@example.com / user123</small>
        </div>
      </div>
    </div>
  )
}
