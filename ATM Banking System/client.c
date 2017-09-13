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

int main(int argc, char ** argv) {

	int sd_udp, sd_tcp, rw;
	struct sockaddr_in udp_addr, tcp_addr;
	fd_set temp_fs;
    fd_set read_fs;
    int fdmax, id;
	char buf[BUFLEN],copy[BUFLEN];
	FILE * fid;

	//Crearea socket-ului udp

	sd_udp = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	
	if( sd_udp == -1 ) {
		printf("-10: Eroare la apel socket\n");
	}

	//Crearea socket-ului tcp

	sd_tcp = socket(PF_INET, SOCK_STREAM, 0);

	if( sd_tcp == -1 ) {
		printf("-10: Eroare la apel socket\n");
	}

	//Stabilirea legaturii udp intre server si client

	udp_addr.sin_family = AF_INET;
	udp_addr.sin_port = htons(atoi(argv[2]));
	inet_aton(argv[1], &udp_addr.sin_addr);
	
	//Stabilirea legaturii tcp intre server si client

	tcp_addr.sin_family = AF_INET;
	tcp_addr.sin_port = htons(atoi(argv[2]));
	inet_aton(argv[1], &tcp_addr.sin_addr);

	//Crearea setului de socketi de la care va primi clientul raspunsuri si cereri

	FD_ZERO(&temp_fs);
	FD_ZERO(&read_fs);
	FD_SET(0,&read_fs);
	FD_SET(sd_udp, &read_fs);
	FD_SET(sd_tcp, &read_fs);

	if(sd_udp > sd_tcp) {
		fdmax = sd_udp;
	} else {
		fdmax = sd_tcp;
	}

	//Realizarea conexiunii tcp intre server si client cu ajutorul socket-ului creat

	if(connect(sd_tcp,(struct sockaddr *) &tcp_addr, sizeof(tcp_addr)) < 0) {
		printf("-10 : Eroare la apel connect\n");
	}

	//Variabile care ne dau informatii despre starea curenta a userului

	int logged_in = 0;
	int card_number = -1;
	int unlock_request = 0;

	id = getpid();

	sprintf(buf,"client-%d.log",id);

	fid = fopen(buf,"w");

	//Inceperea comunicarii

	while(1) {
	
		temp_fs = read_fs;
		if(select(fdmax+1,&temp_fs,NULL,NULL,NULL) == -1) {
			printf("-10 : Eroare la apel select\n");
			fprintf(fid,"-10 : Eroare la apel select\n");
		}

		//Primirea comenzilor de la utilizator

		if(FD_ISSET(0,&temp_fs)) {

			//Citim comanda si o scriem in fisierul de log

			memset(buf,0,BUFLEN);
			fgets(buf,BUFLEN-1,stdin);
			fprintf(fid,"%s",buf);
			
			//Daca anterior am primit o comanda de unlock string-ul citit va fi considerat parola

			if(unlock_request == 1) {

				//Prelucram parola astfel incat sa punem inainte numarul de card si apoi trimitem
				//string-ul format catre bancomat

				memset(copy,0,BUFLEN);
				strcpy(copy,buf);
				
				//Eliminam \n-ul de la final

				copy[strlen(copy)-1]='\0';
				memset(buf,0,BUFLEN);
				
				sprintf(buf,"%d %s",card_number,copy);
				
				//Trimitem pe canalul udp catre bancomat parola si numarul de card

				rw = sendto(sd_udp, buf, strlen(buf), 0,(struct sockaddr*)&udp_addr, sizeof(udp_addr));
			} else {

				//Daca am primit comanda quit din partea utilizatorului transmitem catre server si
				//inchidem conexiunea

				if(strstr(buf,"quit") != NULL) {
					rw = send(sd_tcp, buf, strlen(buf),0);
				
					if(rw < 0 ) {
						printf("-10 : Eroare la apel send\n");
						fprintf(fid,"-10 : Eroare la apel send\n");
					}
				
					break;
				}

				//Daca am primit o comanda de unlock

				if(strstr(buf, "unlock") != NULL) {
				
					//Punem la finalul string-ului numarul de card anterior

					memset(copy,0,BUFLEN);
					strcpy(copy,buf);
					
					memset(buf,0,BUFLEN);
					copy[strlen(copy)-1]='\0';
					sprintf(buf,"%s %d",copy,card_number);
					
					//Trimitem comanda catre bancomat

					rw = sendto(sd_udp, buf, strlen(buf), 0,(struct sockaddr*)&udp_addr, sizeof(udp_addr));
				} else {

					//Daca am primit o comanda de login si suntem deja logati afisam mesajul corespunzator

					if(strstr(buf, "login") != NULL && logged_in == 1) {
					
						printf("-2 : Sesiune deja deschisa\n");
						fprintf(fid,"-2 : Sesiune deja deschisa\n\n");
					
					} else {
						
						//Daca am primit alta comanda in afara de login si nu suntem logati afisam din nou un
						//mesaj corespunzator

						if(strstr(buf,"login") == NULL && logged_in == 0) {
							printf("-1 : Clientul nu este autentificat\n");
							fprintf(fid,"-1 : Clientul nu este autentificat\n\n");
						} else {
							//Daca primim comanda log in salvam numarul de card 

							if(strstr(buf,"login") != NULL) {
								strcpy(copy,buf);
								char * p = strtok(copy," ");
								p = strtok(NULL," ");
								card_number = atoi(p);
							}

							//Daca primim o comanda de logout resetam variabilele

							if(strstr(buf,"logout") != NULL) {
								logged_in = 0;
							}

							//Trimitem comanda catre server

							rw = send(sd_tcp, buf, strlen(buf), 0);
							if( rw < 0 ) {
								printf("-10 : Eroare la apel send\n");
								fprintf(fid,"-10 : Eroare la apel send\n");
							}
						}
					}
				}
			}
		}

		//Daca am primit un raspuns din partea bancomatului pe canalul tcp

		if(FD_ISSET(sd_tcp,&temp_fs)) {

			//Receptionam mesajul

			memset(buf,0,BUFLEN);
			rw = recv(sd_tcp,buf,BUFLEN,0);
			
			if( rw > 0 ) {

				//Afisam clientului mesajul primit si il scriem in fisier doar daca
				//bancomatul nu isi incheie activitatea

				printf("ATM> %s\n",buf);
				if(strstr(buf,"Bancomatul") == NULL) {
					fprintf(fid,"ATM> %s\n\n",buf);
				}
				if(strstr(buf,"Welcome") != NULL) {
					//Daca am primit mesajul Welcome inseamna ca ne-am logat cu succes si atunci
					//modificam variabila corespunzatoare

					logged_in = 1;
				}
			}

			if( rw < 0 ) {
				printf("-10 : Eroare la apel recv\n");
				fprintf(fid,"-10 : Eroare la apel recv");
			}

			if( rw <= 0) {
				FD_CLR(sd_tcp, &read_fs);
				close(sd_tcp);
				return 1;
			}
		}

		//Daca am primit un mesaj pe canalul udp

		if(FD_ISSET(sd_udp,&temp_fs)) {
		
			//Pregatim primirea mesajului

			memset(buf,0,BUFLEN);
			int size = sizeof(udp_addr);
			rw = recvfrom(sd_udp, buf, sizeof(buf), 0, (struct sockaddr *) &udp_addr, &size);

			if(rw == -1) {
				printf("-10 : Eroare la apel recvfrom\n");
				fprintf(fid,"-10 : Eroare la apel recvfrom\n");
			} else {

				printf("UNLOCK> %s\n",buf);
				fprintf(fid,"UNLOCK> %s\n\n",buf);

				//Daca am trimis o cerere de deblocare acceptata modificam variabila pentru a trata string-ul
				//citit ca fiind parola secreta. In orice alta situatie, tratam string-ul urmator ca fiind o
				//comanda.

				if(strstr(buf,"Trimite") != NULL) {
					unlock_request = 1;
				} else {
					unlock_request = 0; 
				}
			}
		}
	}
	
	//Inchidem cei 2 socketi
	
	close(sd_udp);
	close(sd_tcp);
	return 0;
}
