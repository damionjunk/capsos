# Some Notes

To build, export latex from org-mode, then:

```sh
buildvc.tex
latexmk -c && latexmk -pdflatex='xelatex --shell-escape' -pdf preso.tex
```
