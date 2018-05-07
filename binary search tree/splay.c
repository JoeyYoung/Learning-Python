#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
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

/* The struct of Node in a SplayTree */
typedef struct tnode{
	int value;
	struct tnode* left;
	struct tnode* right;
}*SplayTree;


/* Readin the data in the file, store into nodes[] */
void ReadInput();

/* Generare the random array for delete nodes */
void RandDelArray();

/* Prepare data order to be delete */
void PreDelNode();

/* Single right rotation */
SplayTree Zig(SplayTree root);

/* Single left rotation */
SplayTree Zag(SplayTree root);

/* Find the max element of the root's left subtree */
SplayTree FindMax(SplayTree root);

/* Top->down splay */
SplayTree Splay(SplayTree root, int node);

/* Insert new node value into the tree
 * use top-down splay to rotate new node to root 
 */
SplayTree Insert(SplayTree root, int node);

/* Delete one node from the tree 
 * first find the node and rotate to the root
 * remove the node(free), rebuild the tree
 */
SplayTree Delete(SplayTree root, int node);


int main(int argc, char const *argv[]){
	/* init the pars needed */
	SplayTree root = NULL;
	int i, node;
	clock_t start, end;

	/* redirect the input file */ 
	strcpy(FILENAME, argv[1]);

	/* readin the nodes to be inserted */
	ReadInput();

	/* generate nodes to be deleted */
	PreDelNode();


	/* do the insert operation */
	start = clock();
	for(i = 0; i < n_nodes; i++){
		node = nodes[i];
		/* root updated with every insert operation */
		root = Insert(root, node);
		//printf("Insert node %d\n", root->value);
	}
	end = clock();
	double duration = (double)(end - start) / CLK_TCK;
    printf("Success: Insert time in SplayTree: %.3lfs\n", duration);


    /* do the delete operation */
	start = clock();
	for(i = 0; i < n_nodes; i++){
		node = del_rand[i];
		/* root updated with every delete operation */
		root = Delete(root, node);
		//printf("Delete node %d\n", node);
	}
	end = clock();
	duration = (double)(end - start) / CLK_TCK;
    printf("Success: Delete time in SplayTree: %.3lfs\n", duration);

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

/* Single right rotation */
SplayTree Zig(SplayTree root)
{
	SplayTree tmp = root->left;
	root->left = tmp->right;
	tmp->right = root;
	return tmp;
}

/* Single left rotation */
SplayTree Zag(SplayTree root)
{
	SplayTree tmp = root->right;
	root->right = tmp->left;
	tmp->left = root;
	return tmp;
}

/* Find the max element of the root's left subtree */
SplayTree FindMax(SplayTree root)
{
	SplayTree tmp = root;
	if(tmp == NULL)
		return NULL;
	else{
		/* Continue finding the right child */
		while(tmp->right != NULL)
			tmp = tmp->right;

		/* return the position of the Max node */ 
		return tmp;
	}
}

/* Top->down splay */
SplayTree Splay(SplayTree root, int node)
{
	/* init left, right subtree 
	 * tmpLeft used to store any node that smaller than new node
	 * tmpRight used to store any node that larger than new node
	 * because of the top->down process, the tmpLeft should splay in the right(max)
	 * 									 the tmpRight should splay in the left(min)
	 */

	SplayTree tmpLeft, tmpRight, tmp;
	SplayTree nullNode = (SplayTree)malloc(sizeof(struct tnode));
	nullNode->left = nullNode->right = NULL;
	tmpLeft = tmpRight = nullNode;

	if(root == NULL)
		return root;
	while(1){
		
		if(node < root->value){
			if(root->left == NULL)
				break;
			/* do the Zig rotation
			 * break when the certain subtree of root is empty
			 * which means the new node find it's right position
			 */
			if(node < root->left->value){
				root = Zig(root);
				if(root->left == NULL)
					break;
			}
			/* store the root into the min of temp right tree */
			tmpRight->left = root;
			tmpRight = root;
			/* downcast the root */
			root = root->left;
		}

		else if(node > root->value){
			if(root->right == NULL)
				break;
			/* do the Zag rotation
			 * break when the certain subtree of root is empty
			 * which means the new node find it's right position
			 */
			if(node > root->right->value){
				root = Zag(root);
				if(root->right == NULL)
					break;
			}
			/* store the root into the max of temp left tree */
			tmpLeft->right = root;
			tmpLeft = root;
			/* downcast the root */
			root = root->right;
		}
		/* node in the tree, needed in the delete operation */
		else
			break;
	} // while

	/* break from the while loop, node finds the position(empty now) */
	/* root->left = NULL || root->right = NULL */
	if(root->left == NULL){
		tmpLeft->right = NULL;
		tmpRight->left = root->right;
	}else{
		tmpRight->left = NULL;
		tmpLeft->right = root->left;
	}
	/* link the root */ 
	root->left = nullNode->right;
	root->right = nullNode->left;
	return root;
}

/* Insert new node value into the tree
 * use top-down splay to rotate new node to root 
 */
SplayTree Insert(SplayTree root, int node)
{
	SplayTree new = (SplayTree)malloc(sizeof(struct tnode));
	new->value = node;
	new->right = new->left = NULL;

	if(root == NULL)
		return new;

	root = Splay(root, node);
	/* put node on the top */
	/* root be the right subtree */
	if(node < root->value){
		new->left = root->left;
		new->right = root;
		root->left = NULL;
		return new;
	}
	/* root be the left subtree */
	else{
		new->right = root->right;
		new->left = root;
		root->right = NULL;
		return new;
	}
}

/* Delete one node from the tree 
 * first find the node and rotate to the root
 * remove the node(free), rebuild the tree
 */
SplayTree Delete(SplayTree root, int node)
{	
	if(root == NULL)
		return NULL;

	/* return the new root */
	SplayTree nroot;
	root = Splay(root, node);
	/* find the delet root */
	if(root->left == NULL && root->right == NULL)
		return NULL;
	if(root->left == NULL)
		nroot = root->right;
	/* Find the max element in left subtree as the new root */
	else{
		/* find the position of left_Max*/
		SplayTree lMax = FindMax(root->left);
		/* pop up as the new root */
		nroot = Splay(root->left, lMax->value);
		nroot->right = root->right;	
	}
	/* remove the old root */
	free(root);

	return nroot;
}
