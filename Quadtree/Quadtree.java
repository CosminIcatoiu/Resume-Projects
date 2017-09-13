import java.util.*;

/**
 * Una din clasele de baza ale temei. Ea simuleaza un arbore cu 4 copii.
 * Ea are 2 metode de baza, acelea de inserare si stergere din Quadtree.
 * Membrii acestei clase sunt urmatori:
 * -Un vector de figuri in care vom pastra figurile din nodul curent
 * -Un vector de 4 Quadtree, reprezentand copiii nodului curent.
 * -Cadranul specific nodului curent
 * -Coordonatele centrului si dimensiunea cardanului
 * -Un camp care ne spune daca nodul curent e frunza sau nu
 * @author cosmin
 *
 */
public class Quadtree {

	Vector<GeometricObject> o=new Vector<GeometricObject>();

	Quadtree[] children=new Quadtree[4];

	GeometricObject Quadran;

	double xcenter;
	double ycenter;
	double length;

	boolean isleaf;

	public Quadtree(double xcenter,double ycenter,double length){
		
		this.xcenter=xcenter;
		this.ycenter=ycenter;
		this.length=length;
		Quadran = new GeometricObject(xcenter-length,ycenter-length,xcenter+length,ycenter+length,-1);
		isleaf=true;
	}

	/**
	 * Functie ce insereaza o figura in Quadtree, cautand recursiv cadranul in care
	 * trebuie inserat
	 * @param q - Quadtree-ul in care inseram
	 * @param aux- Figura pe care o inseram
	 */
	public void insert(Quadtree q,GeometricObject aux){
		
		//Daca am ajuns la o frunza

		if( q.isleaf ){

			//si aceasta este goala, atunci inseram figura
			
			if( q.o.isEmpty() ){
				q.o.add(aux);
				return ;
			}

			//altfel, cautam prin lista de figuri din frunza si daca gasim
			// o figura cu care noua figura se intersecteaza, o introducem in
			// lista
			
			for(int i=0; i< q.o.size(); i++)
			{
				if(q.o.elementAt(i).colision(aux) == true){
					q.o.add(aux);
					return;
				}
			}
		}
		
		//Daca nu putem insera in nodul curent si acesta este frunza ii 
		// initializam copii si facem nodul sa nu mai fie frunza

		if( q.isleaf ){
			
			Tasks.CreateChildren(q);
			q.isleaf=false;
		}
	
		if( !q.o.isEmpty() ){
			
			//Daca frunza nu era goala, mutam elementele din ea in copiii
			// corespunzatori

			for(int i=0; i< q.o.size(); i++){
		
				for(int j=0 ; j < 4; j++){
					
					//Verificam in ce copil poate fi introdusa figura si il inseram

					if( q.children[j].Quadran.colision(q.o.elementAt(i)) ){
						insert(q.children[j],q.o.elementAt(i));
					}
				}
				
			}

			//La final stergem tot ce era in lista de figuri			

			q.o.clear();
		}
		
		//Dupa ce am reinserat ce se afla in nod, inseram la fel si noua figura

		for(int j=0; j < 4; j++){
			
			if(q.children[j].Quadran.colision(aux)){
				insert(q.children[j],aux);
			}
		}
		
	}	
	
	/**
	 * Functie ce sterge o figura dintr-o lista de figuri.
	 * Parcurgem lista pana gasim figura pe care o cautam si apoi o stergem
	 * @param q - nodul din care stergem
	 * @param id- id-ul figurii pe care o stergem
	 */
	public void removEL(Quadtree q, int id){

		for(int i=0; i< q.o.size(); i++){

			if(q.o.elementAt(i).id == id){
				q.o.remove(i);
				break;
			}
		}
		
	}

	/**
	 * Functie ce sterge o figura dintr-un Quadtree
	 * @param q- Quadtree-ul din care stergem
	 * @param o- Figura pe care o stergem
	 * @param id- Id-ul figurii pe care o stergem
	 */
	public void remove(Quadtree q, GeometricObject o , int id){
		
		//Daca suntem intr-o frunza stergem figura

		if( q.isleaf ){

			removEL(q,id);
			return;
		}
		
		int j;
		
		//Daca nu suntem intr-o frunza, apelam stergerea pe cadranele cu care
		// figura se intersecteaza, daca acestea nu sunt frunze

		for( j = 0; j < 4; j++){

			if( !q.children[j].isleaf && q.children[j].Quadran.colision(o))
				remove(q.children[j],o,id);
		}

		//Pentru cele care sunt frunze si nu sunt goale stergem elementul direct
		
		for( j = 0; j < 4; j++ ){
			
			if( !q.children[j].o.isEmpty() ){
				removEL(q.children[j],id);
			}
		}
		
		//Daca exista macar un fiu al nodului curent care nu este frunza 
		// nu putem sa mai facem nimic si ne intoarecem in recursivitate

		for( j=0; j < 4; j++){
			
			if( !q.children[j].isleaf )
				return;
		}
		
		//Daca toti fii sunt frunze, verificam daca acestia au vreo figura in 
		// ei si daca au introducem index-ul lor intr-un vector

		boolean ok=true;
		Vector<Integer> index= new Vector<Integer>();
		
		for( j = 0; j < 4; j++){
			
			if( !q.children[j].o.isEmpty() ){
				ok=false;
				index.add(j);
			}
		}
		
		//Daca toti fii sunt liberi atunci facem nodul sa fie frunza

		if( ok ){
			
			q.isleaf= true;
			return;
		}
		else{

			//Daca numai un fiu contine ceva mutam lista lui de obiecte in
			//nodul curent si il facem pe acesta sa fie frunza
			
			if(index.size()== 1){
				
				q.o=q.children[index.elementAt(0)].o;
				q.isleaf=true;
				return;
				
			}
			
			//Comparam listele de obiecte din fii si daca exista doua liste
			//diferite, atunci nu putem sa mai facem nimic ne intoarcem din 
			//recursivitate 

			for(int i=0; i< index.size() -1;i++){
				if(q.children[index.elementAt(i)].o.equals(q.children[index.elementAt(i+1)].o) == false){
					return;
				}
			}
		
			//Daca toti copiii contin acelasi lucru copiem lista de obiecte in
			// nodul curent si il facem sa fie frunza

			q.o=q.children[index.elementAt(0)].o;
			q.isleaf=true;
		}
		
	}
}