# 中文字体说明

## 字体文件下载

为了支持 PDF 文件中中文的正确显示，请将中文字体文件放在此目录下。

### 推荐字体（免费开源）

1. **思源黑体 (Source Han Sans)**
   - 下载地址: https://github.com/adobe-fonts/source-han-sans/releases
   - 文件: `SourceHanSansCN-Regular.otf` 或 `SourceHanSansCN-Regular.ttf`
   - 放置在: `fonts/SourceHanSansCN-Regular.otf`

2. **Noto Sans CJK**
   - 下载地址: https://github.com/googlefonts/noto-cjk
   - 文件: `NotoSansCJK-Regular.ttf`
   - 放置在: `fonts/NotoSansCJK-Regular.ttf`

### 手动下载步骤

1. 访问上述 GitHub 地址
2. 下载字体文件（OTF 或 TTF 格式均可）
3. 将字体文件重命名为对应的文件名并放置在此目录
4. 重启应用程序

### 当前支持的字体文件名

代码会自动尝试加载以下字体文件（按优先级排序）：

1. `fonts/NotoSansCJK-Regular.ttf`
2. `fonts/SourceHanSansCN-Regular.otf`
3. `fonts/SimSun.ttf`
4. `fonts/Microsoft-YaHei.ttf`

### 验证字体是否加载成功

查看应用日志，如果看到以下信息表示字体加载成功：

```
✓ 成功从资源目录加载中文字体: fonts/NotoSansCJK-Regular.ttf (大小: xxx bytes)
```

如果看到警告信息，表示字体文件不存在或加载失败，请检查：

1. 字体文件是否在 `resources/fonts` 目录下
2. 文件名是否完全匹配（区分大小写）
3. 文件是否损坏

