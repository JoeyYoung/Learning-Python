#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
 
#define Nmax 70
#define Mmax 1100

clock_t start, stop;
double duration;

int N, M;
int F[Nmax];
char C[Nmax], Code[Nmax][Nmax];

/*Calculate the lowest wpl of these N distinct characters*/
int WPL(int F[]);

/*Percolate an item down into a min heap*/
void Percolate_down(int *Heap, int index);

/*Delete the min item in a min heap*/
int Delete_min(int *Heap);

/*Insert an item into a min heap*/
void Insert(int *Heap, int element);

/*Judge if a string is the prefix of another one*/
int Judge_pre(char *code1, char *code2);

int main()
{	
	start = clock();
	for(int l = 1; l<=10000; l++){
			memset(F,0,sizeof(F));  /*Initialization*/ 
			memset(C,0,sizeof(C));
			freopen("4.txt","r",stdin);
			scanf("%d",&N);  /*Store the number of the characters*/
			getchar();
			for(int i=0;i<N;i++)
			    scanf("%c %d ", &C[i], &F[i]);  /*Store the characters and their frequencies*/
		    scanf("%d",&M);
		    
		    getchar();
		    int Wpl;
		
		    for(int k=1; k<=1; k++){
				Wpl = WPL(F);
			}
			
		      /*Calculate wpl of given N characters*/
		    for(int i=0;i<M;i++){
		    	memset(Code,0,sizeof(Code));  /*Initialization*/
		    	for(int j=0;j<N;j++){
		    		scanf("%c %s\n", &C[j], Code[j]);  /*Store the student's answer*/
		    	}
				int equ_wpl, whe_pre, wpl1;
				equ_wpl = whe_pre = wpl1 = 0;
				for(int j=0;j<N;j++)
					wpl1 += strlen(Code[j]) * F[j];
				if(Wpl == wpl1) equ_wpl = 1;  /*eua_wpl stores whether the answer has the best wpl*/
				for(int j=0;j<N;j++){
					for(int k=j+1; k<N;k++){  /*Check for each pair*/
						if(Judge_pre(Code[j], Code[k]) == 1){
							whe_pre = 1;  /*whe_pre stores whether a code is the prefix of another one in the answer*/
							break;
						}
					}
					if(whe_pre) break;
				}
//				if(equ_wpl == 1 && whe_pre == 0) printf("Yes\n");
//				else printf("No\n");
			}
	}
	
	stop = clock();
		    duration = ((double)(stop - start)) / CLK_TCK;
			printf("%.3lfs\n",duration);
	return 0;
} 

/*Calculate the lowest wpl of these N distinct characters*/
int WPL(int F[])
{
	int *Heap = (int *)malloc(sizeof(int)*(N+1));  /*Allocate a heap*/
	Heap[0] = 0;  /*Heap[0] store the size*/
	for(int i=1;i<=N;i++){
		Heap[i] = F[i-1];
		Heap[0]++;
	}
	for(int i=Heap[0]/2;i>=1;i--){
		Percolate_down(Heap, i);  /*Build the min heap*/
	}
	int wpl = 0;
	int wpl1[Nmax];
	memset(wpl1,0,sizeof(wpl1));
	int count = 0;
	while(Heap[0]>1){
		int n1 = Delete_min(Heap);
		int n2 = Delete_min(Heap);  /*Delete two min items from the min heap*/
		wpl1[count] = n1 + n2;  /*wpl1 is the new item*/
		/*Instead of building the Huffman tree, we add frequency of each character depth by depth.
		  wpl1[N] store the number of non-leaf node in the Huffman tree. Finally, we add all the
		  items in wpl1[N] up, which is the total wpl.*/
		Insert(Heap, wpl1[count]);  /*Insert the new item back to the min heap*/
	    count++;
	}
	for(int i=0;i<count;i++)
	    wpl += wpl1[i];
	free(Heap);
	return wpl;	
}

/*Percolate an item down into a min heap*/
void Percolate_down(int *Heap, int index)
{
	int i, child;
	int element = Heap[index];
	for(i=index; i*2<=Heap[0]; i=child){
		child = i*2;
		if(child < Heap[0] && Heap[child+1] < Heap[child])
		    child++;
		if(element > Heap[child])
		    Heap[i] = Heap[child];
		else break;
	}
	Heap[i] = element;
}

/*Delete the min item in a min heap*/
int Delete_min(int *Heap)
{
	int min = Heap[1];
	int Last_index = Heap[0];
	Heap[1] = Heap[Last_index];
	Heap[0]--;
	Percolate_down(Heap, 1);
	return min;
}

/*Insert an item into a min heap*/
void Insert(int *Heap, int element)
{
	int i;
	for(i=++Heap[0]; Heap[i/2] > element; i/=2){
		Heap[i] = Heap[i/2];  /*Percolate up*/
	}
	Heap[i] = element;
}

/*Judge if a string is the prefix of another one*/
int Judge_pre(char *code1, char *code2)
{
	while(*code1 && *code2){
		if(*code1 == *code2){
			code1++;
			code2++;
		}
		else return 0;  /*If there exist different characters, then code1 and code2
		                  aren't in the relation of prefix*/
	}
	/*Not returning in the loop means there exists prefix relation.*/
	return 1;
}
