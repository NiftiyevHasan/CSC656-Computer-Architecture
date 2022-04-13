#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

void find1eltCPU(int *m, int *cs, int n);

int main(int argc, char **argv)
{
  int t=0, i, j;
  int n = atoi(argv[1]);  // number of matrix rows/cols
  int *hm, // host matrix
    *hcs; // host colsums
  int msize = n * n * sizeof(int);  // size of matrix in bytes
  // allocate space for host matrix
  hm = (int *) malloc(msize);  
  // as a test, fill matrix with consecutive integers

  for (i = 0; i < n; i++) {
    for (j = 0; j < n; j++) {
      hm[i*n+j] = t++;
    }
  }
  
  // allocate host colsum array
  int cssize = n * sizeof(int);
  hcs = (int *) malloc(cssize);  

  // set up timer
  struct timeval tv;
  gettimeofday(&tv, NULL);
  double t0 = tv.tv_sec*1e6 + tv.tv_usec;
  
  find1eltCPU(hm,hcs,n);

  gettimeofday(&tv, NULL);
  double t1 = tv.tv_sec*1e6 + tv.tv_usec;
  printf("Elapsed CPU time = %f ms\n", (t1-t0)*1e-3);
  
  // check results
  if (n < 10) {
    for(i=0; i<n; i++)
      printf("%d\n",hcs[i]);
  }
  else {
    for(i=0; i<10; i++)
      printf("%d\n",hcs[i]);
  }

  // clean up
  free(hm);
  free(hcs);
}

void find1eltCPU(int *m, int *cs, int n)
{
  int i, k;
  int max = 0;

  for (i=0; i < n; i++) {
    max = 0;
    for (k = 0; k < n; k++) {
      if(max < m[i + n*k])
      max = m[i + n*k];
    }
    cs[i] = max;
  }

}