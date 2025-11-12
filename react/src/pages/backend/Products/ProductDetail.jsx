import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductById } from '../../../services/backend/productService';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = React.useState(null);

  React.useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await getProductById(id);
        setProduct(response.data);
      } catch (error) {
        console.error('Error fetching product:', error);
      }
    };

    fetchProduct();
  }, [id]);

  if (!product) return <div>Loading...</div>;

  return (
    <div>
      <h1>{product.name}</h1>
      <p>{product.description}</p>
      <button onClick={() => navigate(-1)}>返回</button>
    </div>
  );
};

export default ProductDetail;




