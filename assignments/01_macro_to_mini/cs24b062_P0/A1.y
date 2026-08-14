%{
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

struct strlist;

int yylex (void);
void free_memory(void);
void yyerror (const char *);
char *join(char *a, char *b, int freeing);
int add_macro_expr(int argc, char *data, char *name, char *args);
char *handle_macro_expr(char *name, struct strlist list);
int add_macro_stmt(int argc, char *data, char *name, char *args);
char *handle_macro_stmt(char *name, struct strlist list);

%}

%union {
  char* str;
  struct strlist {
	char **data;
	int size;
  } list;
}

%glr-parser
%expect 2

%nonassoc LOWER_THAN_ELSE
%nonassoc ELSE

%nonassoc PRIMARY_REDUCE
%right EQ
%left OROR
%left ANDAND
%left NEQ LEQ
%left PLUS MINUS
%left STAR SLASH
%right NOT
%left DOT LBRACKET RBRACKET

%token <str> NUM
%token <str> IDENT
%token PLUS MINUS STAR SLASH EQ OROR ANDAND NEQ LEQ LBRACKET RBRACKET LPAREN RPAREN DOT NOT LBRACE RBRACE SEMICOLON COMMA CLASS TRUE FALSE IF ELSE WHILE THIS NEW INT BOOL PRINTLN LENGTH PUBLIC RETURN STATIC VOID MAIN EXTENDS STRING DEFEXPR DEFEXPR0 DEFEXPR1 DEFEXPR2 DEFSTMT DEFSTMT0 DEFSTMT1 DEFSTMT2

%type <str> type_declaration type_declarations method_decls method_decl decl_args decl_args_nonfirst type_idents type_ident type statements statement call_args_strings call_args_nonfirst_strings expr primary_expr int_literal main_class def_args

%type <list> call_args call_args_nonfirst

%destructor { free($$); } <str>

%destructor {
    for (int i = 0; i < $$.size; i++) {
        free($$.data[i]);
    }
    free($$.data);
} <list>

%%

goal: macro_definitions main_class type_declarations YYEOF {printf("%s\n", $2); printf("%s\n", $3); free($2); free($3);}
;

main_class: CLASS IDENT LBRACE PUBLIC STATIC VOID MAIN LPAREN STRING LBRACKET RBRACKET IDENT RPAREN LBRACE PRINTLN LPAREN expr RPAREN SEMICOLON RBRACE RBRACE                   {$$ = join(join(join(join(join(join("class ", $2, 1), " {\npublic static void main (String [] ", 2), $12, 3), ") {\nSystem.out.println(", 2), $17, 3), ");\n}\n}", 2);}
;

type_declarations:                                      {$$ = strdup("");}
                  | type_declaration type_declarations  {$$ = join(join($1, "\n", 2), $2, 3);}
;

type_declaration: CLASS IDENT LBRACE type_idents method_decls RBRACE                    {$$ = join(join(join(join(join(join("class ", $2, 1), " {\n", 2), $4, 3), "\n", 2), $5, 3), "\n}", 2);}
                  | CLASS IDENT EXTENDS IDENT LBRACE type_idents method_decls RBRACE    {$$ = join(join(join(join(join(join(join(join("class ", $2, 1), " extends ", 2), $4, 3), " {\n", 2), $6, 3), "\n", 2), $7, 3), "\n}", 2);}
;

method_decls:                                   {$$ = strdup("");}
              | method_decl method_decls        {$$ = join(join($1, "\n", 2), $2, 3);}
;

method_decl: PUBLIC type IDENT LPAREN decl_args RPAREN LBRACE type_idents statements RETURN expr SEMICOLON RBRACE {$$ = join(join(join(join(join(join(join(join(join(join(join(join("public ", $2, 1), " ", 2), $3, 3), " (", 2), $5, 3), ") {\n", 2), $8, 3), "\n", 2), $9, 3), "\nreturn ", 2), $11, 3), ";\n}", 2);}
;

decl_args:                                      {$$ = strdup("");}
          | type IDENT decl_args_nonfirst       {$$ = join(join(join($1, " ", 2), $2, 3), $3, 3);}
;

decl_args_nonfirst:                             {$$ = strdup("");}
                  | COMMA type IDENT decl_args_nonfirst {$$ = join(join(join(join(", ", $2, 1), " ", 2), $3, 3), $4, 3);}
;

type_idents:                                    {$$ = strdup("");}
              | type_ident type_idents          {if (strcmp($2, "") == 0) $$ = $1;
												else $$ = join(join($1, "\n", 2), $2, 3);}
;

type_ident: type IDENT SEMICOLON                {$$ = join(join(join($1, " ", 2), $2, 3), ";", 2);}
;

type: INT LBRACKET RBRACKET                     {$$ = strdup("int []");}
    | BOOL                                      {$$ = strdup("boolean");}
    | INT                                       {$$ = strdup("int");}
	| IDENT										{$$ = $1;}
;

statements:                                     {$$ = strdup("");}
            | statement statements              {if (strcmp($2, "") == 0) $$ = $1;
												else $$ = join(join($1, "\n", 2), $2, 3);}
;

statement: LBRACE statements RBRACE             {$$ = join(join("{\n", $2, 1), "\n}", 2);}
        | PRINTLN LPAREN expr RPAREN SEMICOLON  {$$ = join(join("System.out.println (", $3, 1), ");", 2);}
        | IDENT EQ expr SEMICOLON               {$$ = join(join(join($1, " = ", 2), $3, 3), ";", 2);}
        | IDENT LBRACKET expr RBRACKET EQ expr SEMICOLON        {$$ = join(join(join(join(join($1, " [", 2), $3, 3), "] = ", 2), $6, 3), ";", 2);}
        | IF LPAREN expr RPAREN statement %prec LOWER_THAN_ELSE       {$$ = join(join(join("if (", $3, 1), ") ", 2), $5, 3);}
        | IF LPAREN expr RPAREN statement ELSE statement        {$$ = join(join(join(join(join("if (", $3, 1), ") ", 2), $5, 3), "\nelse ", 2), $7, 3);}
        | WHILE LPAREN expr RPAREN statement    {$$ = join(join(join("while (", $3, 1), ") ", 2), $5, 3);}
        | IDENT LPAREN call_args RPAREN SEMICOLON       {$$ = handle_macro_stmt($1, $3); free($1); for (int i = 0; i < $3.size; i++) free($3.data[i]); free($3.data); if (!$$) YYERROR;}
;

call_args:                                      {$$.size = 0; $$.data = NULL;}
            | expr call_args_nonfirst
			{$$.size = $2.size + 1;
			$$.data = (char **) malloc(sizeof(char *) * $$.size);
			$$.data[0] = $1;
			memcpy($$.data + 1, $2.data, $2.size * (sizeof(char *))); if ($2.data) free($2.data);}
;

call_args_nonfirst:                             		{$$.size = 0; $$.data = NULL;}
                | COMMA expr call_args_nonfirst
			{$$.size = $3.size + 1;
			$$.data = (char **) malloc(sizeof(char *) * $$.size);
			$$.data[0] = $2;
			memcpy($$.data + 1, $3.data, $3.size * (sizeof(char *))); if ($3.data) free($3.data);}
;

call_args_strings:                                      {$$ = strdup("");}
            | expr call_args_nonfirst_strings         	{$$ = join($1, $2, 3);}
;

call_args_nonfirst_strings:                             		{$$ = strdup("");}
                  | COMMA expr call_args_nonfirst_strings       {$$ = join(join(", ", $2, 1), $3, 3);}
;

expr: primary_expr ANDAND primary_expr          {$$ = join(join($1, " && ", 2), $3, 3);}
    | primary_expr OROR primary_expr            {$$ = join(join($1, " || ", 2), $3, 3);}
    | primary_expr NEQ primary_expr             {$$ = join(join($1, " != ", 2), $3, 3);}
    | primary_expr LEQ primary_expr             {$$ = join(join($1, " <= ", 2), $3, 3);}
    | primary_expr PLUS primary_expr            {$$ = join(join($1, " + ", 2), $3, 3);}
    | primary_expr MINUS primary_expr           {$$ = join(join($1, " - ", 2), $3, 3);}
    | primary_expr STAR primary_expr            {$$ = join(join($1, " * ", 2), $3, 3);}
    | primary_expr SLASH primary_expr           {$$ = join(join($1, " / ", 2), $3, 3);}
    | primary_expr LBRACKET primary_expr RBRACKET        {$$ = join(join(join($1, " [", 2), $3, 3), "]", 2);}
    | primary_expr DOT LENGTH                    {$$ = join($1, ".length", 2);}
    | primary_expr DOT IDENT LPAREN call_args_strings RPAREN     {$$ = join(join(join(join(join($1, ".", 2), $3, 3), "(", 2), $5, 3), ")", 2);}
    | IDENT LPAREN call_args RPAREN              {$$ = handle_macro_expr($1, $3); free($1); for (int i = 0; i < $3.size; i++) free($3.data[i]); free($3.data); if (!$$) YYERROR;}
    | primary_expr %prec PRIMARY_REDUCE                               {$$ = $1;}
;

primary_expr: int_literal                       {$$ = $1;}
            | TRUE                              {$$ = strdup("true");}
            | FALSE                             {$$ = strdup("false");}
            | IDENT                             {$$ = $1;}
            | THIS                              {$$ = strdup("this");}
            | NEW INT LBRACKET expr RBRACKET    {$$ = join(join("new int [", $4, 1), "]", 2);}
            | NEW IDENT LPAREN RPAREN           {$$ = join(join("new ", $2, 1), "()", 2);}
            | NOT expr                          {$$ = join("!", $2, 1);}
            | LPAREN expr RPAREN                {$$ = join(join("(", $2, 1), ")", 2);}
;

macro_definitions:										{}
                | macro_definition macro_definitions	{}
;

macro_definition: def_expr								{}
                | def_stmt								{}
;

def_expr: DEFEXPR IDENT LPAREN IDENT COMMA IDENT COMMA IDENT def_args RPAREN LPAREN expr RPAREN	{char *b = join(join("(", $12, 1), ")", 2); char *d = join(join(join(join(join($4, ", ", 2), $6, 3), ", ", 2), $8, 3), $9, 3); int stat = add_macro_expr(-1, b, $2, d); free(b); free(d); free($2); if (stat) YYERROR;}
        | DEFEXPR0 IDENT LPAREN RPAREN LPAREN expr RPAREN										{char *b = join(join("(", $6, 1), ")", 2); int stat = add_macro_expr(0, b, $2, ""); free(b); free($2); if (stat) YYERROR;}
        | DEFEXPR1 IDENT LPAREN IDENT RPAREN LPAREN expr RPAREN									{char *b = join(join("(", $7, 1), ")", 2); int stat = add_macro_expr(1, b, $2, $4); free(b); free($2); free($4); if (stat) YYERROR;}
        | DEFEXPR2 IDENT LPAREN IDENT COMMA IDENT RPAREN LPAREN expr RPAREN						{char *b = join(join("(", $9, 1), ")", 2); char *d = join(join($4, ", ", 2), $6, 3); int stat = add_macro_expr(2, b, $2, d); free(b); free($2); free(d); if (stat) YYERROR;}
;

def_stmt: DEFSTMT IDENT LPAREN IDENT COMMA IDENT COMMA IDENT def_args RPAREN LBRACE statements RBRACE	{char *b = join(join("{\n", $12, 1), "\n}", 2); char *d = join(join(join(join(join($4, ", ", 2), $6, 3), ", ", 2), $8, 3), $9, 3); int stat = add_macro_stmt(-1, b, $2, d); free(b); free(d); free($2); if (stat) YYERROR;}
        | DEFSTMT0 IDENT LPAREN RPAREN LBRACE statements RBRACE									{char *b = join(join("{\n", $6, 1), "\n}", 2); int stat = add_macro_stmt(0, b, $2, ""); free(b); free($2); if (stat) YYERROR;}
        | DEFSTMT1 IDENT LPAREN IDENT RPAREN LBRACE statements RBRACE							{char *b = join(join("{\n", $7, 1), "\n}", 2); int stat = add_macro_stmt(1, b, $2, $4); free(b); free($2); free($4); if (stat) YYERROR;}
        | DEFSTMT2 IDENT LPAREN IDENT COMMA IDENT RPAREN LBRACE statements RBRACE				{char *b = join(join("{\n", $9, 1), "\n}", 2); char *d = join(join($4, ", ", 2), $6, 3); int stat = add_macro_stmt(2, b, $2, d); free(b); free($2); free(d); if (stat) YYERROR;}
;

def_args: 							{$$ = strdup("");}
        | COMMA IDENT def_args		{$$ = join(join(", ", $2, 1), $3, 3);}
;

int_literal: NUM				{$$ = $1;}
;

%%

void yyerror (const char *s) {
	printf ("//Failed to parse input code\n");
}

typedef enum {
	PARAM,
	DATA
} tokentype;

struct macro_data_node {
	tokentype t;
	union {
		char *data;
		int arg;
	};
	struct macro_data_node *next;
};

struct Definition_Data {
    int argc;
	char *name;
	struct macro_data_node *data;
};

struct Node {
	struct Definition_Data data;
	struct Node *next;
};

struct Node *head_expr;
struct Node *head_stmt;

int add_macro_expr(int argc, char *data, char *name, char *args) {
	for (struct Node *temp = head_expr; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			yyerror("Redefinition of macro.");
			return -1;
		}
	}
	
	for (struct Node *temp = head_stmt; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			yyerror("Redefinition of macro.");
			return -1;
		}
	}	
	
	int arg_count = 1;
	int s = strlen(args);
	for (int i = 0; i < strlen(args); i++) arg_count += (args[i] == ',');
	if (strlen(args) == 0) arg_count = 0;

	if (argc == -1) argc = arg_count;
	if (argc != arg_count) {
		yyerror("Wrong number of arguments in macro definition!");
		return -1;
	}

	char **passed_args = (char **) malloc(sizeof(char *) * argc);
	int start = 0;
	int i = 0;
	int ptr = 0;
	for (ptr = 1; ptr <= s; ptr++) {
		if (args[ptr] == ',' || args[ptr] == '\0') {
			passed_args[i] = (char *) malloc(sizeof(char) * (ptr - start + 1));
			memcpy(passed_args[i], args + start, ptr - start);
			passed_args[i][ptr - start] = '\0';
			for (int tmp = 0; tmp < i; tmp++) {
				if (strcmp(passed_args[i], passed_args[tmp]) == 0) {
					for (int j = 0; j <= i; j++) free(passed_args[j]);
					free(passed_args);
					yyerror("Duplicate argument name");
					return -1;
				}
			}
			i++;
			start = ptr + 2;
		}
	}

	struct Node *latest = (struct Node *) malloc(sizeof(struct Node));

	latest->data.argc = argc;
	latest->data.name = strdup(name);
	latest->data.data = NULL;
	
	latest->next = head_expr;
	head_expr = latest;

	struct macro_data_node **llptr = &(latest->data.data);
	start = 0;
	int curr_start = 0;
	s = strlen(data);
	for (ptr = 0; ptr <= s; ptr++) {
		if (isspace(data[ptr]) || (ispunct(data[ptr]) && !(data[ptr] == '_')) || data[ptr] == 0) {
			char temp = data[ptr];
			data[ptr] = '\0';
			
            int i;
			for (i = 0; i < argc; i++) {
				if (strcmp(passed_args[i], data + curr_start) == 0) {
					break;
				}
			}
			data[ptr] = temp;
			
            if (i == argc) {
				curr_start = ptr + 1;
			}
			else {
				if (curr_start > start) {
                    struct macro_data_node *latest = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
                    int len = curr_start - start;
                    latest->next = NULL;
                    latest->t = DATA;
                    latest->data = (char *) malloc (sizeof(char) * (len + 1));
                    memcpy(latest->data, data + start, len);
                    latest->data[len] = '\0';

                    *llptr = latest;
                    llptr = &(latest->next);
                }
				
				struct macro_data_node *lastarg = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
				lastarg->t = PARAM;
				lastarg->arg = i;
				lastarg->next = NULL;

				*llptr = lastarg;
				llptr = &(lastarg->next);

				start = ptr;
                curr_start = ptr + 1;
			}
		}
	}

    if (start < s) {
        struct macro_data_node *tail = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
        int len = s - start;
        tail->data = (char *) malloc(sizeof(char) * (len + 1));
        memcpy(tail->data, data + start, len);
        tail->data[len] = '\0';
        tail->t = DATA;
        tail->next = NULL;
        *llptr = tail;
    }

	for (int i = 0; i < arg_count; i++) free(passed_args[i]);
	free(passed_args);

	return 0;
}

char *handle_macro_expr(char *name, struct strlist list) {
	struct Node *temp;
	for (temp = head_expr; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			break;
		}
	}
	
	if (temp == NULL) {
		yyerror("Macro definition not found.");
		return NULL;
	}

	if (list.size != temp->data.argc) {
		yyerror("Wrong number of arguments in macro call.");
		return NULL;
	}

	if (list.size == 0) {
		char *buf = strdup("");
		for (struct macro_data_node *it = temp->data.data; it; it = it->next) {
			buf = join(buf, it->data, 2);
		}
		return buf;
	}

	char **passed_arguments = list.data;

    struct macro_data_node *it;
    char *buf = strdup("");
    for (it = temp->data.data; it; it = it->next) {
        if (it->t == DATA) {
            buf = join(buf, it->data, 2);
        }
        else {
            buf = join(buf, passed_arguments[it->arg], 2);
        }
    }

    return buf;
}

int add_macro_stmt(int argc, char *data, char *name, char *args) {
	for (struct Node *temp = head_stmt; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			yyerror("Redefinition of macro.");
			return -1;
		}
	}

	for (struct Node *temp = head_expr; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			yyerror("Redefinition of macro.");
			return -1;
		}
	}
	
	int arg_count = 1;
	int s = strlen(args);
	for (int i = 0; i < strlen(args); i++) arg_count += (args[i] == ',');
	if (strlen(args) == 0) arg_count = 0;

	if (argc == -1) argc = arg_count;
	if (argc != arg_count) {
		yyerror("Wrong number of arguments in macro definition!");
		return -1;
	}

	char **passed_args = (char **) malloc(sizeof(char *) * argc);
	int start = 0;
	int i = 0;
	int ptr = 0;
	for (ptr = 1; ptr <= s; ptr++) {
		if (args[ptr] == ',' || args[ptr] == '\0') {
			passed_args[i] = (char *) malloc(sizeof(char) * (ptr - start + 1));
			memcpy(passed_args[i], args + start, ptr - start);
			passed_args[i][ptr - start] = '\0';
			for (int tmp = 0; tmp < i; tmp++) {
				if (strcmp(passed_args[i], passed_args[tmp]) == 0) {
					for (int j = 0; j <= i; j++) free(passed_args[j]);
					free(passed_args);
					yyerror("Duplicate argument name");
					return -1;
				}
			}
			i++;
			start = ptr + 2;
		}
	}

	struct Node *latest = (struct Node *) malloc(sizeof(struct Node));

	latest->data.argc = argc;
	latest->data.name = strdup(name);
	latest->data.data = NULL;
	
	latest->next = head_stmt;
	head_stmt = latest;

	struct macro_data_node **llptr = &(latest->data.data);
	start = 0;
	int curr_start = 0;
	s = strlen(data);
	for (ptr = 0; ptr <= s; ptr++) {
		if (isspace(data[ptr]) || (ispunct(data[ptr]) && !(data[ptr] == '_')) || data[ptr] == 0) {
			char temp = data[ptr];
			data[ptr] = '\0';
			
            int i;
			for (i = 0; i < argc; i++) {
				if (strcmp(passed_args[i], data + curr_start) == 0) {
					break;
				}
			}
			data[ptr] = temp;
			
            if (i == argc) {
				curr_start = ptr + 1;
			}
			else {
				if (curr_start > start) {
                    struct macro_data_node *latest = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
                    int len = curr_start - start;
                    latest->next = NULL;
                    latest->t = DATA;
                    latest->data = (char *) malloc (sizeof(char) * (len + 1));
                    memcpy(latest->data, data + start, len);
                    latest->data[len] = '\0';

                    *llptr = latest;
                    llptr = &(latest->next);
                }
				
				struct macro_data_node *lastarg = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
				lastarg->t = PARAM;
				lastarg->arg = i;
				lastarg->next = NULL;

				*llptr = lastarg;
				llptr = &(lastarg->next);

				start = ptr;
                curr_start = ptr + 1;
			}
		}
	}

    if (start < s) {
        struct macro_data_node *tail = (struct macro_data_node *) malloc(sizeof(struct macro_data_node));
        int len = s - start;
        tail->data = (char *) malloc(sizeof(char) * (len + 1));
        memcpy(tail->data, data + start, len);
        tail->data[len] = '\0';
        tail->t = DATA;
        tail->next = NULL;
        *llptr = tail;
    }

	for (int i = 0; i < arg_count; i++) free(passed_args[i]);
	free(passed_args);

	return 0;
}

char *handle_macro_stmt(char *name, struct strlist list) {
	struct Node *temp;
	for (temp = head_stmt; temp; temp = temp->next) {
		if (strcmp(temp->data.name, name) == 0) {
			break;
		}
	}
	
	if (temp == NULL) {
		yyerror("Macro definition not found.");
		return NULL;
	}

	if (list.size != temp->data.argc) {
		yyerror("Wrong number of arguments in macro call.");
		return NULL;
	}

	if (list.size == 0) {
		char *buf = strdup("");
		for (struct macro_data_node *it = temp->data.data; it; it = it->next) {
			buf = join(buf, it->data, 2);
		}
		return buf;
	}

	char **passed_arguments = list.data;

    struct macro_data_node *it;
    char *buf = strdup("");
    for (it = temp->data.data; it; it = it->next) {
        if (it->t == DATA) {
            buf = join(buf, it->data, 2);
        }
        else {
            buf = join(buf, passed_arguments[it->arg], 2);
        }
    }

    return buf;
}

char *join(char *a, char *b, int freeing) {
	int l1 = strlen(a);
	int l2 = strlen(b);
	char *res = (char *) malloc(l1 + l2 + 1);
	memcpy(res, a, l1);
	memcpy(res + l1, b, l2);
	res[l1+l2] = '\0';
	if (freeing & 1) {
		free(b);
	}
	if (freeing & 2) {
		free(a);
	}
	return res;
}

int main () {
    head_expr = NULL;
    head_stmt = NULL;
	int status = yyparse();
	free_memory();
	return status;
}

void free_memory(void) {
	struct Node *temp = head_expr;
	while(temp) {
		struct Node *next = temp->next;
		free(temp->data.name);
		struct macro_data_node *node = temp->data.data;
		while(node) {
			struct macro_data_node *next_node = node->next;
			if (node->t == DATA) free(node->data);
			free(node);
			node = next_node;
		}
		free(temp);
		temp = next;
	}

	temp = head_stmt;
	while(temp) {
		struct Node *next = temp->next;
		free(temp->data.name);
		struct macro_data_node *node = temp->data.data;
		while(node) {
			struct macro_data_node *next_node = node->next;
			if (node->t == DATA) free(node->data);
			free(node);
			node = next_node;
		}
		free(temp);
		temp = next;
	}
}