package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.company.WorkorderCompanyMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 报价单文件生成服务实现类
 */
@Service
@Slf4j
public class QuotedPriceOrderFileGenerateServiceImpl implements QuotedPriceOrderFileGenerateService {

    @Resource
    private FileApi fileApi;

    @Resource
    private WorkorderCompanyMapper workorderCompanyMapper;

    /**
     * 文件类型：1-PDF 2-DOC 3-XLS
     */
    private static final int FILE_TYPE_PDF = 1;
    private static final int FILE_TYPE_DOC = 2;
    private static final int FILE_TYPE_XLS = 3;

    @Override
    public String generateFile(QuotedPriceOrderDO quotedPriceOrder) {
        if (quotedPriceOrder == null || quotedPriceOrder.getFileType() == null) {
            throw new IllegalArgumentException("报价单信息或文件类型不能为空");
        }

        // 1. 准备模板数据
        TemplateData templateData = buildTemplateData(quotedPriceOrder);
        
        log.info("准备生成报价单文件 - 报价单ID: {}, 工单名称: '{}', 收款企业: '{}', 付款企业: '{}', 文件类型: {}", 
                quotedPriceOrder.getId(), quotedPriceOrder.getConfirmOrderName(), 
                quotedPriceOrder.getReceiptCompanyName(), quotedPriceOrder.getPaymentCompanyName(), 
                quotedPriceOrder.getFileType());

        // 2. 根据文件类型选择模板并生成内容
        String content = generateContent(quotedPriceOrder.getFileType(), templateData);
        
        // 验证内容是否包含实际数据
        if (content.contains("{workOrderName}") || content.contains("{receiptCompanyName}") || 
            content.contains("{paymentCompanyName}") || content.contains("{price}")) {
            log.error("模板变量未完全替换！内容预览: {}", content.substring(0, Math.min(1000, content.length())));
            throw new RuntimeException("模板变量替换失败，请检查数据是否正确传递");
        }

        // 3. 生成文件名和路径
        String fileName = generateFileName(quotedPriceOrder);
        String directory = "quoted-price-order";

        // 4. 将HTML转换为指定格式的二进制文件
        byte[] fileBytes;
        String contentType;
        String fileExtension;
        try {
            switch (quotedPriceOrder.getFileType()) {
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
                    throw new IllegalArgumentException("不支持的文件类型：" + quotedPriceOrder.getFileType());
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
            log.info("生成报价单文件成功，文件路径：{}", fileUrl);
        } catch (Exception e) {
            log.error("保存报价单文件失败", e);
            throw new RuntimeException("保存报价单文件失败：" + e.getMessage(), e);
        }

        return fileUrl;
    }

    /**
     * 构建模板数据
     */
    private TemplateData buildTemplateData(QuotedPriceOrderDO quotedPriceOrder) {
        TemplateData data = new TemplateData();
        data.workOrderName = StrUtil.nullToEmpty(quotedPriceOrder.getConfirmOrderName());
        data.receiptCompanyName = StrUtil.nullToEmpty(quotedPriceOrder.getReceiptCompanyName());
        data.paymentCompanyName = StrUtil.nullToEmpty(quotedPriceOrder.getPaymentCompanyName());
        data.price = quotedPriceOrder.getPrice() != null ? quotedPriceOrder.getPrice() : BigDecimal.ZERO;
        data.quantity = quotedPriceOrder.getQuantity() != null ? quotedPriceOrder.getQuantity() : 0;
        data.totalPrice = quotedPriceOrder.getTotalPrice() != null ? quotedPriceOrder.getTotalPrice() : BigDecimal.ZERO;
        data.advancePayment = quotedPriceOrder.getAdvancePayment() != null ? quotedPriceOrder.getAdvancePayment() : BigDecimal.ZERO;
        data.finalPayment = quotedPriceOrder.getFinalPayment() != null ? quotedPriceOrder.getFinalPayment() : BigDecimal.ZERO;
        data.discountPrice = quotedPriceOrder.getDiscountPrice() != null ? quotedPriceOrder.getDiscountPrice() : BigDecimal.ZERO;
        data.remark = StrUtil.nullToEmpty(quotedPriceOrder.getRemark());
        data.currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        data.currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 付款时间：使用报价单的创建时间
        if (quotedPriceOrder.getCreateTime() != null) {
            data.paymentTime = quotedPriceOrder.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        } else {
            data.paymentTime = data.currentDate;
        }
        
        // 查询收款企业账户信息
        if (quotedPriceOrder.getReceiptCompanyId() != null) {
            WorkorderCompanyDO receiptCompany = workorderCompanyMapper.selectById(quotedPriceOrder.getReceiptCompanyId());
            if (receiptCompany != null) {
                data.receiptAccountName = StrUtil.nullToEmpty(receiptCompany.getName()); // 开户名使用企业名称
                data.receiptBankName = StrUtil.nullToEmpty(receiptCompany.getBankName()); // 开户银行
                data.receiptBankAccount = StrUtil.nullToEmpty(receiptCompany.getBankAccount()); // 银行账号
            } else {
                log.warn("未找到收款企业信息，receiptCompanyId: {}", quotedPriceOrder.getReceiptCompanyId());
                data.receiptAccountName = data.receiptCompanyName;
                data.receiptBankName = "";
                data.receiptBankAccount = "";
            }
        } else {
            data.receiptAccountName = data.receiptCompanyName;
            data.receiptBankName = "";
            data.receiptBankAccount = "";
        }
        
        // 格式化金额显示（保留2位小数）
        data.priceStr = formatAmount(data.price);
        data.totalPriceStr = formatAmount(data.totalPrice);
        data.advancePaymentStr = formatAmount(data.advancePayment);
        // 预付款说明文字
        data.advancePaymentWithNote = data.advancePaymentStr + " (" + data.advancePaymentStr + " 元为合同约定的预付款 (本次服务费用的 30%))";
        data.finalPaymentStr = formatAmount(data.finalPayment);
        // 合计字段：直接显示总价，不带括号和注释
        data.totalPriceWithNote = data.totalPriceStr;
        // 尾款字段：直接显示尾款，去掉括号
        data.finalPaymentWithNote = data.finalPaymentStr;
        data.discountPriceStr = formatAmount(data.discountPrice);
        
        return data;
    }

    /**
     * 格式化金额（保留2位小数）
     */
    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * 模板数据内部类
     */
    private static class TemplateData {
        String workOrderName;
        String receiptCompanyName;
        String paymentCompanyName;
        BigDecimal price;
        Integer quantity;
        BigDecimal totalPrice;
        BigDecimal advancePayment;
        BigDecimal finalPayment;
        BigDecimal discountPrice;
        String priceStr;
        String totalPriceStr;
        String advancePaymentStr;
        String advancePaymentWithNote; // 预付款带说明
        String finalPaymentStr;
        String totalPriceWithNote; // 合计带说明（按图片格式）
        String finalPaymentWithNote; // 尾款带重复显示（按图片格式）
        String discountPriceStr;
        String remark;
        String currentDate;
        String currentDateTime;
        String paymentTime; // 付款时间
        String receiptAccountName; // 收款账户开户名
        String receiptBankName; // 收款账户开户银行
        String receiptBankAccount; // 收款账户银行账号
    }

    /**
     * 根据文件类型生成内容
     */
    private String generateContent(Integer fileType, TemplateData templateData) {
        String templatePath;
        switch (fileType) {
            case FILE_TYPE_PDF:
                templatePath = "templates/quoted-price-order-pdf.html";
                break;
            case FILE_TYPE_DOC:
                templatePath = "templates/quoted-price-order-doc.html";
                break;
            case FILE_TYPE_XLS:
                templatePath = "templates/quoted-price-order-xls.html";
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
        
        // 替换基本变量
        result = result.replaceAll("\\{workOrderName\\}", StrUtil.nullToEmpty(data.workOrderName));
        result = result.replaceAll("\\{receiptCompanyName\\}", StrUtil.nullToEmpty(data.receiptCompanyName));
        result = result.replaceAll("\\{paymentCompanyName\\}", StrUtil.nullToEmpty(data.paymentCompanyName));
        result = result.replaceAll("\\{price\\}", data.priceStr);
        result = result.replaceAll("\\{quantity\\}", String.valueOf(data.quantity));
        result = result.replaceAll("\\{totalPrice\\}", data.totalPriceStr);
        result = result.replaceAll("\\{advancePayment\\}", data.advancePaymentStr);
        result = result.replaceAll("\\{advancePaymentWithNote\\}", StrUtil.nullToEmpty(data.advancePaymentWithNote));
        result = result.replaceAll("\\{finalPayment\\}", data.finalPaymentStr);
        result = result.replaceAll("\\{totalPriceWithNote\\}", StrUtil.nullToEmpty(data.totalPriceWithNote));
        result = result.replaceAll("\\{finalPaymentWithNote\\}", StrUtil.nullToEmpty(data.finalPaymentWithNote));
        result = result.replaceAll("\\{discountPrice\\}", data.discountPriceStr);
        result = result.replaceAll("\\{currentDate\\}", StrUtil.nullToEmpty(data.currentDate));
        result = result.replaceAll("\\{currentDateTime\\}", StrUtil.nullToEmpty(data.currentDateTime));
        
        // 替换付款通知单专用变量
        result = result.replaceAll("\\{paymentTime\\}", StrUtil.nullToEmpty(data.paymentTime));
        result = result.replaceAll("\\{receiptAccountName\\}", StrUtil.nullToEmpty(data.receiptAccountName));
        result = result.replaceAll("\\{receiptBankName\\}", StrUtil.nullToEmpty(data.receiptBankName));
        result = result.replaceAll("\\{receiptBankAccount\\}", StrUtil.nullToEmpty(data.receiptBankAccount));
        
        // 替换备注部分
        if (StrUtil.isNotBlank(data.remark)) {
            String remarkSection = generateRemarkSection(data.remark);
            result = result.replaceAll("\\{remarkSection\\}", remarkSection);
            result = result.replaceAll("\\{remarkRow\\}", generateRemarkRow(data.remark));
        } else {
            result = result.replaceAll("\\{remarkSection\\}", "");
            result = result.replaceAll("\\{remarkRow\\}", "");
        }
        
        return result;
    }

    /**
     * 生成备注部分（PDF/DOC）
     */
    private String generateRemarkSection(String remark) {
        return "<div class=\"remark-section\">" +
                "<div class=\"remark-header\">备注</div>" +
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
     */
    private String generateFileName(QuotedPriceOrderDO quotedPriceOrder) {
        String workOrderName = StrUtil.nullToEmpty(quotedPriceOrder.getConfirmOrderName())
                .replaceAll("[^\\w\\u4e00-\\u9fa5\\-\\s]", "_")
                .replaceAll("\\s+", "_")
                .trim();

        if (StrUtil.isBlank(workOrderName)) {
            workOrderName = "报价单";
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String extension = getFileExtensionByType(quotedPriceOrder.getFileType());

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
     * 将HTML转换为PDF
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // 使用Jsoup规范化HTML
            Document doc = Jsoup.parse(htmlContent);
            doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
            doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
            doc.outputSettings().prettyPrint(false);
            doc.outputSettings().charset(StandardCharsets.UTF_8);
            
            String xhtmlContent = doc.html();
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(xhtmlContent, null);
            
            // 加载中文字体
            loadChineseFonts(builder);
            
            builder.useFastMode();
            builder.toStream(os);
            builder.run();
            
            log.info("PDF转换成功，PDF大小: {} bytes", os.size());
            return os.toByteArray();
        } catch (Exception e) {
            log.error("HTML转PDF失败", e);
            throw new RuntimeException("HTML转PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载中文字体
     */
    private void loadChineseFonts(PdfRendererBuilder builder) {
        boolean fontLoaded = false;
        String[] resourceFonts = {
            "fonts/msyh.ttf",
            "fonts/NotoSansCJK-Regular.ttf",
            "fonts/SourceHanSansCN-Regular.otf"
        };
        
        for (String fontPath : resourceFonts) {
            try (InputStream fontStream = this.getClass().getClassLoader().getResourceAsStream(fontPath)) {
                if (fontStream != null) {
                    byte[] fontData = IoUtil.readBytes(fontStream);
                    if (fontData != null && fontData.length > 0) {
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
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("尝试加载资源字体失败: {}", fontPath, e);
            }
        }
        
        if (!fontLoaded) {
            log.warn("未找到任何中文字体文件，中文可能显示为方框或#");
        }
    }

    /**
     * 将HTML转换为DOC (Word文档)
     */
    private byte[] convertHtmlToDoc(String htmlContent) throws Exception {
        try {
            Document doc = Jsoup.parse(htmlContent);
            
            XWPFDocument document = new XWPFDocument();
            
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                // 创建标题段落
                XWPFParagraph titlePara = document.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText("报价单");
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
                            
                            XWPFParagraph para = document.createParagraph();
                            XWPFRun run = para.createRun();
                            run.setText(label + ": " + value);
                            run.addBreak();
                        }
                    }
                } else {
                    String text = doc.body().text();
                    XWPFParagraph para = document.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(text);
                }
                
                document.write(os);
                log.info("DOC转换成功，DOC大小: {} bytes", os.size());
                return os.toByteArray();
            } finally {
                document.close();
            }
        } catch (Exception e) {
            log.error("HTML转DOC失败", e);
            throw new RuntimeException("HTML转DOC失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将HTML转换为XLS (Excel文件) - 从HTML模板解析数据并生成Excel
     */
    private byte[] convertHtmlToXls(String htmlContent, TemplateData templateData) throws Exception {
        // 使用Jsoup解析HTML
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("付款通知单");
            
            // 创建样式
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle sectionHeaderStyle = createSectionHeaderStyle(workbook);
            CellStyle labelStyle = createLabelStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);
            CellStyle tableHeaderStyle = createTableHeaderStyle(workbook);
            CellStyle tableCellStyle = createTableCellStyle(workbook);
            
            int rowIndex = 0;
            
            // 从HTML提取标题 - 合并A/B/C/D四列
            org.jsoup.nodes.Element titleElement = doc.select(".title").first();
            if (titleElement != null) {
                Row titleRow = sheet.createRow(rowIndex++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(titleElement.text());
                titleCell.setCellStyle(titleStyle);
                titleRow.createCell(1);
                titleRow.createCell(2);
                titleRow.createCell(3);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 3));
                titleRow.setHeightInPoints(30);
            }
            
            // 空行
            rowIndex++;
            
            // 解析所有section
            org.jsoup.select.Elements sections = doc.select(".section");
            for (org.jsoup.nodes.Element section : sections) {
                // 解析section-header
                org.jsoup.nodes.Element sectionHeader = section.select(".section-header").first();
                if (sectionHeader != null) {
                    Row sectionRow = sheet.createRow(rowIndex++);
                    Cell sectionCell = sectionRow.createCell(0);
                    sectionCell.setCellValue(sectionHeader.text());
                    sectionCell.setCellStyle(sectionHeaderStyle);
                    sectionRow.createCell(1);
                    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));
                    sectionRow.setHeightInPoints(25);
                }
                
                // 解析表格（使用情况部分）
                org.jsoup.nodes.Element table = section.select(".usage-table").first();
                if (table != null) {
                    org.jsoup.select.Elements headerRows = table.select("thead tr");
                    if (!headerRows.isEmpty()) {
                        Row headerRow = sheet.createRow(rowIndex++);
                        org.jsoup.select.Elements headers = headerRows.first().select("th");
                        for (int i = 0; i < headers.size(); i++) {
                            Cell headerCell = headerRow.createCell(i);
                            headerCell.setCellValue(headers.get(i).text());
                            headerCell.setCellStyle(tableHeaderStyle);
                        }
                        headerRow.setHeightInPoints(20);
                    }
                    
                    org.jsoup.select.Elements dataRows = table.select("tbody tr");
                    for (org.jsoup.nodes.Element dataRowElement : dataRows) {
                        Row dataRow = sheet.createRow(rowIndex++);
                        org.jsoup.select.Elements cells = dataRowElement.select("td");
                        for (int i = 0; i < cells.size(); i++) {
                            Cell cell = dataRow.createCell(i);
                            String cellText = cells.get(i).text();
                            // 尝试解析为数字
                            if (i == 0 && StrUtil.isNumeric(cellText)) {
                                cell.setCellValue(Integer.parseInt(cellText));
                            } else if ((i == 2) && StrUtil.isNumeric(cellText)) {
                                cell.setCellValue(Double.parseDouble(cellText));
                            } else {
                                cell.setCellValue(cellText);
                            }
                            
                            // 如果是合计列，需要支持换行
                            if (i == 3) {
                                CellStyle wrapTextStyle = workbook.createCellStyle();
                                wrapTextStyle.cloneStyleFrom(tableCellStyle);
                                wrapTextStyle.setWrapText(true);
                                cell.setCellStyle(wrapTextStyle);
                                dataRow.setHeightInPoints(30);
                            } else {
                                cell.setCellStyle(tableCellStyle);
                            }
                        }
                    }
                } else {
                    // 解析info-row（付款方信息和收款账户信息部分）
                    org.jsoup.select.Elements infoRows = section.select(".info-row");
                    for (org.jsoup.nodes.Element infoRowElement : infoRows) {
                        Row infoRow = sheet.createRow(rowIndex++);
                        org.jsoup.nodes.Element labelElement = infoRowElement.select(".label").first();
                        org.jsoup.nodes.Element valueElement = infoRowElement.select(".value").first();
                        
                        if (labelElement != null) {
                            Cell labelCell = infoRow.createCell(0);
                            labelCell.setCellValue(labelElement.text());
                            labelCell.setCellStyle(labelStyle);
                        }
                        
                        if (valueElement != null) {
                            Cell valueCell = infoRow.createCell(1);
                            valueCell.setCellValue(valueElement.text());
                            valueCell.setCellStyle(valueStyle);
                            infoRow.createCell(2);
                            infoRow.createCell(3);
                            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex - 1, rowIndex - 1, 1, 3));
                        }
                        infoRow.setHeightInPoints(20);
                    }
                }
                
                // section之间添加空行
                if (rowIndex > 0) {
                    rowIndex++;
                }
            }
            
            // 设置列宽
            sheet.setColumnWidth(0, 3500); // A列
            sheet.setColumnWidth(1, 8000); // B列
            sheet.setColumnWidth(2, 3000); // C列
            sheet.setColumnWidth(3, 12000); // D列
            
            workbook.write(os);
            log.info("从HTML模板生成Excel文件成功");
            return os.toByteArray();
        } catch (Exception e) {
            log.error("从HTML模板生成Excel文件失败", e);
            throw new RuntimeException("从HTML模板生成Excel文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建标题样式（付款通知单）
     */
    private CellStyle createTitleStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建分区标题样式（一、付款方信息等，灰色背景）
     */
    private CellStyle createSectionHeaderStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        // 使用灰色背景
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建表头样式（灰色背景）
     */
    private CellStyle createTableHeaderStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        // 使用灰色背景
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建表格单元格样式
     */
    private CellStyle createTableCellStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createLabelStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createValueStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private void createDataRow(Sheet sheet, int rowIndex, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
    }
}
