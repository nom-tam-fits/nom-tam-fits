/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */
int reportResult(long xsize, long ysize,int * intData,double* matrix ,double bscale,double bzero,int iminval,int imaxval) {
	int index ;
	int size = xsize*ysize;
	printf("matrix");
	for (index = 0; index < size; index++) {
		if (index != 0) {
			printf(",");
		}
		printf(" %20.20e",matrix[index]);
	}
	printf("\n");

	printf("iData");
	for (index = 0; index < size; index++) {
		if (index != 0) {
			printf(",");
		}
		printf(" %i",intData[index]);
	}
	printf("\n");
	printf("bscale %20.20e\n",bscale);
	printf("bzero %20.20e\n",bzero);
	printf("iminval %i\n",iminval);
	printf("imaxval %i\n",imaxval);
	return 0;
}
static const  double  initData []= {
		0.0, 9.999833334166665, 19.99866669333308, 29.99550020249566, 39.98933418663416, 49.979169270678334, 59.96400647944459, 69.94284733753277, 79.91469396917269, 89.87854919801104, 99.83341664682816, 109.77830083717481, 119.71220728891936, 129.63414261969487, 139.54311464423648, 149.4381324735992, 159.31820661424598, 169.18234906699604, 179.02957342582417, 188.85889497650058, 198.66933079506123, 208.45989984609955, 218.22962308086932, 227.9775235351884, 237.7026264271346, 247.40395925452293, 257.08055189215514, 266.73143668883114, 276.3556485641138, 285.9522251048355, 295.52020666133956, 305.0586364434435, 314.5665606161178, 324.0430283948684, 333.4870921408144, 342.89780745545136, 352.27423327508995, 361.615431964962, 370.9204694129827, 380.1884151231614, 389.4183423086505, 398.60932798442286, 407.7604530595701, 416.87080242921076, 425.9394650659996, 434.9655341112302, 443.9481069655198, 452.8862853790683, 461.7791755414829, 470.625888171158, 479.425538604203, 488.17724688290747, 496.88013784373675, 505.5333412048469, 514.1359916531131, 522.6872289306592, 531.1861979208834, 539.6320487339692, 548.0239367918736, 556.3610229127838, 564.6424733950354, 572.8674601004813, 581.035160537305, 589.1447579422695, 597.1954413623921, 605.1864057360395, 613.1168519734338, 620.9859870365597, 628.7930240184686, 636.5371822219679, 644.217687237691, 651.8337710215366, 659.3846719714732, 666.8696350036979, 674.287911628145, 681.6387600233342, 688.9214451105513, 696.1352386273567, 703.2794192004102, 710.3532724176079, 717.3560908995228, 724.2871743701426, 731.1458297268958, 737.9313711099627, 744.6431199708593, 751.2804051402927, 757.842562895277, 764.3289370255051, 770.7388788989693, 777.0717475268239, 783.3269096274834, 789.5037396899505, 795.6016200363661, 801.6199408837772, 807.5581004051143, 813.4155047893737, 819.1915683009983, 824.88571333845, 830.4973704919705, 836.0259786005205, 841.4709848078965, 846.8318446180152, 852.108021949363, 857.2989891886034, 862.4042272433385, 867.4232255940169, 872.3554823449863, 877.2005042746816, 881.9578068849476, 886.6269144494872, 891.2073600614355, 895.6986856800477, 900.1004421765051, 904.4121893788258, 908.6334961158832, 912.7639402605209, 916.8031087717668, 920.7505977361357, 924.6060124080203, 928.3689672491666, 932.0390859672262, 935.6160015533859, 939.0993563190676, 942.4888019316975, 945.7839994495389, 948.9846193555862, 952.0903415905158, 955.1008555846922, 958.015860289225, 960.8350642060727, 963.5581854171929, 966.184951612734, 968.7151001182652, 971.1483779210446, 973.4845416953194, 975.7233578266591, 977.8646024353162, 979.9080613986142, 981.8535303723597, 983.7008148112765, 985.4497299884601, 987.1001010138503, 988.6517628517197, 990.1045603371778, 991.4583481916865, 992.7129910375885, 993.8683634116449, 994.9243497775809, 995.8808445376401, 996.7377520431434, 997.4949866040545, 998.1524724975482, 998.710143975583, 999.167945271476, 999.5258306054791, 999.783764189357, 999.9417202299663, 999.9996829318346, 999.9576464987401, 999.8156151342909, 999.5736030415051, 999.2316344213905, 998.789743470524, 998.2479743776324, 997.6063813191737, 996.8650284539189, 996.0239899165367, 995.0833498101802, 994.043202198076, 992.9036510941185, 991.6648104524686, 990.326804156158, 988.8897660047014, 987.3538397007164, 985.7191788355535, 983.9859468739369, 982.1543171376185, 980.2244727880454, 978.1966068080446, 976.0709219825242, 973.8476308781951, 971.5269558223152, 969.1091288804563, 966.5943918332975, 963.9829961524481, 961.2752029752999, 958.4712830789142, 955.571516852944, 952.5761942715953, 949.4856148646304, 946.3000876874145, 943.0199312900106, 939.6454736853249, 936.1770523163061, 932.6150140222005, 928.9597150038693, 925.2115207881683, 921.3708061913954, 917.4379552818099, 913.4133613412251, 909.2974268256817, 905.090563325201, 900.7931915226272, 896.40574115156, 891.9286509533796, 887.3623686333755, 882.7073508159741, 877.9640629990781, 873.1329795075164, 868.2145834456127, 863.2093666488737, 858.1178296348089, 852.9404815528761, 847.6778401335698, 842.3304316366457, 836.8987907984978, 831.3834607786831, 825.7849931056081, 820.1039476213741, 814.340892425796, 808.4964038195901, 802.5710662467473, 796.5654722360865, 790.4802223420048, 784.3159250844199, 778.0731968879212, 771.752662020126, 765.3549525292535, 758.880708180922, 752.3305763941707, 745.7052121767202, 739.0052780594708, 732.2314440302514, 725.3843874668196, 718.4647930691261, 711.4733527908444, 704.4107657701762, 697.2777382599378, 690.0749835569364, 682.8032219306397, 675.4631805511509, 668.0555934164909, 660.5812012792007, 653.0407515722649
};

double* initMatrix() {
	int index;
	extern const double initData[];
    double* matrix = calloc(1000, sizeof(double));
    for (index = 0; index < 1000; index++) {
        matrix[index] =initData[index];
    }
    return matrix;
}

int Arraysfill(double* a,int fromIndex,int toIndex,double val) {
	int i ;
	for (i = fromIndex; i < toIndex; i++)
	            a[i] = val;
	return 0;
}

   int testDifferentQuantCases() {
	   int index;
        long xsize = 12;
        long ysize = 2;
        double* matrix = initMatrix();
        float someQuant= ysize*2.;
        int* intData = calloc(1000, sizeof(int));
        double NULL_VALUE = -9.1191291391491004e-36;
        double bscale=0.,bzero=0.;
        int iminval=0,imaxval=0;


        matrix = initMatrix();
        fits_quantize_double(3942,matrix, xsize, ysize,1,NULL_VALUE,-4e0,2,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(xsize, ysize,intData,matrix,bscale,bzero,iminval,imaxval);

        matrix = initMatrix();
        fits_quantize_double(0,matrix, xsize - 3, ysize,0,NULL_VALUE,-0.,2,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(xsize - 3, ysize,intData,matrix,bscale,bzero,iminval,imaxval);

        matrix = initMatrix();
        fits_quantize_double(0,matrix, 3, 2,0,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(3, 2,intData,matrix,bscale,bzero,iminval,imaxval);

        matrix = initMatrix();
        matrix[5] = NULL_VALUE;
        fits_quantize_double(0,matrix, 3, 2,1,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(3, 2,intData,matrix,bscale,bzero,iminval,imaxval);

        matrix = initMatrix();
        Arraysfill(matrix, 11, xsize + 1, NULL_VALUE);
        fits_quantize_double(0,matrix, xsize, ysize,1,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(xsize, ysize,intData,matrix,bscale,bzero,iminval,imaxval);

        matrix = initMatrix();
        Arraysfill(matrix, 11, xsize + 1, NULL_VALUE);
        fits_quantize_double(0,matrix, xsize, ysize,1,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(xsize, ysize,intData,matrix,bscale,bzero,iminval,imaxval);

        // test very small image
        matrix = initMatrix();
        fits_quantize_double(0,matrix, 1, 1,1,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(1, 1,intData,matrix,bscale,bzero,iminval,imaxval);

        // test null image
        Arraysfill(matrix, 0,900,NULL_VALUE);
        fits_quantize_double(0,matrix, xsize, ysize,1,NULL_VALUE,4.,1,intData,&bscale,&bzero,&iminval,&imaxval);
        reportResult(xsize, ysize,intData,matrix,bscale,bzero,iminval,imaxval);

        for (index = 8; index > 0; index--) {
            matrix = initMatrix();
            Arraysfill(matrix, index, xsize, NULL_VALUE);
            fits_quantize_double(3942,matrix, xsize, ysize,1,NULL_VALUE,4.,index % 2 == 1 ? 1 : 2,intData,&bscale,&bzero,&iminval,&imaxval);
            reportResult(xsize, ysize,intData,matrix,bscale,bzero,iminval,imaxval);
        }
        return 0;
    }
