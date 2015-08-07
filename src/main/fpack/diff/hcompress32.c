int fits_hcompress(int *a, int ny, int nx, int scale, char *output, 
                  long *nbytes, int *status)
{
  /* 
     compress the input image using the H-compress algorithm
  
   a  - input image array
   nx - size of X axis of image
   ny - size of Y axis of image
   scale - quantization scale factor. Larger values results in more (lossy) compression
           scale = 0 does lossless compression
   output - pre-allocated array to hold the output compressed stream of bytes
   nbyts  - input value = size of the output buffer;
            returned value = size of the compressed byte stream, in bytes

 NOTE: the nx and ny dimensions as defined within this code are reversed from
 the usual FITS notation.  ny is the fastest varying dimension, which is
 usually considered the X axis in the FITS image display

  */

  int stat;
  
  if (*status > 0) return(*status);

  /* H-transform */
  stat = htrans(a, nx, ny);
  if (stat) {
     *status = stat;
     return(*status);
  }

  /* digitize */
  digitize(a, nx, ny, scale);

  /* encode and write to output array */

  FFLOCK;
  noutmax = *nbytes;  /* input value is the allocated size of the array */
  *nbytes = 0;  /* reset */

  stat = encode(output, nbytes, a, nx, ny, scale);
  FFUNLOCK;
  
  *status = stat;
  return(*status);
}
static int htrans(int a[],int nx,int ny)
{
int nmax, log2n, h0, hx, hy, hc, nxtop, nytop, i, j, k;
int oddx, oddy;
int shift, mask, mask2, prnd, prnd2, nrnd2;
int s10, s00;
int *tmp;

	/*
	 * log2n is log2 of max(nx,ny) rounded up to next power of 2
	 */
	nmax = (nx>ny) ? nx : ny;
	log2n = (int) (log((float) nmax)/log(2.0)+0.5);
	if ( nmax > (1<<log2n) ) {
		log2n += 1;
	}
	/*
	 * get temporary storage for shuffling elements
	 */
	tmp = (int *) malloc(((nmax+1)/2)*sizeof(int));
	if(tmp == (int *) NULL) {
	        ffpmsg("htrans: insufficient memory");
		return(DATA_COMPRESSION_ERR);
	}
	/*
	 * set up rounding and shifting masks
	 */
	shift = 0;
	mask  = -2;
	mask2 = mask << 1;
	prnd  = 1;
	prnd2 = prnd << 1;
	nrnd2 = prnd2 - 1;
	/*
	 * do log2n reductions
	 *
	 * We're indexing a as a 2-D array with dimensions (nx,ny).
	 */
	nxtop = nx;
	nytop = ny;

	for (k = 0; k<log2n; k++) {
		oddx = nxtop % 2;
		oddy = nytop % 2;
		for (i = 0; i<nxtop-oddx; i += 2) {
			s00 = i*ny;				/* s00 is index of a[i,j]	*/
			s10 = s00+ny;			/* s10 is index of a[i+1,j]	*/
			for (j = 0; j<nytop-oddy; j += 2) {
				/*
				 * Divide h0,hx,hy,hc by 2 (1 the first time through).
				 */
				h0 = (a[s10+1] + a[s10] + a[s00+1] + a[s00]) >> shift;
				hx = (a[s10+1] + a[s10] - a[s00+1] - a[s00]) >> shift;
				hy = (a[s10+1] - a[s10] + a[s00+1] - a[s00]) >> shift;
				hc = (a[s10+1] - a[s10] - a[s00+1] + a[s00]) >> shift;

				/*
				 * Throw away the 2 bottom bits of h0, bottom bit of hx,hy.
				 * To get rounding to be same for positive and negative
				 * numbers, nrnd2 = prnd2 - 1.
				 */
				a[s10+1] = hc;
				a[s10  ] = ( (hx>=0) ? (hx+prnd)  :  hx        ) & mask ;
				a[s00+1] = ( (hy>=0) ? (hy+prnd)  :  hy        ) & mask ;
				a[s00  ] = ( (h0>=0) ? (h0+prnd2) : (h0+nrnd2) ) & mask2;
				s00 += 2;
				s10 += 2;
			}
			if (oddy) {
				/*
				 * do last element in row if row length is odd
				 * s00+1, s10+1 are off edge
				 */
				h0 = (a[s10] + a[s00]) << (1-shift);
				hx = (a[s10] - a[s00]) << (1-shift);
				a[s10  ] = ( (hx>=0) ? (hx+prnd)  :  hx        ) & mask ;
				a[s00  ] = ( (h0>=0) ? (h0+prnd2) : (h0+nrnd2) ) & mask2;
				s00 += 1;
				s10 += 1;
			}
		}
		if (oddx) {
			/*
			 * do last row if column length is odd
			 * s10, s10+1 are off edge
			 */
			s00 = i*ny;
			for (j = 0; j<nytop-oddy; j += 2) {
				h0 = (a[s00+1] + a[s00]) << (1-shift);
				hy = (a[s00+1] - a[s00]) << (1-shift);
				a[s00+1] = ( (hy>=0) ? (hy+prnd)  :  hy        ) & mask ;
				a[s00  ] = ( (h0>=0) ? (h0+prnd2) : (h0+nrnd2) ) & mask2;
				s00 += 2;
			}
			if (oddy) {
				/*
				 * do corner element if both row and column lengths are odd
				 * s00+1, s10, s10+1 are off edge
				 */
				h0 = a[s00] << (2-shift);
				a[s00  ] = ( (h0>=0) ? (h0+prnd2) : (h0+nrnd2) ) & mask2;
			}
		}
		/*
		 * now shuffle in each dimension to group coefficients by order
		 */
		for (i = 0; i<nxtop; i++) {
			shuffle(&a[ny*i],nytop,1,tmp);
		}
		for (j = 0; j<nytop; j++) {
			shuffle(&a[j],nxtop,ny,tmp);
		}
		/*
		 * image size reduced by 2 (round up if odd)
		 */
		nxtop = (nxtop+1)>>1;
		nytop = (nytop+1)>>1;
		/*
		 * divisor doubles after first reduction
		 */
		shift = 1;
		/*
		 * masks, rounding values double after each iteration
		 */
		mask  = mask2;
		prnd  = prnd2;
		mask2 = mask2 << 1;
		prnd2 = prnd2 << 1;
		nrnd2 = prnd2 - 1;
	}
	free(tmp);
	return(0);
}
static void
shuffle(int a[], int n, int n2, int tmp[])
{

/* 
int a[];	 array to shuffle					
int n;		 number of elements to shuffle	
int n2;		 second dimension					
int tmp[];	 scratch storage					
*/

int i;
int *p1, *p2, *pt;

	/*
	 * copy odd elements to tmp
	 */
	pt = tmp;
	p1 = &a[n2];
	for (i=1; i < n; i += 2) {
		*pt = *p1;
		pt += 1;
		p1 += (n2+n2);
	}
	/*
	 * compress even elements into first half of A
	 */
	p1 = &a[n2];
	p2 = &a[n2+n2];
	for (i=2; i<n; i += 2) {
		*p1 = *p2;
		p1 += n2;
		p2 += (n2+n2);
	}
	/*
	 * put odd elements into 2nd half
	 */
	pt = tmp;
	for (i = 1; i<n; i += 2) {
		*p1 = *pt;
		p1 += n2;
		pt += 1;
	}
}
static void  
digitize(int a[], int nx, int ny, int scale)
{
int d, *p;

	/*
	 * round to multiple of scale
	 */
	if (scale <= 1) return;
	d=(scale+1)/2-1;
	for (p=a; p <= &a[nx*ny-1]; p++) *p = ((*p>0) ? (*p+d) : (*p-d))/scale;
}
static int encode(char *outfile, long *nlength, int a[], int nx, int ny, int scale)
{

/* FILE *outfile;  - change outfile to a char array */  
/*
  long * nlength    returned length (in bytes) of the encoded array)
  int a[];								 input H-transform array (nx,ny)
  int nx,ny;								 size of H-transform array	
  int scale;								 scale factor for digitization
*/
int nel, nx2, ny2, i, j, k, q, vmax[3], nsign, bits_to_go;
unsigned char nbitplanes[3];
unsigned char *signbits;
int stat;

        noutchar = 0;  /* initialize the number of compressed bytes that have been written */
	nel = nx*ny;
	/*
	 * write magic value
	 */
	qwrite(outfile, code_magic, sizeof(code_magic));
	writeint(outfile, nx);			/* size of image */
	writeint(outfile, ny);
	writeint(outfile, scale);		/* scale factor for digitization */
	/*
	 * write first value of A (sum of all pixels -- the only value
	 * which does not compress well)
	 */
	writelonglong(outfile, (LONGLONG) a[0]);

	a[0] = 0;
	/*
	 * allocate array for sign bits and save values, 8 per byte
	 */
	signbits = (unsigned char *) malloc((nel+7)/8);
	if (signbits == (unsigned char *) NULL) {
		ffpmsg("encode: insufficient memory");
		return(DATA_COMPRESSION_ERR);
	}
	nsign = 0;
	bits_to_go = 8;
	signbits[0] = 0;
	for (i=0; i<nel; i++) {
		if (a[i] > 0) {
			/*
			 * positive element, put zero at end of buffer
			 */
			signbits[nsign] <<= 1;
			bits_to_go -= 1;
		} else if (a[i] < 0) {
			/*
			 * negative element, shift in a one
			 */
			signbits[nsign] <<= 1;
			signbits[nsign] |= 1;
			bits_to_go -= 1;
			/*
			 * replace a by absolute value
			 */
			a[i] = -a[i];
		}
		if (bits_to_go == 0) {
			/*
			 * filled up this byte, go to the next one
			 */
			bits_to_go = 8;
			nsign += 1;
			signbits[nsign] = 0;
		}
	}
	if (bits_to_go != 8) {
		/*
		 * some bits in last element
		 * move bits in last byte to bottom and increment nsign
		 */
		signbits[nsign] <<= bits_to_go;
		nsign += 1;
	}
	/*
	 * calculate number of bit planes for 3 quadrants
	 *
	 * quadrant 0=bottom left, 1=bottom right or top left, 2=top right, 
	 */
	for (q=0; q<3; q++) {
		vmax[q] = 0;
	}
	/*
	 * get maximum absolute value in each quadrant
	 */
	nx2 = (nx+1)/2;
	ny2 = (ny+1)/2;
	j=0;	/* column counter	*/
	k=0;	/* row counter		*/
	for (i=0; i<nel; i++) {
		q = (j>=ny2) + (k>=nx2);
		if (vmax[q] < a[i]) vmax[q] = a[i];
		if (++j >= ny) {
			j = 0;
			k += 1;
		}
	}
	/*
	 * now calculate number of bits for each quadrant
	 */

        /* this is a more efficient way to do this, */
 
 
        for (q = 0; q < 3; q++) {
            for (nbitplanes[q] = 0; vmax[q]>0; vmax[q] = vmax[q]>>1, nbitplanes[q]++) ; 
        }


/*
	for (q = 0; q < 3; q++) {
		nbitplanes[q] = (int) (log((float) (vmax[q]+1))/log(2.0)+0.5);
		if ( (vmax[q]+1) > (1<<nbitplanes[q]) ) {
			nbitplanes[q] += 1;
		}
	}
*/

	/*
	 * write nbitplanes
	 */
	if (0 == qwrite(outfile, (char *) nbitplanes, sizeof(nbitplanes))) {
	        *nlength = noutchar;
		ffpmsg("encode: output buffer too small");
		return(DATA_COMPRESSION_ERR);
        }
	 
	/*
	 * write coded array
	 */
	stat = doencode(outfile, a, nx, ny, nbitplanes);
	/*
	 * write sign bits
	 */

	if (nsign > 0) {

	   if ( 0 == qwrite(outfile, (char *) signbits, nsign)) {
	        free(signbits);
	        *nlength = noutchar;
		ffpmsg("encode: output buffer too small");
		return(DATA_COMPRESSION_ERR);
          }
	} 
	
	free(signbits);
	*nlength = noutchar;

        if (noutchar >= noutmax) {
		ffpmsg("encode: output buffer too small");
		return(DATA_COMPRESSION_ERR);
        }  
	
	return(stat); 
}
static int
doencode(char *outfile, int a[], int nx, int ny, unsigned char nbitplanes[3])
{
/* char *outfile;						 output data stream
int a[];							 Array of values to encode			
int nx,ny;							 Array dimensions [nx][ny]			
unsigned char nbitplanes[3];		 Number of bit planes in quadrants	
*/

int nx2, ny2, stat;

	nx2 = (nx+1)/2;
	ny2 = (ny+1)/2;
	/*
	 * Initialize bit output
	 */
	start_outputing_bits();
	/*
	 * write out the bit planes for each quadrant
	 */
	stat = qtree_encode(outfile, &a[0],          ny, nx2,  ny2,  nbitplanes[0]);

        if (!stat)
		stat = qtree_encode(outfile, &a[ny2],        ny, nx2,  ny/2, nbitplanes[1]);

        if (!stat)
		stat = qtree_encode(outfile, &a[ny*nx2],     ny, nx/2, ny2,  nbitplanes[1]);

        if (!stat)
		stat = qtree_encode(outfile, &a[ny*nx2+ny2], ny, nx/2, ny/2, nbitplanes[2]);
	/*
	 * Add zero as an EOF symbol
	 */
	output_nybble(outfile, 0);
	done_outputing_bits(outfile);
	
	return(stat);
}
static int
qtree_encode(char *outfile, int a[], int n, int nqx, int nqy, int nbitplanes)
{

/*
int a[];
int n;								 physical dimension of row in a		
int nqx;							 length of row			
int nqy;							 length of column (<=n)				
int nbitplanes;						 number of bit planes to output	
*/
	
int log2n, i, k, bit, b, bmax, nqmax, nqx2, nqy2, nx, ny;
unsigned char *scratch, *buffer;

	/*
	 * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
	 */
	nqmax = (nqx>nqy) ? nqx : nqy;
	log2n = (int) (log((float) nqmax)/log(2.0)+0.5);
	if (nqmax > (1<<log2n)) {
		log2n += 1;
	}
	/*
	 * initialize buffer point, max buffer size
	 */
	nqx2 = (nqx+1)/2;
	nqy2 = (nqy+1)/2;
	bmax = (nqx2*nqy2+1)/2;
	/*
	 * We're indexing A as a 2-D array with dimensions (nqx,nqy).
	 * Scratch is 2-D with dimensions (nqx/2,nqy/2) rounded up.
	 * Buffer is used to store string of codes for output.
	 */
	scratch = (unsigned char *) malloc(2*bmax);
	buffer = (unsigned char *) malloc(bmax);
	if ((scratch == (unsigned char *) NULL) ||
		(buffer  == (unsigned char *) NULL)) {		
		ffpmsg("qtree_encode: insufficient memory");
		return(DATA_COMPRESSION_ERR);
	}
	/*
	 * now encode each bit plane, starting with the top
	 */
	for (bit=nbitplanes-1; bit >= 0; bit--) {
		/*
		 * initial bit buffer
		 */
		b = 0;
		bitbuffer = 0;
		bits_to_go3 = 0;
		/*
		 * on first pass copy A to scratch array
		 */
		qtree_onebit(a,n,nqx,nqy,scratch,bit);
		nx = (nqx+1)>>1;
		ny = (nqy+1)>>1;
		/*
		 * copy non-zero values to output buffer, which will be written
		 * in reverse order
		 */
		if (bufcopy(scratch,nx*ny,buffer,&b,bmax)) {
			/*
			 * quadtree is expanding data,
			 * change warning code and just fill buffer with bit-map
			 */
			write_bdirect(outfile,a,n,nqx,nqy,scratch,bit);
			goto bitplane_done;
		}
		/*
		 * do log2n reductions
		 */
		for (k = 1; k<log2n; k++) {
			qtree_reduce(scratch,ny,nx,ny,scratch);
			nx = (nx+1)>>1;
			ny = (ny+1)>>1;
			if (bufcopy(scratch,nx*ny,buffer,&b,bmax)) {
				write_bdirect(outfile,a,n,nqx,nqy,scratch,bit);
				goto bitplane_done;
			}
		}
		/*
		 * OK, we've got the code in buffer
		 * Write quadtree warning code, then write buffer in reverse order
		 */
		output_nybble(outfile,0xF);
		if (b==0) {
			if (bits_to_go3>0) {
				/*
				 * put out the last few bits
				 */
				output_nbits(outfile, bitbuffer & ((1<<bits_to_go3)-1),
					bits_to_go3);
			} else {
				/*
				 * have to write a zero nybble if there are no 1's in array
				 */
				output_huffman(outfile,0);
			}
		} else {
			if (bits_to_go3>0) {
				/*
				 * put out the last few bits
				 */
				output_nbits(outfile, bitbuffer & ((1<<bits_to_go3)-1),
					bits_to_go3);
			}
			for (i=b-1; i>=0; i--) {
				output_nbits(outfile,buffer[i],8);
			}
		}
		bitplane_done: ;
	}
	free(buffer);
	free(scratch);
	return(0);
}
static void
qtree_onebit(int a[], int n, int nx, int ny, unsigned char b[], int bit)
{
int i, j, k;
int b0, b1, b2, b3;
int s10, s00;

	/*
	 * use selected bit to get amount to shift
	 */
	b0 = 1<<bit;
	b1 = b0<<1;
	b2 = b0<<2;
	b3 = b0<<3;
	k = 0;							/* k is index of b[i/2,j/2]	*/
	for (i = 0; i<nx-1; i += 2) {
		s00 = n*i;					/* s00 is index of a[i,j]	*/
/* tried using s00+n directly in the statements, but this had no effect on performance */
		s10 = s00+n;				/* s10 is index of a[i+1,j]	*/
		for (j = 0; j<ny-1; j += 2) {

/*
 this was not any faster..
 
         b[k] = (a[s00]  & b0) ? 
	            (a[s00+1] & b0) ?
	                (a[s10] & b0)   ?
		            (a[s10+1] & b0) ? 15 : 14
                         :  (a[s10+1] & b0) ? 13 : 12
		      : (a[s10] & b0)   ?
		            (a[s10+1] & b0) ? 11 : 10
                         :  (a[s10+1] & b0) ?  9 :  8
	          : (a[s00+1] & b0) ?
	                (a[s10] & b0)   ?
		            (a[s10+1] & b0) ? 7 : 6
                         :  (a[s10+1] & b0) ? 5 : 4

		      : (a[s10] & b0)   ?
		            (a[s10+1] & b0) ? 3 : 2
                         :  (a[s10+1] & b0) ? 1 : 0;
*/

/*
this alternative way of calculating b[k] was slowwer than the original code
		    if ( a[s00]     & b0)
			if ( a[s00+1]     & b0)
			    if ( a[s10]     & b0)
				if ( a[s10+1]     & b0)
					b[k] = 15;
				else
					b[k] = 14;
			    else
				if ( a[s10+1]     & b0)
					b[k] = 13;
				else
					b[k] = 12;
			else
			    if ( a[s10]     & b0)
				if ( a[s10+1]     & b0)
					b[k] = 11;
				else
					b[k] = 10;
			    else
				if ( a[s10+1]     & b0)
					b[k] = 9;
				else
					b[k] = 8;
		    else
			if ( a[s00+1]     & b0)
			    if ( a[s10]     & b0)
				if ( a[s10+1]     & b0)
					b[k] = 7;
				else
					b[k] = 6;
			    else
				if ( a[s10+1]     & b0)
					b[k] = 5;
				else
					b[k] = 4;
			else
			    if ( a[s10]     & b0)
				if ( a[s10+1]     & b0)
					b[k] = 3;
				else
					b[k] = 2;
			    else
				if ( a[s10+1]     & b0)
					b[k] = 1;
				else
					b[k] = 0;
*/
			


			b[k] = ( ( a[s10+1]     & b0)
				   | ((a[s10  ]<<1) & b1)
				   | ((a[s00+1]<<2) & b2)
				   | ((a[s00  ]<<3) & b3) ) >> bit;

			k += 1;
			s00 += 2;
			s10 += 2;
		}
		if (j < ny) {
			/*
			 * row size is odd, do last element in row
			 * s00+1,s10+1 are off edge
			 */
			b[k] = ( ((a[s10  ]<<1) & b1)
				   | ((a[s00  ]<<3) & b3) ) >> bit;
			k += 1;
		}
	}
	if (i < nx) {
		/*
		 * column size is odd, do last row
		 * s10,s10+1 are off edge
		 */
		s00 = n*i;
		for (j = 0; j<ny-1; j += 2) {
			b[k] = ( ((a[s00+1]<<2) & b2)
				   | ((a[s00  ]<<3) & b3) ) >> bit;
			k += 1;
			s00 += 2;
		}
		if (j < ny) {
			/*
			 * both row and column size are odd, do corner element
			 * s00+1, s10, s10+1 are off edge
			 */
			b[k] = ( ((a[s00  ]<<3) & b3) ) >> bit;
			k += 1;
		}
	}
}

