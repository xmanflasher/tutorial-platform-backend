package com.waterballsa.tutorial_platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("[GlobalExceptionHandler] Business Exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("[GlobalExceptionHandler] Access Denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "無權限存取此資源");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(RuntimeException ex, HttpServletRequest request) {
        log.warn("[GlobalExceptionHandler] Illegal Argument or State: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 處理 404 資源未找到異常 (Spring Boot 3.2+)
     * 確保路徑錯誤時回傳正確的 404 狀態碼，而非預設的 500
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 處理驗證失敗異常 (DTO @Valid 失敗時觸發)
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        log.warn("[GlobalExceptionHandler] Validation Failed: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "輸入格式錯誤: " + errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("[GlobalExceptionHandler] Unexpected error", ex);
        // 生產環境應隱藏類別名稱與詳細訊息，避免洩漏資料庫欄位或內部邏輯
        String safeMessage = "系統發生非預期錯誤，請聯繫管理員或稍後再試。";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, safeMessage);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
