import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

// 簡化表單 hook 使用，直接傳 schema
export function useFormWithSchema(schema, defaultValues = {}) {
  return useForm({
    resolver: yupResolver(schema),
    defaultValues,
  });
}
