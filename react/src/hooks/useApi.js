import { useState, useCallback } from 'react';

// 用於統一處理 API 請求的 hook
export function useApi(apiFunc) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);

  const request = useCallback(async (...args) => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiFunc(...args);
      setData(result);
      return result;
    } catch (err) {
      setError(err);
      return { success: false, message: err.message };
    } finally {
      setLoading(false);
    }
  }, [apiFunc]);

  return { request, loading, error, data };
}

