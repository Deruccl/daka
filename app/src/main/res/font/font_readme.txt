Task 38.1: 自定义字体文件说明
=====================================

本目录用于存放 TimeMark 应用所需的自定义字体文件。
字体文件未随项目分发，请按本说明自行下载并放入此目录。

一、需要的字体文件列表
----------------------
1. Noto Sans SC（思源黑体简体中文，用于中文显示）
   - noto_sans_sc_regular.ttf  (常规, 400)
   - noto_sans_sc_medium.ttf   (中等, 500)
   - noto_sans_sc_bold.ttf     (粗体, 700)

2. Inter（用于英文与数字显示）
   - inter_regular.ttf  (常规, 400)
   - inter_medium.ttf   (中等, 500)
   - inter_bold.ttf     (粗体, 700)

3. JetBrains Mono（等宽字体，用于统计数字、代码等数据展示）
   - jetbrains_mono_regular.ttf  (常规, 400)
   - jetbrains_mono_bold.ttf     (粗体, 700)

二、下载地址
------------
- Noto Sans SC:  https://fonts.google.com/noto/specimen/Noto+Sans+SC
- Inter:         https://fonts.google.com/specimen/Inter
- JetBrains Mono: https://fonts.google.com/specimen/JetBrains+Mono

在 Google Fonts 页面点击 "Download family" 下载压缩包，
解压后选取对应字重的 TTF 文件，按上述文件名重命名后放入本目录。

三、文件命名规范
----------------
- 全部小写
- 单词以下划线分隔
- 字重后缀放在字体名之后：_regular / _medium / _bold
- 扩展名统一为 .ttf

示例：noto_sans_sc_regular.ttf

四、启用步骤
------------
1. 将 TTF 文件放入本目录（app/src/main/res/font/）
2. 打开同目录下的字体族 XML 配置文件：
   - noto_sans_sc.xml
   - inter.xml
   - jetbrains_mono.xml
3. 取消注释其中的 <font> 元素（删除 <!-- 与 -->）
4. 重新编译应用，字体将自动生效

五、降级方案
------------
若未提供 TTF 文件：
- Noto Sans SC / Inter 字体族将降级为系统默认字体（FontFamily.Default）
- JetBrains Mono 字体族将降级为系统等宽字体（FontFamily.Monospace）
- 应用功能不受影响，仅字体外观使用系统默认

降级逻辑在 core 模块的 Type.kt 中通过运行时资源查找实现，
无需修改代码即可在添加/删除字体文件后自动切换。
