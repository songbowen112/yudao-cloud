package cn.iocoder.yudao.module.workorder.controller.admin.confirmorder;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.service.confirmorder.ConfirmOrderService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.CONFIRM_ORDER_NOT_EXISTS;

@Tag(name = "管理后台 - 确认单")
@RestController
@RequestMapping("/workorder/confirm-order")
@Validated
@Slf4j
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建确认单")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:create')")
    public CommonResult<Long> create(@Valid @RequestBody ConfirmOrderSaveReqVO createReqVO) {
        return success(confirmOrderService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新确认单")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ConfirmOrderSaveReqVO updateReqVO) {
        confirmOrderService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除确认单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        confirmOrderService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得确认单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public CommonResult<ConfirmOrderRespVO> get(@RequestParam("id") Long id) {
        ConfirmOrderDO data = confirmOrderService.get(id);
        return success(BeanUtils.toBean(data, ConfirmOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得确认单分页")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public CommonResult<PageResult<ConfirmOrderRespVO>> page(ConfirmOrderPageReqVO pageReqVO) {
        PageResult<ConfirmOrderDO> pageResult = confirmOrderService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ConfirmOrderRespVO.class));
    }

    @GetMapping("/list-init")
    @Operation(summary = "查询所有初始化状态的确认单")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public CommonResult<List<ConfirmOrderRespVO>> listInitStatus() {
        List<ConfirmOrderDO> list = confirmOrderService.getListByInitStatus();
        return success(BeanUtils.toBean(list, ConfirmOrderRespVO.class));
    }

    @GetMapping("/download")
    @Operation(summary = "下载确认单文件")
    @Parameter(name = "id", description = "确认单编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public void downloadFile(@RequestParam("id") Long id, HttpServletResponse response) throws Exception {
        // 1. 查询确认单信息
        ConfirmOrderDO confirmOrder = confirmOrderService.get(id);
        if (confirmOrder == null) {
            throw exception(CONFIRM_ORDER_NOT_EXISTS);
        }

        // 2. 检查文件路径是否存在
        if (StrUtil.isBlank(confirmOrder.getFileUrl())) {
            throw new IllegalArgumentException("确认单文件不存在，请先生成文件");
        }

        String fileUrl = confirmOrder.getFileUrl();
        log.info("下载确认单文件, id: {}, fileUrl: {}", id, fileUrl);

        // 3. 从URL下载文件内容并返回
        downloadFileFromUrl(fileUrl, confirmOrder.getName(), confirmOrder.getFileType(), response);
    }

    /**
     * 从URL下载文件并返回给前端
     */
    private void downloadFileFromUrl(String fileUrl, String defaultFileName, Integer fileType, HttpServletResponse response) throws Exception {
        java.io.InputStream inputStream = null;
        try {
            // 使用HttpURLConnection来更好地控制HTTP请求
            URL url = new URL(fileUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            
            // 设置请求属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setReadTimeout(30000); // 30秒读取超时
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);
            
            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                log.error("HTTP请求失败, fileUrl: {}, responseCode: {}", fileUrl, responseCode);
                throw new RuntimeException("无法获取文件内容，HTTP响应码: " + responseCode);
            }
            
            // 检查响应Content-Type
            String responseContentType = connection.getContentType();
            log.info("文件URL响应Content-Type: {}, fileUrl: {}", responseContentType, fileUrl);
            
            // 读取文件内容 - 使用BufferedInputStream确保完整读取
            inputStream = connection.getInputStream();
            java.io.BufferedInputStream bufferedInputStream = new java.io.BufferedInputStream(inputStream);
            byte[] content = IoUtil.readBytes(bufferedInputStream);
            bufferedInputStream.close();
            
            if (content == null || content.length == 0) {
                log.error("文件内容为空, fileUrl: {}", fileUrl);
                throw new RuntimeException("文件内容为空");
            }
            
            // 检查Content-Length是否匹配（如果服务器提供了）
            String contentLengthHeader = connection.getHeaderField("Content-Length");
            if (StrUtil.isNotBlank(contentLengthHeader)) {
                long expectedLength = Long.parseLong(contentLengthHeader);
                if (content.length != expectedLength) {
                    log.warn("文件大小不匹配, fileUrl: {}, 期望: {} bytes, 实际: {} bytes", fileUrl, expectedLength, content.length);
                }
            }
            
            log.info("从URL获取文件内容成功, fileUrl: {}, 文件大小: {} bytes, Content-Type: {}, Content-Length: {}", 
                    fileUrl, content.length, responseContentType, contentLengthHeader);
            
            // 从URL中提取文件名
            String fileName = extractFileNameFromUrl(fileUrl, defaultFileName, fileType);
            
            // 注意：生成的文件内容是HTML格式（因为我们使用的是HTML模板），所以即使扩展名是.pdf/.doc/.xls，内容也是HTML
            // 这是正常的，不需要抛出异常
            
            // 设置响应头并输出文件
            setResponseHeaders(response, fileName, content, responseContentType);
            
            // 写入响应流
            java.io.OutputStream outputStream = response.getOutputStream();
            outputStream.write(content);
            outputStream.flush();
            
            log.info("文件下载成功, fileUrl: {}, fileName: {}, size: {} bytes", fileUrl, fileName, content.length);
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            throw new RuntimeException("下载文件失败: " + e.getMessage(), e);
        } finally {
            // 确保关闭输入流
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
     * 从URL中提取文件名，如果提取失败则生成新文件名：工单名+年月日时分秒.扩展名
     */
    private String extractFileNameFromUrl(String fileUrl, String defaultFileName, Integer fileType) {
        String extension = getFileExtensionByType(fileType);
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (StrUtil.isNotBlank(path)) {
                String urlFileName = path.substring(path.lastIndexOf("/") + 1);
                if (StrUtil.isNotBlank(urlFileName)) {
                    // 如果URL中的文件名已经有正确的扩展名，直接使用
                    if (urlFileName.toLowerCase().endsWith(extension.toLowerCase())) {
                        return urlFileName;
                    }
                    // 如果有其他扩展名，替换为正确的扩展名
                    if (urlFileName.contains(".")) {
                        int lastDotIndex = urlFileName.lastIndexOf(".");
                        return urlFileName.substring(0, lastDotIndex) + extension;
                    }
                    // 如果没有扩展名，添加正确的扩展名
                    return urlFileName + extension;
                }
            }
        } catch (Exception e) {
            log.warn("从URL提取文件名失败: {}", fileUrl, e);
        }
        
        // 生成新文件名：工单名+年月日时分秒.扩展名
        return generateDownloadFileName(defaultFileName, fileType);
    }

    /**
     * 生成下载文件名：工单名+年月日时分秒.扩展名
     */
    private String generateDownloadFileName(String workOrderName, Integer fileType) {
        // 工单名称，去除特殊字符
        String name = StrUtil.nullToEmpty(workOrderName)
                .replaceAll("[^\\w\\u4e00-\\u9fa5\\-\\s]", "_")
                .replaceAll("\\s+", "_")
                .trim();
        
        // 如果工单名为空，使用默认名称
        if (StrUtil.isBlank(name)) {
            name = "确认单";
        }
        
        // 生成时间戳：年月日时分秒
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String timestamp = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        // 获取文件扩展名
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
        // 检测内容是否是HTML或文件扩展名是.html
        boolean isHtml = content.length > 0 && (isHtmlContent(content) || fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm"));
        
        // 确定Content-Type
        String contentType;
        if (isHtml) {
            // 如果内容是HTML或扩展名是.html，使用text/html
            // 这样浏览器和Office软件（Word、Excel）都能正确识别和打开HTML文件
            contentType = "text/html; charset=utf-8";
            log.info("使用text/html Content-Type，fileName: {}", fileName);
        } else if (StrUtil.isNotBlank(serverContentType)) {
            // 使用服务器返回的Content-Type
            contentType = serverContentType;
        } else {
            // 根据文件扩展名确定
            contentType = getContentType(fileName);
        }
        
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        
        // 设置Content-Disposition - 对于.html文件，可以使用inline让浏览器直接打开，或attachment强制下载
        // 这里使用attachment，让用户选择是打开还是保存
        String encodedFileName = HttpUtils.encodeUtf8(fileName);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\";filename*=UTF-8''" + encodedFileName);
        
        // 设置Content-Length
        response.setContentLengthLong(content.length);
        
        // 禁用缓存
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

    /**
     * 获取文件扩展名（已废弃，统一使用.html）
     * 保留此方法以兼容现有代码
     */
    @Deprecated
    private String getFileExtension(Integer fileType) {
        // 统一返回.html，因为生成的内容都是HTML格式
        return ".html";
    }
}


