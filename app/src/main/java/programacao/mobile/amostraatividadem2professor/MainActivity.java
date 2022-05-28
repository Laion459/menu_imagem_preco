package programacao.mobile.amostraatividadem2professor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Produto> produtos = new ArrayList<>();
    private int aux = 0;
    private int auxII = 0;
    private boolean online = true;

    private class AsyncTaskJSONPost extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            // ip universidade
            String resourceURI="http://192.168.0.22:5500/json.json";

            // ip job
            //String resourceURI="http://192.168.0.25:5500/json.json";

            String formatedURL=resourceURI;
            URL url = null;
            try {
                url = new URL(formatedURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                con.setRequestMethod("GET");
                InputStream is=con.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";


                // Aqui transforma arquivo json em string
                Gson gson = new GsonBuilder().create();
                ArrayList<Produto> listaFromJSON = (ArrayList<Produto>) gson.fromJson(response, new TypeToken<ArrayList<Produto>>() {}.getType());

                // imprime lista de produtos e salva no sqlite?
                for (Produto p : listaFromJSON){
                    produtos.add(p);
                    Log.i("produto" ,p.getNome());
                    Log.i("preco" ,p.getPreco());
                    Log.i("imagem", p.getImagem());

                }
                return response;

            } catch (Exception e) {
                Log.e("HTTP", e.getMessage());
                e.printStackTrace();
            }
            online = false;
            return "OFFLINE";
        }

        // impressao tela
        @Override
        protected void onPostExecute (String result) {

            for (int i = 0; i < produtos.size(); i++){
                new AsyncTaskShowImage().execute();
            }

            Log.i("result", result);


            // IMPRIMIR NA TELA  cria exibição dinamica
        }
    }

    private class AsyncTaskShowImage extends AsyncTask<String, Integer, byte[]> {
        @Override
        protected byte[] doInBackground(String... urls) {
            String resourceURI = produtos.get(auxII).getImagem();
            auxII++;
            String formatedURL = resourceURI;
            URL url = null;

            try {
                url = new URL(formatedURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                con.setRequestMethod("GET");
                InputStream is = con.getInputStream();

                ByteArrayOutputStream os= new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }

                byte[] conteudoImg = os.toByteArray();
                File f = new File(getApplicationContext().getFilesDir(),produtos.get(aux).getNome() +".png");
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(conteudoImg);
                fos.close();
                return conteudoImg;

            } catch (Exception e) {
                Log.e("HTTP", e.getMessage());
                e.printStackTrace();

                try{
                    // AQUI PRINTA
                    File f = new File(getApplicationContext().getFilesDir(), "01.png");
                    FileInputStream fos = new FileInputStream(f);
                    byte[] conteudo = new byte[fos.available()];
                    fos.read(conteudo);
                    fos.close();

                    return conteudo;

                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
            return null;
        }

        // impressao tela 2 (imagem)
        @Override
        protected void onPostExecute (byte[] result) {
            LinearLayout l = (LinearLayout) findViewById(R.id.verticalScroll);
            LinearLayout l2 = new LinearLayout(getApplicationContext());

            l2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            l2.setOrientation(LinearLayout.HORIZONTAL);
            l.addView(l2);

            if (result != null){
                ImageView iv = new ImageView(getApplicationContext()); //findViewById(R.id.imageView);

                Bitmap bmp = BitmapFactory.decodeByteArray(result,0,result.length);
                iv.setImageBitmap(bmp);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(500, 500);
                iv.setLayoutParams(lp);
                l2.addView(iv);

                // Nome produto
                TextView tv = new TextView(getApplicationContext());
                tv.setText(produtos.get(aux).getNome());
                tv.setPadding(5,100,5,5);
                tv.setTextSize(16);
                l2.addView(tv);

                // Preço produto
                if (online){
                    TextView tvPreco = new TextView(getApplicationContext());
                    tvPreco.setText(" R$: " +produtos.get(aux).getPreco());
                    tvPreco.setTextSize(16);
                    l2.addView(tvPreco);
                }
                aux++;

            }
        }
    }



    // Class produto que uso para representar entrada do json, transforma em objeto (java)
    class Produto {
        private String nome;
        private String preco;
        private String imagem;

        public Produto(String nome, String preco, String imagem) {
            this.nome = nome;
            this.preco = preco;
            this.imagem = imagem;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getPreco() {
            return preco;
        }

        public void setPreco(String preco) {
            this.preco = preco;
        }

        public String getImagem() {
            return imagem;
        }

        public void setImagem(String imagem) {
            this.imagem = imagem;
        }

        @Override
        public String toString() {
            return "Produto{" +
                    "nome='" + nome + '\'' +
                    ", preco='" + preco + '\'' +
                    ", imagem='" + imagem + '\'' +
                    '}';
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTaskJSONPost().execute();
    }
}