#include <stdio.h>
#include <stdlib.h>
#include <math.h>

double Cmax, Dtotal, Davg;
int N;
typedef struct node{
	double price;
	double dist;
}station;

station* S;

void adjustWithD();

int main(){
	scanf("%lf %lf %lf %d", &Cmax, &Dtotal, &Davg, &N);
	int i;
	S = (station*)malloc(sizeof(station)*(N+1));

	for(i = 0; i < N; i++){
		scanf("%lf %lf", &S[i].price, &S[i].dist);
	}

	S[N].price = 999999;
	S[N].dist = Dtotal;

	adjustWithD();

	//加满最远可跑的距离
	double maxrun = Cmax*Davg;
	//汽油余量
	double gasRemain = 0.0;
	// 总价
	double Cost = 0.0;

	// 开局无法加油
	if(S[0].dist != 0){
		printf("The maximum travel distance = 0.00");
		return 0;
	}

	for(i = 0; i < N; i++){
		if(i != 0)
			gasRemain -= (S[i].dist-S[i-1].dist)/Davg;

		double now_price = S[i].price;
		double now_dist = S[i].dist;

		// find cheaper
		int j = i+1;
		while(j < N && S[j].price >= now_price)
			j++;

		if(S[i].dist + maxrun < S[j].dist){
			double gasNeed = Cmax - gasRemain;
			Cost += gasNeed*now_price;
			gasRemain = Cmax;
		}else{
			double reach_dist = gasRemain*Davg;
			if(reach_dist + now_dist >= S[j].dist)
				continue;
			double gasNeed = (S[j].dist - reach_dist - now_dist)/Davg;
			Cost += gasNeed*now_price;
			gasRemain += gasNeed;
		}
		
		if(now_dist + maxrun < S[i+1].dist){
			printf("The maximum travel distance = %.2f", now_dist + maxrun);
			break;
		}
	}
	if(i==N)
		printf("%.2f",Cost);
}

void adjustWithD(){
	int k, m;
	for(k = 0; k < N-1; k++){
		for(m = 0; m < N-1-k; m++){
			if(S[m].dist > S[m+1].dist){
				station temp;
				temp.dist = S[m].dist;
				temp.price = S[m].price;

				S[m].dist = S[m+1].dist;
				S[m].price = S[m+1].price;

				S[m+1].dist = temp.dist;
				S[m+1].price = temp.price;
			}
		}
	}
}
