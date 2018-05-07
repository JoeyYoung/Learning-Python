#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

/* TEST Loop */
static int looptimes = 10;

/* Filename of input data */
static char FILENAME[50];

/* Number of total nodes */
static int n_nodes = 0;

/* Insert Nodes Array */
static int nodes[10000];

/* Delete Nodes Arrays of different orders */
static int del_incrs[10000];
static int del_rand[10000];
static int del_revr[10000];

/* The struct of Node in a Binary Search Tree */
typedef struct tnode{
	int value;
	struct tnode* left;
	struct tnode* right;
}*BTree;

/* Readin the data in the file, store into nodes[] */
void ReadInput();

/* Generare the random array for delete nodes */
void RandDelArray();

/* Prepare data order to be delete */
void PreDelNode();

/* Find the mmin element of the root's right subtree */
BTree FindMin(BTree root);

/* Perform the insert operation of a certain value of node */
BTree Insert(BTree root, int node);

/* Perform the delete operation of a certain value of node */
BTree Delete(BTree root, int node);


int main(int argc, char const *argv[])
{
	/* init the pars needed */
	int i, node, k;
	clock_t start, end;
	double duration;
	double total_insert = 0, total_delete = 0;

	/* redirect the input file */ 
	strcpy(FILENAME, argv[1]);

	/* readin the nodes to be inserted */
	ReadInput();

	/* generate nodes to be deleted */
	PreDelNode();

	for(k = 1; k <= looptimes; k++){
		/* do the insert operation */
		start = clock();
		for(i = 0; i < n_nodes; i++){
			node = nodes[i];
			root = Insert(root, node);
			// printf("insert node %d\n", node);
		}
		end = clock();
		duration = (double)(end - start) / CLK_TCK;
	    total_insert += duration;

	    /* do the delete operation */
	    start = clock();
	    for(i = 0; i < n_nodes; i++){
	    	node = del_incrs[i];
	    	root = Delete(root, node);
	    }
	    end = clock();
	    duration = (double)(end - start) / CLK_TCK;
	    total_delete += duration;
	}

	printf("Success: Total Insert time in Unbalance BST: %.3lfs\n", total_insert);
	printf("Success: Total Delete time in Unbalance BST: %.3lfs\n", total_delete);

    return 0;
}


/* Readin the data in the file, store into nodes[] */
void ReadInput()
{
	FILE *fp;
	fp = fopen(FILENAME, "r");
	if(!fp)
		printf("can't open file\n");
	else{
		while(!feof(fp)){
			fscanf(fp, "%d", &nodes[n_nodes]);
			n_nodes++;
		}
	}
	fclose(fp);
}

/* Generare the random array for delete nodes */
void RandDelArray()
{
	int i, j;
	int T = 10000;
	int tmp;
	srand((unsigned)(time(NULL)));
	while(T--){
		i = rand()%n_nodes;
		j = rand()%n_nodes;
		tmp = del_rand[i];
		del_rand[i] = del_rand[j];
		del_rand[j] = tmp;
	}
}

/* Prepare data order to be delete */
void PreDelNode()
{
	int i;
	for(i = 0; i < n_nodes; i++){
		/* increasing order same as insert */
		del_incrs[i] = nodes[i];
		/* reverse order nodes to be deleted */
		del_revr[i] = nodes[n_nodes - i - 1];
		/* init random array */
		del_rand[i] = nodes[i];
	}
	/* generate the random order array */
	RandDelArray();
}

/* Find the mmin element of the root's right subtree */
BTree FindMin(BTree root)
{
	BTree tmp = root;
	if(tmp == NULL)
		return NULL;
	else{
		/* continue finding the left child */
		while(tmp->left != NULL)
			tmp = tmp->left;

		/* return the position of the Min node */ 
		return tmp;
	}
}

/* Perform the insert operation of a certain value of node */
BTree Insert(BTree root, int node)
{
	/* new a memory space for the new rode 
 	 * perform as the exitus of recursive
	 */
	if(root == NULL){
		root = (BTree)malloc(sizeof(struct tnode));
		root->right = root->left = NULL;
		root->value = node;
	}else{
		/* insert into the right tree */
		if(node > root->value){
			root->right = Insert(root->right, node);
		}

		/* insert into the left tree */
		else if(node < root->value){
			root->left = Insert(root->left, node);
		}
	}
	return root;
}

/* Perform the delete operation of a certain value of node */
BTree Delete(BTree root, int node)
{
	/* The destination of the recursive process */
	if(root == NULL){
		printf("node %d not found!\n", node);
		return NULL;
	}

	/* find from the left */
	else if(node < root->value)
		root->left = Delete(root->left, node);

	/* find from the right */
	else if(node > root->value)
		root->right = Delete(root->right, node);

	/* find the node to be deleted */
	else if(node == root->value){	

		/* left and right subtree are both not empty */
		if(root->right && root->left){
			BTree rmin = FindMin(root->right);
			/* put the value to the root */
			root->value = rmin->value;
			/* delete the right min node */
			root->right = Delete(root->right ,rmin->value);
		}

		/* only one child is empty */
		else{
			BTree del = root;

			/* link hte right or left subtree */
			if(root->left == NULL)
				root = root->right;
			else if(root->right == NULL)
				root = root->left;
			//printf("delete node %d\n", del->value);
			free(del);
		}
	}
	return root;
}
