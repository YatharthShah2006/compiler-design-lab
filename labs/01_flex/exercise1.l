
SPACE           [ ]
TAB             [\t]
TRAILING_SPACE_A    ({SPACE}|{TAB})+\n
TRAILING_SPACE_B    ({SPACE}|{TAB})+<<EOF>

%%

{TAB}               {printf("    ");}
{TRAILING_SPACE_A}    {printf("\n");}
{TRAILING_SPACE_B}    {return 0;}
.|\n                   {printf("%s", yytext);}

%%

int main() {
    yylex();
    return 0;
}