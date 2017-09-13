module Interpreter
  (
    -- * Types
    Prog,
    Asgn,

    -- * Functions
    evalRaw,
    evalAdt,
  ) where

import Data.Char
-------------------------------------------------------------------------------
--------------------------------- The Expr ADT  -------------------------------
-------------------------------------------------------------------------------
data Expr = Add Expr Expr
          | Sub Expr Expr
          | Mult Expr Expr
          | Equal Expr Expr
          | Smaller Expr Expr
          | Symbol String
          | Value Int deriving (Show, Read)

-------------------------------------------------------------------------------
---------------------------------- The Prog ADT -------------------------------
-------------------------------------------------------------------------------
data Asgn = Asgn String Expr deriving (Show, Read)

data Prog = Eq Asgn
          | Seq Prog Prog
          | If Expr Prog Prog
          | For Asgn Expr Asgn Prog
          | Assert Expr
          | Return Expr deriving (Show, Read)

-- Mapa este folosita pentru retinerea variabilelor la evaluare
type MyMap a = [(String,a)]

-- Tip  de date ce va defini tipul diferitelor functii cu care parsez inputul
type Parser a = String -> Maybe (a,String)

-------------------------------------------------------------------------------
----------------------------------- The Parser --------------------------------
-------------------------------------------------------------------------------

-- Functie ce elimina toate spatiile dintr-un string
eat_ws :: String -> String
eat_ws (' ':l) = eat_ws l
eat_ws [] = []
eat_ws (a:l) = a:(eat_ws l)

-- Functie ce va elimina spatiile de la inceputul unui string
eat_starting_ws :: String -> String
eat_starting_ws (' ':l) = eat_starting_ws l
eat_starting_ws l =l

-- Functie ce verifica daca primul string se afla la inceputul celui de-al
-- doilea string si returneaza restul stringului.
is_contained :: String->String->Maybe String
is_contained [] s = Just s
is_contained s [] = Nothing
is_contained (x:rest) (y:rest') = if (x == y) then (is_contained rest rest') 
                                              else Nothing

-- Functia extract este functia ce extrage intr-un string un if sau un for,
-- aflat la inceputul stringului. Functia va extrage atat conditiile cat si
-- blocul de instructiuni al forului sau cele 2 blocuri de instructiuni ale
-- if-ului si de asemenea tine cont de diferite erori ce pot aparea. Functia
-- va intoarce Nothing daca s-a gasit o eroare de sintaxa iar daca nu, va
-- intoarce o pereche formata din instructiunea parsata si restul string-ului.
-- Parametrii au urmatoarele semnificatii. Primul string este string-ul parsat,
-- iar ultimul string este un acumulator in care salvez ce am parcurs, pentru
-- a putea intoarce instructiunea parsata. Primul integer contorizeaza numarul
-- de acolade din expresie, al doilea verifica numarul de paranteze rotunde din
-- expresie si se verifica astfel sa se faca match. Al treilea integer face 
-- distinctia intre if(0) si for(1) si practic spune ca if are 2 blocuri de luat
-- fata de unul singur la for. Imediat ce am preluat blocul de dupa then
-- parametrul se schimba in 1 si se extrage si blocul de else

extract :: String -> Integer -> Integer -> Integer -> String -> Maybe (String,String)
extract [] y z t first = Nothing 
extract ('}':rest) 1 0 1 first = Just (first ++ "}",rest)
extract ('}':rest) 1 0 0 first = extract rest 0 0 1 (first ++ "}")
extract ('}':rest) 1 x y first = Nothing
extract ('}':rest) 0 y z first = Nothing
extract ('}':rest) y z t first = extract rest (y-1) z t (first ++ "}")
extract ('(':rest) y z t first = extract rest y (z+1) t (first ++ "(")
extract (')':rest) y z t first = extract rest y (z-1) t (first ++ ")")
extract ('{':rest) y z t first = extract rest (y+1) z t (first ++ "{")
extract (a:rest) y z t first = extract rest y z t (first ++ (a:[]))

-- Functia extract semi va extrage o instuctiune ce se termina cu ';'. Daca
-- nu s-a gasit punct si virgula inseamna ca este o eroare.
extractSemi :: String->String-> Maybe (String,String)
extractSemi [] first = Nothing
extractSemi (';':rest) first = Just (first,rest)
extractSemi (a:rest) first = extractSemi rest (first ++ a:[])

-- Functia parseaza input-ul si intoarce o lista cu instructiuni sau Nothing 
-- daca s-a gasit vreo eroare. Practic functia alege intre functiile prezentate
-- anterior. De asemenea daca nu cumva lipseste cuvantul "if" sau "for" si in
-- acest caz intoarce Nothing.
extractComponents :: String -> [String] -> Maybe [String]
extractComponents s l = case (eat_starting_ws s) of
    [] -> Just l
    ('f':'o':'r':' ':'(':rest) -> 
        case (extract (eat_starting_ws s) 0 0 1 []) of 
            Just (for,rest1) -> (extractComponents rest1 (l ++ [for]))
            Nothing -> Nothing

    ('i':'f':' ':'(':rest) -> 
        case (extract (eat_starting_ws s) 0 0 0 []) of 
            Just (iff,rest1) -> (extractComponents rest1 (l ++ [iff]))
            Nothing -> Nothing

    ('(':rest) -> Nothing

    _ -> case (extractSemi  (eat_starting_ws s) []) of 
        Just (body,rest) -> (extractComponents rest (l ++ [body]))
        Nothing -> Nothing

-- Functie ce elimina din input tab-urile si newline-urile
removeBlanks :: String -> String
removeBlanks ('\n':rest) = removeBlanks rest
removeBlanks ('\t':rest) = removeBlanks rest
removeBlanks (char:rest) = char : (removeBlanks rest)
removeBlanks [] = []

-- Functie ce schimba semnele intr-o expresie matematica
revertSigns :: String -> String
revertSigns [] = []
revertSigns ('-':l) = '+':(revertSigns l)
revertSigns ('+':l) = '-':(revertSigns l)
revertSigns (a:l) = a:(revertSigns l)

-- Functie ce ia conditiile pentru for sau if, din interiorul parantezelor 
-- rotunde.
takeConditions :: String->String-> (String,String)
takeConditions ('(':rest) l = takeConditions rest l
takeConditions (')':rest) l = (l,rest)
takeConditions (a:rest) l = takeConditions rest (l ++ [a])

-- Functie ce parseaza blocurile if si elimina cuvintele then sau else in
-- functie de blocul analizat si de asemenea, elimina si acoladele.
getBlock :: String -> String
getBlock ('}':[]) = []
getBlock (' ':'t':'h':'e':'n':' ':'{':rest) = getBlock rest
getBlock (')':' ':'t':'h':'e':'n':' ':'{':rest) = ") then {" ++ getBlock rest
getBlock (' ':'e':'l':'s':'e':' ':'{':rest) = getBlock rest
getBlock ('}':' ':'e':'l':'s':'e':' ':'{':rest) = "} else {" ++ getBlock rest
getBlock (a:rest) = a : (getBlock rest)

-- Functie ce elimina prima si ultima acolada dintr-un string. Folosita pentru
-- a obtine blocul de instructiuni din for.
removeCurly :: Integer -> String -> String
removeCurly x ('}':[]) = []
removeCurly 0 ('{':rest) = removeCurly 1 rest
removeCurly x (a:rest) = a : (removeCurly x rest)

-- Functie ce cauta un string specific la inceputul unui sir si intoarce o
-- pereche sir si rest daca s-a gasit si Nothing altfel.

operator :: String-> Parser String
operator x = \s -> 
    case (is_contained x s) of
         (Just rest) -> Just (x,rest)
         Nothing -> Nothing

-- Functie ce parseaza un nume de variabila, luand toate literele consecutive.
var :: Parser Expr
var = \s -> case s of
    (h:l) -> if ( not (isLetter h) ) then Nothing
             else case (var l) of
        Nothing ->  Just (Symbol (h:[]), l)
        Just (Symbol x, rest) -> Just (Symbol (h:x), rest)
    _ -> Nothing

-- Functie ce parseaza o valoare si o extrage sub forma de string, luand toate
-- cifrele consecituive.
constants_utils :: Parser String
constants_utils = \s -> case s of
    (h:l) -> if ( not (isDigit h) ) then Nothing 
             else case (constants_utils l) of
        Nothing -> Just (h:[],l)
        Just (x, rest) -> Just ((h:x), rest)
    _ -> Nothing

-- Functia apeleaza functia anterioara, iar la final converteste valoarea la int
constants :: Parser Expr
constants = \s -> case (constants_utils s) of
    Just (string , rest) -> Just (Value (read string :: Int), rest)
    Nothing -> Nothing

-- Combinator de parsere care practic aplica operatia sau intre parsere.
-- Folosit pentru a testa pe un string mai multe tipuri de parsere.
(.||.) :: Parser a -> Parser a -> Parser a
p .||. p' = \s -> case (p s) of
    Nothing -> (p' s)
    x -> x

-- Parser pentru valori si variabile
operand :: Parser Expr
operand = var .||. constants

-- Functie ce parseaza un produs prin extragerea variabilelor, a semnului 
-- de inmultire si le combina. Din nou, se returneaza ce ramane dupa parsare.
parseProduct :: Parser Expr
parseProduct = \s -> case (operand (eat_ws s)) of
    Just(expr, '*':rest) -> case (parseProduct rest) of
        Just (expr1,rest1)-> Just((Mult expr expr1), rest1)
        Nothing -> Nothing
    result -> result

-- Functie ce parseaza expresii matematice ce contin sume si produse.
parseSumandProd :: Parser Expr
parseSumandProd = \s -> case (parseProduct (eat_ws s)) of
    Just(expr, '+':rest) -> case (parseSumandProd rest) of
        Just (expr1,rest1) -> Just ((Add expr expr1),rest1)
        Nothing -> Nothing
    result -> result

-- Functie ce parseaza orice fel de expresie matematica a programului.
parseMath :: Parser Expr
parseMath = \s -> case (parseSumandProd (eat_ws s)) of
    Just(expr, '-':rest) -> case (parseMath (revertSigns rest)) of
        Just(expr1,rest1) -> Just ((Sub expr expr1),rest1)
        Nothing -> Nothing
    result -> result

-- Functie ce parseaza expresiile < si == si intoarce nothing daca sirul daca
-- e vreo eroare la unul din cei 2 termeni sau daca acesti operatori nu sunt
-- in sir. De asemenea, daca dupa parsare mai ramane ceva in sir inseamna ca
-- e un caracter aiurea si se intoarce eroare de sintaxa.
parseExpression :: Parser Expr
parseExpression = \s -> case (parseMath (eat_ws s)) of
    Just(expr, '<':rest) -> case (parseMath rest) of
        Just(expr1,"") -> Just ((Smaller expr expr1),"")
        _ -> Nothing
    Just(expr2, '=':'=':rest2) -> case (parseMath rest2) of
        Just(expr3,"") -> Just ((Equal expr2 expr3),"")
        _ -> Nothing
    _ -> Nothing 

-- Functie ce parseaza un asign, parsand variabila si apoi expresia din dreapta.
-- De asemenea se verifica faptul ca nu ramane nimic la sfarsitul expresiei.
parseAssign :: Parser Prog
parseAssign = \s -> case (var (eat_ws s)) of
    Just(Symbol variable,'=':rest) -> case (parseMath rest) of
        Just(expr,"") -> Just ( Eq (Asgn variable expr), "")
        _ -> Nothing
    _ -> Nothing

-- Functie ce parseaza un string ce contine doar un return si verifica daca 
-- are spatiu dupa return si daca nu cumva mai sunt caractere in plus la final.
parseReturn :: Parser Prog
parseReturn = \s -> case (operator "return " s) of
    Just(str,rest) -> case (parseMath (eat_ws rest)) of
        Just(expr,"") -> Just (Return expr,"")
        _->Nothing
    Nothing -> Nothing

-- Functie ce parseaza un assert, asemanator cu parsarea returnului.
parseAssert :: Parser Prog
parseAssert = \s -> case (operator "assert " s) of
    Just(str,rest) -> case (parseExpression (eat_ws rest)) of
        Just(expr,"") -> Just (Assert expr,"")
        _->Nothing
    Nothing -> Nothing

-- Functie ce parseaza o instructiune if impartind-o in cele 3 parti: conditia
-- si cele 2 blocuri. De asemenea se verifica prezenta cuvintelor speciale 
-- then si else. Blocurile se parseaza cu ajutorul functiei tryParsers, iar la
-- final daca nu a aparut nicio eroare in nicio parsare se intoarce programul
-- if
parseIf :: Parser Prog
parseIf s = case (operator "if " s) of
    Nothing -> Nothing
    Just(str,rest) -> 
        let 
            (condition,rest1) = (takeConditions rest [])
            expr = parseExpression condition
            Just (block1,block2) = extract rest1 0 0 1 []
            
            prog1 = if ((is_contained " then" block1) /= Nothing) then 
             (tryParsers (extractComponents ( getBlock block1) [])) else Nothing
            
            prog2 = if ((is_contained " else" block2) /= Nothing) then 
             (tryParsers (extractComponents ( getBlock block2) [])) else Nothing
             
        in case (expr,prog1,prog2) of
            (Nothing,_,_)->Nothing
            (_,Nothing,_)->Nothing
            (_,_,Nothing)->Nothing
            ((Just (ex,"")),(Just pr1),(Just pr2)) -> Just (If ex pr1 pr2,"")

-- Functie ce parseaza o instructiune for prin impartirea ei in cele 4 parti
-- componente. Mai intai sunt extrase conditiile din interiorul parantezelor
-- rotunde si se impart in 3 parti cu ajutorul functiei extractSemi. Apoi,
-- cu ajutorul functiei removeCurly se extrage blocul si se parseaza cu ajutorul
-- functiei tryParsers.
parseFor :: Parser Prog
parseFor s = case (operator "for " s) of
    Nothing -> Nothing
    Just(str,rest) ->
        let
            (conditions,rest1) = (takeConditions rest [])
            Just (asgn1,rest2) = extractSemi (eat_starting_ws conditions) []
            Just (cond,asgn2) = extractSemi (eat_starting_ws rest2) []

            expr = parseExpression cond
            expr1 = parseAssign asgn1  
            expr2 = parseAssign asgn2
            block = removeCurly 0 rest1
            prog = tryParsers (extractComponents block [])
           
        in case (expr1,expr,expr2,prog) of
               (Nothing,_,_,_)->Nothing
               (_,Nothing,_,_)->Nothing
               (_,_,Nothing,_)->Nothing
               (_,_,_,Nothing)->Nothing
               (Just (Eq x,""),Just (y,""),Just (Eq z,""),Just t) -> Just (For x y z t,"")

-- Functie ce primeste lista cu instructiunile programului si incearca sa 
-- parseze pe fiecare cu un parser si le inlantuieste in Seq-uri. De asemenea,
-- daca s-a detectat o eroare de sintaxa la impartirea programului in
-- instructiuni nu se mai parseaza si se intoarce eroare automat

tryParsers :: (Maybe [String]) -> Maybe Prog
tryParsers (Just (h:[])) = case ( (parseAssign .||. (parseAssert .||. 
                          (parseReturn .||. (parseIf .||. parseFor)))) h) of
    Nothing -> Nothing
    Just(progr,"") -> Just progr

tryParsers (Just (h:l)) = case ( (parseAssign .||. (parseAssert .||. 
                          (parseReturn .||. (parseIf .||. parseFor)))) h) of 
    Nothing -> Nothing
    Just (expr,"") -> case tryParsers (Just l) of
        Nothing -> Nothing
        Just prog -> Just (Seq expr prog)

tryParsers Nothing = Nothing

-- Functia ce parseaza un intreg program.
parse :: String -> Maybe Prog
parse x = tryParsers (extractComponents (removeBlanks x) [])

-------------------------------------------------------------------------------
-------------------------------- The Interpreter ------------------------------
-------------------------------------------------------------------------------

-- Functie ce adauga noi intrari in mapa. De asemenea, daca se actualizeaza o 
-- valoare a unei variabile se pastreaza doar ultima.
addInMap :: String -> a -> MyMap a -> MyMap a
addInMap s elem [] = [(s,elem)]
addInMap s elem (h:l) = if ((fst h) == s) then 
                               (addInMap s elem l) else (h:(addInMap s elem l))

-- Functie ce cauta un element intr-o mapa dupa cheia acestuia. Practic, se 
-- returneaza valoarea unei variabile si Nothing daca nu exista o intrare in
-- mapa pentru aceasta.
getFromMap :: String -> MyMap a -> Maybe a
getFromMap s [] = Nothing
getFromMap s (h:l) = if ((fst h) == s) then (Just (snd h)) else (getFromMap s l)

-- Functie ce evalueaza diferite tipuri de expresii prin evaluarea fiecarui 
-- membru al expresiei.
evaluateExpression :: MyMap Int -> Expr -> Maybe (Either Int Bool)
evaluateExpression l (Value a) = Just (Left a)
evaluateExpression l (Symbol s) = case (getFromMap s l) of Nothing -> Nothing
                                                           Just a -> Just (Left a)
evaluateExpression l (Add x y) = 
    case ((evaluateExpression l x),(evaluateExpression l y)) of 
        (Just (Left a), Just (Left b)) -> Just (Left (a+b)) 
        (_,_) -> Nothing

evaluateExpression l (Mult x y) = 
    case ((evaluateExpression l x),(evaluateExpression l y)) of 
        (Just (Left a), Just (Left b)) -> Just (Left (a*b)) 
        (_,_) -> Nothing

evaluateExpression l (Sub x y) = 
    case ((evaluateExpression l x),(evaluateExpression l y)) of 
        (Just (Left a), Just (Left b)) -> Just (Left (a-b)) 
        (_,_) -> Nothing

evaluateExpression l (Equal x y) = 
    case ((evaluateExpression l x),(evaluateExpression l y)) of 
        (Just (Left a),Just (Left b)) -> Just (Right (a == b)) 
        (_,_) -> Nothing

evaluateExpression l (Smaller x y) = 
    case ((evaluateExpression l x),(evaluateExpression l y)) of 
        (Just (Left a), Just (Left b)) -> Just (Right (a < b)) 
        (_,_) -> Nothing

-- Functia de evaluare a unui program. Ea va intoarce un string daca s-a gasit
-- o eroare, un int daca s-a gasit un return si o mapa altfel. Prioritatea cea
-- mai mare o are eroarea si cea mai mica o are mapa.
myEval :: Prog -> MyMap Int -> Either (Either String Int) (MyMap Int)
myEval (Eq (Asgn str expr)) map = 
    case (evaluateExpression map expr) of 
        Nothing -> Left (Left "Uninitialized variable")
        Just (Left a) -> Right (addInMap str a map)

myEval (If expr prog1 prog2) map =
    case (evaluateExpression map expr) of 
        Nothing -> Left (Left "Uninitialized variable")
        Just (Right True) -> myEval prog1 map
        Just (Right False) -> myEval prog2 map

myEval (Assert expr) map = 
    case (evaluateExpression map expr) of 
        Nothing -> Left (Left "Uninitialized variable")
        Just (Right False) -> Left (Left "Assert failed")
        Just (Right True) -> Right map

myEval (Return expr) map =
    case (evaluateExpression map expr) of 
        Nothing -> Left (Left "Uninitialized variable")
        Just (Left a) -> Left (Right a)

myEval (Seq prog1 prog2) map = case (myEval prog1 map) of 
    Right map1 -> myEval prog2 map1
    Left (Left str) -> Left (Left str)
    Left (Right a) -> Left (Right a)

myEval (For asgn1 expr asgn2 prog) map = case (myEval (Eq asgn1) map) of
    Left (Left str) -> Left (Left str)
    Right map1 -> case (evaluateExpression map1 expr) of
        Nothing -> Left (Left "Uninitialized variable")
        Just (Right False) -> Right map1
        Just (Right True) -> case (myEval prog map1) of
            Left (Left str) -> Left (Left str)
            Right map2 -> myEval (For asgn2 expr asgn2 prog) map2

-- Functie ce va apela functia precedenta si returneaza rezultatul. Daca la 
-- finalul evaluarii unui intreg program ni s-a returnat o mapa inseamna ca in
-- program nu a fost nicio eroare, dar de asemenea nu a fost intalnit niciun
-- return si astfel apare eroarea "Missing return".
evalAdt :: Prog -> Either String Int
evalAdt x = case (myEval x []) of Left (Right a) -> Right a
                                  Left (Left str) -> Left str
                                  Right map -> Left "Missing return"
 
-- The *evalRaw* function is already implemented, but it relies on the *parse*
-- function which you have to implement.
--
-- Of couse, you can change this definition.  Only its name and type are
-- important.

evalRaw :: String -> Either String Int
evalRaw rawProg = case parse rawProg of
        Just prog -> evalAdt prog
        Nothing   -> Left "Syntax error"