import React, { useEffect, useMemo, useState } from 'react'

type MissionModal = {
  id: number
  date: string
  serverTime: string
  targetKm: number
  currentKm: number
  progressPercent: number
  completed: boolean
  claimed: boolean
  rewardCoins: number
}

type ClaimResponse = {
  rewardCoins: number
  claimed: boolean
}

const fmt = (v: number) => (Number.isFinite(v) ? v.toFixed(2) : '0.00')

// 에러를 안전하게 문자열로 변환 (any 금지 → unknown 사용)
function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  if (typeof err === 'string') return err
  try {
    return JSON.stringify(err)
  } catch {
    return String(err)
  }
}

const DailyMissionTest: React.FC = () => {
  const [mission, setMission] = useState<MissionModal | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [wallet, setWallet] = useState(0)

  // 토스트 상태 및 헬퍼
  const [toast, setToast] = useState<string | null>(null)
  const notify = (msg: string) => {
    setToast(msg)
    window.setTimeout(() => setToast(null), 2000) // 2초 후 자동 닫힘
  }

  const progressWidth = useMemo(() => `${mission?.progressPercent ?? 0}%`, [mission])

  const load = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await fetch('/api/v1/daily-mission/modal')
      if (!res.ok) throw new Error(`modal ${res.status}`)
      const data: MissionModal = await res.json()
      setMission(data)
    } catch (e: unknown) {
      setError(getErrorMessage(e))
      notify('미션 정보를 불러오지 못했어요.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const addKm = async (delta: number) => {
    if (!mission) return
    try {
      setLoading(true)
      setError(null)
      const res = await fetch(`/api/v1/daily-mission/${mission.id}/progress?addKm=${delta}`, {
        method: 'PATCH',
      })
      if (!res.ok) throw new Error(`progress ${res.status}`)
      await load()
      notify(`${delta.toFixed(1)} km 추가됐어요!`)
    } catch (e: unknown) {
      setError(getErrorMessage(e))
      notify('진행도 업데이트에 실패했어요.')
    } finally {
      setLoading(false)
    }
  }

  const claim = async () => {
    if (!mission) return

    // 프런트 가드 (즉시 토스트)
    if (mission.claimed) {
      notify('오늘은 이미 보상을 받았습니다!')
      return
    }
    if (!mission.completed) {
      notify('미션 완료 후 수령 가능합니다.')
      return
    }

    try {
      setLoading(true)
      setError(null)

      const res = await fetch(`/api/v1/daily-mission/${mission.id}/claim`, { method: 'POST' })

      if (res.ok) {
        const data: ClaimResponse = await res.json()
        setWallet((w) => w + (data.rewardCoins ?? 0))
        notify(`보상 ${data.rewardCoins ?? 0} 코인을 수령했습니다! 🎉`)
        await load()
        return
      }

      // 서버 상태코드에 따른 메시지
      if (res.status === 409) {
        notify('오늘은 이미 보상을 받았습니다!')
      } else if (res.status === 400) {
        notify('미션을 먼저 완료하세요.')
      } else if (res.status === 404) {
        notify('미션 정보를 찾을 수 없습니다.')
      } else {
        const text = await res.text()
        notify(`서버 오류(${res.status}): ${text || '다시 시도해주세요.'}`)
      }
    } catch (e: unknown) {
      const msg = getErrorMessage(e)
      setError(msg)
      notify(`네트워크 오류: ${msg}`)
    } finally {
      setLoading(false)
    }
  }

  const reset = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await fetch(`/api/v1/daily-mission/reset`, { method: 'POST' })
      if (!res.ok) throw new Error(`reset ${res.status}`)
      await load()
      notify('오늘 미션이 초기화됐어요.')
    } catch (e: unknown) {
      setError(getErrorMessage(e))
      notify('초기화에 실패했어요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={{ marginTop: 0 }}>Daily Mission — 테스트 페이지</h2>

        {/* 상단 상태 라인 */}
        <div style={styles.row}>
          <span>임시 지갑(코인): </span>
          <strong style={{ marginLeft: 8 }}>{wallet}</strong>
        </div>

        <div style={{ height: 8 }} />
        {loading && <div style={styles.badge}>Loading...</div>}
        {error && <div style={{ ...styles.badge, background: '#ffebee', color: '#c62828' }}>Error: {error}</div>}

        {/* 본문 */}
        {!mission ? (
          <button style={styles.button} onClick={load}>불러오기</button>
        ) : (
          <>
            <div style={styles.kv}>
              <div><b>날짜</b> {mission.date}</div>
              <div><b>서버시간</b> {new Date(mission.serverTime).toLocaleString()}</div>
              <div><b>목표 거리</b> {fmt(mission.targetKm)} km</div>
              <div><b>현재 거리</b> {fmt(mission.currentKm)} km</div>
              <div><b>진행률</b> {mission.progressPercent}%</div>
              <div><b>상태</b> {mission.completed ? '완료' : '진행중'} / {mission.claimed ? '수령완료' : '미수령'}</div>
              <div><b>보상</b> {mission.rewardCoins} 크레딧</div>
              <div><b>미션 ID</b> {mission.id}</div>
            </div>

            {/* Progress Bar */}
            <div style={styles.progressWrap}>
              <div style={styles.progressTrack}>
                <div style={{ ...styles.progressFill, width: progressWidth }} />
              </div>
            </div>

            {/* 버튼들 */}
            <div style={styles.actions}>
              <button style={styles.button} onClick={() => addKm(0.5)} disabled={loading}>+0.5 km</button>
              <button style={styles.button} onClick={() => addKm(1)} disabled={loading}>+1.0 km</button>
              <button
                style={{ ...styles.button, background: '#1e88e5' }}
                onClick={claim}
                disabled={loading} // 로딩 중에만 비활성화
                title={
                  !mission.completed
                    ? '미션 완료 후 수령 가능'
                    : (mission.claimed ? '이미 수령됨' : '수령 가능')
                }
              >
                보상 수령
              </button>
              <button style={{ ...styles.button, background: '#8e24aa' }} onClick={reset} disabled={loading}>
                초기화
              </button>
              <button style={{ ...styles.button, background: '#455a64' }} onClick={load} disabled={loading}>
                새로고침
              </button>
            </div>
          </>
        )}
      </div>

      {/* 토스트 */}
      {toast && (
        <div
          style={toastStyles.container}
          role="status"
          aria-live="polite"
        >
          {toast}
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  page: { minHeight: '100vh', background: '#f5f7fb', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, fontFamily: 'system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial' },
  card: { width: 560, background: '#fff', borderRadius: 16, padding: 20, boxShadow: '0 6px 18px rgba(0,0,0,0.08)' },
  kv: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, background: '#fafafa', padding: 12, borderRadius: 12, fontSize: 14 },
  row: { display: 'flex', alignItems: 'center' },
  progressWrap: { margin: '14px 0 8px' },
  progressTrack: { width: '100%', height: 14, background: '#eceff1', borderRadius: 999, overflow: 'hidden' },
  progressFill: { height: '100%', background: 'linear-gradient(90deg,#42a5f5,#26c6da)', transition: 'width 250ms ease' },
  actions: { display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' },
  button: { padding: '10px 14px', background: '#607d8b', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' },
  badge: { display: 'inline-block', padding: '4px 8px', borderRadius: 8, background: '#e3f2fd', color: '#1565c0' },
}

const toastStyles: Record<string, React.CSSProperties> = {
  container: {
    position: 'fixed',
    right: 20,
    bottom: 20,
    background: '#323a46',
    color: '#fff',
    padding: '10px 14px',
    borderRadius: 10,
    boxShadow: '0 6px 18px rgba(0,0,0,0.15)',
    zIndex: 9999,
    fontSize: 14,
    maxWidth: 320,
    wordBreak: 'keep-all',
  },
}

export default DailyMissionTest
