import api from '../api';

export const getProducts = (params) =>
    api.get('/admin/v1/products', { params });

export const getProductById = (id) =>
    api.get(`/admin/v1/products/${id}`);

export const createProduct = (data) =>
    api.post('/admin/v1/products', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });

export const updateProduct = (id, data) =>
    api.put(`/admin/v1/products/${id}`, data, {
        headers: { 'Content-Type': 'application/json' },
    });

export const deleteProduct = (id) =>
    api.delete(`/admin/v1/products/${id}`);

export const hardDeleteProduct = (id) =>
    api.delete(`/admin/v1/products/${id}/hard`);

export const searchProductsByName = (name) =>
    api.get('/admin/v1/products/search', { params: { name } });
