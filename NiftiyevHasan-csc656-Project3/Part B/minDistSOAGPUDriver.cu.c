/****

     File: minDistSOAGPUDriver.cu
     Date: 7/24/2018
     By: Bill Hsu
     Revised by: Hasan Niftiyev
     Compile: nvcc minDistSOAGPUDriver.cu -o minDistSOAGPUDriver
     Run: ./minDistSOAGPUDriver

****/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <cuda.h>

// You may edit NUMPARTICLES and THREADSPERBLOCK for measurements
#define NUMPARTICLES 8192
#define THREADSPERBLOCK 32

void initPos(float *);
__host__ __device__ float findDistance(float *, int, int);
__global__ void findMinsGPU(float *p, int *minI, float *minD);
void findMinsG(float *pos, int *minIndex, float *minDistance);
void dumpResults(int index[], float d[]);
float findDistance(float *, int, int);

// You are not allowed to change main()!
int main() {
  cudaEvent_t start, stop;
  float time;
  
  float *pos;
  int *minIndex;
  float *minDistance;

  pos = (float *) malloc(NUMPARTICLES * 3 * sizeof(float));
  minIndex = (int *) malloc(NUMPARTICLES * sizeof(int));
  minDistance = (float *) malloc(NUMPARTICLES * sizeof(float));

  initPos(pos);

  // create timer events
  cudaEventCreate(&start);
  cudaEventCreate(&stop);

  cudaEventRecord(start, 0);

  findMinsG(pos, minIndex, minDistance);

  cudaEventRecord(stop, 0);
  cudaEventSynchronize(stop);
  cudaEventElapsedTime(&time, start, stop);

  printf("%d particles, %d threads per block\n", NUMPARTICLES, THREADSPERBLOCK);
  printf("Elapsed time = %f\n", time);

  dumpResults(minIndex, minDistance);

}

void initPos(float *p) {
  // this should be identical to initPos() for minDistSOA.c
  int i;
  for (i=0; i<NUMPARTICLES; i++) {
    p[i] = rand() / (float) RAND_MAX;
    p[NUMPARTICLES+i] = rand() / (float) RAND_MAX;
    p[(NUMPARTICLES*2)+i] = rand() / (float) RAND_MAX;
  }
}

void findMinsG(float *pos, int *minIndex, float *minDistance) {
  // wrapper function for CUDA code
  float *dpos;
  int *dminIndex;
  float *dminDistance;

  float psize = 3 * NUMPARTICLES * sizeof(float);
  int minIndexSize = NUMPARTICLES * sizeof(int);
  float minDistanceSize = NUMPARTICLES * sizeof(float);

//   allocate data arrays on the GPU with cudaMalloc()

  cudaMalloc((void ** )&dpos, psize);
  cudaMalloc((void ** )&dminIndex, minIndexSize);
  cudaMalloc((void ** )&dminDistance, minDistanceSize);

// copy the host array pos[] to the corresponding device array

  cudaMemcpy(dpos, pos, psize, cudaMemcpyHostToDevice);
  cudaMemcpy(dminIndex, minIndex, minIndexSize, cudaMemcpyHostToDevice);
  cudaMemcpy(dminDistance, minDistance, minDistanceSize, cudaMemcpyHostToDevice);
// call a kernel function (named, for example) findMinsGPU() to compute minimum distances

  findMinsGPU<<<NUMPARTICLES/THREADSPERBLOCK,THREADSPERBLOCK>>>(dpos,dminIndex,dminDistance);
  cudaThreadSynchronize();


// copy the results (indices and distances) to host arrays
  cudaMemcpy(pos, dpos, psize, cudaMemcpyDeviceToHost);
  cudaMemcpy(minIndex, dminIndex, minIndexSize, cudaMemcpyDeviceToHost);
  cudaMemcpy(minDistance, dminDistance, minDistanceSize, cudaMemcpyDeviceToHost);

// clean up
  cudaFree(dpos);
  cudaFree(dminIndex);
  cudaFree(dminDistance);
  
}

__host__ __device__ float findDistance(float *p, int i, int j) {
  float dx, dy, dz;

  dx = p[i] - p[j];
  dy = p[NUMPARTICLES+i] - p[NUMPARTICLES+j];
  dz = p[(NUMPARTICLES*2)+i] - p[(NUMPARTICLES*2)+j];

  return(dx*dx + dy*dy + dz*dz);
}

__global__ void findMinsGPU(float *p, int *minI, float *minD) {
  // your kernel code goes here

  /* blockDim.x = threads per block (T)
     blockIdx.x = block ID (0 to #blocks-1)
     threadIdx.x = thread ID (0 to T-1) */
  int i = blockDim.x * blockIdx.x + threadIdx.x;
  int j;
  float distance;
  int mI;
  float mD;

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

void dumpResults(int index[], float d[]) {
  int i;
  FILE *fp;

  fp = fopen("./dump_minDistSOAGPUDriver.out", "w");
  
  for (i=0; i<NUMPARTICLES; i++) {
    fprintf(fp, "%d %d %f\n", i, index[i], d[i]);
  }

  fclose(fp);
}