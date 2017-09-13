/**
 * Clasa de baza a tuturor figurilor geometrice. Contine dreptunghiul inconjurator
 * si id-ul figurii geometrice. Contine metode de intersectie a unui dreptunghi
 * cu un alt obiect si daca un dreptunghi contine un punct pentru ca folosim clasa
 * si pentru a vedea in ce cadran trebuie sa introducem un obiect.Constructorul 
 * initializeaza id-ul si dreptunghiul inconjurator.
 * @author Icatoiu Vlad Cosmin
 *
 */
public class GeometricObject {

	public double xl,yl,xr,yr;
	public int id;

	public GeometricObject(double xl, double yl, double xr, double yr, int id){
		
		this.xl=xl;
		this.yl=yl;
		this.xr=xr;
		this.yr=yr;
		this.id=id;
		
	}

	/**
	 * Functie ce verifica daca doua obiecte se intersecteaza ( si mai exact daca un obiect o
	 * se intersecteaza cu dreptunghiul inconjurator sau cu un cadran)
	 * @param o - obiectul pentru care testam coliziunea
	 * @return - se returneaza true daca cele doua obiecte se interesecteaza si false altfel
	 */
	public boolean colision(GeometricObject o){
		
		// daca a doua figura se afla in stanga sau in dreapta primei figuri

		if(o.xl> xr || o.xr < xl)
			return false;

		// sau daca a doua figura este deasupra sau sub prima inseamna ca nu 
		// se intersecteaza
		
		if(o.yl > yr || o.yr < yl )
			return false;
		
		return true;
	}

	/**
	 * Functie ce verifica daca un punct este continut de un cadran
	 * @param x - coordonata x a punctului
	 * @param y - coordonata y a punctului
	 * @return - true daca punctul este continut de cadran si false, altfel
	 */
	public boolean contains(double x, double y){
		
		if(x <= xr && x >= xl && y <=yr && y >= yl)
			return true;
		
		return false;
		
	}
}