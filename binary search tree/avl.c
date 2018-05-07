#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <string.h>

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

/* Insert direction */
#define LEFT 1
#define RIGHT 2

/* The struct of Node in a AvlTree */
typedef struct tnode
{
	int value;
	struct tnode* right;
	struct tnode* left;
}*AvlTree;

/* Open the input file, data to be insert should in random or increasing order 
 * at the same time, date to be deleted should be generated.
 */
void ReadInput();

/* Generare the random array for delete nodes */
void RandDelArray();

/* Prepare data order to be delete */
void PreDelNode();

/* Right-Right rotation operation to keep Balance Factor */
AvlTree RR(AvlTree root);

/* Left-Left rotation operation to keep Balance Factor */
AvlTree LL(AvlTree root);

/* Right-Left rotation operation to keep Balance Factor */
AvlTree RL(AvlTree root);

/* Left-Right rotation operation to keep Balance Factor */
AvlTree LR(AvlTree root);

/* Get the height of one node recursivly */ 
int GetHeight(AvlTree root);

/* Get the balance factor of a node
 * BF = Height(left) - Height(right)
 * When |BF| >= 2, the node is unbalance. 
 */
int GetBalFac(AvlTree root);

/* Keep a node to be balance when doing the insert operation */
AvlTree KeepBalFac(AvlTree root, int node, int direct);

/* Fix a node to be balance when doing the delete operation */
AvlTree FixBalFac(AvlTree root);

/* Perform the insert operation of a certain value of node */
AvlTree Insert(AvlTree root, int node);

/* Find the minimun node from right subtree */
AvlTree FindMin(AvlTree root);

/* Perform the delete operation of a certain value of node */
AvlTree Delete(AvlTree root, int node);


int main(int argc, char const *argv[]){
	/* init the pars needed */
	AvlTree root = NULL;
	int i, node;
	clock_t start, end;

	/* redirect the input file */ 
	strcpy(FILENAME, argv[1]);

	/* readin the nodes to be inserted */
	ReadInput();

	/* prepare data order to be delete */
	PreDelNode();


	/* do the insert operation */
	start = clock();
	for(i = 0; i < n_nodes; i++){
		node = nodes[i];
		root = Insert(root, node);
		//printf("insert node %d\n", root->value);
	}
	end = clock();
	double duration = (double)(end - start) / CLK_TCK;
    printf("Success: Insert time in AVLTree: %.3lfs\n", duration);


    /* do the delete operation */
    start = clock();
    
    /* choose one order to delete */
    for(i = 0; i < n_nodes; i++){
    	node = del_rand[i];
    	root = Delete(root, node);
    }
    end = clock();
    duration = (double)(end - start) / CLK_TCK;
    printf("Success: Delete time in AVLTree: %.3lfs\n", duration);

	return 0;
}


/* Open the input file, data to be insert should in random or increasing order 
 * at the same time, date to be deleted should be generated.
 */
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

/* Right-Right rotation operation to keep Balance Factor */
AvlTree RR(AvlTree root)
{
	/* temp save the root->right node */
	AvlTree newr = root->right;
	root->right = newr->left;
	newr->left = root;

	/* return the new root */ 
	return newr;
}

/* Left-Left rotation operation to keep Balance Factor */
AvlTree LL(AvlTree root)
{
	/* temp save the root->left node */
	AvlTree newr = root->left;
	root->left = newr->right;
	newr->right = root;

	/* return the new root */
	return newr;
}

/* Right-Left rotation operation to keep Balance Factor */
AvlTree RL(AvlTree root)
{
	/* double left rotate the right tree
 	 * then double right rotate the root 
	 */ 
	root->right = LL(root->right);
	return RR(root);
}

/* Left-Right rotation operation to keep Balance Factor */
AvlTree LR(AvlTree root)
{
	/* double right rotate the left tree
 	 * then double left rotate the root 
	 */ 
	root->left = RR(root->left);
	return LL(root);
}

/* Get the height of one node recursivly */ 
int GetHeight(AvlTree root)
{
	/* NULL root defines as height 0 */
	if(root == NULL)
		return 0;
	else{
		int max;
		int h_left = GetHeight(root->left);
		int h_right = GetHeight(root->right);
		
		/* compare the height of letf & right tree
		 * choose the max one 
		 */
		if(h_left > h_right)
			max = h_left;
		else 
			max = h_right;
		return (max+1);
	}
}

/* Get the balance factor of a node
 * BF = Height(left) - Height(right)
 * When |BF| >= 2, the node is unbalance. 
 */
int GetBalFac(AvlTree root)
{
	int h_left = GetHeight(root->left);
	int h_right = GetHeight(root->right);
	int bf = h_left - h_right;
	return bf;
}

/* Keep a node to be balance when doing the insert operation */
AvlTree KeepBalFac(AvlTree root, int node, int direct)
{	
	int bf = GetBalFac(root);

	/* |BF| >= 2, the node need to rotate */
	if(bf <= -2 || bf >= 2){
		if(direct == LEFT){
			if(node < root->left->value)
				root = LL(root);
			else
				root = LR(root);
		}else if(direct == RIGHT){
			if(node > root->right->value)
				root = RR(root);
			else 
				root = RL(root);
		}		
	}

	/* root is updated after the rotation */
	return root;
}

/* Fix a node to be balance when doing the delete operation */
AvlTree FixBalFac(AvlTree root)
{
	/* use height to determine the direction of rotate */
	if(GetHeight(root->left) > GetHeight(root->right)){
		if(GetHeight(root->left->left) > GetHeight(root->left->right))
			root = LL(root);
		else
			root = LR(root);
	}else{
		if(GetHeight(root->right->right) > GetHeight(root->right->left))
			root = RR(root);
		else
			root = RL(root); 
	}

	/* root is updated after the rotation */
	return root;
}

/* Perform the insert operation of a certain value of node */
AvlTree Insert(AvlTree root, int node)
{
	/* RIGHT || LEFT */
	int direct; 

	/* new a memory space for the new rode 
 	 * perform as the exitus of recursive
	 */
	if(root == NULL){
		root = (AvlTree)malloc(sizeof(struct tnode));
		root->right = root->left = NULL;
		root->value = node;
	}else{

		/* insert into the right tree */
		if(node > root->value){

			/* Perform recursive DOWN */
			root->right = Insert(root->right, node);
			direct = RIGHT;
		}

		/* insert into the left tree */
		else if(node < root->value){

			/* Perform recursive DOWN */
			root->left = Insert(root->left, node);
			direct = LEFT;
		}

		/* exam the node, and keep it height balance */
		root = KeepBalFac(root, node, direct);
	}

	return root;
}

/* Find the minimun node from right subtree */
AvlTree FindMin(AvlTree root)
{
	AvlTree tmp = root;
	if(tmp == NULL)
		return NULL;
	else{
		/* Continue finding the left child */
		while(tmp->left != NULL)
			tmp = tmp->left;

		/* return the position of the Min node */ 
		return tmp;
	}
}

/* Perform the delete operation of a certain value of node */
AvlTree Delete(AvlTree root, int node)
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
			AvlTree rmin = FindMin(root->right);
			/* put the value to the root */
			root->value = rmin->value;
			
			/* Delete the min node (now with the value of root) */
			root->right = Delete(root->right ,rmin->value);
		}

		/* only one child is empty */
		else{
			AvlTree del = root;

			/* link the right or left subtree */
			if(root->left == NULL)
				root = root->right;
			else if(root->right == NULL)
				root = root->left;
			//printf("delete node %d\n", del->value);
			free(del);
		}
	}

	/* test BF and rebuild the tree */
	if(root != NULL){
		int bf = GetBalFac(root);
		if(bf >= 2 || bf <= -2)
			root = FixBalFac(root);
	}

	return root;
}
