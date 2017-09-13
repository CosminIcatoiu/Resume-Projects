//Icatoiu Vlad-Cosmin 314CB  

#include<stdio.h>
#include<stdlib.h>
#include<string.h>

typedef struct celula{

	//Informatia va fi un nod din arbore
	void * info;

	struct celula *urm;
}TCelula,*TLG;

typedef struct nod{
	
	//cod-ul si litera in caz ca aceasta exista
	char lit,cod;

	//parintele nodului curent
	struct nod * parinte;

	//lista cu fii nodului curent, fiecare fiu fiind legat de urmatorul,iar
	//nodul curent doar de primul

	TLG fii;
}TNod,*TArb;


//Functie ce construieste un nod de tip frunza

TArb ConstrFr( char cod)
{

	//se aloca memorie 

	TArb aux=malloc(sizeof(TNod));
	if(!aux)
		return NULL;
	//si se initializeaza campurile

	aux->cod=cod;
	aux->lit='\0';
	aux->parinte=NULL;
	aux->fii=NULL;
	return aux;
}

//Functie ce aloca o celula din lista de fii

TLG AlocCel( char cod)
{
	TLG aux;
	aux=malloc(sizeof(TCelula));
	if(!aux)
		return NULL;

	aux->urm=NULL;
	
	//Se introduce nodul in informatia celulei 

	aux->info=ConstrFr(cod);
	if(!aux->info)
	{
		free(aux);
		return NULL;
	}
	return aux;
}


//Functie ce introduce un nod nou in arbore

void add( TArb a, char * codificare, char lit)
{
	int i=0;
	int ok=0;
	TLG ant=NULL;
	TLG aux,aux1;

	//Daca arborele nu are fii 

	if(a->fii==NULL)
	{
		while(codificare[i]!='\0')
		{

			//Alocam succesiv celule pana cand nu mai avem caractere in
			//codificare, le legam la arbore si avansam in arbore

			a->fii=AlocCel(codificare[i]);

			if(!a->fii)
				return ;

			//retinem parintele

			((TArb)a->fii->info)->parinte=a;
		
			//daca am ajuns la ultimul caracter din codificare atunci introducem
			//si litera
			if(codificare[i+1]=='\0')
				((TArb)a->fii->info)->lit=lit;

			a=(TArb)a->fii->info;

			//avansam in codificare

			i++;
		}
	}
	else
	{
		while(codificare[i]!='\0')
		{
			//Parcurgem lista de fii 
			aux1=a->fii;

			while(aux1!=NULL)
			{
				if( ((TArb)aux1->info)->cod==codificare[i] )
				{
					//avansam in arbore daca am gasit un element al codificarii

					a=(TArb)aux1->info;

					//daca toata codificarea e deja in arbore adaugam doar litera

					if(codificare[i+1]=='\0')
						a->lit=lit;

					ok=1;
					break;
				}

				//altfel, retinem anteriorul si avansam in lista de fii
		
				ant=aux1;
				aux1=aux1->urm;
			}
			
			//daca anterioriul nu e null si nu am introdus litera deja

			if(ok==0 && ant!=NULL)
			{
				//alocam o noua celula
				aux=AlocCel(codificare[i]);

				if(!aux)
					return ;

				//legam parintele

				((TArb)aux->info)->parinte=a;

				//daca e ultimul element al codificarii introucem si litera

				if(codificare[i+1]=='\0')
					((TArb)aux->info)->lit=lit;

				//legam auxiliarul la lista de fii

				ant->urm=aux;

				//avansam in arbore

				a=(TArb)aux->info;
			}
			if(ant==NULL && ok ==0)
			{

				//daca am ajuns la un subarbore fara fii si nu am introdus
				//deja litera, alocam o noua celula

				a->fii=AlocCel(codificare[i]);

				if(!a->fii)
					return ;				

				//legam parintele

				((TArb)a->fii->info)->parinte=a;

				//daca am ajuns la finalul codificarii introducem si litera

				if(codificare[i+1]=='\0')
					((TArb)a->fii->info)->lit=lit;

				//avansam in arbore

				a=(TArb)a->fii->info;
			}

			//reinitializam anteriorul si verificatorul si avansam in codificare
 
			ant=NULL;
			i++;
			ok=0;
		}
	}
}


//Functie ce intoarce numarul de nivele dintr-un arbore

int NrNiv(TArb a, int nivc, int * nivmax)
{
	if(a==NULL)
		return 0;

	//Daca nivelul curent e mai mare ca nivelul maxim actual, il actualizam

	if(nivc > *nivmax)
		*nivmax=nivc;

	TLG aux=a->fii;

	while(aux!=NULL)
	{
		//Apelam functia pentru toti fii arborelui

		NrNiv( ((TArb)aux->info),nivc+1,nivmax);
		aux=aux->urm;
	}
	return 1;
}

//Functie ce scrie un anumit nivel din arbore intr-un fisier

void AfiNiv(TArb a,int nivc, int nivref,FILE * dfp)
{
	if(a==NULL)
		return ;
	
	//Daca nivelul curent e egal cu nivelul referinta

	if(nivc == nivref)
	{
		//afisam codificarea si litera daca sunt initializate cu o valoare

		fprintf(dfp,"(");

		if(a->cod !='\0')
			fprintf(dfp,"%c",a->cod);
		fprintf(dfp,":");

		if(a->lit != '\0')
			fprintf(dfp,"%c",a->lit);
		fprintf(dfp,")");

		return;
	}
	else
	{
		//altfel, apelam functia pe fii arborelui curent

		TLG aux=a->fii;

		while(aux!=NULL)
		{
			AfiNiv( ((TArb)aux->info),nivc+1,nivref,dfp);
			aux=aux->urm;
		}
	}
}

//Functie ce cauta o litera intr-un arbore 

TArb CautaLit( TArb a, char lit)
{
	if( a == NULL)
		return NULL;
	
	//Daca am gasit litera returnam nodul ce o contine

	if(a->lit==lit)
	{
		return a;
	}

	//Daca nu, apelam functia pentru fiii nodului

	TLG aux=a->fii;

	while(aux != NULL)
	{
		//Daca am gasit litera intr-un fiu, returnam fiul respectiv

		if(CautaLit( (TArb)aux->info,lit) !=NULL)
			return CautaLit( (TArb)aux->info,lit);

		aux=aux->urm;
	}
	return NULL;
}

//Functie ce elibereaza o celula
	
void elibel ( TLG a)
{
	free(a->info);
	free(a);
	a=NULL;
}

//Functia ce sterge o litera dintr-un arbore

void delete( TArb a, char lit)
{
	TArb b;
	
	//Cautam litera in arborele respectiv

	b=CautaLit(a,lit);
	
	//daca nu am gasit o iesim din functie

	if( b==NULL)
		return ;

	//daca nodul in care e litera are fii, stergem doar litera din nod

	if(b->fii !=NULL)
	{
		b->lit='\0';
		return ;
	}
	
	//retinem parintele

	TArb aux=b->parinte;

	TLG aux1,ant=NULL;

	//daca parintele  nu are litera in el si nu mai are alti fii si nu e 
	// radacina

	while( aux->lit == '\0' && aux->fii->urm == NULL && aux->parinte != NULL)
	{
			
		aux1=aux->fii;

		//cautam fiul in lista de fii a parintelui si retinem si anteriorul

		while( ((TArb)aux1->info)->cod != b->cod )
		{
			ant=aux1;
			aux1=aux1->urm;
		}

		//daca anteriorul e null reinitializam inceputul de lista si eliberam
		//celula

		if( ant == NULL )
		{
			aux->fii=aux->fii->urm;
			elibel(aux1);
		}
		else
		{

			//altfel legam anteriorul la urmatorul fiu si eliberam auxiliarul

			ant->urm=aux1->urm;
			elibel(aux1);
		}
		
		//mergem in sus in arbore

		b=aux;
		aux=aux->parinte;
		ant=NULL;
	}

	//procedeul mai trebuie repetat o data pentru a elimina ultima celula ramasa
	//pentru ca in while avansul se face in functie de parinte

	ant=NULL;
	aux1=aux->fii;
	while( ((TArb)aux1->info)->cod != b->cod )
	{
		ant=aux1;
		aux1=aux1->urm;
	}
	if( ant == NULL )
	{
		aux->fii=aux->fii->urm;
		elibel(aux1);
	}
	else
	{
		ant->urm=aux1->urm;
		elibel(aux1);
	}
}		

//Functia ce elibereaza memoria din arbore

void Distrugere( TArb a)
{
	TLG aux=a->fii;

	//Daca arborele nu are fii eliberam nodul 

	if( aux == NULL)
	{
		free(a);
	}
	else
	{
		//altfel eliberam fii, ulterior eliberand celula din lista si nodul 
		//parintelui

		while(aux)
		{
			TLG aux1=aux;
			aux=aux->urm;
			Distrugere( (TArb)aux1->info );
			free(aux1);
		}
		free(a);
	}
}

//functie ce decodifica un text

void decodificare( TArb a, char * text, char * delim,FILE * dfp)
{	
	int i;
	TLG aux=a->fii;
	char *p;

	//Cautam delimitatorul in text

	p=strtok(text,delim);

	while(p!=NULL)
	{
		//pentru fiecare cuvant gasit

		for(i=0;i<strlen(p);i++)
		{
			//cautam in lista de fii caracterul din codificare si avansam
			
			while(aux!=NULL)
			{
				if( ((TArb)aux->info)->cod == p[i])
				{
					if(i!= strlen(p) -1)
						aux=((TArb)aux->info)->fii;
					break;
				}
				else
					aux=aux->urm;
			}
		}

		//daca la finalul codificarii am gasit o litera, o scriem in fisier

		if(((TArb)aux->info)->lit != '\0')
			fprintf(dfp,"%c",((TArb)aux->info)->lit);

		//Repetam procedeul pentru urmatorul cuvant

		aux=a->fii;
		p=strtok(NULL,delim);
	}

}
int main(int argc, char * argv[])
{

	//Variabile in care vom citi din fisier

	int nivs=0,i,nrc,nrop,j,contor=1;
	char * text=malloc(1000);

	if(!text)
		return 0;

	char *lit=malloc(2);

	if(!lit)
	{
		free(text);
		return 0;
	}

	char*delim=malloc(2);
	if(!delim)
	{
		free(text);
		free(lit);
		return 0;
	}

	char * cod=malloc(200);
	if(!cod)
	{
		free(text);
		free(lit);
		free(delim);
		return 0;
	}	

	char *op=malloc(10);
	if(!op)
	{
		free(text);
		free(lit);
		free(delim);
		free(op);
		return 0;
	}

	char separ;
	
	//Fisierele de intrare,respectiv iesire

	FILE *fp1,*fp2,*dfp1,*dfp2;

	TArb a;

	//Initializam radacina cu '\0'

	a=ConstrFr('\0');
	
	//Deschidem fisierele

	fp1=fopen(argv[1],"r");
	fp2=fopen(argv[2],"r");
	dfp1=fopen(argv[3],"w");
	dfp2=fopen(argv[4],"w");

	//Citim numarul de caractere codificate

	fscanf(fp1,"%d",&nrc);

	for(i=1;i<=nrc;i++)
	{
		//pentru fiecare citim litera si codificarea si le introducem in arbore

		fscanf(fp1,"%s",lit);
		fscanf(fp1,"%s",cod);
		add(a,cod,lit[0]);
						
	}

	//Aflam numarul de nivele din arbore

	NrNiv(a,0,&nivs);

	//Afisam fiecare nivel din arbore

	for(i=0;i<=nivs;i++)
	{
		AfiNiv(a,0,i,dfp1);
		fprintf(dfp1,"\n");
	}

	//Citim delimitatorul si numarul de operatii

	fscanf(fp1,"%s",delim);

	fscanf(fp1,"%d",&nrop);

	for(i=1;i<=nrop;i++)
	{
		//In functie de operatie citim lucrurile corespunzatoare, apelam functia
		//ce executa operatia si apoi afisam arborele

		fscanf(fp1,"%s",op);

		if(strcmp(op,"add") == 0)
		{
			fscanf(fp1,"%s%s",lit,cod);

			add(a,cod,lit[0]);

			NrNiv(a,0,&nivs);

			for(j=0;j<=nivs;j++)
			{
				AfiNiv(a,0,j,dfp1);
				fprintf(dfp1,"\n");
			}
		}

		if(strcmp(op,"delete")== 0)
		{
			fscanf(fp1,"%s",lit);

			delete(a,lit[0]);

			NrNiv(a,0,&nivs);

			for(j=0;j<=nivs;j++)
			{
				AfiNiv(a,0,j,dfp1);
				fprintf(dfp1,"\n");
			}
		}
	}
	
	//Citim din fisier bucati de text pana la caractere albe

	while(fscanf(fp2,"%s",text) == 1)
	{
		
		decodificare(a,text,delim,dfp2);
		//Citim caracterele albe

		separ=fgetc(fp2);

		//Daca acestea sunt \n sau spatiu le scriem in fisier

		if(separ!='\0')
			fprintf(dfp2,"%c",separ);
		else
			break;
		separ=fgetc(fp2);

		//Retinem cate caractere am citit si mergem cu fseek inapoi pentru a nu
		//pierde din caracterele citite si reinitializam contorul

		while(separ=='\n')
		{
			fprintf(dfp2,"%c",separ);
			separ=fgetc(fp2);
			contor++;
		}	
		contor=-contor;	
		fseek(fp2,contor,SEEK_CUR);
		contor=1;
	}

	//Inchidem fisierele

	fclose(fp1);
	fclose(fp2);
	fclose(dfp1);
	fclose(dfp2);

	//Eliberam memoria alocata

	free(delim);
	free(lit);
	free(cod);
	free(op);
	free(text);
	Distrugere(a);
	return 0;
}
