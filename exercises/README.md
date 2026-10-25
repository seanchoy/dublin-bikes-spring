# Dublin Bikes → Spring Boot 练习册

题目、知识点、示例和提示都在 PDF 里：[dublin-bikes-spring-workbook.pdf](dublin-bikes-spring-workbook.pdf)。从“开始之前”一章读起。

## 打开 PDF

```bash
open exercises/dublin-bikes-spring-workbook.pdf
```

（在仓库根目录运行；也可以直接在 Finder 或 IDE 里双击打开。）

## 修改后重新编译

源文件在 `latex/`：`main.tex` 是主文件，`preamble.tex` 是样式，`parts/` 下每一关一个文件。需要 TeX Live（`xelatex`、`latexmk`、`ctex` 宏包）。

```bash
cd exercises/latex
latexmk -xelatex -outdir=build main.tex
cp build/main.pdf ../dublin-bikes-spring-workbook.pdf
```

`build/` 是编译的中间文件，已经加进 `.gitignore`。

## 其他

- 参考答案：`solutions/`，路径和 `src/main/java/ie/dublinbikes/` 一一对应。
