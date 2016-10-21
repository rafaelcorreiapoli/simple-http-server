import java.net.*;
import java.io.*;
import java.util.*;

public class ConexaoWeb {
    private static String DEFAULT_INDEX = "index.html";
    private static String SERVER_NAME = "Java sucks";
    private static String METHOD_GET = "GET";
    private static String USERNAME = "admin";
    private static String PASSWORD = "password";
    Socket socket; 					//socket que vai tratar com o cliente.

    //coloque aqui o construtor

    ConexaoWeb(Socket s) {
        this.socket = s;
    }

    //metodo TrataConexao, aqui serao trocadas informacoes com o Browser...

    private String getDateString() {
        return new Date().toString();
    }
    private void notFound(DataOutputStream os) {
        try {
            os.writeBytes("HTTP/1.1 404 Not Found\n");
            os.writeBytes("Date: " + this.getDateString() + "\n");
            os.writeBytes("Server: " + SERVER_NAME + "\n");
            os.writeBytes("\n");
            os.writeBytes("<html><head><title>Error</title></head><body><p>Not found:(</p></body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unauthorized(DataOutputStream os) {
        try {
            os.writeBytes("HTTP/1.1 401 Unauthorized\n");
            os.writeBytes("Date: " + this.getDateString() + "\n");
            os.writeBytes("Server: " + SERVER_NAME + "\n");
            os.writeBytes("WWW-Authenticate: Basic realm=”System Administrator”\n");
            os.writeBytes("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean authUser(String base64) {
        String encoded = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
        System.out.println("encoded " + encoded);
        return base64.equals(encoded);
    }

    public void trataConexao() {
        String metodo=""; 				//String que vai guardar o metodo HTTP requerido
        String ct; 					//String que guarda o tipo de arquivo: text/html;image/gif....
        String versao = ""; 			//String que guarda a versao do Protocolo.
        File arquivo; 				//Objeto para os arquivos que vao ser enviados.
        String nomeArquivo; 				//String para o nome do arquivo.
        String raiz = "/home/rafa93br/site"; 				//String para o diretorio raiz.
        String inicio;				//String para guardar o inicio da linha
        String senhaUser="";		//String para armazenar o nome e a senha do usuario
        String serverName = "Java Sucks";
        String base64auth = "";
        Date now = new Date();

        BufferedReader is = null;
        DataOutputStream os = null;

        try {
            os = new DataOutputStream(this.socket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            String line;
            line = is.readLine();
            StringTokenizer st = new StringTokenizer(line);
            metodo = st.nextToken();
            nomeArquivo = st.nextToken();
            // servir index.html caso nome do arquivo == "/"
            nomeArquivo = nomeArquivo.endsWith("/") ? DEFAULT_INDEX : nomeArquivo;
            versao = st.nextToken();


            System.out.println("Metodo " + metodo);
            System.out.println("nomeArquivo " + nomeArquivo);
            System.out.println("versao " + versao);

            if (metodo.equals(METHOD_GET)) {
                String nextLine;
                while (!(nextLine = is.readLine()).isEmpty()) {
                    if (nextLine.startsWith(("Host: "))) {
                        System.out.println(nextLine);
                        StringTokenizer stNextLine = new StringTokenizer(nextLine);
                        String key = stNextLine.nextToken();
                        String host = stNextLine.nextToken();
                        System.out.println("host " + host);
                        if (host.equals("localhost2:8080")) {
                            raiz = "/home/rafa93br/host2";
                        }
                    }

                    if (nextLine.startsWith("Authorization: ")) {
                        System.out.println(nextLine);
                        StringTokenizer stNextLine = new StringTokenizer(nextLine);
                        String key = stNextLine.nextToken();
                        String param1 = stNextLine.nextToken();
                        base64auth = stNextLine.nextToken();

                        System.out.println("Base64 Auth: " + base64auth);
                    }
                }

                if (nomeArquivo.startsWith(("/protegido"))) {
                    if (!base64auth.isEmpty()) {
                        if (this.authUser(base64auth)) {
                            System.out.println("AUTH OK!");
                        } else {
                            System.out.println("auth failed");
                            this.unauthorized(os);
                            throw new Exception("auth failed");
                        }
                    } else {
                        System.out.println("no base64 found");
                        this.unauthorized(os);
                        throw new Exception("auth failed");
                    }
                }

                System.out.println("===METHOD GET===");
                ct = this.tipoArquivo(nomeArquivo);

                arquivo = new File(raiz, nomeArquivo);
                FileInputStream fis = new FileInputStream(arquivo);
                byte[] dado = new byte[(int) arquivo.length()];
                fis.read(dado);

                os.writeBytes("HTTP/1.1 200 OK\n");
                os.writeBytes("Date: " + now.toString() + "\n");
                os.writeBytes("Server: " + serverName + "\n");
                os.writeBytes("Connection: close" + "\n");
                os.writeBytes("Content-type: " + ct + "\n");
                os.writeBytes("\n");
                os.write(dado);
            }

        } catch(FileNotFoundException e)
        {
            System.err.println("File does not exists");
            this.notFound(os);
        } catch(IOException e)
        {
            System.err.println(e.toString());
        } catch (Exception e) {
            e.toString();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                    System.out.println("closing os");
                }
            } catch (IOException e) {

            }

        }

        //Fecha o socket.
        try {
            socket.close();
        }
        catch(IOException e) {
        }
    }

//Funcao que retorna o tipo do arquivo.

    public String tipoArquivo(String nome) {
        if(nome.endsWith(".html") || nome.endsWith(".htm")) return "text/html";
        else if(nome.endsWith(".txt") || nome.endsWith(".java")) return "text/plain";
        else if(nome.endsWith(".gif")) return "image/gif";
        else if(nome.endsWith(".class"))  return "application/octed-stream";
        else if( nome.endsWith(".jpg") || nome.endsWith(".jpeg") ) return "image/jpeg";
        else return "text/plain";
    }
}










