package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import cn.iocoder.yudao.module.workorder.service.quotedpriceorder.QuotedPriceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.QUOTED_PRICE_ORDER_NOT_EXISTS;

@Tag(name = "管理后台 - 报价单")
@RestController
@RequestMapping("/workorder/quoted-price-order")
@Validated
@Slf4j
public class QuotedPriceOrderController {

    @Resource
    private QuotedPriceOrderService quotedPriceOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建报价单")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:create')")
    public CommonResult<Long> create(@Valid @RequestBody QuotedPriceOrderSaveReqVO createReqVO) {
        return success(quotedPriceOrderService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报价单")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody QuotedPriceOrderSaveReqVO updateReqVO) {
        quotedPriceOrderService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报价单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        quotedPriceOrderService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报价单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:query')")
    public CommonResult<QuotedPriceOrderRespVO> get(@RequestParam("id") Long id) {
        QuotedPriceOrderDO data = quotedPriceOrderService.get(id);
        return success(BeanUtils.toBean(data, QuotedPriceOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报价单分页")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:query')")
    public CommonResult<PageResult<QuotedPriceOrderRespVO>> page(QuotedPriceOrderPageReqVO pageReqVO) {
        PageResult<QuotedPriceOrderDO> pageResult = quotedPriceOrderService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QuotedPriceOrderRespVO.class));
    }

    @GetMapping("/download")
    @Operation(summary = "下载报价单文件")
    @Parameter(name = "id", description = "报价单编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:query')")
    public void downloadFile(@RequestParam("id") Long id, HttpServletResponse response) throws Exception {
        // 1. 查询报价单信息
        QuotedPriceOrderDO quotedPriceOrder = quotedPriceOrderService.get(id);
        if (quotedPriceOrder == null) {
            throw exception(QUOTED_PRICE_ORDER_NOT_EXISTS);
        }

        // 2. 检查文件路径是否存在
        if (StrUtil.isBlank(quotedPriceOrder.getFileUrl())) {
            throw new IllegalArgumentException("报价单文件不存在，请先生成文件");
        }

        String fileUrl = quotedPriceOrder.getFileUrl();
        log.info("下载报价单文件, id: {}, fileUrl: {}", id, fileUrl);

        // 3. 从URL下载文件内容并返回
        downloadFileFromUrl(fileUrl, quotedPriceOrder.getConfirmOrderName(), quotedPriceOrder.getFileType(), response);
    }

    /**
     * 从URL下载文件并返回给前端
     */
    private void downloadFileFromUrl(String fileUrl, String defaultFileName, Integer fileType, HttpServletResponse response) throws Exception {
        InputStream inputStream = null;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("HTTP请求失败, fileUrl: {}, responseCode: {}", fileUrl, responseCode);
                throw new RuntimeException("无法获取文件内容，HTTP响应码: " + responseCode);
            }

            String responseContentType = connection.getContentType();
            log.info("文件URL响应Content-Type: {}, fileUrl: {}", responseContentType, fileUrl);

            inputStream = connection.getInputStream();
            java.io.BufferedInputStream bufferedInputStream = new java.io.BufferedInputStream(inputStream);
            byte[] content = IoUtil.readBytes(bufferedInputStream);
            bufferedInputStream.close();

            if (content == null || content.length == 0) {
                log.error("文件内容为空, fileUrl: {}", fileUrl);
                throw new RuntimeException("文件内容为空");
            }

            String fileName = extractFileNameFromUrl(fileUrl, defaultFileName, fileType);
            setResponseHeaders(response, fileName, content, responseContentType);

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(content);
            outputStream.flush();

            log.info("文件下载成功, fileUrl: {}, fileName: {}, size: {} bytes", fileUrl, fileName, content.length);
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            throw new RuntimeException("下载文件失败: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.warn("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl, String defaultFileName, Integer fileType) {
        String extension = getFileExtensionByType(fileType);
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (StrUtil.isNotBlank(path)) {
                String urlFileName = path.substring(path.lastIndexOf("/") + 1);
                if (StrUtil.isNotBlank(urlFileName)) {
                    if (urlFileName.toLowerCase().endsWith(extension.toLowerCase())) {
                        return urlFileName;
                    }
                    if (urlFileName.contains(".")) {
                        int lastDotIndex = urlFileName.lastIndexOf(".");
                        return urlFileName.substring(0, lastDotIndex) + extension;
                    }
                    return urlFileName + extension;
                }
            }
        } catch (Exception e) {
            log.warn("从URL提取文件名失败: {}", fileUrl, e);
        }

        return generateDownloadFileName(defaultFileName, fileType);
    }

    /**
     * 生成下载文件名：工单名+年月日时分秒.扩展名
     */
    private String generateDownloadFileName(String workOrderName, Integer fileType) {
        String name = StrUtil.nullToEmpty(workOrderName)
                .replaceAll("[^\\w\\u4e00-\\u9fa5\\-\\s]", "_")
                .replaceAll("\\s+", "_")
                .trim();

        if (StrUtil.isBlank(name)) {
            name = "报价单";
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String timestamp = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String extension = getFileExtensionByType(fileType);

        return name + "_" + timestamp + extension;
    }

    /**
     * 根据文件类型获取扩展名
     */
    private String getFileExtensionByType(Integer fileType) {
        if (fileType == null) {
            return ".html";
        }
        switch (fileType) {
            case 1: // PDF
                return ".pdf";
            case 2: // DOC
                return ".doc";
            case 3: // XLS
                return ".xls";
            default:
                return ".html";
        }
    }

    /**
     * 设置HTTP响应头
     */
    private void setResponseHeaders(HttpServletResponse response, String fileName, byte[] content, String serverContentType) throws IOException {
        boolean isHtml = content.length > 0 && (isHtmlContent(content) || fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm"));

        String contentType;
        if (isHtml) {
            contentType = "text/html; charset=utf-8";
            log.info("使用text/html Content-Type，fileName: {}", fileName);
        } else if (StrUtil.isNotBlank(serverContentType)) {
            contentType = serverContentType;
        } else {
            contentType = getContentType(fileName);
        }

        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");

        String encodedFileName = HttpUtils.encodeUtf8(fileName);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\";filename*=UTF-8''" + encodedFileName);

        response.setContentLengthLong(content.length);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * 检查内容是否是HTML
     */
    private boolean isHtmlContent(byte[] content) {
        if (content == null || content.length < 10) {
            return false;
        }
        try {
            String preview = new String(content, 0, Math.min(100, content.length), java.nio.charset.StandardCharsets.UTF_8).trim();
            return preview.startsWith("<!DOCTYPE") || preview.startsWith("<html") || preview.startsWith("<HTML");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据文件名获取Content-Type
     */
    private String getContentType(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "html":
            case "htm":
                return "text/html; charset=utf-8";
            default:
                return "application/octet-stream";
        }
    }
}


