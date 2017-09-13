#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <arpa/inet.h>
#define BUFLEN 1500

//Structura unui user

typedef struct {

	char nume[12];
	char prenume[12];
	int numar_card;
	int pin;
	char parola_secreta[16];
	double sold;
	int logged_in;
	int id;
	int attempts;
	int blocked;
} user;

int main(int argc, char ** argv) {

	int sd_udp,sd_tcp,new_sd;
	struct sockaddr_in addr_tcp,addr_udp,addr_cli;
	int users_nr;
	char buf[BUFLEN],copy[BUFLEN],send_buf[BUFLEN];
	fd_set read_fs,temp_fs;
	int fdmax, rw,numar_card,pin,ok, suma_retrasa;
	double suma_depusa;
	char parola_secreta[16];
	char *p;
	FILE * fid;

	//Cream socket-ul udp

	sd_udp = socket(AF_INET,SOCK_DGRAM,0);

	//Cream socket-ul tcp

	sd_tcp = socket(PF_INET,SOCK_STREAM,0);

	//Cream legatura udp

	addr_udp.sin_family = AF_INET;
	addr_udp.sin_port = htons(atoi(argv[1]));
	addr_udp.sin_addr.s_addr = htons(INADDR_ANY);

	//Cream legatura tcp

	addr_tcp.sin_family = AF_INET;
	addr_tcp.sin_addr.s_addr = INADDR_ANY;
	addr_tcp.sin_port = htons(atoi(argv[1]));

	//Facem bind-ul pe canalul udp

	if ( bind(sd_udp, (struct sockaddr*) &addr_udp, sizeof(addr_udp)) < 0) {
		printf("-10: Eroare la apel bind\n");
		return 0;
	}

	//Si pe canalul tcp

	if (bind(sd_tcp, (struct sockaddr *) &addr_tcp, sizeof(addr_tcp)) < 0) {
        printf("-10: Eroare la apel bind\n");
		return 0;
	}

	//Deschidem fisierul pentru a citi datele utilizatorilor

	fid = fopen(argv[2],"r");

	fscanf(fid,"%d",&users_nr);

	user users[users_nr];

	int i,j;

	//Salvam in vectorul cu utilizatori atat datele lor cat si anumite elemente care ne
	//ofera informatii despre starea fiecarui utilizator (socket-ul pe care e deschis),
	//daca e logat, daca e un cont blocat, daca s-a mai gresit pin-ul etc

	for(i = 0; i < users_nr; i++) {
		fscanf(fid,"%s%s%d%d%s%lf",users[i].nume,users[i].prenume,&users[i].numar_card,
			&users[i].pin,users[i].parola_secreta,&users[i].sold);
		users[i].logged_in = -1;
		users[i].id = -1;
		users[i].attempts = 0;
		users[i].blocked = -1;
	}

	fclose(fid);

	//Facem server-ul sa asculte de un anumit numar de clienti

	listen(sd_tcp,2*users_nr);

	//Si initializam seturile cu socketii de la care va primii server-ul cereri

	FD_ZERO(&read_fs);
	FD_ZERO(&temp_fs);
	FD_SET(0,&read_fs);
	FD_SET(sd_tcp,&read_fs);
	FD_SET(sd_udp,&read_fs);
	
	//Stabilim socket-ul cu numarul maxim

	if(sd_udp > sd_tcp) {
		fdmax = sd_udp;
	} else {
		fdmax = sd_tcp;
	}

	//Incepem sa asteptam comenzi

	while(1) {
		temp_fs = read_fs;
		
		if(select(fdmax+1,&temp_fs,NULL,NULL,NULL) == -1) {
			printf("-10: Eroare la apel select\n");
		}

		for(i = 0; i <= fdmax; i++) {

			//Pentru fiecare numar posibil al unui socket, daca este in setul de socketi

			if(FD_ISSET(i,&temp_fs)) {
				
				//Daca este chiar socket-ul tcp acceptam un nou client si il introducem
				//in set-ul de socketi.

				if( i == sd_tcp ) {
					int len = sizeof(addr_cli);
					new_sd = accept(sd_tcp, (struct sockaddr *)&addr_cli, &len);

					if(new_sd == -1) {
						printf("-10: Eroare la apel accept\n");
					} else {
						FD_SET(new_sd,&read_fs);

						//Updatam numarul maxim al unui socket

						if( new_sd > fdmax ) {
							fdmax = new_sd;
						}
					}
				
					break;

				} else {
					//Daca am primit pe canalul udp o comanda

					if( i == sd_udp ) {

						//Pregatim primirea mesajului

						memset(buf,0,BUFLEN);
						int size = sizeof(addr_udp);

						rw = recvfrom(sd_udp, buf, sizeof(buf), 0, (struct sockaddr *) &addr_udp, &size);
						
						memset(copy,0,BUFLEN);
						memset(send_buf,0,BUFLEN);

						strcpy(copy,buf);

						p = strtok(copy," ");

						//Vedem daca este cererea de unlock sau este parola secreta

						if(strcmp(p,"unlock") == 0) {
							
							ok = 0;

							p=strtok(NULL," ");
							numar_card = atoi(p);

							//Cautam printre useri pe cel cu numarul de card primit ca in comanda

							for(j = 0; j < users_nr; j++) {
								if(users[j].numar_card == numar_card) {
									ok = 1;
									if(users[j].blocked == 1) {
										//Daca am primit o cerere de unlock cand eram logati si contul nu era blocat
										//trimitem mesajul de tranzactie esuata, altfel cerem parola secreta

										sprintf(send_buf,"Trimite parola secreta");
									} else {
										sprintf(send_buf,"-6 : Operatie esuata");
									}
									break;	
								}
							}

							//Daca nu am gasit pe nimeni cu acel numar de card 

							if(ok == 0) {
								sprintf(send_buf,"-4 : Numar card inexistent");
							}

							//Trimitem mesajul corespunzator, tot pe canalul udp

							sendto(sd_udp, send_buf, strlen(send_buf), 0,(struct sockaddr*)&addr_udp, sizeof(addr_udp));
						} else {

							//Daca am primit parola secreta verificam daca este corecta si trimitem apoi mesajul

							numar_card = atoi(p);

							p = strtok(NULL," ");

							for(j = 0; j < users_nr; j++) {

								if(users[j].numar_card == numar_card) {

									if(strcmp(users[j].parola_secreta,p) == 0) {
										users[j].blocked = -1;
										sprintf(send_buf,"Client deblocat");

									} else {
										sprintf(send_buf,"-7 : Deblocare esuata");
									}
								}
							}

							sendto(sd_udp, send_buf, strlen(send_buf), 0,(struct sockaddr*)&addr_udp, sizeof(addr_udp));
						}

					} else {
						if( i == 0 ) {

							//Daca bancomatul primeste mesajul de quit

							memset(buf,0,BUFLEN);
							fgets(buf,BUFLEN-1,stdin);
							buf[strlen(buf)-1]='\0';
							memset(send_buf,0,BUFLEN);
							
							sprintf(send_buf,"Bancomatul isi incheie activitatea");

							//Anuntam toti clientii din acel moment

							if(strstr(buf,"quit")) {

								for(j = 3; j <= fdmax; j++) {
									if(j != sd_udp && j!= sd_tcp) {
										if(FD_ISSET(j,&read_fs)) {
											send(j,send_buf,strlen(send_buf),0);
										}
									}
								}

							//Inchidem cei doi socketi si iesim

							close(sd_tcp);
							close(sd_udp);
							return 0;
							}
						} else {

							//Daca am primit o comanda de la unul din clienti pe un canal tcp
							// receptionam comanda.

							memset(buf,0,BUFLEN);
							rw = recv(i, buf, sizeof(buf), 0);
							
							if( rw <= 0 ) {
								printf("-10 : Eroare la apel recv\n");
							} else {
								
								memset(send_buf,0,BUFLEN);
								strcpy(copy,buf);

								//Daca am primit o cerere de login

								if(strstr(buf, "login") != NULL) {
									ok = 0;

									//Extragem pin-ul si numarul de card

									p=strtok(copy," ");
									p=strtok(NULL," ");
									numar_card = atoi(p);
									p=strtok(NULL," ");
									pin = atoi(p);

									//Cautam un user care are acel numarul de card

									for(j = 0; j < users_nr; j++) {
										
										if( users[j].numar_card == numar_card ) {
											ok = 1;

											//Verificam daca are cardul blocat

											if(users[j].blocked == 1) {
												sprintf(send_buf,"-5 : Card blocat");
											} else {

												//Sau daca nu este cumva deja logat de pe alt client

												if(users[j].logged_in == 1) {
													sprintf(send_buf,"-2 : Sesiune deja deschisa");
												} else {

													//Verificam daca pinul este corect

													if(users[j].pin == pin) {
														sprintf(send_buf,"Welcome %s %s",users[j].nume,users[j].prenume);
														users[j].logged_in = 1;
														users[j].attempts = 0;
														users[j].id = i;
													} else {
														//Daca nu este, vedem daca am gresit de 3 ori si in functie de situatie
														//afisam mesajul corespunzator

														users[j].attempts++;
														if(users[j].attempts == 3) {
															users[j].blocked = 1;
															users[j].attempts = 0;
															sprintf(send_buf,"-5 : Card blocat");
														} else {
															sprintf(send_buf,"-3 : Pin gresit");
														}
													}	
												}									
											}					
										}
									}
									
									//Daca nu am gasit niciun user cu acel numar de card trimitem eraorea corespunzatoare

									if(ok == 0) {
										sprintf(send_buf,"-4 : Numar card inexistent");
									}
									
									//La final trimitem mesajul

									send(i,send_buf,strlen(send_buf),0);
								}
							
								//daca am primit o cerere de logout, cautam user-ul care are asociat acel socket
								// si il delogam si apoi confirmam delogarea

								if(strstr(buf,"logout") != NULL) {

									for(j = 0; j < users_nr; j++) {
										
										if(users[j].id == i) {
											users[j].logged_in = -1;
											users[j].id = -1;
											break;
										}
									}

									sprintf(send_buf,"Deconectare de la bancomat");
									send(i,send_buf,strlen(send_buf),0);
								}

								//Daca am primit o cerere de listsold, cautam din nou user-ul dupa socket si 
								//ii trimitem sold-ul

								if(strstr(buf,"listsold") != NULL) {
									
									for(j = 0; j < users_nr; j++) {
										if(users[j].id == i) {
											sprintf(send_buf,"%.2lf",users[j].sold);
											send(i,send_buf,strlen(send_buf),0);
											break;
										}
									}
								}

								//Daca am primit o cerere de extragere a banilor cautam din nou userul dupa socket

								if(strstr(buf,"getmoney") != NULL) {

									memset(copy,0,BUFLEN);
									strcpy(copy,buf);
									p = strtok(copy," ");
									p = strtok(NULL," ");
									suma_retrasa = atoi(p);

									//Daca suma retrasa nu e multiplu de 10 nu mai are sens sa cautam userul

									if(suma_retrasa % 10 != 0) {
										sprintf(send_buf,"-9 : Suma nu e multiplu de 10");
									} else {

										for(j = 0; j < users_nr; j++) {
											if(users[j].id == i) {
												
												//Verificam daca userul are sau nu fonduri suficente si daca are,
												//extragem din sold.

												if(users[j].sold < suma_retrasa) {
													sprintf(send_buf,"-8 : Fonduri insuficiente");
												} else {
													sprintf(send_buf,"Suma %d retrasa cu succes",suma_retrasa);
													users[j].sold -= suma_retrasa;
												}
												break;
											}
										}
									}
									//Trimitem mesajul corespunzator

									send(i,send_buf,strlen(send_buf),0);
								}

								//Daca am primit o comanda de depunere de bani extragem suma depusa din comanda si
								//o adunam la soldul curent.

								if(strstr(buf,"putmoney") != NULL) {

									memset(copy,0,BUFLEN);
									strcpy(copy,buf);
									p=strtok(copy," ");
									p=strtok(NULL," ");
									suma_depusa = atof(p);

									for(j = 0; j < users_nr; j++) {
										if(users[j].id == i) {
											users[j].sold += suma_depusa;
											break;
										}
									}

									sprintf(send_buf,"Suma depusa cu succes");
									send(i,send_buf,strlen(send_buf),0);
								}

								//Daca un client vrea sa se deconecteze il stergem din lista de socketi si de asemenea
								//verificam sa il delogam daca nu a facut el delogarea

								if(strstr(buf,"quit") != NULL) {

									for(j = 0; j < users_nr; j++) {
										
										if(users[j].id == i) {
											users[j].logged_in = -1;
											users[j].id = -1;
											break;
										}
									}

									FD_CLR(i,&read_fs);
									close(i);
								}
							}
						}
					}
				}
			}
		}	
	}

	//La final inchidem cei 2 socketi

	close(sd_udp);
	close(sd_tcp);
	return 0;
}								
