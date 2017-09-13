import java.util.*;
import java.io.*;

/**
 * Clasa care initializeaza Quadtree-ul si rezolva tema apeland
 * metodele corespunzatoare.
 * @author Icatoiu Vlad-Cosmin
 *
 */
public class Main {

	/**
	 * Metoda ce rezolva tema, apeland metodele corespunzatoare
	 * @throws IOException - in cazul in care avem o eroare la citire
	 */
	public void homework() throws IOException{
		
		//Un map cu ajutorul caruia determin ce figura trebuie stearsa 

		Map <Integer,GeometricObject> hm =new HashMap<Integer,GeometricObject>();
		
		FileWriter Writer = null;

		//Deschidem fisierele de intrare, respectiv iesire
		
		Scanner in = new Scanner( new BufferedReader( new FileReader("quadtree.in")));

		Writer= new FileWriter("quadtree.out");
		
		double x1,y1,x2,y2,x,y,operation,length;
		int obj,id;
		
		//Citim coordonatele planului

		x1=in.nextDouble();
		y1=in.nextDouble();
		x2=in.nextDouble();
		y2=in.nextDouble();
		
		//Aflam centrul acestuia si dimensiunea

		length=(x2-x1)/2;
		x=(x1+x2)/2;
		y=(y1+y2)/2;
		
		//Initializam quadtree-ul

		Quadtree q= new Quadtree(x,y,length);
		
		//Cat timp avem ce citi din fisier

		while(in.hasNextInt()){
			
			//Vedem ce operatie trebuie sa executam

			operation=in.nextInt();
			
			//Daca trebuie sa inseram

			if(operation == 11){

				//Citim ce figura trebuie sa inseram si id-ul acesteia

				obj=in.nextInt();
				id=in.nextInt();
				
				// In functie de tipul figurii, apelam functia de inserare
				// corespunzatoare

				if(obj == 1){
					Rectangle.insertRectangle(q, in, hm, id);
				}
				
				if(obj == 2){
					Triangle.insertTriangle(q, in , hm, id);
				}
				
				if(obj == 3){
					Circle.insertCircle(q, in, hm, id);
				}
				
				if(obj == 4){
					Rhombus.insertRhombus(q, in, hm, id);
				}	

			}
			
			//Daca trebuie sa stergem, citim id-ul si apelam metoda remove
			//si stergem si figura din map

			if(operation == 22){
				id=in.nextInt();
				q.remove(q,hm.get(id),id);
				hm.remove(id);
			}
			
			//Daca trebuie sa cautam figurile in care se afla un punct, citim
			//coordonatele punctului, cream un vector in care vom pune id-urile
			//figurilor si apelam metodele ce introduc figurile in vector,
			//respectiv afiseaza elementele din vector

			if(operation == 33){
				x=in.nextDouble();
				y=in.nextDouble();
				Vector<Integer> v= new Vector<Integer>();
				Tasks.ContainsPoint(q, x, y, v);
				Tasks.print(v,Writer);
			}
			
			//Daca trebuie sa cautam figurile cu care se intersecteaza o noua
			// figura

			if(operation == 44){

				//Determinam tipul noii figuri

				obj=in.nextInt();
				
				//si in functie de tipul acesteia apelam metoda corespunzatoare

				if(obj == 1){
					Rectangle.RectangleColision(q, in, Writer);
				}
				
				if(obj == 2){
					Triangle.TriangleColision(q, in, Writer);
				}
				
				if(obj == 3){
					Circle.CircleColision(q, in, Writer);
				}
				
				if(obj == 4){
					Rhombus.RhombusColision(q, in, Writer);
				}
			}
		}
		
		//La final inchidem scanner-ul si writer-ul

		in.close();
		Writer.flush();
		Writer.close();
	}
	
	/**
	 * Metoda main apeleaza metoda homework, ce rezolva tema
	 * @param args -parametrii liniei de comanda
	 * @throws IOException - arunca o exceptie in caz ca nu se citeste sau nu se scrie bine
	 */
	public static void main(String[] args) throws IOException{
		Main homework=new Main();
		homework.homework();
		
	}
}