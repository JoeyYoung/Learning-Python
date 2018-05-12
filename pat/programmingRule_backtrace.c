#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int N, M, L;
int main(){
	scanf("%d", &N);
	scanf("%d", &M);

	int* EvaOrder = (int*)malloc(sizeof(int)*M);

	int i;
	for(i = 0; i < M; i++)
		scanf("%d", &EvaOrder[i]);

	scanf("%d", &L);
	int* Color = (int*)malloc(sizeof(int)*L);

	for (i = 0; i < L; i++)
		scanf("%d", &Color[i]);

	int* len = (int*)malloc(sizeof(int)*L);
	int* priority = (int*)malloc(sizeof(int)*L);

	/* init index 0 */
	priority[0] = -1;
	len[0] = 0;
	for(i = 0; i < M; i++){
		if(EvaOrder[i] == Color[0]){
			priority[0] = i;
			len[0] = 1;
			break;
		}
	}

	/* dynamic process */
	for(i = 1; i < L; i++){
		int j;
		priority[i] = -1;
		for(j = 0; j < M; j++){
			if(Color[i] == EvaOrder[j]){
				priority[i] = j;
				break;
			}
		}

		if(priority[i] == -1){
			for(j = 0; j < i; j++)
				if(len[j] > len[i])
					len[i] = len[j];
			continue;
		}
		
		/* color is liked, */
		len[i] = 1;
		for(j = 0; j < i; j++){
			if(priority[j] <= priority[i] && priority[j] != -1){
				int temp = len[j]+1;
				if(temp > len[i])
					len[i] = temp;
			}
		}
	}
	int maxlen = 0;
	for(i = 0; i < L; i++){
		if(len[i] > maxlen)
			maxlen = len[i];
	}



	printf("%d", maxlen);
}
