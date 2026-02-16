clang -Xclang -ast-dump=json -fsyntax-only -std=c++17 -Xclang -ast-dump-filter=wui -ID:\dev\wui\include\ -ID:\dev\wui\thirdparty\ D:\dev\wui\src\control\image.cpp | Out-File -Encoding utf8 ast.json
