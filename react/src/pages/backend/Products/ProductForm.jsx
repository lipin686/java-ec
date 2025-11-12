import React, { useState, useEffect } from 'react';
import { createProduct, getProductById, updateProduct } from '../../../services/backend/productService';
import {
  Box,
  Typography,
  Button,
  TextField,
  MenuItem,
  Select,
  InputLabel,
  FormControl,
  Stack,
  CircularProgress,
} from '@mui/material';

const initialState = {
  name: '',
  price: '',
  stock: '', // 改為 stock
  status: '',
  startAt: '',
  endAt: '',
  description: '',
};

const statusOptions = [
  { value: 'OPEN', label: '開啟' },
  { value: 'CLOSED', label: '關閉' }, // 改為 CLOSED
  { value: 'HIDDEN', label: '隱藏' },
];

const ProductForm = ({ id, onClose }) => {
  const [form, setForm] = useState(initialState);
  const [image, setImage] = useState(null);
  const [isEdit, setIsEdit] = useState(false);
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState('');

  useEffect(() => {
    if (!id) return;
    setIsEdit(true);
    setLoading(true);
    getProductById(id).then(res => {
      setForm({
        ...res.data,
        stock: res.data.stock ?? res.data.inStock ?? '', // 兼容舊資料
        productNo: undefined, // 移除商品編號
      });
      setPreview(res.data.imageUrl || '');
      setLoading(false);
    });
  }, [id]);

  useEffect(() => {
    if (!id) {
      setIsEdit(false);
      setForm(initialState);
      setPreview('');
      setImage(null);
    }
  }, [id]);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleImageChange = e => {
    const file = e.target.files[0];
    setImage(file);
    if (file) {
      const reader = new FileReader();
      reader.onload = ev => setPreview(ev.target.result);
      reader.readAsDataURL(file);
    } else {
      setPreview('');
    }
  };

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      setLoading(true);
      // 處理數字欄位型態
      const submitForm = {
        ...form,
        price: form.price === '' ? null : Number(form.price),
        stock: form.stock === '' ? null : Number(form.stock),
      };
      if (isEdit) {
        await updateProduct(id, submitForm);
        alert('商品更新成功');
      } else {
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(submitForm)], { type: 'application/json' }));
        if (image) formData.append('image', image);
        await createProduct(formData);
        alert('商品新增成功');
      }
      onClose(true);
    } catch (err) {
      alert('操作失敗');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" mb={3}>{isEdit ? '編輯商品' : '新增商品'}</Typography>
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <TextField
              label="名稱"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
              fullWidth
            />
            <TextField
              label="價格"
              name="price"
              value={form.price}
              onChange={handleChange}
              required
              type="number"
              fullWidth
            />
            <TextField
              label="庫存"
              name="stock"
              value={form.stock}
              onChange={handleChange}
              required
              type="number"
              fullWidth
            />
            <FormControl fullWidth required>
              <InputLabel>狀態</InputLabel>
              <Select
                label="狀態"
                name="status"
                value={form.status}
                onChange={handleChange}
              >
                <MenuItem value="">請選擇</MenuItem>
                {statusOptions.map(opt => (
                  <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="開始時間"
              name="startAt"
              value={form.startAt ? form.startAt.substring(0, 16) : ''}
              onChange={handleChange}
              type="datetime-local"
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="結束時間"
              name="endAt"
              value={form.endAt ? form.endAt.substring(0, 16) : ''}
              onChange={handleChange}
              type="datetime-local"
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="描述"
              name="description"
              value={form.description || ''}
              onChange={handleChange}
              multiline
              minRows={2}
              fullWidth
            />
            <Box>
              <Typography variant="body2" mb={1}>商品圖片</Typography>
              {preview && (
                <Box mb={1}>
                  <img src={preview} alt="預覽" style={{ width: 120, height: 120, objectFit: 'cover', borderRadius: 8 }} />
                </Box>
              )}
              {!isEdit && (
                <Button variant="outlined" component="label">
                  上傳圖片
                  <input type="file" accept="image/*" hidden onChange={handleImageChange} />
                </Button>
              )}
            </Box>
            <Stack direction="row" spacing={2} justifyContent="flex-end">
              <Button variant="contained" color="primary" type="submit">
                {isEdit ? '更新' : '新增'}
              </Button>
              <Button variant="outlined" onClick={() => onClose(false)}>返回</Button>
            </Stack>
          </Stack>
        </Box>
      )}
    </Box>
  );
};

export default ProductForm;



