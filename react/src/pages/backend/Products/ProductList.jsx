import React, { useEffect, useState, useRef } from 'react';
import { getProducts, deleteProduct } from '../../../services/backend/productService';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Stack,
  MenuItem,
  Select,
  InputLabel,
  FormControl
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ProductForm from './ProductForm';
import SearchIcon from '@mui/icons-material/Search';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import Inventory2Icon from '@mui/icons-material/Inventory2';

const initialSearch = {
  name: '',
  productNo: '',
  inStock: '', // 改回 inStock，預設空字串
  deleted: '',
  status: '',
  startAtFrom: '',
  startAtTo: '',
};

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState(initialSearch);
  const [deleteId, setDeleteId] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const navigate = useNavigate();
  const didFetch = useRef(false);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const params = { ...search, page, size };
      // 處理空字串參數
      Object.keys(params).forEach(key => {
        if (params[key] === '') delete params[key];
      });
      const res = await getProducts(params);
      setProducts(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 1);
    } catch (e) {
      alert('取得商品失敗');
    }
    setLoading(false);
  };

  useEffect(() => {
    if (didFetch.current) return;
    didFetch.current = true;
    fetchProducts();
    // eslint-disable-next-line
  }, [page, size]);

  const handleDelete = async () => {
    if (!deleteId) return;
    await deleteProduct(deleteId);
    setDeleteId(null);
    fetchProducts();
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearch(prev => ({ ...prev, [name]: value }));
  };

  const handleReset = () => {
    setSearch(initialSearch);
    setPage(0);
  };

  const handleOpenCreate = () => {
    setEditId(null);
    setFormOpen(true);
  };
  const handleOpenEdit = (id) => {
    setEditId(id);
    setFormOpen(true);
  };
  const handleFormClose = (refresh = false) => {
    setFormOpen(false);
    setEditId(null);
    if (refresh) fetchProducts();
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Inventory2Icon sx={{ fontSize: 36, color: 'primary.main', mr: 2 }} />
        <Typography variant="h4" sx={{ fontWeight: 600 }}>商品管理</Typography>
      </Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Box />
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
          新增商品
        </Button>
      </Box>
      <Box component="form" onSubmit={e => { e.preventDefault(); fetchProducts(); }}>
        <Stack direction="row" flexWrap="wrap" gap={2} mb={2}>
          <TextField
            label="名稱"
            name="name"
            value={search.name}
            onChange={handleInputChange}
            size="small"
            sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}
          />
          <TextField
            label="商品編號"
            name="productNo"
            value={search.productNo}
            onChange={handleInputChange}
            size="small"
            sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}
          />
          <FormControl size="small" sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}>
            <InputLabel>庫存狀態</InputLabel>
            <Select
              label="庫存狀態"
              name="inStock"
              value={search.inStock}
              onChange={handleInputChange}
            >
              <MenuItem value="">全部</MenuItem>
              <MenuItem value="true">有庫存</MenuItem>
              <MenuItem value="false">無庫存</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}>
            <InputLabel>狀態</InputLabel>
            <Select
              label="狀態"
              name="status"
              value={search.status}
              onChange={handleInputChange}
            >
              <MenuItem value="">全部</MenuItem>
              <MenuItem value="OPEN">開啟</MenuItem>
              <MenuItem value="CLOSED">關閉</MenuItem>
              <MenuItem value="HIDDEN">隱藏</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}>
            <InputLabel>已刪除</InputLabel>
            <Select
              label="已刪除"
              name="deleted"
              value={search.deleted}
              onChange={handleInputChange}
            >
              <MenuItem value="">全部</MenuItem>
              <MenuItem value="false">否</MenuItem>
              <MenuItem value="true">是</MenuItem>
            </Select>
          </FormControl>
          <TextField
            label="開始時間(起)"
            name="startAtFrom"
            type="datetime-local"
            value={search.startAtFrom}
            onChange={handleInputChange}
            size="small"
            InputLabelProps={{ shrink: true }}
            sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}
          />
          <TextField
            label="開始時間(迄)"
            name="startAtTo"
            type="datetime-local"
            value={search.startAtTo}
            onChange={handleInputChange}
            size="small"
            InputLabelProps={{ shrink: true }}
            sx={{ flexBasis: { xs: '100%', sm: '220px' }, flexGrow: 1 }}
          />
        </Stack>
        <Stack direction="row" justifyContent="flex-end" gap={1}>
          <Button
            variant="contained"
            color="primary"
            type="submit"
            startIcon={<SearchIcon />} // 搜尋icon
            sx={{ minWidth: 100, borderRadius: 2, boxShadow: 1 }}
          >
            搜尋
          </Button>
          <Button
            variant="outlined"
            color="secondary"
            onClick={handleReset}
            startIcon={<RestartAltIcon />} // 重設icon
            sx={{ minWidth: 100, borderRadius: 2, boxShadow: 1 }}
          >
            重設
          </Button>
        </Stack>
      </Box>
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>名稱</TableCell>
                <TableCell>商品編號</TableCell>
                <TableCell>描述</TableCell>
                <TableCell>價格</TableCell>
                <TableCell>庫存</TableCell>
                <TableCell>狀態</TableCell>
                <TableCell>上架時間</TableCell>
                <TableCell>下架時間</TableCell>
                <TableCell>圖片</TableCell>
                <TableCell align="center">操作</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {products.map(p => (
                <TableRow key={p.id}>
                  <TableCell>{p.id}</TableCell>
                  <TableCell>{p.name}</TableCell>
                  <TableCell>{p.productNo}</TableCell>
                  <TableCell>{p.description}</TableCell>
                  <TableCell>{p.price}</TableCell>
                  <TableCell>{p.stock}</TableCell>
                  <TableCell>{p.status}</TableCell>
                  <TableCell>{p.startAt ? new Date(p.startAt).toLocaleString() : ''}</TableCell>
                  <TableCell>{p.endAt ? new Date(p.endAt).toLocaleString() : ''}</TableCell>
                  <TableCell>
                    {p.imageUrl && (
                      <img src={`http://localhost:8080${p.imageUrl}`} alt={p.name} style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 4 }} />
                    )}
                  </TableCell>
                  <TableCell align="center">
                    <IconButton color="primary" onClick={() => handleOpenEdit(p.id)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton color="error" onClick={() => setDeleteId(p.id)}>
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      <Stack direction="row" spacing={2} justifyContent="center" alignItems="center" sx={{ mt: 2 }}>
        <Button disabled={page === 0} onClick={() => setPage(page - 1)}>上一頁</Button>
        <Typography>{page + 1} / {totalPages}</Typography>
        <Button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>下一頁</Button>
      </Stack>
      <Dialog open={!!deleteId} onClose={() => setDeleteId(null)}>
        <DialogTitle>確認刪除</DialogTitle>
        <DialogContent>確定要刪除此商品嗎？</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteId(null)}>取消</Button>
          <Button color="error" onClick={handleDelete}>刪除</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={formOpen} onClose={() => handleFormClose(false)} maxWidth="md" fullWidth keepMounted>
        <ProductForm id={editId} onClose={handleFormClose} />
      </Dialog>
    </Box>
  );
};

export default ProductList;



