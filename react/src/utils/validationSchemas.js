import * as yup from 'yup';

export const loginSchema = yup.object({
  email: yup
    .string()
    .required('請輸入郵箱')
    .email('請輸入有效的郵箱格式'),
  password: yup
    .string()
    .required('請輸入密碼')
    .min(6, '密碼長度不能少於6位'),
});

export const adminLoginSchema = yup.object({
  email: yup
    .string()
    .required('請輸入管理員郵箱')
    .email('請輸入有效的郵箱格式'),
  password: yup
    .string()
    .required('請輸入密碼')
    .min(6, '密碼長度不能少於6位'),
});

