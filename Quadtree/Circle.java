import java.util.*;
import java.io.*;

/**
 * Clasa ce extinde clasa de baza GeometricObject si contine metodele utile in tema pentru
 * operatiile cu cercuri. In ea retinem coordonatele centrului unui cerc si raza acestuia.
 * Constructorul acestei clase initializeaza atributele cercului si creeaza si dretpunghiul
 * inconjurator din GeometricObject.
 * @author Icatoiu Vlad-Cosmin
 * 
 */
public class Circle extends GeometricObject {

	double xcenter,ycenter,radius;
	
	public Circle(double x, double y, double radius,int id){
		
		super(x-radius,y-radius,x+radius,y+radius,id);
		
		xcenter=x;
		ycenter=y;
		this.radius=radius;
	}

	/**
	 * Functie ce verifica daca un punct se afla intr-un cerc si il introduce
	 *in lista de figuri in care se afla punctul, daca nu a fost introdus deja
	 * @param x - coordonata x a punctului
	 * @param y - coordonata y a punctului
	 * @param found - vector in care se introduc id-urile figurilor
	 * @param o - cercul in care verificam daca se afla punctul
	 */
	static void Contains(double x, double y, Vector<Integer> found, GeometricObject o){
		
		//Conditia ca un punct sa fie in interiorul cercului

		if(Math.sqrt(( x- ((Circle)o).xcenter)*(x- ((Circle)o).xcenter) + 
				(y-((Circle)o).ycenter)*(y-((Circle)o).ycenter)) <= ((Circle)o).radius ){
		
			//Verificam daca figura nu se afla deja in lista

			boolean ok = false;
			for(int j=0; j< found.size(); j++){
			
				if(found.elementAt(j) == o.id){
					ok = true;
					break;
				}
			}
			
			//Daca nu se afla, atunci o introducem

			if( ok == false){
				found.add(o.id);
			}
		}
	}

	/**
	 * Functie ce citeste input-ul pentru un cerc il introduce in Quadtree si
	 *realizeaza maparea intre el si id-ul sau
	 * @param q -Quadtree-ul in care introducem cercul
	 * @param in- Scanner-ul cu care citim parametrii cercului
	 * @param hm- hashmap-ul ce realizeaza maparea intre figura si id
	 * @param id- id-ul cercului ce urmeaza sa fie introdus
	 */
	static void insertCircle(Quadtree q,Scanner in, Map<Integer,GeometricObject> hm, int id){

		double x,y, radius;

		radius=in.nextDouble();
		x=in.nextDouble();
		y=in.nextDouble();

		hm.put(id, new Circle(x,y,radius,id));
		q.insert(q, new Circle(x,y,radius,id));

	}

	/**
	 * Functie ce afiseaza cu ce figuri din Quadtree se intersecteaza un cerc
	 * dupa ce ii citeste parametrii
	 * @param q - Quadtree-ul in care pastram figurile
	 * @param in- Scanner-ul cu care citim parametrii noului cerc
	 * @param Writer - FileWriter-ul cu care scriem in fisier
	 * @throws IOException - In cazul in care nu resueste citirea aruncam exceptia
	 */
	static void CircleColision(Quadtree q, Scanner in,FileWriter Writer) throws IOException{

		double x1,y1,radius;

		radius=in.nextDouble();
		x1=in.nextDouble();
		y1=in.nextDouble();
		
		Vector<Integer> v= new Vector<Integer>();

		//Apelam metodele ce introduc id-urile figurilor in lista, respectiv 
		//afiseaza figurile din vector
		
		Tasks.ColisionFigures(q, new Circle(x1,y1,radius,-1), v);
		Tasks.print(v, Writer);
	}
}