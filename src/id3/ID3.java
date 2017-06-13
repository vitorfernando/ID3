/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.


Author(s):  Daniel Liberato	RA: 552127
            Elisa Castro	RA: 587303
            Letícia Berto	RA: 587354
            Vitor Fernando	RA: 552488

           
 */
package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */
public class ID3 {
    static int numAtributos;
    static int numIntancias;
    static Vector[] dominios;
    static String[] classes;
    static DataPoint[] conjuntoteste;
    static int interator = 0;
    String[] nomeAtributos;
    Arvore raiz = new Arvore();
    
    public static void main(String[] args) {
        ID3 me = new ID3();
        int status;
        Scanner scanner = new Scanner(System.in);
        try {
            //le arquivo onde esta os meta dados do conjunto de dados examplo
            System.out.println("Arquivo de informação do conjunto de exemplos: ");
            me.leituraInfoData("C:\\"+scanner.nextLine());

            System.out.println("Arquivo do conjunto de exemplos: ");
            status = me.leituraData("C:\\"+scanner.nextLine());//le os dados

            if (status <= 0) return;

            System.out.println("Arquivo do conjunto de teste: ");
            status = me.leituraTeste("C:\\"+scanner.nextLine());//le os dados

            me.createDecisionTree();
            } catch (Exception ex) {
                 Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
            }
    } 
     
    //identifica informações do conjunto de dados
    public void leituraInfoData(String dataset_info) throws IOException{
        FileInputStream in = null;
        //abre arquivo
        try {
            File inputFile = new File(dataset_info);
            in = new FileInputStream(inputFile);
        } catch ( Exception e) {
            System.err.println( "Nao foi possivel abrir o arquivo: " + dataset_info + "\n" + e);
        }
        BufferedReader buffer_in = new BufferedReader(new InputStreamReader(in));
        String input;
        input = buffer_in.readLine(); // le linha com numero de atributos e numero de intancias
        StringTokenizer st = new StringTokenizer(input);
        numAtributos = Integer.parseInt(st.nextToken());//le numero de atributos
        numIntancias = Integer.parseInt(st.nextToken()); //le numero de intancias
        nomeAtributos = new String[numAtributos];
        dominios = new Vector[numAtributos];
        int qtd = 0,i;
        for(i=0; i < numAtributos; i++ ){
            input = buffer_in.readLine(); // le linha com nome de atributos e seus possiveis valores
            st = new StringTokenizer(input);
            qtd = st.countTokens(); //qts de possiveis valores do atributo + 1
            nomeAtributos[i] = st.nextToken(); //guarda o nome do atributo
            dominios[i] = new Vector(qtd-1);
            //le qtd valores possiveis do atributo i
            while(qtd-1 > 0){
                dominios[i].addElement(st.nextToken()); //guarda os possiveis valores do atributo              
                qtd--;
            }
        }
    }

    public int []getAllValues(Vector data, int attribute) {
        Vector values = new Vector();
        int num = data.size();  
        
        for (int i=0; i< num; i++) {
            DataPoint point = (DataPoint)data.elementAt(i);
            String symbol =(String)dominios[attribute].elementAt(point.atributos[attribute] );
                      
            int index = values.indexOf(symbol);
            if (index < 0) {
                values.addElement(symbol);
           }
        }
        int []array = new int[values.size()];
        
        for (int i=0; i< array.length; i++) {
            String symbol = (String)values.elementAt(i);
            array[i] = dominios[attribute].indexOf(symbol);
        }

        values = null;
        return array;
    }
    
    //retorn subconjunto das intancias 
    public Vector getSubset(Vector data, int atributo, int valor) {
        Vector subset = new Vector();
        int num = data.size();

        for (int i=0; i< num; i++) {
            DataPoint point = (DataPoint)data.elementAt(i);
            if (point.atributos[atributo] == valor)
                subset.addElement(point);
        }
        return subset;
    }
    
    public double calculoEntropia(Vector data) {
        int qtddata = data.size(); //get qts de valores do atributo
        if (qtddata == 0)
            return 0;
        // numero de possibilidades de valores de classe
        int qtdvalores = dominios[numAtributos-1].size(); 
        double soma = 0;

        for (int i=0; i< qtdvalores; i++) {
            int count=0;
            //conta qnts exemplos estão na classe i
            for (int j=0; j< qtddata; j++) {
                DataPoint point = (DataPoint)data.elementAt(j);
                if (point.atributos[numAtributos-1] == i)
                    count++;
            }
            //calcula a proporção de elemento de data que estão na classe i
            double prob = 1.*count/qtddata;
            //calculo da entropia
            if (count > 0) 
                soma += -prob*Math.log(prob);
        }
        return soma;
    }
    //verifica se atributo ja esta sendo usado para decomposicao
    public boolean emUso(Arvore node, int atributo) {
        if (node.filho != null) {
            if (node.atributoDecomposicao == atributo )
                return true;
        }

        if (node.pai == null) return false;
            return emUso(node.pai, atributo);
    }
    //decomponhe nos de acordo com do ganho calculado
    public void decomporNo(Arvore node) {
        double melhorGanho=0;
        boolean selected=false;
        int atributoSelecionado=0;
        int numdata = node.data.size();
        
        //calcula a entropia para o conjunto de dados
        node.entropy = calculoEntropia(node.data);
        if (node.entropy == 0) return;
        //calculo para os subconjuntos
        for (int i=0; i< numAtributos-1; i++) {
            int numvalues = dominios[i].size();
            //verifica se o o atributo ja nao esta sendo usado como no, se esta passa para a proxima interação
            if ( emUso(node, i) ) continue;
            double ganhoInformacao = 0;
            
            for (int j=0; j< numvalues; j++) {
                //obten subconjunto de data para qual o atributo i tem valor j
                Vector subset = getSubset(node.data, i, j); 
                if (subset.size() == 0) continue;
                double subentropy = calculoEntropia(subset); //calcula a entropia do subconjunto
                //calcula a media de todas as subentropias
                ganhoInformacao += subentropy *
                subset.size();
            }
            ganhoInformacao = node.entropy - (ganhoInformacao / numdata); //calculo do ganho
            
            if (selected == false) { //se o atributo nao foi selecionado como no
                selected = true;    
                melhorGanho = ganhoInformacao;
                atributoSelecionado = i; //marca atributo escolhido
            } else {
                if (ganhoInformacao > melhorGanho) { //compara qual ganho é melhor
                    selected = true;
                    melhorGanho = ganhoInformacao;
                    atributoSelecionado = i; //guarda melho ganho
                }
            }
        }
        if(selected == false) return; //ja construi todos os nos atributos
        
        int numvalues = dominios[atributoSelecionado].size();
        node.atributoDecomposicao = atributoSelecionado;
        node.filho = new Arvore [numvalues];

        for (int j=0; j< numvalues; j++) {
            node.filho[j] = new Arvore();
            node.filho[j].pai = node;//marca pai do no filho j
            node.filho[j].data = getSubset(node.data,atributoSelecionado, j); //atribui data do no no j
            node.filho[j].valorDecomposicao = j;
        }
        //decompoem no filho
        for (int j=0; j< numvalues; j++) {
            decomporNo(node.filho[j]);
        }
        //apaga 
        node.data = null;
    }
    public int leituraTeste(String filename) throws IOException{
        FileInputStream in = null;
        try {
            File inputFile = new File(filename);
            in = new FileInputStream(inputFile);
        } catch ( Exception e) {
            System.err.println( "Não foi possivel abrir o arquivo: " + filename + "\n" + e);
            return 0;
        }
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        String input;
        StringTokenizer tokenizer;
        int numTeste = 1728 - numIntancias ;
        int j = 0;
        conjuntoteste = new DataPoint[numTeste];
        classes = new String[numTeste];
        //le entradas de teste
        while(j < numTeste){
            input = bin.readLine(); //le um instancia
            if (input == null) break; //para se chegou ao fim do arquivo
            tokenizer = new StringTokenizer(input, ",");
            if(tokenizer.countTokens() != numAtributos) System.err.println("Erro");
            DataPoint point = new DataPoint(numAtributos);
            for (int i = 0; i < numAtributos; i++) {
                //guarda o valor do atributo de acordo com o dominio do atributo
                point.atributos[i] = dominios[i].indexOf(tokenizer.nextToken());
            }
            conjuntoteste[j] = point;
            j++;
        }
        bin.close();
        return 1;
    }
    public int leituraData(String filename) throws Exception {
        FileInputStream in = null;
        try {
            File inputFile = new File(filename);
            in = new FileInputStream(inputFile);
        } catch ( Exception e) {
            System.err.println( "Não foi possivel abrir o arquivo: " + filename + "\n" + e);
            return 0;
        }
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        String input;
        StringTokenizer tokenizer;
        int j=0;
        
        while(true){
            input = bin.readLine(); //le um instancia
            if(input == null) break;  //se entrada estiver vazia sai do loop
            tokenizer = new StringTokenizer(input, ","); //split da entrada         
            DataPoint point = new DataPoint(numAtributos); //instancia um novo DataPoint
            for (int i=0; i < numAtributos; i++) {
                //guarda o valor do atributo de acordo com o dominio do atributo
                point.atributos[i] = dominios[i].indexOf(tokenizer.nextToken());
            }
            raiz.data.addElement(point);//armazena os dados no primeiro no da arvore
            j++;
        }
        
        bin.close(); //fecha buffer de entrada
        return 1;
    }
    
    public void classificationTeste(Arvore node, DataPoint teste) {
        int classe = numAtributos -1;
        
        if(node.filho == null){ //nofolha
            int []values = getAllValues(node.data, classe );
           
            if (values.length == 1){
                classes[interator] = (String)dominios[classe].elementAt(values[0]);
                interator++;
                return;
            }
            else if(values.length > 1){ //mais de um classe
                System.out.print(nomeAtributos[classe] + " = {");
                for (int i=0; i < values.length; i++) {
                    System.out.print("\"" + dominios[classe].elementAt(values[i]) + "\"");
                    classes[interator]= (String)dominios[classe].elementAt(values[i]);
                    if ( i != values.length-1 ){
                        System.out.print( " , " );
                        classes[interator]= ",";
                    }
                }
                interator++;
                System.out.println( " };");
                return;
            }
            else{
                System.out.println(nomeAtributos[classe] + " = \"desconhecida\";");
                classes[interator]= "desconhecida";
                interator++;
                return;
            }
        }
        
        int numfilhos = node.filho.length; //numero de filhos
        
        boolean flag = true;
        for(int i=0; i < numfilhos; i++){
          
            String symbol = (String) dominios[node.atributoDecomposicao].elementAt(i);
   
            if(teste.atributos[node.atributoDecomposicao] == 
                    dominios[node.atributoDecomposicao].indexOf(symbol) && flag){
             
                classificationTeste(node.filho[i], teste);
               
                flag = false;
            }
        }
    }
    
    public void printTree(Arvore node, String tab) {
        int outputattr = numAtributos-1;
        if (node.filho == null) {
            int []values = getAllValues(node.data, outputattr );
       
            if (values.length == 1) {
                System.out.println(tab + "\t" + nomeAtributos[outputattr] + " = \"" +
                    dominios[outputattr].elementAt(values[0]) + "\";");
                return;
            }
            System.out.print(tab + "\t" + nomeAtributos[outputattr] + " = {");
            for (int i=0; i < values.length; i++) {
                System.out.print("\"" + dominios[outputattr].elementAt(values[i]) + "\"");

                if ( i != values.length-1 ) 
                    System.out.print( " , " );
            }
            System.out.println( " };");
            return;
        }
        int numvalues = node.filho.length;

        for (int i=0; i < numvalues; i++) {
            System.out.println(tab + "if( " + nomeAtributos[node.atributoDecomposicao] 
                    + " == \"" + dominios[node.atributoDecomposicao].elementAt(i)+ "\") {" );
            printTree(node.filho[i], tab + "\t");

            if (i != numvalues-1) 
                System.out.print(tab + "} else ");

            else System.out.println(tab + "}");

        }
    }

    public void avaliaClassificador(){
        int soma=0;
        for(int i=0; i< conjuntoteste.length; i++){
            System.out.println("instancia : "+(i+1)+" Classe correta: "
                    +dominios[6].elementAt(conjuntoteste[i].atributos[6])
                    +" X Classificação dada: "+classes[i]);
            if(dominios[6].elementAt(conjuntoteste[i].atributos[6]) == classes[i]){
                soma++;
            }
        }
        float acerto = ((float)soma/conjuntoteste.length)*100;
        System.out.println("Taxa de acerto: "+ acerto +"%");
    }
    
    public void createDecisionTree(){
        decomporNo(raiz);
        for(int i=0 ; i < conjuntoteste.length; i++){
            classificationTeste(raiz, conjuntoteste[i]);
        }
        avaliaClassificador();
        
    }
}