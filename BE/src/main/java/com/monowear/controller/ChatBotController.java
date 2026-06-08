package com.monowear.controller;

import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.chatbot.ChatBotResponse;
import com.monowear.dto.chatbot.ChatRequest;
import com.monowear.dto.common.ApiResponse;
import com.monowear.exception.BadRequestException;
import com.monowear.service.OpenRouterService;
import com.monowear.service.OrderService;
import com.monowear.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/public/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final OpenRouterService openRouterService;
    private final ProductService productService;
    private final OrderService orderService;

    private static final String SYSTEM_PROMPT = "Bạn là trợ lý ảo thông minh của cửa hàng thời trang MONO WEAR (chuyên quần áo tối giản, hiện đại).\n" +
            "Nhiệm vụ của bạn là hỗ trợ khách hàng mua sắm, tư vấn thời trang, giải đáp chính sách của cửa hàng một cách lịch sự, thân thiện và ngắn gọn bằng tiếng Việt.\n" +
            "Thông tin Mono Wear:\n" +
            "- Hotline: 1900 8888\n" +
            "- Email: support@monowear.vn\n" +
            "- Phương thức thanh toán: COD (nhận hàng trả tiền), chuyển khoản VietQR tự động, ví điện tử MoMo.\n" +
            "- Chính sách đổi trả: Lỗi nhà sản xuất hoặc giao sai được hỗ trợ đổi trả trong vòng 30 ngày. Hỗ trợ đổi size trong vòng 15 ngày. Đổi ý hoặc không vừa ý hỗ trợ trả hàng trong vòng 7 ngày kể từ ngày nhận hàng (sản phẩm phải còn nguyên trạng, nguyên tem mác và chưa qua sử dụng).\n" +
            "Quy định trả lời:\n" +
            "1. Nếu người dùng muốn kiểm tra trạng thái đơn hàng, hãy nhắc họ nhập mã đơn dạng \"#123\" hoặc bấm vào nút \"Tra đơn\" để hệ thống tự động truy vấn thông tin chính xác.\n" +
            "2. Nếu người dùng hỏi về sản phẩm, tư vấn thời trang hoặc tìm kiếm sản phẩm: Hãy tư vấn nhiệt tình, thân thiện, và BẮT BUỘC đính kèm cú pháp đặc biệt ở cuối câu trả lời dạng `[KEYWORDS: từ_khóa]` để hệ thống hiển thị danh sách sản phẩm. Tối đa 2 từ khóa, ví dụ: `[KEYWORDS: polo]` hoặc `[KEYWORDS: quần, áo]`.\n" +
            "3. Đối với các câu hỏi khác, hãy trả lời tự nhiên dựa trên thông tin trên.";

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatBotResponse>> chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new BadRequestException("Nội dung tin nhắn không được trống");
        }

        log.info("ChatBot stateful query: {}", request.message());

        // Tra đơn hàng nếu có mã
        String orderContext = "";
        Pattern orderPattern = Pattern.compile("#?(\\d{1,12})");
        Matcher orderMatcher = orderPattern.matcher(request.message());
        if (orderMatcher.find()) {
            try {
                Long orderId = Long.parseLong(orderMatcher.group(1));
                var orderTracking = orderService.getTrackingById(orderId);
                if (orderTracking != null) {
                    orderContext = String.format(
                        "\n[THÔNG TIN ĐƠN HÀNG THỰC TẾ]: Đơn hàng #%d có trạng thái vận chuyển hiện tại là: %s. Tổng giá trị thanh toán: %,.0f VNĐ. Ngày đặt hàng: %s. Phương thức thanh toán: %s.",
                        orderTracking.id(),
                        orderTracking.status(),
                        orderTracking.totalAmount(),
                        orderTracking.createdAt().toString(),
                        orderTracking.paymentMethod()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to fetch order tracking context for bot", e);
            }
        }

        // Thêm context user
        String finalSystemPrompt = SYSTEM_PROMPT;
        if (request.userContext() != null && request.userContext().fullName() != null && !request.userContext().fullName().isBlank()) {
            finalSystemPrompt += String.format(
                "\nThông tin tài khoản khách hàng đang nhắn tin: Tên: %s, Email: %s, Số sản phẩm hiện có trong giỏ hàng: %d." +
                " Hãy ưu tiên chào hỏi thân mật bằng tên riêng của họ (ví dụ: Chào anh/chị %s).",
                request.userContext().fullName(),
                request.userContext().email(),
                request.userContext().cartCount() != null ? request.userContext().cartCount() : 0,
                request.userContext().fullName()
            );
        }

        if (!orderContext.isEmpty()) {
            finalSystemPrompt += orderContext + "\nHãy dựa vào dữ liệu [THÔNG TIN ĐƠN HÀNG THỰC TẾ] phía trên để trả lời trực tiếp và tóm tắt ngắn gọn trạng thái đơn hàng này cho khách hàng.";
        }

        // Gọi AI
        String answer = openRouterService.chat(finalSystemPrompt, request.message(), request.history());

        List<ProductResponse> products = new ArrayList<>();
        String cleanAnswer = answer;

        Pattern pattern = Pattern.compile("\\[KEYWORDS:\\s*([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(answer);
        if (matcher.find()) {
            String keywordsStr = matcher.group(1);
            cleanAnswer = matcher.replaceAll("").trim();

            String[] keywords = keywordsStr.split(",");
            if (keywords.length > 0) {
                String primaryKeyword = keywords[0].trim();
                try {
                    var paged = productService.listActive(0, 8, null, primaryKeyword, null, null, false);
                    if (paged != null && paged.content() != null) {
                        products.addAll(paged.content());
                    }
                } catch (Exception e) {
                    log.error("Failed to query products for chatbot", e);
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(new ChatBotResponse(cleanAnswer, products)));
    }
}
