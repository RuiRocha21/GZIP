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
public class TesteHuffmanTree
{	
	public static void main (String args[])
	{
		HuffmanTree hft = new HuffmanTree();
		int erro, pos;
		String code;
		boolean verbose = true;
		
		//Inserir código novo
		code = "000";		
		erro = hft.addNode(code, 0, verbose);

		//Inserir código já inserido
		code = "000";
		erro = hft.addNode(code, 1, verbose);

		//Tentar derivar folha
		code = "00001";
		erro = hft.addNode(code, 1, verbose);
		
		
		//Inserir código novo
		code = "11100";
		erro = hft.addNode(code, 3, verbose);

		//Inserir código já inserido
		code = "11100";
		erro = hft.addNode(code, 3, verbose);

		//Tentar derivar folha
		code = "111001";
		erro = hft.addNode(code, 3, verbose);


		// ------------------- Pesquisa
		
		code = "000";
		pos = hft.findNode(code, verbose);
		
		code = "11100";
		pos = hft.findNode(code, verbose);

		code = "111";
		pos = hft.findNode(code, verbose);
		
		
		//pesquisar código bit a bit
		String buffer = "111000100";
		int lv = 0, len = buffer.length();
		boolean sair = false;
		code = "";
		char nextBit;
		
		while(!sair && lv < len)
		{
			nextBit = buffer.charAt(lv);
			code = code + nextBit;
			
			pos = hft.nextNode(nextBit);
			//System.out.println(lv + "   " + hft.curNode.index + "   " + hft.curNode.left + "   " + hft.curNode.right);
						
			if (pos != -2)
				sair = true;
			else
				lv = lv + 1;
		}
		if (pos == -1)
			System.out.println("Código '" + code + "' não encontrado!!!");
		else if (pos == -2)
			System.out.println("Código '" + code + "': não encontrado mas prefixo!!!");
		else
			System.out.println("Código '" + code + "' corresponde à posição " + pos + " do alfabeto");
			
		//procurar código bit a bit
		hft.resetCurNode();
		System.out.println("---------------");
		buffer = "1110";
		lv = 0; len = buffer.length();
		sair = false;
		code = "";
		
		while(lv < len && !sair)
		{
			nextBit = buffer.charAt(lv);
			code = code + nextBit;
			
			pos = hft.nextNode(nextBit);
			
			if (pos == -1)
				sair = true;
			else
				lv = lv + 1;
		}
		
		if (pos == -1)
			System.out.println("Código '" + code + "' não encontrado!!!");
		else if (pos == -2)
			System.out.println("Código '" + code + "': não encontrado mas prefixo!!!");
		else
			System.out.println("Código '" + code + "' corresponde à posição " + pos + " do alfabeto");
	}
}