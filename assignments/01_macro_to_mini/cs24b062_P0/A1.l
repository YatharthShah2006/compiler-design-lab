%{
#include "A1.tab.h"

%}

Integer         [0-9]+
Identifier      [a-zA-Z_][a-zA-Z_0-9]*
SingleComment   "//"[^\n]*\n
MultiComment    "/*"([^*]|(\*+[^*/]))*\*+"/"

%%

[ \t\r\n]         {}

"+"             {return PLUS;}
"-"             {return MINUS;}
"*"             {return STAR;}
"/"             {return SLASH;}
"="             {return EQ;}
"||"            {return OROR;}
"&&"            {return ANDAND;}
"!="            {return NEQ;}
"<="            {return LEQ;}
"["             {return LBRACKET;}
"]"             {return RBRACKET;}
"("             {return LPAREN;}
")"             {return RPAREN;}
"."             {return DOT;}
"!"             {return NOT;}
"{"             {return LBRACE;}
"}"             {return RBRACE;}
";"             {return SEMICOLON;}
","             {return COMMA;}

"class"         {return CLASS;}
"true"          {return TRUE;}
"false"         {return FALSE;}
"if"            {return IF;}
"else"          {return ELSE;}
"while"         {return WHILE;}
"this"          {return THIS;}
"new"           {return NEW;}
"int"           {return INT;}
"boolean"       {return BOOL;}
"System.out.println"    {return PRINTLN;}
"length"        {return LENGTH;}
"public"        {return PUBLIC;}
"return"        {return RETURN;}
"static"        {return STATIC;}
"void"          {return VOID;}
"main"          {return MAIN;}
"extends"       {return EXTENDS;}
"String"        {return STRING;}

"#defineExpr"   {return DEFEXPR;}
"#defineExpr0"  {return DEFEXPR0;}
"#defineExpr1"  {return DEFEXPR1;}
"#defineExpr2"  {return DEFEXPR2;}
"#defineStmt"   {return DEFSTMT;}
"#defineStmt0"  {return DEFSTMT0;}
"#defineStmt1"  {return DEFSTMT1;}
"#defineStmt2"  {return DEFSTMT2;}

{Integer}       {yylval.str = strdup(yytext); return NUM;}
{Identifier}    {yylval.str = strdup(yytext); return IDENT;}

{SingleComment} {}
{MultiComment}  {}

.               {printf ("//Failed to parse input code\n"); exit (1);}

%%