%{
#include <stdio.h>
char *macros[8];
char *definitions[8];
void handle_definition(char *txt);
void handle_macro(char *txt);
%}

MACRO       [a-zA-Z]([a-zA-Z0-9]*)
DEFINE      #define[ ]{MACRO}[ ][^\n]*\n

%%

{DEFINE}        {handle_definition(yytext);}
{MACRO}         {handle_macro(yytext);}
<<EOF>>         {return 0;}
.|\n            {printf("%s", yytext);}

%%

int main() {
    for (int i = 0; i < 8; i++) {
        macros[i] = definitions[i] = 0;
    }
    yylex();
    for (int i = 0; i < 8; i++) {
        if (!macros[i]) break;
        free(macros[i]);
        free(definitions[i]);
    }
}

void handle_definition(char *txt) {
    txt += 8;
    int i;
    for (i = 0; txt[i] != ' '; i++);
    char *macro = (char *) malloc(sizeof(char) * (i + 1));
    for (int j = 0; j < i; j++) macro[j] = txt[j];
    macro[i] = '\0';
    char *definition = (char *) malloc(sizeof(char) * (strlen(txt)-1 - i));
    for (int j = 0; j < strlen(txt)-1 - i - 1; j++) {
        definition[j] = txt[j+i+1];
    }
    definition[strlen(txt)-1 - i - 1] = '\0';
    int j;
    for (j = 0; j < 8 && macros[j]; j++);
    if (j == 8) {
        fprintf(stderr, "Too many macro definitions\n");
        exit(-1);
    }
    macros[j] = macro;
    definitions[j] = definition;
}

void handle_macro(char *txt) {
    for (int i = 0; i < 8; i++) {
        if (!macros[i]) break;
        if (strcmp(macros[i], txt) == 0) {
            printf("%s", definitions[i]);
            return;
        }
    }
    printf("%s", txt);
    return;
}