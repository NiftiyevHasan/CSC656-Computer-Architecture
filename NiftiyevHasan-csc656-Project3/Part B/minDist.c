/****

     File: minDist.c
     Date: 4/28/2017
     By: Bill Hsu
     Compile: gcc minDist.c -O3 -o minDist -lm
     Run: ./minDist

****/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <sys/time.h>

#define NUMPARTICLES 8192
void initPos(float *);
float findDistance(float *, int, int);
void findMinsCPU(float *p, int *mI, float *mD);
void dumpResults(int index[], float d[]);

int main() {
  int i;
  float *pos;
  int *minIndex;
  float *minDistance;

  /* set up timer */
  struct timeval tv;
  gettimeofday(&tv, NULL);
  double t0 = tv.tv_sec*1e6 + tv.tv_usec;

  pos = (float *) malloc(NUMPARTICLES * 3 * sizeof(float));
  minIndex = (int *) malloc(NUMPARTICLES * sizeof(int));
  minDistance = (float *) malloc(NUMPARTICLES * sizeof(float));

  initPos(pos);

  findMinsCPU(pos, minIndex, minDistance);

  gettimeofday(&tv, NULL);
  double t1 = tv.tv_sec*1e6 + tv.tv_usec;
  printf("%d particles\n", NUMPARTICLES);
  printf("Elapsed CPU time = %f ms\n", (t1-t0)*1e-3);

  dumpResults(minIndex, minDistance);
}

void initPos(float *p) {
  int i;
  for (i=0; i<NUMPARTICLES; i++) {
    p[i*3] = rand() / (float) RAND_MAX;
    p[i*3+1] = rand() / (float) RAND_MAX;
    p[i*3+2] = rand() / (float) RAND_MAX;
  }

}

float findDistance(float *p, int i, int j) {
  float dx, dy, dz;

  dx = p[i*3] - p[j*3];
  dy = p[i*3+1] - p[j*3+1];
  dz = p[i*3+2] - p[j*3+2];

  return(dx*dx + dy*dy + dz*dz);
}

void findMinsCPU(float *p, int *minI, float *minD) {
  int i, j;
  float distance;
  int mI;
  float mD;

  for (i=0; i<NUMPARTICLES; i++) {
    if (i!=0) {
      mI = 0;
      mD = findDistance(p, i, 0);
    }
    else {
      mI = 1;
      mD = findDistance(p, 0, 1);
    }
    for (j=0; j<NUMPARTICLES; j++) {
      if (i!=j) {
	/* calculate distance between particles i, j */
       distance = findDistance(p, i, j);
	/* if distance < min distance for i, save */
       if (distance < mD) {
         mD = distance;
         mI = j;
       }
     }
   }
   minI[i] = mI;
   minD[i] = mD;
 }

}


void dumpResults(int index[], float d[]) {
  int i;
  FILE *fp;

  fp = fopen("./dump_minDist.out", "w");
  
  for (i=0; i<NUMPARTICLES; i++) {
    fprintf(fp, "%d %d %f\n", i, index[i], d[i]);
  }

  fclose(fp);
}