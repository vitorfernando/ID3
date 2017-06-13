/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id3;

import java.util.Vector;

/**
 *
 * @author Vitor
 */
public class Arvore {
    public double entropy;

    public Vector data; //vetor de intancias de dados
    public int atributoDecomposicao;
    public int valorDecomposicao; 
    public Arvore []filho; //nos filhos
    public Arvore pai; //no pai
    
    public Arvore() {
        data = new Vector();
    }
}
