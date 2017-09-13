import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

/**
 * Clasa ce contine metodele necesare pentru rezolvarea temei.
 * @author Icatoiu Vlad-Cosmin
 *
 */
public class Tasks {
	
	/**
	 * Functie ce gaseste figurile in care se afla un punct si le introduce intr-un
	 * vector
	 * @param q- Quadtree-ul in care afla figurile
	 * @param x- coordonata x a punctului
	 * @param y- coordonata y a punctului
	 * @param found - vectorul in care introducem figurile
	 */
	static void ContainsPoint(Quadtree q,double x, double y, Vector <Integer> found){
		
		//Daca am ajuns la o frunza( unde se afla figuri)

		if( q.isleaf ){

			//Daca nu exista nicio figura, nu avem ce introduce
			
			if( q.o.isEmpty() ){
				return;
			}

			//Pentru fiecare figura din lista verificam de ce tip este si o
			//introducem in lista cu ajutorul functiei Contains, specifica fiecarei
			// clase
			
			for(int i=0; i<q.o.size(); i++){
			
				if(q.o.elementAt(i) instanceof Rectangle){
					Rectangle.Contains(x, y, found, q.o.elementAt(i));
				}
			
				if(q.o.elementAt(i) instanceof Circle){
					Circle.Contains(x, y, found, q.o.elementAt(i));
				}
			
				if(q.o.elementAt(i) instanceof Triangle){
					Triangle.Contains(x, y, found, q.o.elementAt(i));
				}
			
				if(q.o.elementAt(i) instanceof Rhombus){
					Rhombus.Contains(x, y, found, q.o.elementAt(i));
				}
			}
			return ;
		}
		
		//Daca nu suntem intr-o frunza, verificam in ce cadran se afla punctul
		//si cautam acolo

		for(int i = 0; i < 4; i++){
			if( q.children[i].Quadran.contains(x, y) ){
				ContainsPoint(q.children[i],x,y,found);
			}
		}

	}

	/**
	 * Functie ce detecteaza cu ce figuri se intersecteaza o noua figura si le
	 * introduce pe acestea intr-o lista 
	 * @param q- Quadtree-ul in care se afla figurile
	 * @param o- Figura pentru care cautam coliziunile
	 * @param found - vectorul in care introducem id-urile figurilor
	 */
	static void ColisionFigures(Quadtree q, GeometricObject o,Vector<Integer> found){
		
		//Daca am ajuns la o frunza( unde se afla figurile) 

		if( q.isleaf ){
			
			//Daca frunza e goala nu avem ce introduce

			if( q.o.isEmpty() ){
				return;
			}
			
			//Pentru fiecare element din frunza verificam daca se intersecteaza
			//cu noua figura si daca se afla in lista si daca nu se afla
			// o introducem

			for(int i=0 ; i< q.o.size(); i++){

				if(q.o.elementAt(i).colision(o)== true){
					if(found.contains(q.o.elementAt(i).id ) == false){
						found.add(q.o.elementAt(i).id);
					}
							
				}
			}
			return;
		}

		//Daca nu suntem intr-o frunza verificam cu ce cadrane se intersecteaza
		//figura si cautam acolo

		for(int i = 0; i < 4; i++){
			if(q.children[i].Quadran.colision(o)){
				ColisionFigures(q.children[i],o,found);
			}
		}
		
	}
	
	/**
	 * Functie ce scrie id-urile dintr-un vector intr-un fisier
	 * @param v - vectorul ce contine id-urile figurilor
	 * @param Writer- FileWriter-ul cu care scriem in fisier
	 * @throws IOException- Daca nu reusim sa scriem aruncam exceptia
	 */
	static void print(Vector<Integer> v, FileWriter Writer) throws IOException{
		
		//Daca vectorul este gol atunci scriem NIL

		if(v.size() == 0){
			Writer.write("NIL");
		}
		
		int i;
		
		//Sortam vectorul crescator

		Collections.sort(v);		
		
		//Scriem toate elementele din vector in fisier cu un spatiu intre ele

		for( i=0; i< v.size() ; i++){
			
			if(i < v.size() -1){
				Writer.write(v.elementAt(i)+" ");
			}
			else{
				Writer.write(v.elementAt(i)+"");
			}
		}
		
		//Trecem pe o linie noua la final

		Writer.write("\n");
	}
	
	/**
	 * Functie ce creaza copiii unui nod prin apelarea constructorului
	 * @param q- nod-ul pentru care cream fiii
	 */
	static void CreateChildren(Quadtree q){
		
		q.children[0]=new Quadtree(q.xcenter+q.length/2,q.ycenter+q.length/2,q.length/2);
		q.children[1]=new Quadtree(q.xcenter+q.length/2,q.ycenter-q.length/2,q.length/2);
		q.children[2]=new Quadtree(q.xcenter-q.length/2,q.ycenter-q.length/2,q.length/2);
		q.children[3]=new Quadtree(q.xcenter-q.length/2,q.ycenter+q.length/2,q.length/2);
		
	}

}