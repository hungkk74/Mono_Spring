package com.monowear.dto.chatbot;

import com.monowear.dto.catalog.ProductResponse;
import java.util.List;

public record ChatBotResponse(String answer, List<ProductResponse> products) {}
