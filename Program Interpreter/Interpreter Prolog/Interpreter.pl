valnum(X):- char_type(X, alnum), char_type(X, ascii).
vother(X):- member(X, [';','<','+','-','*','(',')','{','}']).
validc(X):- valnum(X) ; vother(X) ;  X == '='.

lparseq(['='|L],'==',L).
lparseq([X|L],'=',[X|L]):-dif(X,'=').
lparseq([],'=',[]).

lparsealn([X|L],L2,R,L3):- valnum(X), lparsealn(L, [X|L2], R, L3).
lparsealn([X|L],L2,R,[X|L]):- \+valnum(X), reverse(L2, L3), atom_chars(R, L3).
lparsealn([],L2,R,[]):- reverse(L2, L3), atom_chars(R, L3).

lparse2(['='|L],L2,L3):- lparseq(L,R,L4), lparse2(L4,[R|L2],L3).
lparse2([X|L],L2,L3):- valnum(X),lparsealn(L,[X],R,L4), lparse2(L4,[R|L2],L3).
lparse2([X|L],L2,L3):- vother(X), lparse2(L,[X|L2],L3).
lparse2([X|L],L2,L3):- \+validc(X), lparse2(L,L2,L3).
lparse2([],L2,L3):- reverse(L2,L3).

lparse(S, L):- atom_chars(S, L2), lparse2(L2,[],L),!.

																			%Parser

%Parser care verifica daca primul element al listei este unul trimis ca parametru
parse_atom(C,[C|T],C,T).

%Functie ce verifica daca un atom este alcatuit doar din caractere alfanumerice
is_string([H | T]):- char_type(H,alpha),is_string(T),!.
is_string([]).

%Functie ce parseaza un numar
parse_val([H | T],value(R),T):-atom_number(H,R),!.

%Functie ce parseaza o variabila si verifica respectarea cerintelor din enutul temei
parse_var([H | T],variable(H),T):- atom_chars(H,L),is_string(L), H \= 'return', H \= 'assert', H \= 'then' , H \= 'else', H \= 'if', H \= 'for', !.

%Functie ce parseaza un operand
parse_tok(L,O,R):-(parse_val(L,O,R);parse_var(L,O,R)),!.

%Functie ce parseaza operatia de inmultire si operanzi
parse_mult_expr(L,mult(X,Y),R):- parse_tok(L,X,R1),parse_atom('*',R1,_,R2),parse_mult_expr(R2,Y,R),!.
parse_mult_expr(L,O,R):-parse_tok(L,O,R),!.

%Functie ce parseaza adunari, inmultiri si operanzi
parse_add_expr(L,add(X,Y),R):- parse_mult_expr(L,X,R1),parse_atom('+',R1,_,R2),parse_add_expr(R2,Y,R),!.
parse_add_expr(L,O,R):- parse_mult_expr(L,O,R),!.

%Functie ce primeste un string si schimba semnele(+ in - si invers) pana la primul separator(<,=,),;)
change_signs(['+' | T], ['-' | R]) :- change_signs(T,R),!.
change_signs(['-' | T], ['+' | R]) :- change_signs(T,R),!.
change_signs([H | T], [H | R]) :- H \= ';' , H \= '=' , H \= '<', H \= ')' ,change_signs(T,R),!.
change_signs([H | T], [H | T]).

%Functie ce parseaza expresiile matematice prezente in tema
parse_sub_expr(L,sub(X,Y),R):- parse_add_expr(L,X,R1), parse_atom('-',R1,_,R2), change_signs(R2,R3), parse_sub_expr(R3,Y,R),!.
parse_sub_expr(L,O,R):- parse_add_expr(L,O,R),!.

%Functie ce parseaza expresii matematice si operatia de comparatie
parse_smaller_expr(L,smaller(X,Y),R):- parse_sub_expr(L,X,R1),parse_atom('<',R1,_,R2),parse_smaller_expr(R2,Y,R),!.
parse_smaller_expr(L,O,R):- parse_sub_expr(L,O,R),!.

%Functie ce parseaza orice fel de expresie din tema
parse_expression(L,equal(X,Y),R):- parse_smaller_expr(L,X,R1),parse_atom(==,R1,_,R2),parse_expression(R2,Y,R),!.
parse_expression(L,O,R) :- parse_smaller_expr(L,O,R),!.

%Functie ce parseaza o operatie de atribuire prin parsarea unei variabile si a parsarii expresiei de dupa
parse_assign(L,assign(Var,Expr),R):- parse_var(L,Var,R1),parse_atom(=,R1,_,R2),parse_expression(R2,Expr,R),!.

%Functie ce parseaza un return prin parsarea cuvantului cheie si parsarea expresiei de dupa
parse_return(L,ret(Expr),R):- parse_atom(return,L,_,R1),parse_expression(R1,Expr,R),!.

%Functie ce parseaza un assert prin parsarea cuvantului cheie ,parsarea tokenilor intermediari si parsarea expresiei
parse_assert(L,assert(Expr),R):- parse_atom(assert,L,_,R1),parse_atom('(',R1,_,R2),parse_expression(R2,Expr,R3),parse_atom(')',R3,_,R),!.

%Functie ce parseaza un bloc prin inceracarea tuturor parserelor pe expresia curenta si apelarea recursiva
%a functiei. Functia se opreste cand se gaseste o acolada inchisa( in cazul instructiunilor for si if) sau
%cand programul a ajuns la final
parse_seq(L,O,[]) :- try_parsers(L,O,[]),!.
parse_seq(L,O,['}' | T]):- try_parsers(L,O,['}' | T]),!.
parse_seq(L,seq(X,Y),R):- try_parsers(L,X,R1),parse_seq(R1,Y,R),!.

%Functie ce parseaza un if prin impunere pattern-ului specificat in enunt si parsarea tokenilor intermediari
parse_if(L,iff(Expr,Prog1,Prog2),R):- parse_atom(if,L,_,R1),parse_atom('(',R1,_,R2),parse_expression(R2,Expr,R3),parse_atom(')',R3,_,R4),
	parse_atom(then,R4,_,R5),parse_atom('{',R5,_,R6),parse_seq(R6,Prog1,R7),parse_atom('}',R7,_,R8),parse_atom(else,R8,_,R9),
	parse_atom('{',R9,_,R10),parse_seq(R10,Prog2,R11),parse_atom('}',R11,_,R),!.

%Functie ce parseaza for-ul prin impunerea pattern-ului specificat in enunt si parsarea tokenilor intermediari
parse_for(L,forr(Prog1,Expr,Prog2,Prog3),R):- parse_atom(for,L,_,R1),parse_atom('(',R1,_,R2),instruction_parsers(R2,Prog1,R3),
	parse_expression(R3,Expr,R4),parse_atom(;,R4,_,R5),parse_assign(R5,Prog2,R6),parse_atom(')',R6,_,R7),parse_atom('{',R7,_,R8),
	parse_seq(R8,Prog3,R9),parse_atom('}',R9,_,R),!.

%Functie ce incearca sa parseze prin elementele ce parseaz instructiuni si apoi parseaza tokenul ';' de la final. De asemenea,
%functia parseaza blocuri goale(care contin doar ;)
instruction_parsers(L,O,R):- ((parse_assert(L,O,R1) ; parse_assign(L,O,R1)); parse_return(L,O,R1)), parse_atom(;,R1,_,R),!.
instruction_parsers([; | T],no_instruction(),T).

%Functie ce incearca sa parseze prin if si for
loop_parsers(L,O,R):- (parse_if(L,O,R) ; parse_for(L,O,R)),!.

%Functie ce incearca toate tipurile de parsere
try_parsers(L,O,R):- instruction_parsers(L,O,R) ; loop_parsers(L,O,R),!.

														%Functii de prelucrare a unei mape

%Functie ce extrage un element dintr-o mapa(lista de perechi), cautand recursiv prin ea
getFromMap([(A,B) | _],A,B) :- !.
getFromMap([_ | T],A,R) :- getFromMap(T,A,R),!.

%Functie ce adauga noi elemente in mapa, si in caz ca ele exista, actualizeaza valoarea
addInMap([(A,_) | T],(A,C),[(A,C) | T]):-!.
addInMap([H | T],X,[H | R]):- addInMap(T,X,R),!.
addInMap([],X,[X]):-!.

																	%Interpretor

%Evaluarea unei constante se face prin intoarcearea valorii
evalExpr(_,value(A),A):- !.

%Pentru evaluarea variabilei se cauta valoarea in mapa si se intoarce valoarea corespunzatoare
%si in caz ca aceasta nu exista in mapa se intoarce eroare
evalExpr(Map,variable(X),R) :- getFromMap(Map,X,R),!.
evalExpr(_,variable(_),'e'):- !.

%Pentru evaluarea adunarii se evalueaza fiecare operand si in cazul in care niciunul nu intoarce
%eroare, se returneaza suma, altfel se returneaza eroare
evalExpr(Map,add(X,Y),R):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e', R is R1 + R2,!.
evalExpr(_,add(_,_),'e'):-!.

%Se procedeaza analog ca la adunare
evalExpr(Map,mult(X,Y),R):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e', R is R1 * R2,!.
evalExpr(_,mult(_,_),'e'):-!.

%Analog adunarii si inmultirii
evalExpr(Map,sub(X,Y),R):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e', R is R1 - R2,!.
evalExpr(_,sub(_,_),'e'):-!.

%Pentru operatiile de comparatie si de verificare a egalitatii singura diferenta este ca se intoarce
%1 daca se respecta conditia si 0 daca nu este eroare
evalExpr(Map,equal(X,Y),1):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e', R1 =:= R2 ,!.
evalExpr(Map,equal(X,Y),0):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e',!.
evalExpr(_,equal(_,_),'e'):-!.

evalExpr(Map,smaller(X,Y),1):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e', R1 < R2 ,!.
evalExpr(Map,smaller(X,Y),0):- evalExpr(Map,X,R1), R1 \= 'e', evalExpr(Map,Y,R2), R2 \= 'e',!.
evalExpr(_,smaller(_,_),'e'):-!.

% Evaluarea unui program

%Evaluarea unui bloc care contine doar ';' nu face nimic, doar se trece peste el
evalProgram(Map,no_instruction(),Map):-!.

%Evaluarea unui return intoarce rezultatul expresiei daca nu apare eroare si altfel intoarce 'e'
evalProgram(Map,ret(X),R):- evalExpr(Map,X,R), R\='e',!.
evalProgram(_,ret(_),'e'):-!.

%Evaluarea unui assert intoarce mapa utilizata daca acesta reuseste, a daca assertul este gresit
%si e daca apare o eroare la evaluarea expresiei
evalProgram(Map,assert(X),Map):- evalExpr(Map,X,R), R =:= 1, !.
evalProgram(Map,assert(X),'a'):- evalExpr(Map,X,R), R \= 'e',!.
evalProgram(_,assert(_),'e'):-!.

%La evaluarea unui assign se verifica daca primul operand este o variabila, apoi se evalueaza expresia si daca
%nu apare vreo eroare se introduce noua pereche in mapa si se returneaza noua mapa.
evalProgram(Map,assign(variable(Var),Y),Map1):- atom(Var),evalExpr(Map,Y,Val), Val \= 'e',addInMap(Map,(Var,Val),Map1),!.
evalProgram(_,assign(_,_),'e'):-!.

%Evaluarea unei secvente evalueaza primul program si daca acesta intoarce o mapa evalueaza si al doilea program si 
%intoarce ce returneaza acesta. Daca se intoarce un asssert failed, se evalueaza si al doilea program si daca acesta
%nu intoarce eroare se intoarce assert failed. Daca dupa primul program se intoarce un numar ( a fost o instructiune)
%de return se intoarce direct acel rezultat. In orice alt caz, se intoarce eroare.
evalProgram(Map,seq(X,Y),R):- evalProgram(Map,X,R1), R1 \= 'e', R1 \= 'a', not(number(R1)), evalProgram(R1,Y,R),!.
evalProgram(Map,seq(X,Y),'a'):- evalProgram(Map,X,R1), R1 \= 'e', not(number(R1)), evalProgram(Map,Y,R), R \= 'e',!.
evalProgram(Map,seq(X,_),R):- evalProgram(Map,X,R), R \= 'e', R \= 'a',!.
evalProgram(_,seq(_,_),'e'):- !.

%Evaluarea unui if verifica daca expresia din conditie este a devarata si daca da se executa prima ramura, iar daca
%este falsa se executa a doua ramura si se intoarce de fiecare data rezultatul ramurii respective. In orice alt caz
%se intoarce eroare.
evalProgram(Map,iff(X,Y,_),R):- evalExpr(Map,X,R1), R1 =:= 1, evalProgram(Map,Y,R),!.
evalProgram(Map,iff(X,_,Z),R):- evalExpr(Map,X,R1), R1 \= 'e', evalProgram(Map,Z,R),!.
evalProgram(_,iff(_,_,_),'e'):-!.

%Evaluarea unui for evalueaza primul assign, apoi evalueaza conditia de oprire si daca ambele au avut succes si
%evaluarea conditiei a intors 1, se evalueaza blocul si daca acesta intoarce o mapa se reexecuta recursiv for-ul.
%Daca blocul for-ului intoarce o eroare sau un numar se intoarce direct acel rezultat. Daca evaluarea conditie 
%intoarce zero oprim executia for-ului.In orice alt caz se intoarce eroare.
evalProgram(Map,forr(X,Y,Z,T),R):-evalProgram(Map,X,R1), R1 \= 'e', evalExpr(R1,Y,R2), R2 =:= 1, evalProgram(R1,T,R3), R3 \='e',
	R3 \= 'a', not(number(R3)), evalProgram(R3,forr(Z,Y,Z,T),R),!.
evalProgram(Map,forr(X,Y,_,T),R):-evalProgram(Map,X,R1), R1 \= 'e', evalExpr(R1,Y,R2), R2 =:= 1, evalProgram(R1,T,R),!.
evalProgram(Map,forr(X,Y,_,_),R):-evalProgram(Map,X,R), R \= 'e', evalExpr(R,Y,R2), R2 =:= 0,!.
evalProgram(_,forr(_,_,_,_),'e'):-!.


%Functie care combina parsarea cu evaluarea si rezolva tema. Daca rezultatul evaluarii este o mapa inseamna ca,
%desi nu a existat nicio eroare, nu a existat nici o instructiune return si atunci se intoarce x. Altfel,
%se intoarce rezultatul evaluarii. Daca nu se reuseste parsarea se intoarce direct e ( eroare de sintaxa ).
parseInputAux(L,'x'):-parse_seq(L,O,[]),evalProgram([],O,R), R \= 'a', R \= 'e', not(number(R)),!.
parseInputAux(L,R):- parse_seq(L,O,[]),evalProgram([],O,R),!.
parseInputAux(_,'e'):-!.

parseInput(F,R):-read_file_to_string(F,S,[]), lparse(S,L), parseInputAux(L,R),!.