import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorWeb {
    public static void main(String[] args) {
        int porta; 		//porta aonde o servidor vai aguardar por pedido de conexao.
        ServerSocket sw; 	//Socket servidor
        Socket cw;
        //de um valor para a porta!


        porta = 8080;

        try {

            sw = new ServerSocket(porta);
            sw.setReuseAddress(true);

            while(true) {
                cw = sw.accept();

                ConexaoWeb conexaoWeb = new ConexaoWeb(cw);

                System.out.println("Aceitou conexao");
                conexaoWeb.start();
            }

        }
        catch(IOException e) {
            System.err.println(e.toString());
            System.err.println("Servidor foi abortardo");
        }
    }
}