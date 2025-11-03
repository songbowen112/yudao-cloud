package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.tag.WorkorderTagMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 确认单文件生成服务实现类
 */
@Service
@Slf4j
public class ConfirmOrderFileGenerateServiceImpl implements ConfirmOrderFileGenerateService {

    @Resource
    private FileApi fileApi;

    @Resource
    private WorkorderTagMapper workorderTagMapper;

    /**
     * 文件类型：1-PDF 2-DOC 3-XLS
     */
    private static final int FILE_TYPE_PDF = 1;
    private static final int FILE_TYPE_DOC = 2;
    private static final int FILE_TYPE_XLS = 3;

    @Override
    public String generateFile(ConfirmOrderDO confirmOrder) {
        if (confirmOrder == null || confirmOrder.getFileType() == null) {
            throw new IllegalArgumentException("确认单信息或文件类型不能为空");
        }

        // 1. 准备模板数据
        TemplateData templateData = buildTemplateData(confirmOrder);
        
        // 验证数据是否正确（用于调试）
        log.info("准备生成文件 - 工单ID: {}, 工单名称: '{}', 标的企业: '{}', 文件类型: {}", 
                confirmOrder.getId(), confirmOrder.getName(), 
                confirmOrder.getContractCompanyName(), 
                confirmOrder.getFileType());

        // 2. 根据文件类型选择模板并生成内容
        String content = generateContent(confirmOrder.getFileType(), templateData);
        
        // 验证内容是否包含实际数据（用于调试）
        if (content.contains("{workOrderName}") || content.contains("{contractCompanyName}") || 
            content.contains("{currentDate}")) {
            log.error("模板变量未完全替换！未替换的变量: workOrderName={}, contractCompanyName={}, currentDate={}", 
                    content.contains("{workOrderName}"), content.contains("{contractCompanyName}"), 
                    content.contains("{currentDate}"));
            log.error("替换后的内容预览: {}", content.substring(0, Math.min(1000, content.length())));
            throw new RuntimeException("模板变量替换失败，请检查数据是否正确传递");
        }
        
        // 验证内容是否包含实际数据值（不是空字符串）
        if (StrUtil.isNotBlank(templateData.workOrderName) && !content.contains(templateData.workOrderName)) {
            log.error("工单名称在替换后的内容中不存在！期望包含: '{}', 内容预览: {}", 
                    templateData.workOrderName, content.substring(0, Math.min(500, content.length())));
        }

        // 3. 生成文件名和路径
        String fileName = generateFileName(confirmOrder);
        String directory = "confirm-order";

        // 4. 将HTML转换为指定格式的二进制文件
        byte[] fileBytes;
        String contentType;
        String fileExtension;
        try {
            switch (confirmOrder.getFileType()) {
                case FILE_TYPE_PDF:
                    fileBytes = convertHtmlToPdf(content);
                    contentType = "application/pdf";
                    fileExtension = ".pdf";
                    break;
                case FILE_TYPE_DOC:
                    fileBytes = convertHtmlToDoc(content);
                    contentType = "application/msword";
                    fileExtension = ".doc";
                    break;
                case FILE_TYPE_XLS:
                    fileBytes = convertHtmlToXls(content, templateData);
                    contentType = "application/vnd.ms-excel";
                    fileExtension = ".xls";
                    break;
                default:
                    throw new IllegalArgumentException("不支持的文件类型：" + confirmOrder.getFileType());
            }
            log.info("HTML转换为{}格式成功，文件大小: {} bytes", fileExtension, fileBytes.length);
        } catch (Exception e) {
            log.error("转换文件格式失败", e);
            throw new RuntimeException("转换文件格式失败：" + e.getMessage(), e);
        }

        // 5. 更新文件名扩展名
        if (!fileName.endsWith(fileExtension)) {
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileName = fileName.substring(0, lastDotIndex) + fileExtension;
            } else {
                fileName = fileName + fileExtension;
            }
        }

        // 6. 保存文件
        String fileUrl;
        try {
            fileUrl = fileApi.createFile(fileBytes, fileName, directory, contentType);
            log.info("生成确认单文件成功，文件路径：{}", fileUrl);
        } catch (Exception e) {
            log.error("保存确认单文件失败", e);
            throw new RuntimeException("保存确认单文件失败：" + e.getMessage(), e);
        }

        return fileUrl;
    }

    /**
     * 构建模板数据
     */
    private TemplateData buildTemplateData(ConfirmOrderDO confirmOrder) {
        TemplateData data = new TemplateData();
        data.workOrderName = StrUtil.nullToEmpty(confirmOrder.getName());
        data.contractCompanyName = StrUtil.nullToEmpty(confirmOrder.getContractCompanyName());
        data.remark = StrUtil.nullToEmpty(confirmOrder.getRemark());
        data.currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        data.currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 解析标签并按照一级标签分类
        List<TagCategory> tagCategories = parseAndCategorizeTags(confirmOrder.getTagIds());
        data.tagCategories = tagCategories;
        
        // 生成标签HTML（用于PDF模板）
        data.tagList = generateTagListHtml(tagCategories);
        
        // 生成标签文本（用于DOC和XLS模板）
        data.tagText = generateTagText(tagCategories);
        
        log.info("构建模板数据 - 工单名称: {}, 标的企业: {}, 标签分类数量: {}", 
                data.workOrderName, data.contractCompanyName, tagCategories.size());
        
        return data;
    }

    /**
     * 解析标签ID并按照一级标签分类
     */
    private List<TagCategory> parseAndCategorizeTags(String tagIds) {
        if (StrUtil.isBlank(tagIds)) {
            return new ArrayList<>();
        }

        // 1. 解析标签ID
        List<Long> tagIdList = Arrays.stream(tagIds.split(","))
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(tagIdList)) {
            return new ArrayList<>();
        }

        // 2. 查询所有标签
        List<WorkorderTagDO> allTags = workorderTagMapper.selectBatchIds(tagIdList);
        if (CollUtil.isEmpty(allTags)) {
            log.warn("未找到标签信息，tagIds: {}", tagIds);
            return new ArrayList<>();
        }

        // 3. 建立标签ID到标签对象的映射
        Map<Long, WorkorderTagDO> tagMap = allTags.stream()
                .collect(Collectors.toMap(WorkorderTagDO::getId, tag -> tag));

        // 4. 找到所有一级标签（父标签ID为null或-1）
        Set<Long> parentTagIds = new HashSet<>();
        for (WorkorderTagDO tag : allTags) {
            Long parentTagId = tag.getParentTagId();
            if (parentTagId == null || parentTagId == -1L) {
                // 这是一级标签，添加到分类中
                parentTagIds.add(tag.getId());
            } else {
                // 这是二级标签，添加其父标签ID
                parentTagIds.add(parentTagId);
            }
        }

        // 5. 查询一级标签信息
        List<WorkorderTagDO> parentTags = workorderTagMapper.selectBatchIds(new ArrayList<>(parentTagIds));
        Map<Long, WorkorderTagDO> parentTagMap = parentTags.stream()
                .collect(Collectors.toMap(WorkorderTagDO::getId, tag -> tag));

        // 6. 按照一级标签分类组织数据
        Map<Long, TagCategory> categoryMap = new LinkedHashMap<>();
        
        for (WorkorderTagDO tag : allTags) {
            Long parentTagId = tag.getParentTagId();
            
            // 判断是否为一级标签
            if (parentTagId == null || parentTagId == -1L) {
                // 一级标签，直接作为分类
                Long categoryId = tag.getId();
                TagCategory category = categoryMap.computeIfAbsent(categoryId, k -> {
                    TagCategory cat = new TagCategory();
                    cat.parentTagId = categoryId;
                    cat.parentTagName = tag.getTagName();
                    cat.childTags = new ArrayList<>();
                    return cat;
                });
                // 一级标签本身也可以作为内容展示（如果需要）
            } else {
                // 二级标签，找到其父标签分类
                TagCategory category = categoryMap.computeIfAbsent(parentTagId, k -> {
                    TagCategory cat = new TagCategory();
                    cat.parentTagId = parentTagId;
                    WorkorderTagDO parentTag = parentTagMap.get(parentTagId);
                    if (parentTag != null) {
                        cat.parentTagName = parentTag.getTagName();
                    } else {
                        cat.parentTagName = "未分类";
                        log.warn("未找到父标签，parentTagId: {}", parentTagId);
                    }
                    cat.childTags = new ArrayList<>();
                    return cat;
                });
                category.childTags.add(tag);
            }
        }

        // 7. 按照父标签ID排序（确保顺序一致）
        List<TagCategory> result = new ArrayList<>(categoryMap.values());
        result.sort(Comparator.comparing(cat -> cat.parentTagId));

        log.info("标签分类完成 - 一级标签数量: {}, 总标签数量: {}", result.size(), allTags.size());

        return result;
    }

    /**
     * 标签分类数据结构
     */
    private static class TagCategory {
        Long parentTagId;
        String parentTagName;
        List<WorkorderTagDO> childTags;
    }

    /**
     * 生成标签列表HTML（用于PDF模板）
     */
    private String generateTagListHtml(List<TagCategory> tagCategories) {
        if (CollUtil.isEmpty(tagCategories)) {
            return "<div class=\"no-tags\">暂无标签</div>";
        }

        StringBuilder html = new StringBuilder();
        for (TagCategory category : tagCategories) {
            html.append("<div class=\"tag-category\">");
            html.append("<div class=\"tag-category-title\">").append(category.parentTagName).append("</div>");
            html.append("<div class=\"tag-category-content\">");
            
            if (CollUtil.isEmpty(category.childTags)) {
                html.append("<span class=\"tag-item-empty\">暂无子标签</span>");
            } else {
                for (WorkorderTagDO childTag : category.childTags) {
                    html.append("<span class=\"tag-item\">").append(childTag.getTagName()).append("</span>");
                }
            }
            
            html.append("</div>");
            html.append("</div>");
        }

        return html.toString();
    }

    /**
     * 生成标签文本（用于DOC和XLS模板）
     */
    private String generateTagText(List<TagCategory> tagCategories) {
        if (CollUtil.isEmpty(tagCategories)) {
            return "暂无标签";
        }

        StringBuilder text = new StringBuilder();
        for (TagCategory category : tagCategories) {
            text.append(category.parentTagName).append("：");
            
            if (CollUtil.isEmpty(category.childTags)) {
                text.append("暂无子标签");
            } else {
                String childTagNames = category.childTags.stream()
                        .map(WorkorderTagDO::getTagName)
                        .collect(Collectors.joining("、"));
                text.append(childTagNames);
            }
            
            text.append("\n");
        }

        return text.toString().trim();
    }

    /**
     * 模板数据内部类
     */
    private static class TemplateData {
        String workOrderName;
        String contractCompanyName;
        String tagList;  // HTML格式，用于PDF
        String tagText;  // 文本格式，用于DOC和XLS
        List<TagCategory> tagCategories;  // 结构化数据
        String remark;
        String currentDate;
        String currentDateTime;
    }

    /**
     * 根据文件类型生成内容
     */
    private String generateContent(Integer fileType, TemplateData templateData) {
        String templatePath;
        switch (fileType) {
            case FILE_TYPE_PDF:
                templatePath = "templates/confirm-order-pdf.html";
                break;
            case FILE_TYPE_DOC:
                templatePath = "templates/confirm-order-doc.html";
                break;
            case FILE_TYPE_XLS:
                templatePath = "templates/confirm-order-xls.html";
                break;
            default:
                throw new IllegalArgumentException("不支持的文件类型：" + fileType);
        }

        // 读取模板文件
        String template = loadTemplate(templatePath);
        
        // 替换模板变量
        return replaceTemplateVariables(template, templateData);
    }

    /**
     * 加载模板文件
     */
    private String loadTemplate(String templatePath) {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new RuntimeException("模板文件不存在: " + templatePath);
            }
            return IoUtil.readUtf8(inputStream);
        } catch (Exception e) {
            log.error("加载模板文件失败: {}", templatePath, e);
            throw new RuntimeException("加载模板文件失败: " + templatePath, e);
        }
    }

    /**
     * 替换模板变量
     */
    private String replaceTemplateVariables(String template, TemplateData data) {
        String result = template;
        
        // 记录替换前的数据（用于调试）
        log.debug("开始替换模板变量 - 工单名称: '{}', 标的企业: '{}'", 
                data.workOrderName, data.contractCompanyName);
        
        // 替换基本变量 - 确保所有变量都被替换，即使值为空也要替换
        String workOrderName = StrUtil.nullToEmpty(data.workOrderName);
        String contractCompanyName = StrUtil.nullToEmpty(data.contractCompanyName);
        String currentDate = StrUtil.nullToEmpty(data.currentDate);
        String currentDateTime = StrUtil.nullToEmpty(data.currentDateTime);
        
        // 如果数据为空，使用默认值（避免显示空白）
        if (StrUtil.isBlank(workOrderName)) {
            workOrderName = "未设置";
            log.warn("工单名称为空，使用默认值");
        }
        if (StrUtil.isBlank(contractCompanyName)) {
            contractCompanyName = "未设置";
            log.warn("标的企业名称为空，使用默认值");
        }
        
        // 执行替换，使用 replaceAll 确保替换所有出现
        result = result.replaceAll("\\{workOrderName\\}", workOrderName);
        result = result.replaceAll("\\{contractCompanyName\\}", contractCompanyName);
        result = result.replaceAll("\\{currentDate\\}", currentDate);
        result = result.replaceAll("\\{currentDateTime\\}", currentDateTime);
        
        // 替换标签列表（PDF使用HTML格式）
        if (StrUtil.isNotBlank(data.tagList)) {
            result = result.replaceAll("\\{tagList\\}", data.tagList);
        } else {
            result = result.replaceAll("\\{tagList\\}", "<div class=\"no-tags\">暂无标签</div>");
        }
        
        // 替换标签文本（DOC和XLS使用文本格式）
        if (StrUtil.isNotBlank(data.tagText)) {
            result = result.replaceAll("\\{tagText\\}", data.tagText);
        } else {
            result = result.replaceAll("\\{tagText\\}", "暂无标签");
        }
        
        // 替换备注部分
        if (StrUtil.isNotBlank(data.remark)) {
            String remarkSection = generateRemarkSection(data.remark);
            result = result.replaceAll("\\{remarkSection\\}", remarkSection);
            result = result.replaceAll("\\{remarkRow\\}", generateRemarkRow(data.remark));
        } else {
            result = result.replaceAll("\\{remarkSection\\}", "");
            result = result.replaceAll("\\{remarkRow\\}", "");
        }
        
        // 检查是否还有未替换的变量（用于调试）
        if (result.contains("{") && result.contains("}")) {
            String remainingVars = result.replaceAll(".*?\\{([^}]+)\\}.*", "$1");
            log.warn("模板中可能存在未替换的变量: {}", remainingVars);
        }
        
        // 验证替换结果（检查是否包含实际数据）
        if (!result.contains(workOrderName) && result.contains("{workOrderName}")) {
            log.error("工单名称替换失败！模板: {}, 数据: {}", result.substring(0, Math.min(200, result.length())), workOrderName);
        }
        
        log.debug("模板变量替换完成，结果长度: {}", result.length());
        
        return result;
    }

    /**
     * 生成备注部分（PDF/DOC）
     */
    private String generateRemarkSection(String remark) {
        return "<div class=\"remark-section\">" +
                "<div class=\"remark-title\">备注信息</div>" +
                "<div class=\"remark-content\">" + remark + "</div>" +
                "</div>";
    }

    /**
     * 生成备注行（XLS）
     */
    private String generateRemarkRow(String remark) {
        return "<tr>" +
                "<td class=\"label-cell\">备注</td>" +
                "<td class=\"value-cell\">" + remark + "</td>" +
                "</tr>";
    }

    /**
     * 生成文件名：工单名+年月日时分秒.扩展名
     * 例如：测试工单_20251101_205530.pdf
     */
    private String generateFileName(ConfirmOrderDO confirmOrder) {
        // 工单名称，去除特殊字符，只保留字母、数字、中文和常用符号
        String workOrderName = StrUtil.nullToEmpty(confirmOrder.getName())
                .replaceAll("[^\\w\\u4e00-\\u9fa5\\-\\s]", "_")
                .replaceAll("\\s+", "_") // 空格替换为下划线
                .trim();
        
        // 如果工单名为空，使用默认名称
        if (StrUtil.isBlank(workOrderName)) {
            workOrderName = "确认单";
        }
        
        // 生成时间戳：年月日时分秒，格式：yyyyMMdd_HHmmss
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        // 根据文件类型获取扩展名
        String extension = getFileExtensionByType(confirmOrder.getFileType());
        
        return workOrderName + "_" + timestamp + extension;
    }

    /**
     * 根据文件类型获取扩展名
     */
    private String getFileExtensionByType(Integer fileType) {
        if (fileType == null) {
            return ".html";
        }
        switch (fileType) {
            case FILE_TYPE_PDF:
                return ".pdf";
            case FILE_TYPE_DOC:
                return ".doc";
            case FILE_TYPE_XLS:
                return ".xls";
            default:
                return ".html";
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

    /**
     * 获取内容类型
     * 使用 HTML 格式，可以被浏览器、Word、Excel 等打开
     */
    private String getContentType(Integer fileType) {
        // 使用 text/html，这样生成的文件可以在浏览器和 Office 软件中打开
        return "text/html; charset=utf-8";
    }

    /**
     * 将HTML转换为PDF
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // 验证HTML内容是否包含实际数据（用于调试）
            String contentPreview = htmlContent.substring(0, Math.min(500, htmlContent.length()));
            log.debug("PDF转换 - HTML内容预览: {}", contentPreview);
            
            // 使用Jsoup规范化HTML，确保是有效的XHTML格式
            Document doc = Jsoup.parse(htmlContent);
            doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml); // 设置为XML格式
            doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml); // 使用XHTML转义
            doc.outputSettings().prettyPrint(false); // 不美化输出，保持紧凑格式
            doc.outputSettings().charset(StandardCharsets.UTF_8); // 确保使用UTF-8编码
            
            String xhtmlContent = doc.html();
            
            // 验证XHTML内容是否包含实际数据（用于调试）
            String xhtmlPreview = xhtmlContent.substring(0, Math.min(500, xhtmlContent.length()));
            log.debug("PDF转换 - XHTML内容预览: {}", xhtmlPreview);
            
            // 记录转换信息（用于调试）
            log.debug("PDF转换 - 原始HTML长度: {}, XHTML长度: {}", htmlContent.length(), xhtmlContent.length());
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(xhtmlContent, null);
            
            // 优先从 resources/fonts 目录加载中文字体
            boolean fontLoaded = false;
            
            // 尝试加载 resources/fonts 目录下的字体文件（优先使用 msyh.ttf 微软雅黑）
            String[] resourceFonts = {
                "fonts/msyh.ttf",  // 微软雅黑（用户提供的字体，最高优先级）
                "fonts/NotoSansCJK-Regular.ttf",  // Noto Sans CJK 字体（TTF格式，备选）
                "fonts/SourceHanSansCN-Regular.otf",  // 思源黑体（OTF格式，备选）
                "fonts/SimSun.ttf",  // 宋体（如果存在）
                "fonts/Microsoft-YaHei.ttf"  // 微软雅黑（如果存在）
            };
            
            for (String fontPath : resourceFonts) {
                try (InputStream fontStream = this.getClass().getClassLoader().getResourceAsStream(fontPath)) {
                    if (fontStream != null) {
                        // 将字体文件读入内存
                        byte[] fontData = IoUtil.readBytes(fontStream);
                        
                        if (fontData == null || fontData.length == 0) {
                            log.warn("字体文件为空: {}", fontPath);
                            continue;
                        }
                        
                        // 为多个字体名称注册同一个字体数据，以支持不同的 CSS 字体名称
                        // 注意：openhtmltopdf 要求字体名称必须与 CSS 中使用的名称完全匹配
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "SimSun");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Microsoft YaHei");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "SimHei");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "STHeiti");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "PingFang SC");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Arial Unicode MS");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "sans-serif");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Noto Sans CJK");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Source Han Sans");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Source Han Sans CN");
                        
                        log.info("✓ 成功从资源目录加载中文字体: {} (大小: {} bytes)", fontPath, fontData.length);
                        fontLoaded = true;
                        break; // 找到一个字体就足够了
                    } else {
                        log.debug("资源字体文件不存在: {}", fontPath);
                    }
                } catch (Exception e) {
                    log.warn("加载资源字体失败: {} - {}", fontPath, e.getMessage());
                }
            }
            
            // 如果资源目录没有字体，尝试从系统字体目录加载（作为回退）
            if (!fontLoaded) {
                log.info("资源目录未找到中文字体，尝试从系统目录加载...");
                String osName = System.getProperty("os.name").toLowerCase();
                java.io.File fontFile = null;
                
                if (osName.contains("win")) {
                    // Windows 系统字体目录
                    String[] winFonts = {"C:/Windows/Fonts/simsun.ttc", "C:/Windows/Fonts/simsun.ttf", 
                                        "C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/msyh.ttf"};
                    for (String fontPath : winFonts) {
                        java.io.File f = new java.io.File(fontPath);
                        if (f.exists()) {
                            fontFile = f;
                            break;
                        }
                    }
                } else if (osName.contains("mac")) {
                    // macOS 系统字体目录
                    String[] macFonts = {"/System/Library/Fonts/PingFang.ttc", "/Library/Fonts/SimSun.ttf",
                                        "/System/Library/Fonts/STHeiti Light.ttc"};
                    for (String fontPath : macFonts) {
                        java.io.File f = new java.io.File(fontPath);
                        if (f.exists()) {
                            fontFile = f;
                            break;
                        }
                    }
                } else {
                    // Linux 系统字体目录
                    String[] linuxFonts = {"/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                                          "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                                          "/usr/share/fonts/truetype/arphic/uming.ttc"};
                    for (String fontPath : linuxFonts) {
                        java.io.File f = new java.io.File(fontPath);
                        if (f.exists()) {
                            fontFile = f;
                            break;
                        }
                    }
                }
                
                // 如果找到系统字体文件，加载它
                if (fontFile != null && fontFile.exists()) {
                    try {
                        // 将字体文件读入内存
                        byte[] fontData = IoUtil.readBytes(new java.io.FileInputStream(fontFile));
                        
                        // 为多个字体名称注册同一个字体数据
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "SimSun");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Microsoft YaHei");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "SimHei");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "STHeiti");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "PingFang SC");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "Arial Unicode MS");
                        builder.useFont(() -> new java.io.ByteArrayInputStream(fontData), "sans-serif");
                        
                        log.info("成功从系统目录加载中文字体: {} (大小: {} bytes)", fontFile.getAbsolutePath(), fontData.length);
                        fontLoaded = true;
                    } catch (Exception e) {
                        log.warn("加载系统字体失败: {}", fontFile.getAbsolutePath(), e);
                    }
                }
            }
            
            if (!fontLoaded) {
                log.warn("未找到任何中文字体文件，中文可能显示为方框或#。请确保 resources/fonts 目录下有中文字体文件");
            }
            
            // 设置快速模式（可选，可以提高性能）
            builder.useFastMode();
            
            builder.toStream(os);
            builder.run();
            
            log.info("PDF转换成功，PDF大小: {} bytes", os.size());
            
            return os.toByteArray();
        } catch (Exception e) {
            log.error("HTML转PDF失败，HTML内容预览: {}", htmlContent.substring(0, Math.min(1000, htmlContent.length())), e);
            throw new RuntimeException("HTML转PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将HTML转换为DOC (Word文档)
     */
    private byte[] convertHtmlToDoc(String htmlContent) throws Exception {
        try {
            // 验证HTML内容是否包含实际数据（用于调试）
            String contentPreview = htmlContent.substring(0, Math.min(500, htmlContent.length()));
            log.debug("DOC转换 - HTML内容预览: {}", contentPreview);
            
            // 解析HTML
            Document doc = Jsoup.parse(htmlContent);
            
            // 创建新的 Word 文档
            XWPFDocument document = new XWPFDocument();
            
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                // 创建标题段落
                XWPFParagraph titlePara = document.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText("确认单");
                titleRun.setBold(true);
                titleRun.setFontSize(20);
                titleRun.addBreak();
                titleRun.addBreak();
                
                // 提取表格数据
                Elements tables = doc.select("table");
                if (!tables.isEmpty()) {
                    Element table = tables.first();
                    Elements rows = table.select("tr");
                    
                    for (Element row : rows) {
                        Elements cells = row.select("td, th");
                        if (cells.size() == 2) {
                            String label = cells.get(0).text();
                            String value = cells.get(1).text();
                            
                            // 记录提取的数据（用于调试）
                            log.debug("DOC转换 - 提取数据: {} = {}", label, value);
                            
                            XWPFParagraph para = document.createParagraph();
                            XWPFRun run = para.createRun();
                            run.setText(label + ": " + value);
                            run.addBreak();
                        }
                    }
                } else {
                    // 如果没有表格，提取所有文本内容
                    String text = doc.body().text();
                    log.debug("DOC转换 - 提取的文本内容: {}", text.substring(0, Math.min(200, text.length())));
                    
                    XWPFParagraph para = document.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(text);
                }
                
                // 写入文档到输出流
                document.write(os);
                log.info("DOC转换成功，DOC大小: {} bytes", os.size());
                return os.toByteArray();
            } finally {
                // 确保文档被关闭
                document.close();
            }
        } catch (Exception e) {
            log.error("HTML转DOC失败，HTML内容预览: {}", htmlContent.substring(0, Math.min(1000, htmlContent.length())), e);
            throw new RuntimeException("HTML转DOC失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将HTML转换为XLS (Excel文件)
     */
    private byte[] convertHtmlToXls(String htmlContent, TemplateData templateData) throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("确认单信息");
            
            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderTop(BorderStyle.THIN);
            labelStyle.setBorderLeft(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle valueStyle = workbook.createCellStyle();
            valueStyle.setBorderBottom(BorderStyle.THIN);
            valueStyle.setBorderTop(BorderStyle.THIN);
            valueStyle.setBorderLeft(BorderStyle.THIN);
            valueStyle.setBorderRight(BorderStyle.THIN);
            valueStyle.setWrapText(true);
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("确认单信息");
            titleCell.setCellStyle(headerStyle);
            
            Cell titleCell2 = titleRow.createCell(1);
            titleCell2.setCellStyle(headerStyle);
            
            // 合并标题单元格
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
            titleRow.setHeightInPoints(30);
            
            int rowIndex = 2;
            
            // 工单名称
            Row row1 = sheet.createRow(rowIndex++);
            Cell label1 = row1.createCell(0);
            label1.setCellValue("工单名称");
            label1.setCellStyle(labelStyle);
            Cell value1 = row1.createCell(1);
            value1.setCellValue(templateData.workOrderName);
            value1.setCellStyle(valueStyle);
            
            // 标的企业
            Row row2 = sheet.createRow(rowIndex++);
            Cell label2 = row2.createCell(0);
            label2.setCellValue("标的企业");
            label2.setCellStyle(labelStyle);
            Cell value2 = row2.createCell(1);
            value2.setCellValue(templateData.contractCompanyName);
            value2.setCellStyle(valueStyle);
            
            // 标签列表
            Row row4 = sheet.createRow(rowIndex++);
            Cell label4 = row4.createCell(0);
            label4.setCellValue("标签列表");
            label4.setCellStyle(labelStyle);
            Cell value4 = row4.createCell(1);
            value4.setCellValue(templateData.tagText != null ? templateData.tagText : "暂无标签");
            value4.setCellStyle(valueStyle);
            
            // 生成日期
            Row row5 = sheet.createRow(rowIndex++);
            Cell label5 = row5.createCell(0);
            label5.setCellValue("生成日期");
            label5.setCellStyle(labelStyle);
            Cell value5 = row5.createCell(1);
            value5.setCellValue(templateData.currentDate);
            value5.setCellStyle(valueStyle);
            
            // 备注
            if (StrUtil.isNotBlank(templateData.remark)) {
                Row row6 = sheet.createRow(rowIndex++);
                Cell label6 = row6.createCell(0);
                label6.setCellValue("备注");
                label6.setCellStyle(labelStyle);
                Cell value6 = row6.createCell(1);
                value6.setCellValue(templateData.remark);
                value6.setCellStyle(valueStyle);
            }
            
            // 设置列宽
            sheet.setColumnWidth(0, 4000); // 标签列
            sheet.setColumnWidth(1, 12000); // 值列
            
            workbook.write(os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("HTML转XLS失败", e);
            throw new RuntimeException("HTML转XLS失败: " + e.getMessage(), e);
        }
    }
}
