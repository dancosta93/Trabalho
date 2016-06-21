package dacost.com.trabalho;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ProdutoActivity extends AppCompatActivity {

    private final String TAG = "SaveActivity";

    private DataBaseHelper helper;
    private TextView txtCodigo;
    private EditText txtDescricao, txtValor;

    private Button btnExcluir;
    private Button btnSalvar;

    String codProduto = "";

    Boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        helper = new DataBaseHelper(this);

        btnExcluir = (Button) findViewById(R.id.btnExcluir);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);
        txtCodigo = (TextView) findViewById(R.id.txtCodigo);
        txtDescricao = (EditText) findViewById(R.id.txtDescricao);
        txtValor = (EditText) findViewById(R.id.txtValor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.readBarCode();
    }

    /**
     * chama a leitura
     */
    public void readBarCode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    /**
     * Processa o barcode, atualiza os fields na tela, busca o produto
     * @param barCode
     */
    private void processBarCode(String barCode) {
        txtCodigo.setText(barCode);
        this.buscaProduto(barCode);

        String titulo = "Novo Produto";

        if (isUpdate) {
            codProduto = barCode;
            titulo = "Editar Produto";
        } else {
            btnExcluir.setVisibility(View.GONE);
        }

        //Mostra o voltar na action bar
        getSupportActionBar().setTitle(titulo);
    }

    /**
     * Processa o retorno do reader
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String content = scanningResult.getContents();
            if (content != null && !content.isEmpty()) {
                try {
                    Log.d(TAG, "Tipo :" + scanningResult.getFormatName());
                    this.processBarCode(content); //os bar codes serao os produtos
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Ocorreu um erro!", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Leitura Invalida!", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Leitura Invalida!", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }


    /**
     * Busca o produto pelo codigo
     *
     * @param codProduto
     */
    private void buscaProduto(String codProduto) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String param[] = new String[]{codProduto};
        Cursor cursor = db.rawQuery("SELECT * FROM PRODUTO WHERE codigo = ?", param);
        cursor.moveToFirst();
        if (cursor.getCount() == 1) {
            txtCodigo.setText(cursor.getString(0));
            txtDescricao.setText(cursor.getString(1));
            txtValor.setText(String.valueOf(cursor.getDouble(2)));
            isUpdate = true;
        }
        cursor.close();
    }

    /**
     * Quando clica o voltar da action bar
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    /**
     * Salva/Atualiza o Produto no DB
     * @param v
     */
    public void onSaveClick(View v) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("descricao", txtDescricao.getText().toString());
        values.put("valor", Double.parseDouble(txtValor.getText().toString()));

        long resultado = -1;
        if (isUpdate) {
            String where[] = new String[]{codProduto};
            resultado = db.update("PRODUTO", values, "codigo = ?", where);
        } else {
            values.put("codigo", txtCodigo.getText().toString());
            resultado = db.insert("PRODUTO", null, values);
        }


        if (resultado != -1) {
            Toast.makeText(ProdutoActivity.this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
            this.onBackPressed(); //volta depois de salvar
        } else {
            Toast.makeText(ProdutoActivity.this, "Erro ao Salvar!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Deleta o Produto
     * @param v
     */
    public void onDeleteClick(View v) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String where[] = new String[]{codProduto};

        long resultado = db.delete("PRODUTO", "codigo = ?", where);

        if (resultado != -1) {
            Toast.makeText(this, "Excluido com successo!", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        } else {
            Toast.makeText(ProdutoActivity.this, "Erro ao Excluir!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "DESTROY");
        helper.close();
        super.onDestroy();
    }
}
