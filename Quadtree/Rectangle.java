import java.util.*;
import java.io.*;

/**
 * Clasa ce extinde clasa de baza GeometricObject.
 * Pentru dreptunghi nu avem nevoie de coordonate in plus deoarece
 * dreptunghiul inconjurator coincide cu dreptunghiul nostru.
 * Constructorul initializeaza dreptunghiul.
 * @author Icatoiu Vlad-Cosmin
 *
 */
public class Rectangle extends GeometricObject{



	public Rectangle(double x1,double y1,double x2, double y2, int id){
		
		super(x1,y1,x2,y2,id);
		
	}
	
	/**
	 * Metoda ce verifica daca un punct se afla intr-un dreptunghi si ii 
	 * introduce id-ul in lista daca nu era deja introdus
	 * @param x- coordonata x a punctului
	 * @param y- coordonata y a punctului
	 * @param found - vectorul cu id-uri ale figurilor ce contin punctul
	 * @param o - dreptunghiul in care cautam punctul
	 */
	static void Contains(double x, double y, Vector<Integer> found , GeometricObject o){
		
		//Conditia ca un punct sa se afle intr-un dreptunghi

		if(x <= o.xr && x >= o.xl && y <= o.yr && y >= o.yl){
			
			//Cautam in lista de id-uri si verificam daca dreptunghiul e acolo
	
			boolean ok = false;
		
			for(int j=0; j< found.size(); j++){
				if(found.elementAt(j) == o.id){
					ok = true;
					break;
				}
			}

			//Daca nu e deja introdus, il introducem
			
			if( ok == false){
				found.add(o.id);
			}
		}
	}

	/**
	 * Functie ce citeste input-ul pentru un dreptunghi, il insereaza in Quadtree
	 * prin apelarea functiei mari insert si realizeaza maparea intre el si id
	 * @param q -Quadtree-ul in care inseram
	 * @param in-Scanner-ul cu care citim coordonatele
	 * @param hm- hashmap-ul cu care realizam maparea intre id si figura
	 * @param id- id-ul figurii
	 */
	static void insertRectangle(Quadtree q, Scanner in, Map<Integer,GeometricObject> hm , int id){
		
		double x1,y1,x2,y2;

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();

		hm.put(id,new Rectangle(x1,y1,x2,y2,id));
		q.insert(q, new Rectangle(x1,y1,x2,y2,id));		
	}

	//

	/**
	 * Functie ce detecteaza cu ce figurie se intersecteaza un dreptunghi
	 * dupa ce i-a citit parametrii, prin apelarea functiei mari ColisionFigures.
	 * Dupa aceea functia scrie id-urile figurilor cu care se intersecteaza 
	 * dreptunghiul intr-un fisier cu ajutorul functiei print
	 * @param q - Quadtree-ul in care cautam
	 * @param in - Scanner-ul cu care citim coordonatele punctelor
	 * @param Writer - FileWriter-ul cu care scriem in fisier
	 * @throws IOException - aruncam eroarea in caz ca nu reusim citirea
	 */
	static void RectangleColision(Quadtree q, Scanner in, FileWriter Writer) throws IOException{

		double x1,y1,x2,y2;

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();

		Vector<Integer> v=new Vector<Integer>();

		Tasks.ColisionFigures(q, new Rectangle(x1,y1,x2,y2,-1), v);
		Tasks.print(v, Writer);
	}
}