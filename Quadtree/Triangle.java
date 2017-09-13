import java.util.*;
import java.io.*;

/**
 * Clasa ce extinde clasa de baza GeometricObject si pastreaza
 * coordonatele varfurilor unui triunghi. Constructorul initializeaza
 * atat varfurile cat si dreptunghiul inconjurator
 * @author cosmin
 *
 */
public class Triangle extends GeometricObject {

	double x1,y1;
	double x2,y2;
	double x3,y3;
	
	public Triangle(double x1, double y1,double x2, double y2, double x3, double y3,int id){
		
		//Coordonatele specifice dreptunghiului inconjurator

		super( Math.min(x3,Math.min(x1,x2)),Math.min(y3,Math.min(y1,y2)),
				Math.max(x3,Math.max(x1,x2)),Math.max(y3,Math.max(y1,y2)),id );
		
		this.x1=x1;
		this.x2=x2;
		this.x3=x3;
		this.y1=y1;
		this.y2=y2;
		this.y3=y3;
		
	}
	
	

	/**
	 * Functie ce returneaza aria unui triunghi
	 * @param x1 - Coordonata x a primului varf
	 * @param y1 - Coordonata y a primului varf
	 * @param x2 - Coordonata x a celui de-al doilea varf
	 * @param y2 - Coordonata y a celui de-al doilea varf
	 * @param x3 - Coordonata x a celui de-al treilea varf
	 * @param y3 - Coordonata y a celui de-al treilea varf
	 * @return - returneaza aria
	 */
	static double area(double x1, double y1, double x2, double y2, double x3, double y3)
	{
   		return Math.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2.0);
	}

	//

	/**
	 * Metoda ce verifica daca un punct se afla intr-un triunghi si ii 
	 * introduce id-ul in lista daca nu era deja introdus
	 * @param x- coordonata x a punctului
	 * @param y- coordonata y a punctului
	 * @param found - vectorul cu id-uri ale figurilor ce contin punctul
	 * @param o - triunghiul in care cautam punctul
	 */
	static void Contains(double x,double y, Vector <Integer> found, GeometricObject o){
		
		//Conditiile ca un punct sa se afle intr-un triunghi este ca suma ariilor
		//triunghiurilor pe care acesta le formeaza cu varfurile sa fie chiar 
		// aria triunghiului

		double A = area (((Triangle)o).x1, ((Triangle)o).y1, ((Triangle)o).x2, 
		((Triangle)o).y2, ((Triangle)o).x3, ((Triangle)o).y3);
  
   		double A1 = area (x, y, ((Triangle)o).x2, ((Triangle)o).y2, ((Triangle)o).x3, ((Triangle)o).y3);
  
   		double A2 = area (((Triangle)o).x1, ((Triangle)o).y1, x, y, ((Triangle)o).x3, ((Triangle)o).y3);
    
   		double A3 = area (((Triangle)o).x1, ((Triangle)o).y1,((Triangle)o).x2, ((Triangle)o).y2, x, y);
	
		if(A == A1 + A2 + A3){
			
			//Cautam id-ul triunghiului in lista 

			boolean ok = false;
			
			for(int j=0; j< found.size(); j++){
				if(found.elementAt(j) == o.id){
					ok = true;
					break;
				}
			}
			
			//Daca nu a fost gasit, atunci il introducem

			if( ok == false){
				found.add(o.id);
			}
		}
	}

	/**
	 * Functie ce citeste input-ul pentru un triunghi, il insereaza in Quadtree
	 * prin apelarea functiei mari insert si realizeaza maparea intre el si id
	 * @param q -Quadtree-ul in care inseram
	 * @param in-Scanner-ul cu care citim coordonatele
	 * @param hm- hashmap-ul cu care realizam maparea intre id si figura
	 * @param id- id-ul figurii
	 */
	static void insertTriangle( Quadtree q, Scanner in, Map<Integer,GeometricObject> hm, int id){

		double x1,y1,x2,y2,x3,y3;

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();
		x3=in.nextDouble();
		y3=in.nextDouble();

		hm.put(id, new Triangle(x1,y1,x2,y2,x3,y3,id));
		q.insert(q, new Triangle(x1,y1,x2,y2,x3,y3,id));
	}

	/**
	 * Functie ce detecteaza cu ce figurie se intersecteaza un triunghi
	 * dupa ce i-a citit parametrii, prin apelarea functiei mari ColisionFigures.
	 * Dupa aceea functia scrie id-urile figurilor cu care se intersecteaza 
	 * dreptunghiul intr-un fisier cu ajutorul functiei print
	 * @param q - Quadtree-ul in care cautam
	 * @param in - Scanner-ul cu care citim coordonatele punctelor
	 * @param Writer - FileWriter-ul cu care scriem in fisier
	 * @throws IOException - aruncam eroarea in caz ca nu reusim citirea
	 */
	static void TriangleColision( Quadtree q, Scanner in, FileWriter Writer) throws IOException{

		double x1,x2,x3,y1,y2,y3;

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();
		x3=in.nextDouble();
		y3=in.nextDouble();
					
		Vector<Integer> v= new Vector<Integer>();

		Tasks.ColisionFigures(q, new Triangle(x1,y1,x2,y2,x3,y3,-1), v);
		Tasks.print(v, Writer);
	}
}