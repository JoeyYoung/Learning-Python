#include <stdio.h>
#include <math.h>
#include <time.h>
#define MAXNUM 30000

int MyAbs(int x) {
    return (x < 0) ? -x : x;
}

void print(int a[], int n)
{
    for (int i = 0; i < n; i++)
        printf("%d ", a[i]);
    printf("\n"); 
}

// Longest inceasing subsequence
void LongestIncSub_1(int a[], int len[], int n)
{
    int ThisMaxLen;

    // Dynamic Programming
    for (int i = 0; i < n; i++)
        len[i] = 1;

    for (int i = 1; i < n; i++) {  
        ThisMaxLen = 0;

        for (int j = 0; j < i; j++) 
            if (a[j] < a[i] && len[j] > ThisMaxLen)  //find the maxlen increasing subsequence from 0 to i-1
                ThisMaxLen = len[j];

        len[i] = ThisMaxLen + 1;
    }  
}

int BinarySearch(int target, int a[], int start, int end)
{
    // if the target is not in a[start] to a[end]-1, its position is trivial
    if (a[end] < target)
        return -1;
    if (a[start] >= target)
        return start;
    
    int mid = (start + end) / 2;
    
    while (start < end-1) {
        if (a[mid] < target && target <= a[mid+1]) // if an number has the same value as the target, then we find its position
            return mid+1;
        else if (target > a[mid+1])
            start = mid+1;
        else if (target <= a[mid])
            end = mid;
        mid = (start + end) / 2;       
    }

    return mid+1;
}

void LongestIncSub_2(int a[], int len[], int n)
{
    // the stack stores the smallest tails of the LongestIncSubs
    // of the corresponding length
    int stack[MAXNUM + 1]; 
    int top = -1;

    stack[++top] = a[0];
    len[0] = 1;

    for (int i = 1; i < n; i++) {
        if (a[i] > stack[top]) {
            stack[++top] = a[i];
            len[i] = top + 1;
        }
        else {
            int pos = BinarySearch(a[i], stack, 0, top);
            stack[pos] = a[i];
            len[i] = pos + 1;
        }
    }
}

int getLength(int a[], int k)
{
    int MaxLen = 1;
    int ThisMaxLen;

    for (int i = 0; i < k; i++) {
        if (a[i] < a[k]) {
            ThisMaxLen = getLength(a, i) + 1;
            if (ThisMaxLen > MaxLen)
                MaxLen = ThisMaxLen;
        }
    }

    return MaxLen;
}

void LongestIncSub_3(int a[], int len[], int n)
{
    for (int i = 0; i < n; i++)
        len[i] = getLength(a, i);
}


int main()
{
	freopen("test-for time complexity.txt", "r", stdin);
	long start,end;
	start = clock();
    int n;
    int a[MAXNUM], reverse_a[MAXNUM], len1[MAXNUM], len2[MAXNUM], len[MAXNUM]; //a[] stores the input numbers
    // len1[] stores the max increasing subsequence end at the specified index.
    // len2[] stores the max decreasing subsequence start at the specified index.

    // input
    scanf("%d", &n);
    for (int i = 0; i < n; i++)
        scanf("%d", &a[i]);
    for (int i = 0; i < n; i++) {
        reverse_a[i] = a[n - 1 - i];
    }

    // process to get the value of len1 and len2
    LongestIncSub_2(a, len1, n);
    LongestIncSub_2(reverse_a, len2, n);
    
    // reverse len2
    int tmp, i = 0, j = n-1;
    while (i < j) {
        tmp = len2[i]; // swap len2[i] and len2[j]
        len2[i] = len2[j];
        len2[j] = tmp;
        ++i;
        --j;
    }

    // calculate len[] of "peak"
    for (int i = 0; i < n; i++) {
        if (len1[i] == 1 || len2[i] == 1)
            len[i] = 0;
        else
            len[i] = len1[i] + len2[i] - 1;
    }

    int max = 0;
    int index = 0;
    for (int i = 0; i < n; i++) {
        if (len[i] >= max) {
            if (len[i] == max) {
                if (MyAbs(len1[i] - len2[i]) < MyAbs(len1[index] - len2[index])) {
                    index = i;
                }
            } else {
                max = len[i];
                index = i;
            }
            
        }
    }

    // output
    printf("Algorithm 3:\n");
    if (max == 0) // a peak has both left and right parts
        printf("No peak shape\n"); 
    else 
        printf("%d %d %d\n", max, index + 1, a[index]);
    
    end = clock();
    printf("%ld\n",end-start);

    return 0;
}
