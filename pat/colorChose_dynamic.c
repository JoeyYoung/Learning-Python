#include <stdio.h>
#include <stdlib.h>
#include <string.h>
using namespace std;
/* Content hours, problem number, read time */
int H, N, t0, Hmin;

typedef struct node{
	char name[20];
	int t;
	int d;
	bool sovled;
}*Problem;

Problem problems[10];
int order[10];
int final_order[10];

int total_time = 9999999;
int solved_problem = 0;

void BackTrack(int n, int usedT, int finalT){
	// solve one
	Problem cur = problems[order[n]];
	int debugN = (usedT+cur->t-1)/60;
	int curT = usedT + cur->t + debugN*cur->d;
	int totalT = finalT + curT + debugN*20;
	int i;

	if(curT <= Hmin){
		n++;
		if(n == N){
			if(n > solved_problem ||
			(n == solved_problem && totalT < total_time)){
				total_time = totalT;
				solved_problem = N;
				for(i = 0; i < N; i++)
					final_order[i] = order[i];
			}
		}else{
			for(i = 0; i < N; i++){
				if(problems[i]->sovled == true)
					continue;
				problems[i]->sovled = true;
				order[n] = i;
				BackTrack(n, curT, totalT);
				problems[i]->sovled = false;
			}
		}
	}else{ // timeout 
		if(n > solved_problem ||
			(n == solved_problem && finalT < total_time)){
			solved_problem = n;
			total_time = finalT;
			for(i = 0; i < n; i++)
				final_order[i] = order[i];
		}
	}
}

int main(){
	int i, j, k;
	while(1){
		total_time = 9999999;
		solved_problem = 0;
		/* each case */
		scanf("%d", &H);
		/* H = -1 */
		if(H < 0)
			break;
		Hmin = H * 60;
		scanf("%d %d", &N, &t0);
		
		/* init problem properity */
		for(i = 0; i < N; i++){
			problems[i] = (Problem)malloc(sizeof(struct node));
			scanf("%s %d %d", problems[i]->name, &problems[i]->t, &problems[i]->d);
			problems[i]->sovled = false;
			/* index of array can represents the id */
		}

		for(i = 0; i < N; i++){
			problems[i]->sovled = true;
			order[0] = i;
			BackTrack(0, t0, 0);
			problems[i]->sovled = false;
		}

		printf("Total Time = %d\n", total_time);
	    for (i = 0; i < solved_problem; i++) {
	        printf("%s\n", problems[final_order[i]]->name);
	    }
	} // while
	return 0;
}
