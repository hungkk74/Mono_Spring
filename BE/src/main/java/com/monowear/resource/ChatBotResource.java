package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.chatbot.ChatBotResponse;
import com.monowear.service.OpenRouterService;
import com.monowear.service.ProductService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/api/public/chatbot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class ChatBotResource {

    private static final Logger LOG = Logger.getLogger(ChatBotResource.class);

    @Inject
    OpenRouterService openRouterService;

    @Inject
    ProductService productService;

    private static final String SYSTEM_PROMPT = "Bạn là trợ lý ảo thông minh của cửa hàng thời trang MONO WEAR (chuyên quần áo tối giản, hiện đại).\n" +
            "Nhiệm vụ của bạn là hỗ trợ khách hàng mua sắm, tư vấn thời trang, giải đáp chính sách của cửa hàng một cách lịch sự, thân thiện và ngắn gọn bằng tiếng Việt.\n" +
            "Thông tin Mono Wear:\n" +
            "- Hotline: 1900 8888\n" +
            "- Email: support@monowear.vn\n" +
            "- Phương thức thanh toán: COD (nhận hàng trả tiền), chuyển khoản VietQR tự động, ví điện tử MoMo.\n" +
            "- Chính sách đổi trả: Lỗi nhà sản xuất hoặc giao sai được hỗ trợ đổi trả trong vòng 30 ngày. Hỗ trợ đổi size trong vòng 15 ngày. Đổi ý hoặc không vừa ý hỗ trợ trả hàng trong vòng 7 ngày kể từ ngày nhận hàng (sản phẩm phải còn nguyên trạng, nguyên tem mác và chưa qua sử dụng).\n" +
            "Quy định trả lời:\n" +
            "1. Nếu người dùng muốn kiểm tra trạng thái đơn hàng, hãy nhắc họ nhập mã đơn dạng \"#123\" hoặc bấm vào nút \"Tra đơn\" để hệ thống tự động truy vấn thông tin chính xác.\n" +
            "2. Nếu người dùng hỏi về sản phẩm, tư vấn thời trang hoặc tìm kiếm sản phẩm: Hãy tư vấn nhiệt tình, thân thiện, và BẮT BUỘC đính kèm cú pháp đặc biệt ở cuối câu trả lời dạng `[KEYWORDS: từ_khóa]` để hệ thống hiển thị danh sách sản phẩm. Bạn chỉ được chọn các từ khóa trong danh sách này: polo, áo thun, thể thao, giày nam, giày nữ, đồng hồ, phụ kiện, quần, áo, sơ mi, khoác. Tối đa 2 từ khóa cách nhau bởi dấu phẩy, ví dụ: `[KEYWORDS: polo]` hoặc `[KEYWORDS: quần, áo]`. Nếu không liên quan đến gợi ý sản phẩm, KHÔNG cần thêm cú pháp này.\n" +
            "3. Đối với các câu hỏi khác, hãy trả lời tự nhiên dựa trên thông tin trên.";

    @POST
    @Path("/chat")
    public RestResponse<ApiResponse<ChatBotResponse>> chat(Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            throw new BadRequestException("Nội dung tin nhắn không được trống");
        }

        LOG.infof("ChatBot query: %s", message);
        String answer = openRouterService.chat(SYSTEM_PROMPT, message);
        
        List<ProductResponse> products = new ArrayList<>();
        String cleanAnswer = answer;
        
        // Trích xuất KEYWORDS bằng regex
        Pattern pattern = Pattern.compile("\\[KEYWORDS:\\s*([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(answer);
        if (matcher.find()) {
            String keywordsStr = matcher.group(1);
            // Xóa tag [KEYWORDS: ...] khỏi câu trả lời của AI
            cleanAnswer = matcher.replaceAll("").trim();
            
            String[] keywords = keywordsStr.split(",");
            if (keywords.length > 0) {
                // Lấy keyword đầu tiên để tìm kiếm
                String primaryKeyword = keywords[0].trim();
                try {
                    var paged = productService.listActive(0, 4, null, primaryKeyword, null, null, false);
                    if (paged != null && paged.content() != null) {
                        products.addAll(paged.content());
                    }
                } catch (Exception e) {
                    LOG.error("Failed to query products for chatbot", e);
                }
            }
        }

        return RestResponse.ok(ApiResponse.success(new ChatBotResponse(cleanAnswer, products)));
    }
}
