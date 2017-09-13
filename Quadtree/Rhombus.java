import java.util.*;
import java.io.*;

/**
 * Clasa ce extinde clasa de baza GeometricObject si retine
 * coordonatele celor 4 varfuri ale unui romb. Constructorul
 * initializeaza coordonatele celor 4 varfuri si dreptunghiul
 * inconjurator
 * @author Icatoiu Vlad-Cosmin
 *
 */
public class Rhombus extends GeometricObject {

	public double lx,ly;
	public double rx,ry;
	public double ux,uy;
	public double dx,dy;
	
	public Rhombus(double xd,double yd, double xl, double yl, double xu,double yu, double xr,double yr, int id){

		super(xl,yd,xr,yu,id);
		
		lx=xl;
		ly=yl;
		rx=xr;
		ry=yr;
		dx=xd;
		dy=yd;
		ux=xu;
		uy=yu;
		
	}
	


	/**
	 * Metoda ce verifica daca un punct se afla intr-un romb si ii 
	 * introduce id-ul in lista daca nu era deja introdus
	 * @param x- coordonata x a punctului
	 * @param y- coordonata y a punctului
	 * @param found - vectorul cu id-uri ale figurilor ce contin punctul
	 * @param o - rombul in care cautam punctul
	 */
	static void Contains(double x, double y, Vector<Integer> found, GeometricObject o) {

		//Se creeaza cele doua triunghiuri ce formeaza un romb, acestea avand
		//acelasi id cu rombul
		
		Triangle upper = new Triangle( ((Rhombus)o).ux, ((Rhombus)o).uy, ((Rhombus)o).lx, 
				((Rhombus)o).ly, ((Rhombus)o).rx, ((Rhombus)o).ry, o.id);
		
		Triangle lower = new Triangle( ((Rhombus)o).dx, ((Rhombus)o).dy, ((Rhombus)o).lx, 
				((Rhombus)o).ly, ((Rhombus)o).rx, ((Rhombus)o).ry, o.id);
		
		//Se apeleaza metoda Contains a triunghiului ce va introduce rombul in
		// lista. Deoarece metoda Contains a triunghiului introduce id-ul in
		// mod unic, rombul nu va fi introdus de doua ori in cazul in care punctul
		// se afla pe latura comuna a triunghiurilor

		Triangle.Contains(x, y, found, lower);
		Triangle.Contains(x, y, found, upper);
	}

	/**
	 * Functie ce citeste input-ul pentru un romb, il insereaza in Quadtree
	 * prin apelarea functiei mari insert si realizeaza maparea intre el si id
	 * @param q -Quadtree-ul in care inseram
	 * @param in-Scanner-ul cu care citim coordonatele
	 * @param hm- hashmap-ul cu care realizam maparea intre id si figura
	 * @param id- id-ul figurii
	 */
	static void insertRhombus(Quadtree q, Scanner in, Map<Integer,GeometricObject> hm, int id){

		double x1,y1,x2,y2,x3,y3,x4,y4;

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();
		x3=in.nextDouble();
		y3=in.nextDouble();
		x4=in.nextDouble();
		y4=in.nextDouble();
		
		hm.put(id,new Rhombus(x1,y1,x2,y2,x3,y3,x4,y4,id));
		q.insert(q, new Rhombus(x1,y1,x2,y2,x3,y3,x4,y4,id));
	}

	/**
	 * Functie ce detecteaza cu ce figurie se intersecteaza un romb
	 * dupa ce i-a citit parametrii, prin apelarea functiei mari ColisionFigures.
	 * Dupa aceea functia scrie id-urile figurilor cu care se intersecteaza 
	 * dreptunghiul intr-un fisier cu ajutorul functiei print
	 * @param q - Quadtree-ul in care cautam
	 * @param in - Scanner-ul cu care citim coordonatele punctelor
	 * @param Writer - FileWriter-ul cu care scriem in fisier
	 * @throws IOException
	 */
	static void RhombusColision(Quadtree q, Scanner in, FileWriter Writer) throws IOException{

		double x1,y1,x2,y2,x3,y3,x4,y4;		
	
		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();
		x3=in.nextDouble();
		y3=in.nextDouble();
		x4=in.nextDouble();
		y4=in.nextDouble();
					
		Vector<Integer> v= new Vector<Integer>();
					
		Tasks.ColisionFigures(q, new Rhombus(x1,y1,x2,y2,x3,y3,x4,y4,-1), v);
		Tasks.print(v, Writer);

	}
	
}