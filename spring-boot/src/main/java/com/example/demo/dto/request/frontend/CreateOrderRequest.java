package com.example.demo.dto.request.frontend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 創建訂單請求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "收件人姓名不能為空")
    @Size(max = 100, message = "收件人姓名不能超過100個字符")
    private String receiverName;

    @NotBlank(message = "收件人電話不能為空")
    @Size(max = 20, message = "收件人電話不能超過20個字符")
    private String receiverPhone;

    @NotBlank(message = "收件地址不能為空")
    @Size(max = 500, message = "收件地址不能超過500個字符")
    private String receiverAddress;

    @Size(max = 1000, message = "備註不能超過1000個字符")
    private String remark;
}

