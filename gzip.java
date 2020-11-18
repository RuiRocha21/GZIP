/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gzip;

/**
 *
 * @author Rui
 */
import java.io.*;
import java.util.*;

//class principal para leitura de um ficheiro gzip
//Métodos:
//gzip(String fileName) throws IOException --> construtor
//int getHeader() throws IOException --> lê o cabeçalho do ficheiro para um objecto da class gzipHeader
//void closeFiles() throws IOException --> fecha os ficheiros
//String bits2String(byte b) --> converte um byte para uma string com a sua representação binária
public class gzip
{
	static gzipHeader gzh;
	static String gzFile;
	static long fileSize;
	static long origFileSize;
	static int numBlocks = 0;
	static RandomAccessFile is;
	static int rb = 0, needBits = 0, availBits = 0;		
        static String buffer = "";
	//função principal, a qual gere todo o processo de descompactação
	public static void main (String args[])
	{			
            //--- obter ficheiro a descompactar
            //String fileName = "FAQ.txt.gz";
            String fileName = "DiskDriveBmp.bmp.gz";
            
            /*if (args.length != 1)
            {
                    System.out.println("Linha de comando inválida!!!");
                    return;
            }
            String fileName = args[0];*/			

            //--- processar ficheiro
            try
            {
                gzip gz = new gzip(fileName);
                //System.out.println(fileSize);

                //ler tamanho do ficheiro original e definir Vector com símbolos
                origFileSize = getOrigFileSize();
                System.out.println(origFileSize);

                //--- ler cabeçalho
                int erro = getHeader();
                if (erro != 0)
                {
                        System.out.println ("Formato inválido!!!");
                        return;
                }
                //else				
                //	System.out.println(gzh.fName);


                //--- Para todos os blocos encontrados
                //pergunta 1
                int BFINAL, HLIT, HDIST, HCLEN, indice;	
                int[] array={16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};
                int[] comprimento=new int[19];
                int[] comprimentoBits=new int[19];//comprimentos de comprimentos de codigo
                do{				
                    //--- ler o block header: primeiro byte depois do cabeçalho do ficheiro
                    needBits = 3;
                    if (availBits < needBits)
                    {
                            rb = is.readUnsignedByte() << availBits | rb;
                            availBits += 8;
                    }

                    //obter BFINAL
                    //ver se é o último bloco
                    BFINAL = (byte) (rb & 0x01); //primeiro bit é o menos significativo
                    System.out.println("BFINAL = " + BFINAL);

                    //analisar block header e ver se é huffman dinâmico					
                    if (!isDynamicHuffman(rb))  //ignorar bloco se não for Huffman dinâmico
                            continue;

                    //descartar os 3 bits correspondentes ao tipo de bloco
                    rb = rb >> 3;
                    availBits -= 3;

                    //pergunta1
                    HLIT = lerBits(5);
                    System.out.println("HLIT =  "+HLIT);
                    HDIST= lerBits(5);
                    System.out.println("HDIST = "+HDIST);
                    HCLEN = lerBits(4);
                    System.out.println("HCLEN = "+HCLEN);


                    //armazenar um array os comprimentos dos codigo do alfabeto do alfabeto de comprimentos de codigo
                    //pergunta2
                    for(indice=0;indice<HCLEN+4;indice++){
                        comprimento[indice]=lerBits(3);
                    }




                    //pergunta 2 codigos de huffman
                    ordenarValores(array,comprimento);
                    comprimentoBits=converterBits(comprimento);
                    System.out.println("\nSimbolo codigo    Huffman");
                    for(indice = 0; indice<comprimento.length;indice++){
                        System.out.println(array[indice]+"      "+comprimento[indice] );
                        if(comprimento[indice]>0){
                            System.out.println("                    "+bits2String((byte)comprimentoBits[indice]).substring(8-comprimento[indice]));
                        }
                        System.out.println();
                    }

                    //armazenar numa arvora de huffman
                    //pergunta3
                    HuffmanTree arvoraHuffman = new HuffmanTree();
                    boolean flag = true; //verbose
                    String codigo;
                    System.out.println("arvore de codigos \n");
                    System.out.println();
                    for(indice=0;indice<comprimento.length;indice++){
                            if(comprimento[indice]!=0) {
                                if(arvoraHuffman.findNode(bits2String((byte)(comprimentoBits[indice])).substring(8-comprimento[indice]), flag) == -1){
                                    arvoraHuffman.addNode(bits2String((byte)comprimentoBits[indice]).substring(8-comprimento[indice]), array[indice], flag);
                                }
                            }
                    }
                    
                    
                    //semana 4 pergunta 5
                    //ler bit a bit
                    /*armazene num array os HLIT + 257 comprimentos dos códigos referentes ao alfabeto de
                    literais/comprimentos, codificados segundo o código de Huffman decomprimentos de códigos*/
                    
                    int literais[] = new int[286];
                    int simbolosLiterais[] = new int[286];
                    literais=procuraArvoreHuffman(arvoraHuffman,286,257+HLIT);
                    int[] comprimentoLiterais = new int[literais.length];
                    comprimentoLiterais=converterBits(literais);
                    
                    System.out.println("\nSimbol 	Codigo	Huffman");

                    for(indice=0;indice<literais.length;indice++){
                        System.out.print(indice+"	"+literais[indice]);
                        if(literais[indice]!=0){
                            System.out.print("	");
                            if(literais[indice]>8){
                                //System.out.print("  "+bits2String((byte)(comprimentoLiterais[indice]>>8)).substring(16-literais[indice])+bits2String((byte)comprimentoLiterais[indice]));
                                System.out.println("\n");
                            }
                            else{
                                    //System.out.print("  "+bits2String((byte)comprimentoLiterais[indice]).substring(8-literais[indice]));
                                    System.out.println("\n");
                            }
                        }
                        simbolosLiterais[indice]=indice;
                        System.out.println("\n");
                    }
                    
                    HuffmanTree arvoreLiterias = new HuffmanTree();
                    System.out.println();
                    flag = true;
                    System.out.println("arvore de literais \n");
                    for(indice = 0;indice<literais.length;indice++){
                        if(literais[indice] != 0){
                             if(literais[indice]>8){
                                 if(arvoreLiterias.findNode(bits2String((byte)(comprimentoLiterais[indice]>>8)).substring(16-literais[indice])+bits2String((byte)comprimentoLiterais[indice]), flag) == -1){
                                    arvoreLiterias.addNode(bits2String((byte)(comprimentoLiterais[indice]>>8)).substring(16-literais[indice])+bits2String((byte)comprimentoLiterais[indice]), simbolosLiterais[indice], flag);
                                 }
                             }else{
                                 if(arvoreLiterias.findNode(bits2String((byte)(comprimentoLiterais[indice])).substring(8-literais[indice]), flag) == -1){
                                    arvoreLiterias.addNode(bits2String((byte)(comprimentoLiterais[indice])).substring(8-literais[indice]), simbolosLiterais[indice], flag);
                                 }
                             }
                        }
                    }
                    
                    
                    
                    //semana 5 pergunta 6
                    int distancias[] = new int[30];
                    int simbolosDistancia[] = new int[30];
                    distancias = procuraArvoreHuffman(arvoraHuffman,30,HDIST+1);
                    int[] comprimentoDistancias = new int[distancias.length];
                    comprimentoDistancias = converterBits(distancias);
                    System.out.println("\nSimbol 	comprimento	Huffman");
                    for(indice = 0;indice<distancias.length;indice++){
                        System.out.print(indice+"	"+distancias[indice]);
                        if(distancias[indice] !=0){
                            System.out.println("    ");
                            if(distancias[indice]>8){
                                //System.out.print("              "+bits2String((byte)(comprimentoDistancias[indice]>>8)).substring(16-distancias[indice])+bits2String((byte)comprimentoDistancias[indice]));
                                System.out.print("\n");
                            }
                            else{
                                    //System.out.print("              "+bits2String((byte)comprimentoDistancias[indice]).substring(8-distancias[indice]));
                                    System.out.print("\n");
                            }
                        }
                        simbolosDistancia[indice]=indice;
                        System.out.print("\n");
                    }
                    
                    HuffmanTree arvoreDistancias = new HuffmanTree();
                    System.out.println();
                    flag = true;
                    System.out.println("arvore de distancias \n");
                    for(indice = 0;indice<distancias.length;indice++){
                        if(distancias[indice] != 0){
                            if(distancias[indice]>8){
                                if(arvoreDistancias.findNode(bits2String((byte)(comprimentoDistancias[indice]>>8)).substring(16-distancias[indice])+bits2String((byte)comprimentoDistancias[indice]), flag) == -1){
                                    arvoreDistancias.addNode(bits2String((byte)(comprimentoDistancias[indice]>>8)).substring(16-distancias[indice])+bits2String((byte)comprimentoDistancias[indice]), simbolosDistancia[indice], flag);
                                }
                            }else{
                                if(arvoreDistancias.findNode(bits2String((byte)comprimentoDistancias[indice]).substring(8-distancias[indice]), flag) == -1){
                                    arvoreDistancias.addNode(bits2String((byte)comprimentoDistancias[indice]).substring(8-distancias[indice]), simbolosDistancia[indice], flag);
                                }
                            }
                        }
                    }
                    
                    

                    //--- Se chegou aqui --> compactado com Huffman dinâmico --> descompactar
                    //adicionar programa...	
                    LZ77(arvoreLiterias,arvoreDistancias);
                    
                    //actualizar número de blocos analisados
                    numBlocks++;				
                }while(BFINAL == 0);
                String nomeOriginal = gzh.fName;
                guardarDados(buffer,nomeOriginal);

                //terminações			
                is.close();	
                System.out.println("End: " + numBlocks + " bloco(s) analisado(s).");
            }
            catch (IOException erro)
            {
                    System.out.println("Erro ao usar o ficheiro!!!");
                    System.out.println(erro);
            }
	}
        
        public static void guardarDados(String dados,String nome) throws IOException{
		BufferedWriter escritor = new BufferedWriter(new FileWriter(nome));
		escritor.write(dados + "\r\n");
		escritor.close();
	}
        
        public static void LZ77(HuffmanTree arvLiterais,HuffmanTree arvDistancias) throws IOException{
            int comprimentoBitsExtra[] = {3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258};
            int comprimentoBits[] =  {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0};
            int bitsDistanciaExtra[] = {1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577};
            int bitsDistancia[] = {0, 0, 0 , 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13};
            
            String codigo;
            int pos, comprimento, distancia, bitExtra, aux,i;
            while(true){
                //le os bits e compara-os com a arvore dos literais
                codigo = inverter(bits2String((byte)lerBits(1)));
                pos = arvLiterais.nextNode(codigo.charAt(0));
                while(pos == -2){
                    codigo = inverter(bits2String((byte)lerBits(1)));
                    pos = arvLiterais.nextNode(codigo.charAt(0));
                }
                arvLiterais.resetCurNode();
                //se for menor de 256 adiciona o carater
                if(pos<256){
                        buffer += (char)pos;
                }
                //se for igual a 256 sai do ciclo
                else if(pos==256) break;
                //se for maior de 256 descobre a distancia e o comprimento
                else if(pos>256){
                    bitExtra = lerBits(comprimentoBits[pos-257]);
                    comprimento = bitExtra + comprimentoBitsExtra[pos-257];

                    //le os bits e compara-os com a arvore das distancias
                    codigo = inverter(bits2String((byte) lerBits(1)));
                    pos = arvDistancias.nextNode(codigo.charAt(0));
                    while(pos==-2){
                            codigo = inverter(bits2String((byte) lerBits(1)));
                            pos = arvDistancias.nextNode(codigo.charAt(0));	
                    }
                    arvDistancias.resetCurNode();

                    bitExtra = lerBits(bitsDistancia[pos]);
                    distancia = bitExtra + bitsDistanciaExtra[pos];
                    
                    //escreve no buffer
                    aux=(int)(buffer.length()-distancia);
                    if(aux+comprimento < buffer.length()){
                        buffer += buffer.substring(aux, aux + comprimento);
                        //System.out.println(buffer);
                    }
                    else{
                        for(i=0;i<comprimento;i++){
                            buffer += buffer.charAt(aux+i);
                                //System.out.println(buffer);
                        }
                    }
                    
                }
            }
            System.out.println(buffer);
        }
        
        
        public static int[] procuraArvoreHuffman(HuffmanTree arvoreHuffman,int tamanho286,int tamanho257maisHLIT) throws IOException{
            int repete=0, pos, elemento;
            String codigos;
            System.out.println();
            elemento=0;
            int literais[] = new int[tamanho286];
            while(elemento<tamanho257maisHLIT){
                codigos=inverter(bits2String((byte)lerBits(1)));
                pos=arvoreHuffman.nextNode(codigos.charAt(0));

                while(pos==-2){
                        codigos=inverter(bits2String((byte)lerBits(1)));
                        pos=arvoreHuffman.nextNode(codigos.charAt(0));				
                }
                if(pos>15){
                    if(pos==16){
                                    repete=lerBits(2) + 3;
                    }
                    if(pos==17){
                                    repete=lerBits(3) + 3;
                    }
                    if(pos==18){
                                    repete=lerBits(7) + 11;
                    }
                    while((repete--)>0){
                        if(pos==16){
                                literais[elemento]=literais[elemento-1];
                        }
                        else{
                                literais[elemento]=0;
                        }
                        elemento++;
                    }
                }
                else{
                    literais[elemento]=pos;
                    elemento++;
                }
                arvoreHuffman.resetCurNode();
            }
            return literais;
        }
        
        
        
        public static String inverter(String str){
            if ( str. length ( ) <= 1 ) { 
		     return str ;
		}
		return inverter( str. substring ( 1 , str. length ( ) ) ) + str. charAt ( 0 );
        }
        
        public static int[] converterBits(int[] comprimentos){
            int[] nSimbolos = new int[19]; //numero de simbolos a serem codificados com i bits
            int[] proximoCodigo = new int[19];
            int []codigosHuffman = new int[comprimentos.length];
            int codigo= 0, bits, n;
            nSimbolos[0] = 0;
            
            
            for (bits= 1; bits < nSimbolos.length; bits++) {
                //o codigo do primeiro simbolos a ser codificado com i bits e igual ao ultimo simbolo com i-1bits << 1
                codigo = (proximoCodigo[bits-1])<<1;
                nSimbolos[bits] = codigo;

                proximoCodigo[bits]=nSimbolos[bits];
                for(n=0;n<comprimentos.length;n++){
                    //encontra todos os simbolos a serem codificados com i bits, para encontrar o ultimo+1
                    if(comprimentos[n]==bits){
                        codigosHuffman[n]=proximoCodigo[bits];
                        proximoCodigo[bits]++;
                    }
                }
            }
            return codigosHuffman;
        }
        
        
      
       
        //metodo pergunta 1
        public static int lerBits(int numeroBits) throws IOException{
            /*
                          //1     3     7      15      31      63     127 decimal
            int[] mascara={0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF}; //hexadecimal
            
            needBits=numeroBits;
            //mascara = 2^n-1
            */
            int valoresBits = 0;
            int i = 0;
            int bit;

            for ( i = 0; i < numeroBits; i++ ){
                    bit = proximoBit();
                    valoresBits |= ( bit << i );
            }
            return valoresBits;
	}
        
     
        public static int proximoBit() throws IOException{
		int aux;
		needBits = 1;
		if (availBits < needBits){
			rb = is.readUnsignedByte() << availBits | rb;
			availBits += 8;		
		}
		aux=(char)(rb & 0x01);
		rb = rb >> needBits;
		availBits -= needBits;

	  return aux;
	}
        
        
        //bublesort
        public static void ordenarValores(int[] vetor, int[] comprimento){
            int i,j,aux1,aux2;
            for(i=0;i<vetor.length;i++){
                for(j=i;j<vetor.length;j++){
                    if(vetor[j]<vetor[i]){
                        aux1 = vetor[i];
                        aux2 = comprimento[i];
                        vetor[i] = vetor[j];
                        vetor[j] = aux1;
                        comprimento[i] = comprimento[j];
                        comprimento[j] = aux2;
                        
                    }
                }
            }
        }
        
       
        
        
	//--------------------------------------------------------------------------------------------------------
	//Construtor: recebe nome do ficheiro a descompactar e cria File Streams
	gzip(String fileName) throws IOException
	{
		gzFile = fileName;
		is = new RandomAccessFile(fileName, "r");
		fileSize = is.length();
	}
	
	
	//Obtém tamanho do ficheiro original
	public static long getOrigFileSize() throws IOException
	{
		//salvaguarda posição actual do ficheiro
		long fp = is.getFilePointer();
		
		//últimos 4 bytes = ISIZE;
		is.seek(fileSize-4);
		
		//determina ISIZE (só correcto se cabe em 32 bits)
		long sz = 0;
		sz = is.readUnsignedByte();
		for (int i = 0; i <= 2; i++)
			sz = (is.readUnsignedByte() << 8*(i+1)) + sz;			
		
		//restaura file pointer
		is.seek(fp);
		
		return sz;		
	}
		

	//Lê o cabeçalho do ficheiro gzip: devolve erro se o formato for inválido
	public static int getHeader() throws IOException  //obtém cabeçalho
	{
		gzh = new gzipHeader();
		
		int erro = gzh.read(is);
		if (erro != 0) return erro; //formato inválido		
		
		return 0;
	}
		
	
	//Analisa block header e vê se é huffman dinâmico
	public static boolean isDynamicHuffman(int k)
	{				
		byte BTYPE = (byte) ((k & 0x06) >> 1);
						
		if (BTYPE == 0) //--> sem compressão
		{
			System.out.println("Ignorando bloco: sem compactação!!!");
			return false;
		}
		else if (BTYPE == 1)
		{
			System.out.println("Ignorando bloco: compactado com Huffman fixo!!!");
			return false;
		}
		else if (BTYPE == 3)
		{
			System.out.println("Ignorando bloco: BTYPE = reservado!!!");
			return false;
		}
		else
			return true;
		
	}
	
	
	//Converte um byte para uma string com a sua representação binária
	public static String bits2String(byte b)
	{
		String strBits = "";
		byte mask = 0x01;  //get LSbit
		
		for (byte bit, i = 1; i <= 8; i++)
		{
			bit = (byte)(b & mask);
			strBits = bit + strBits; //add bit to the left, since LSb first
			b >>= 1;
		}
		return strBits;		
	}
}