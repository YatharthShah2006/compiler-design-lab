%{
#include <stdio.h>
%}

DIGIT           [0-9]
INTEGER         [+-]?{DIGIT}+
DECIMAL         (({INTEGER}\.{DIGIT}*)|([+-]?\.{DIGIT}+))
REAL            (({INTEGER}|{DECIMAL})[Ee]{INTEGER})
DELIM           [^a-zA-Z_]

%%

{DELIM}{INTEGER}/{DELIM}       {printf("INTEGER: %s\n", yytext+1);}
{DELIM}{DECIMAL}/{DELIM}       {printf("DECIMAL: %s\n", yytext+1);}
{DELIM}{REAL}/{DELIM}          {printf("REAL: %s\n", yytext+1);}
<<EOF>>             {return 0;}
.|\n                {}


%%

int main() {
    yylex();
}