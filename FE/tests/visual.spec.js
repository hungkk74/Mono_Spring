import { test, expect } from '@playwright/test';

// Sử dụng biến môi trường cho linh hoạt trong CI/CD, mặc định cổng 5173 (Vite/React)
const BASE_URL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('Kiểm thử giao diện (Visual Testing) - Trang chủ', () => {

  test('Trang chủ Mono Wear hiển thị đúng thiết kế (Visual Regression)', async ({ page }) => {
    // 1. Điều hướng tới trang chủ
    await page.goto(BASE_URL);

    // 2. Chờ phần tử cốt lõi xuất hiện (Thay thế cho 'networkidle')
    // Đợi Logo hoặc thẻ Heading của Mono Wear render xong để đảm bảo React đã mount hoàn toàn
    await expect(page.getByRole('heading', { name: /Mono Wear/i }).first()).toBeVisible();

    // 3. Kiểm tra Visual Regression
    // Playwright sẽ so sánh ảnh chụp hiện tại với ảnh chuẩn (baseline)
    // Chạy lần đầu tiên: Npx playwright test --update-snapshots để tạo ảnh gốc
    await expect(page).toHaveScreenshot('homepage-layout.png', { fullPage: true });

    // 4. Kiểm tra Title hoặc các thông tin SEO cơ bản
    await expect(page).toHaveTitle(/Mono Wear/i);
  });
});