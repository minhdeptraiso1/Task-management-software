import { useState, useCallback } from 'react'

export function useButtonAnimation(durationMs = 500) {
  const [isAnim, setIsAnim] = useState(false)

  const triggerAnim = useCallback((callback?: () => void) => {
    setIsAnim(true)
    setTimeout(() => {
      setIsAnim(false)
    }, durationMs)
    if (callback) callback()
  }, [durationMs])

  return { isAnim, triggerAnim }
}
