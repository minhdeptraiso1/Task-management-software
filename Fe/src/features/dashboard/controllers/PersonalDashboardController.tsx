import { useEffect, useState, useCallback } from 'react'
import type { User } from '../../user/models/user.model'
import { getMyDashboard, getMyTasks, getMyTimeSummary } from '../services/dashboard.service'
import type { MyDashboardResponse, MyTaskPageResponse, MyTimeSummaryResponse } from '../models/dashboard.model'
import { PersonalDashboardView } from '../views/PersonalDashboardView'

export function PersonalDashboardController({ me }: { me: User }) {
  const [dashboard, setDashboard] = useState<MyDashboardResponse | null>(null)
  const [tasks, setTasks] = useState<MyTaskPageResponse | null>(null)
  const [timeSummary, setTimeSummary] = useState<MyTimeSummaryResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [taskPage, setTaskPage] = useState(0)

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [dash, tasksResp, timeResp] = await Promise.all([
        getMyDashboard(),
        getMyTasks(taskPage, 10),
        getMyTimeSummary(),
      ])
      setDashboard(dash)
      setTasks(tasksResp)
      setTimeSummary(timeResp)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi tải trang tổng quan cá nhân')
    } finally {
      setLoading(false)
    }
  }, [taskPage])

  useEffect(() => {
    void Promise.resolve().then(loadData)
  }, [loadData])

  return (
    <PersonalDashboardView
      me={me}
      dashboard={dashboard}
      tasks={tasks}
      timeSummary={timeSummary}
      loading={loading}
      error={error}
      taskPage={taskPage}
      onTaskPageChange={setTaskPage}
    />
  )
}
