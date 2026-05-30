const fs = require('fs');

async function seedProducts() {
  try {
    // Lấy danh mục
    const catRes = await fetch('http://localhost:8080/api/categories');
    let categoriesResponse = await catRes.json();
    let categories = categoriesResponse;
    if (categoriesResponse.data) categories = categoriesResponse.data;
    if (!Array.isArray(categories) || categories.length === 0) {
      console.log("No categories found!", categories);
      return;
    }

    const catIdMap = {};
    // Extract available category IDs. For simplicity, just use the first few available IDs
    let availableCatIds = [];
    categories.forEach(c => {
        availableCatIds.push(c.id);
        if (c.children) {
            c.children.forEach(ch => availableCatIds.push(ch.id));
        }
    });

    if (availableCatIds.length === 0) availableCatIds = [1, 2, 3]; // fallback

    const getRandomCatId = () => availableCatIds[Math.floor(Math.random() * availableCatIds.length)];

    const products = [
      { name: "Áo Polo Basic Trắng", material: "Cotton Pima", imageUrl: "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800&q=80", price: 350000 },
      { name: "Áo Thun Cổ Tròn Xám Khói", material: "Organic Cotton", imageUrl: "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80", price: 250000 },
      { name: "Quần Kaki Chinos Beige", material: "Khaki Cao Cấp", imageUrl: "https://images.unsplash.com/photo-1624378439575-d1eaa6ada46f?w=800&q=80", price: 450000 },
      { name: "Áo Sơ Mi Linen Trắng", material: "Linen Tự Nhiên", imageUrl: "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&q=80", price: 400000 },
      { name: "Áo Khoác Bomber Đen", material: "Polyester Chống Nước", imageUrl: "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80", price: 850000 },
      { name: "Áo Thun Dài Tay Kẻ Sọc", material: "Cotton 100%", imageUrl: "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=800&q=80", price: 290000 },
      { name: "Áo Polo Zip Thể Thao", material: "Spandex Thoáng Khí", imageUrl: "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800&q=80", price: 390000 },
      { name: "Quần Jeans Slim-Fit Xanh", material: "Denim Co Giãn", imageUrl: "https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=800&q=80", price: 550000 },
      { name: "Áo Len Cổ Lọ Nâu", material: "Len Merino", imageUrl: "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&q=80", price: 650000 },
      { name: "Áo Cardigan Xám Cổ Điển", material: "Len Mềm", imageUrl: "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=800&q=80", price: 600000 },
      { name: "Quần Đùi Short Linen Ngắn", material: "Linen Trúc", imageUrl: "https://images.unsplash.com/photo-1550995694-3f5f4a7e1bd2?w=800&q=80", price: 250000 },
      { name: "Áo Sơ Mi Vải Oxford Kẻ Xanh", material: "Vải Oxford Dày", imageUrl: "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80", price: 420000 },
      { name: "Áo Thun Graphic In Chữ", material: "Cotton Compact", imageUrl: "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800&q=80", price: 280000 },
      { name: "Áo Sơ Mi Ngắn Tay Trắng Trơn", material: "Lụa Nhẹ", imageUrl: "https://images.unsplash.com/photo-1505022610485-0249ba5b3675?w=800&q=80", price: 380000 },
      { name: "Áo Thun Basic Đen", material: "Organic Cotton", imageUrl: "https://images.unsplash.com/photo-1516826957135-700ede19ebc1?w=800&q=80", price: 220000 },
      { name: "Áo Hoodie Xám Năng Động", material: "Nỉ Bông Da Cá", imageUrl: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80", price: 480000 },
      { name: "Áo Khoác Gió Trắng Đen", material: "Gió Dù", imageUrl: "https://images.unsplash.com/photo-1507680434267-325608a91470?w=800&q=80", price: 590000 },
      { name: "Áo Sơ Mi Caro Đỏ Đen", material: "Flannel", imageUrl: "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800&q=80", price: 390000 },
      { name: "Quần Vải Tây Ống Rộng Đen", material: "Vải Tây Tuyết Mưa", imageUrl: "https://images.unsplash.com/photo-1618517351600-b6cb5f28c50c?w=800&q=80", price: 520000 },
      { name: "Áo Denim Jacket Xanh Bạc", material: "Denim Dày", imageUrl: "https://images.unsplash.com/photo-1485230895905-eb56b6c09b52?w=800&q=80", price: 790000 }
    ];

    console.log(`Starting to seed ${products.length} products...`);
    
    // Auth token needed? Assume local API is open or use a hardcoded basic auth if required.
    // The API might not require auth for POST /api/products based on previous context, or maybe it does?
    // Let's check if we have a token in the app or just try POST without it if it's admin.
    // Since we are running locally, let's just make the request.

    let createdCount = 0;

    for (let i = 0; i < products.length; i++) {
        const p = products[i];
        
        const payload = {
            name: p.name,
            categoryId: getRandomCatId(),
            material: p.material,
            description: `Sản phẩm ${p.name} thuộc phong cách Minimalist, mang đến sự tinh tế, thoải mái và đẳng cấp.`,
            imageUrl: p.imageUrl
        };

        const res = await fetch('http://localhost:8080/api/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const created = await res.json();
            const productId = created.id;

            // create SKUs
            const skus = [
                { productId, skuCode: `SKU-${Date.now()}-M`, size: 'M', color: 'Trắng', price: p.price, stock: 50 },
                { productId, skuCode: `SKU-${Date.now()}-L`, size: 'L', color: 'Đen', price: p.price + 20000, stock: 30 }
            ];

            for(const s of skus) {
                await fetch('http://localhost:8080/api/skus', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(s)
                });
            }
            createdCount++;
            console.log(`Created product: ${p.name} (ID: ${productId})`);
        } else {
            console.log(`Failed to create product: ${p.name}`, res.status, await res.text());
        }
    }
    
    console.log(`Finished! Successfully created ${createdCount} products.`);
  } catch(e) {
    console.error(e);
  }
}

seedProducts();
